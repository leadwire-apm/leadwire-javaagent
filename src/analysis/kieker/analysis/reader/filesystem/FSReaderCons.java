/***************************************************************************
 * Copyright 2011 by
 *  + Christian-Albrechts-University of Kiel
 *    + Department of Computer Science
 *      + Software Engineering Group 
 *  and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/

package kieker.analysis.reader.filesystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import kieker.analysis.plugin.MonitoringRecordConsumerException;
import kieker.analysis.reader.MonitoringReaderException;
import kieker.common.record.DummyMonitoringRecord;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.IMonitoringRecordReceiver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Andre van Hoorn
 * 
 */
// TODO: AVH: we have to clean this up and need to test the error handling!
// See ticket http://samoa.informatik.uni-kiel.de:8000/kieker/ticket/142
public class FSReaderCons implements IMonitoringRecordReceiver {

	private static final Log LOG = LogFactory.getLog(FSReaderCons.class);

	// FSReaderConsData data = new FSReaderConsData(new AtomicBoolean(false),
	// new AtomicBoolean(false),
	// new ArrayList<Thread>(), new DummyMonitoringRecord());

	private final Collection<Thread> readerThreads = new ArrayList<Thread>();
	private final IMonitoringRecordReceiver master;
	private final String[] inputDirs;
	private final Collection<Class<? extends IMonitoringRecord>> readOnlyRecordsOfType;
	// private final AtomicBoolean isTerminated = new AtomicBoolean(false);
	private final AtomicBoolean errorOccurred = new AtomicBoolean(false);
	private final static IMonitoringRecord FS_READER_TERMINATION_MARKER = new DummyMonitoringRecord();

	public FSReaderCons(final IMonitoringRecordReceiver master, final String[] inputDirs, final Collection<Class<? extends IMonitoringRecord>> readOnlyRecordsOfType) {
		this.master = master;
		this.inputDirs = Arrays.copyOf(inputDirs, inputDirs.length);
		this.readOnlyRecordsOfType = readOnlyRecordsOfType;
	}

	/**
	 * Threads executing this method (concurrently) put record into sorted
	 * buffer, notify the buffer consumer and block until they are granted to
	 * read the next record.
	 */
	@Override
	public boolean newMonitoringRecord(final IMonitoringRecord monitoringRecord) {
		// if (this.isTerminated.get()) {
		// FSReaderCons.log.error("Consumer already terminated");
		// return false;
		// }

		try {
			final CountDownLatch myLatch = new CountDownLatch(1);
			synchronized (this.orderRecordBuffer) {
				this.orderRecordBuffer.put(new OrderRecordBufferElement(monitoringRecord), myLatch);
				this.orderRecordBuffer.notifyAll(); // notify main thread of
													// new record
			}
			if (monitoringRecord != FSReaderCons.FS_READER_TERMINATION_MARKER) {
				myLatch.await(); // countDown will be called by main thread
			}

		} catch (final InterruptedException ex) {
			FSReaderCons.LOG.error("Reader thread has been interrupted.", ex);
			this.errorOccurred.set(true);
			return false;
		}
		return !this.errorOccurred.get(); // pass errors to threads
	}

	public boolean execute() throws MonitoringRecordConsumerException {
		try {
			{ // 1. init and start reader threads
				for (final String inputDir : this.inputDirs) {
					final FSDirectoryReader directoryReader = new FSDirectoryReader(inputDir, this, this.readOnlyRecordsOfType);
					// consume records of any type and pass to this:
					final Thread t = new Thread(new Runnable() {

						@Override
						public void run() {
							try {
								directoryReader.read(); // throws an Exception
														// on error
								// signal termination:
								FSReaderCons.this.newMonitoringRecord(FSReaderCons.FS_READER_TERMINATION_MARKER);
							} catch (final Exception ex) {
								FSReaderCons.LOG.error(directoryReader, ex);
								FSReaderCons.this.reportReaderException(ex);
							}
						}
					}, "Reader thread for " + inputDir);
					this.readerThreads.add(t);
					t.start();
				}
			}

			// 2. now process all records provided by the threads in the
			// right order
			while (true) {
				// Does not work with Java 1.5:
				// Map.Entry<AbstractMonitoringRecord, Thread> firstEntry;
				final IMonitoringRecord nextRecord;
				CountDownLatch consumerLatch = null;
				synchronized (this.orderRecordBuffer) {
					while (this.orderRecordBuffer.size() < this.readerThreads.size()) { // always there must be one record from each thread
						try {
							this.orderRecordBuffer.wait();
						} catch (final InterruptedException e) { // ignore
						}
						if (this.errorOccurred.get()) {
							FSReaderCons.LOG.error("Found error flag set");
							throw new MonitoringReaderException("An error occured");
						}
					}

					final OrderRecordBufferElement nextBufferElement = this.orderRecordBuffer.firstKey(); // do not poll since FS_READER_TERMINATION_MARKER
																											// remains in list
					nextRecord = nextBufferElement.getRecord();
					// 1.5 incompatibility: firstEntry =
					// this.nextRecordsFromReaders.firstEntry(); // do not
					// poll since FS_READER_TERMINATION_MARKER remains in
					// list
					if (nextRecord == FSReaderCons.FS_READER_TERMINATION_MARKER) {
						FSReaderCons.LOG.info("All reader threads provided FS_READER_TERMINATION_MARKER");
						/**
						 * This must remain the only place we leave this method!
						 */
						return !this.errorOccurred.get(); // we're done
					} else {
						consumerLatch = this.orderRecordBuffer.get(nextBufferElement);
						if (this.orderRecordBuffer.remove(nextBufferElement) == null) {
							/*
							 * now, we'll remove
							 */
							FSReaderCons.LOG.warn("failed to remove nextRecord " + nextRecord + "\n" + "consumerLatch: " // NOCS (MultipleStringLiteralsCheck)
									+ consumerLatch + "\n" + "first key: " + this.orderRecordBuffer.firstKey());
							throw new MonitoringRecordConsumerException("failed to remove nextRecord " + nextRecord); // NOCS (MultipleStringLiteralsCheck)
						}
						if (!this.master.newMonitoringRecord(nextRecord)) {
							// we cannot simply return. We have to shutdown the
							// concurrent threads.
							this.errorOccurred.set(true);
						}
					}
				} // release monitor
				if (consumerLatch != null) {
					/* wake up blocked thread (in consume...()) */
					consumerLatch.countDown();
				} else {
					FSReaderCons.LOG.warn("consumerLatch == null");
				}
			}
		} catch (final MonitoringRecordConsumerException ex) {
			FSReaderCons.LOG.error("Exception while reading. Terminating.", ex);
			this.errorOccurred.set(true);
			/**
			 * There may be threads in #newMonitoringRecord() waiting for there
			 * latch to count down.
			 */
			synchronized (this.orderRecordBuffer) {
				FSReaderCons.LOG.info("Shutting down possibly waiting threads ...");
				for (final CountDownLatch latch : this.orderRecordBuffer.values()) {
					latch.countDown();
				}
			}
			return false;
		} catch (final MonitoringReaderException ex) {
			FSReaderCons.LOG.error("Exception while reading. Terminating.", ex);
			this.errorOccurred.set(true);
			/**
			 * There may be threads in #newMonitoringRecord() waiting for there
			 * latch to count down.
			 */
			synchronized (this.orderRecordBuffer) {
				FSReaderCons.LOG.info("Shutting down possibly waiting threads ...");
				for (final CountDownLatch latch : this.orderRecordBuffer.values()) {
					latch.countDown();
				}
			}
			return false;
		}
	}

