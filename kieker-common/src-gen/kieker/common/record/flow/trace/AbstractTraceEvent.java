/***************************************************************************
 * Copyright 2017 Kieker Project (http://kieker-monitoring.net)
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
package kieker.common.record.flow.trace;

import java.nio.BufferUnderflowException;

import kieker.common.record.flow.AbstractEvent;
import kieker.common.record.flow.ITraceRecord;
import kieker.common.record.io.IValueDeserializer;

/**
 * @author Jan Waller
 *
 * @since 1.5
 */
public abstract class AbstractTraceEvent extends AbstractEvent implements ITraceRecord {
	private static final long serialVersionUID = -3022261747819944031L;

	/** default constants. */
	public static final long TRACE_ID = -1L;
	public static final int ORDER_INDEX = -1;
	
		
	/** property declarations. */
	private long traceId;
	private int orderIndex;
	
	/**
	 * Creates a new instance of this class using the given parameters.
	 *
	 * @param timestamp
	 *            timestamp
	 * @param traceId
	 *            traceId
	 * @param orderIndex
	 *            orderIndex
	 */
	public AbstractTraceEvent(final long timestamp, final long traceId, final int orderIndex) {
		super(timestamp);
		this.traceId = traceId;
		this.orderIndex = orderIndex;
	}

	/**
	 * This constructor uses the given array to initialize the fields of this record.
	 *
	 * @param values
	 *            The values for the record.
	 * @param valueTypes
	 *            The types of the elements in the first array.
	 */
	protected AbstractTraceEvent(final Object[] values, final Class<?>[] valueTypes) { // NOPMD (values stored directly)
		super(values, valueTypes);
		this.traceId = (Long) values[1];
		this.orderIndex = (Integer) values[2];
	}

	/**
	 * This constructor converts the given array into a record.
	 *
	 * @param deserializer
	 *            The value deserializer to use.
	 *
	 * @throws BufferUnderflowException
	 *             if buffer not sufficient
	 */
	public AbstractTraceEvent(final IValueDeserializer deserializer) throws BufferUnderflowException {
		super(deserializer);

		this.traceId = deserializer.getLong();
		this.orderIndex = deserializer.getInt();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated This record uses the {@link kieker.common.record.IMonitoringRecord.Factory} mechanism. Hence, this method is not implemented.
	 */
	@Override
	@Deprecated
	public void initFromArray(final Object[] values) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != this.getClass()) {
			return false;
		}

		final AbstractTraceEvent castedRecord = (AbstractTraceEvent) obj;
		if (this.getLoggingTimestamp() != castedRecord.getLoggingTimestamp()) {
			return false;
		}
		if (this.getTimestamp() != castedRecord.getTimestamp()) {
			return false;
		}
		if (this.getTraceId() != castedRecord.getTraceId()) {
			return false;
		}
		if (this.getOrderIndex() != castedRecord.getOrderIndex()) {
			return false;
		}
		return true;
	}

	@Override
	public final long getTraceId() {
		return this.traceId;
	}
	
	public final void setTraceId(long traceId) {
		this.traceId = traceId;
	}
	
	public final int getOrderIndex() {
		return this.orderIndex;
	}
	
	public final void setOrderIndex(int orderIndex) {
		this.orderIndex = orderIndex;
	}

}