	private void reportReaderException(final Exception ex) {
		final Thread t = Thread.currentThread();
		FSReaderCons.LOG.error("FSReader thread '" + t.getName() + "' reports exception", ex);
		this.errorOccurred.set(true);
		synchronized (this.orderRecordBuffer) {
			this.orderRecordBuffer.notifyAll(); // notify main thread of
												// new
			// record
		}
	}

	// private void terminate(final boolean error) {
	// if (error) {
	// this.errorOccurred.set(true);
	// }
	// synchronized (this.orderRecordBuffer) {
	// this.orderRecordBuffer.put(new OrderRecordBufferElement(
	// this.FS_READER_TERMINATION_MARKER), null);
	// // notify main thread of new record:
	// this.orderRecordBuffer.notifyAll();
	// }
	// }

	private final TreeMap<OrderRecordBufferElement, CountDownLatch> orderRecordBuffer = new TreeMap<OrderRecordBufferElement, CountDownLatch>( // NOCS (IllegalTypeCheck)
			new Comparator<OrderRecordBufferElement>() {

				@Override
				public int compare(final OrderRecordBufferElement e, final OrderRecordBufferElement e1) {
					final IMonitoringRecord t = e.getRecord();
					final IMonitoringRecord t1 = e1.getRecord();

					if (t == FSReaderCons.FS_READER_TERMINATION_MARKER) {
						return 1;
					}
					if (t1 == FSReaderCons.FS_READER_TERMINATION_MARKER) {
						return -1;
					}
					/*
					 * Only return 0 (equal) iff the objects are identical,
					 * i.e., the same
					 */
					if (t == t1) {
						return 0;
					}

					if (t.getLoggingTimestamp() == t1.getLoggingTimestamp()) {
						/*
						 * Here, two records have an equal timestamp but they
						 * are not the same and thus, we must not return 0! We
						 * use the id of the wrapping element to order these two
						 * elements in a deterministic way.
						 */
						// log.info("Elements have equal timestamp; ordering by wrapper id.");
						return (e.getElementId() < e1.getElementId()) ? -1 : 1; // NOCS
					}

					/*
					 * In every case, the timestamps are different at this
					 * place!
					 */
					return t.getLoggingTimestamp() < t1.getLoggingTimestamp() ? -1 : 1; // NOCS
				}
			});
}

/**
 * @author Andre van Hoorn
 * 
 */
final class OrderRecordBufferElement {

	private static final AtomicLong NEXT_ORDER_RECORD_BUFFER_ELEMENT_ID = new AtomicLong(0);

	private final long elementId = OrderRecordBufferElement.NEXT_ORDER_RECORD_BUFFER_ELEMENT_ID.getAndIncrement();

	/**
	 * Wrapped record
	 */
	private final IMonitoringRecord record;

	/**
	 * Must not be used for construction
	 */
	@SuppressWarnings("unused")
	private OrderRecordBufferElement() {
		this.record = null;
	}

	public OrderRecordBufferElement(final IMonitoringRecord record) {
		this.record = record;
	}

	public long getElementId() {
		return this.elementId;
	}

	/**
	 * Returns the wrapped record
	 * 
	 * @return
	 */
	public IMonitoringRecord getRecord() {
		return this.record;
	}
}
