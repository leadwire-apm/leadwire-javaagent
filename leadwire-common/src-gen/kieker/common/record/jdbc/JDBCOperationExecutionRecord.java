/***************************************************************************
 * Copyright 2017 Lead Wire (https://leadwire-apm.com)
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
package kieker.common.record.jdbc;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

import kieker.common.record.AbstractMonitoringRecord;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.io.IValueDeserializer;
import kieker.common.record.io.IValueSerializer;
import kieker.common.util.registry.IRegistry;

/**
 * @author Wassim DHIB
 *
 * @since 1.0
 */
public class JDBCOperationExecutionRecord extends AbstractMonitoringRecord implements IMonitoringRecord.Factory, IMonitoringRecord.BinaryFactory {
	private static final long serialVersionUID = -1108574095L;

	/** Descriptive definition of the serialization size of the record. */
	public static final int SIZE = TYPE_SIZE_STRING // JDBCOperationExecutionRecord.sqlStatement
			+ TYPE_SIZE_STRING // JDBCOperationExecutionRecord.sessionId
			+ TYPE_SIZE_LONG // JDBCOperationExecutionRecord.traceId
			+ TYPE_SIZE_LONG // JDBCOperationExecutionRecord.tin
			+ TYPE_SIZE_LONG // JDBCOperationExecutionRecord.tout
			+ TYPE_SIZE_STRING // JDBCOperationExecutionRecord.hostname
			+ TYPE_SIZE_INT // JDBCOperationExecutionRecord.eoi
			+ TYPE_SIZE_INT // JDBCOperationExecutionRecord.ess
	;

	public static final Class<?>[] TYPES = {
		String.class, // JDBCOperationExecutionRecord.sqlStatement
		String.class, // JDBCOperationExecutionRecord.sessionId
		long.class, // JDBCOperationExecutionRecord.traceId
		long.class, // JDBCOperationExecutionRecord.tin
		long.class, // JDBCOperationExecutionRecord.tout
		String.class, // JDBCOperationExecutionRecord.hostname
		int.class, // JDBCOperationExecutionRecord.eoi
		int.class, // JDBCOperationExecutionRecord.ess
	};
	
	/** user-defined constants. */
	public static final String NO_HOSTNAME = "<default-host>";
	public static final String NO_SESSION_ID = "<no-session-id>";
	public static final String NO_OPERATION_SIGNATURE = "noOperation";
	public static final long NO_TRACE_ID = -1L;
	public static final long NO_TIMESTAMP = -1L;
	public static final int NO_EOI_ESS = -1;

	/** default constants. */
	public static final String OPERATION_SIGNATURE = NO_OPERATION_SIGNATURE;
	public static final String SESSION_ID = NO_SESSION_ID;
	public static final long TRACE_ID = NO_TRACE_ID;
	public static final long TIN = NO_TIMESTAMP;
	public static final long TOUT = NO_TIMESTAMP;
	public static final String HOSTNAME = NO_HOSTNAME;
	public static final int EOI = NO_EOI_ESS;
	public static final int ESS = NO_EOI_ESS;
	
	/** property name array. */
	private static final String[] PROPERTY_NAMES = {
		"sqlStatement",
		"sessionId",
		"traceId",
		"tin",
		"tout",
		"hostname",
		"eoi",
		"ess",
	};
	
	/** property declarations. */
	private String sqlStatement;
	private String sessionId;
	private long traceId;
	private long tin;
	private long tout;
	private String hostname;
	private int eoi;
	private int ess;
	
	/**
	 * Creates a new instance of this class using the given parameters.
	 *
	 * @param sqlStatement
	 *            sqlStatement
	 * @param sessionId
	 *            sessionId
	 * @param traceId
	 *            traceId
	 * @param tin
	 *            tin
	 * @param tout
	 *            tout
	 * @param hostname
	 *            hostname
	 * @param eoi
	 *            eoi
	 * @param ess
	 *            ess
	 */
	public JDBCOperationExecutionRecord(final String sqlStatement, final String sessionId, final long traceId, final long tin, final long tout,
			final String hostname, final int eoi, final int ess) {
		this.sqlStatement = sqlStatement == null ? NO_OPERATION_SIGNATURE : sqlStatement;
		this.sessionId = sessionId == null ? NO_SESSION_ID : sessionId;
		this.traceId = traceId;
		this.tin = tin;
		this.tout = tout;
		this.hostname = hostname == null ? NO_HOSTNAME : hostname;
		this.eoi = eoi;
		this.ess = ess;
	}

	/**
	 * This constructor converts the given array into a record.
	 * It is recommended to use the array which is the result of a call to {@link #toArray()}.
	 *
	 * @param values
	 *            The values for the record.
	 */
	public JDBCOperationExecutionRecord(final Object[] values) { // NOPMD (direct store of values)
		AbstractMonitoringRecord.checkArray(values, TYPES);
		this.sqlStatement = (String) values[0];
		this.sessionId = (String) values[1];
		this.traceId = (Long) values[2];
		this.tin = (Long) values[3];
		this.tout = (Long) values[4];
		this.hostname = (String) values[5];
		this.eoi = (Integer) values[6];
		this.ess = (Integer) values[7];
	}

	/**
	 * This constructor uses the given array to initialize the fields of this record.
	 *
	 * @param values
	 *            The values for the record.
	 * @param valueTypes
	 *            The types of the elements in the first array.
	 */
	protected JDBCOperationExecutionRecord(final Object[] values, final Class<?>[] valueTypes) { // NOPMD (values stored directly)
		AbstractMonitoringRecord.checkArray(values, valueTypes);
		this.sqlStatement = (String) values[0];
		this.sessionId = (String) values[1];
		this.traceId = (Long) values[2];
		this.tin = (Long) values[3];
		this.tout = (Long) values[4];
		this.hostname = (String) values[5];
		this.eoi = (Integer) values[6];
		this.ess = (Integer) values[7];
	}

	/**
	 * This constructor converts the given array into a record.
	 *
	 * @param deserializer
	 *            The deserializer to use
	 *
	 * @throws BufferUnderflowException
	 *             if buffer not sufficient
	 */
	public JDBCOperationExecutionRecord(final IValueDeserializer deserializer) throws BufferUnderflowException {
		this.sqlStatement = deserializer.getString();
		this.sessionId = deserializer.getString();
		this.traceId = deserializer.getLong();
		this.tin = deserializer.getLong();
		this.tout = deserializer.getLong();
		this.hostname = deserializer.getString();
		this.eoi = deserializer.getInt();
		this.ess = deserializer.getInt();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] toArray() {
		return new Object[] {
			this.getsqlStatement(),
			this.getSessionId(),
			this.getTraceId(),
			this.getTin(),
			this.getTout(),
			this.getHostname(),
			this.getEoi(),
			this.getEss()
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerStrings(final IRegistry<String> stringRegistry) { // NOPMD (generated code)
		stringRegistry.get(this.getsqlStatement());
		stringRegistry.get(this.getSessionId());
		stringRegistry.get(this.getHostname());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void serialize(final IValueSerializer serializer) throws BufferOverflowException {
		serializer.putString(this.getsqlStatement());
		serializer.putString(this.getSessionId());
		serializer.putLong(this.getTraceId());
		serializer.putLong(this.getTin());
		serializer.putLong(this.getTout());
		serializer.putString(this.getHostname());
		serializer.putInt(this.getEoi());
		serializer.putInt(this.getEss());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] getValueTypes() {
		return TYPES; // NOPMD
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getValueNames() {
		return PROPERTY_NAMES; // NOPMD
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSize() {
		return SIZE;
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

		final JDBCOperationExecutionRecord castedRecord = (JDBCOperationExecutionRecord) obj;
		if (this.getLoggingTimestamp() != castedRecord.getLoggingTimestamp()) {
			return false;
		}
		if (!this.getsqlStatement().equals(castedRecord.getsqlStatement())) {
			return false;
		}
		if (!this.getSessionId().equals(castedRecord.getSessionId())) {
			return false;
		}
		if (this.getTraceId() != castedRecord.getTraceId()) {
			return false;
		}
		if (this.getTin() != castedRecord.getTin()) {
			return false;
		}
		if (this.getTout() != castedRecord.getTout()) {
			return false;
		}
		if (!this.getHostname().equals(castedRecord.getHostname())) {
			return false;
		}
		if (this.getEoi() != castedRecord.getEoi()) {
			return false;
		}
		if (this.getEss() != castedRecord.getEss()) {
			return false;
		}
		return true;
	}

	public final String getsqlStatement() {
		return this.sqlStatement;
	}
	
	public final void setsqlStatement(String sqlStatement) {
		this.sqlStatement = sqlStatement;
	}
	
	public final String getSessionId() {
		return this.sessionId;
	}
	
	public final void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	public final long getTraceId() {
		return this.traceId;
	}
	
	public final void setTraceId(long traceId) {
		this.traceId = traceId;
	}
	
	public final long getTin() {
		return this.tin;
	}
	
	public final void setTin(long tin) {
		this.tin = tin;
	}
	
	public final long getTout() {
		return this.tout;
	}
	
	public final void setTout(long tout) {
		this.tout = tout;
	}
	
	public final String getHostname() {
		return this.hostname;
	}
	
	public final void setHostname(String hostname) {
		this.hostname = hostname;
	}
	
	public final int getEoi() {
		return this.eoi;
	}
	
	public final void setEoi(int eoi) {
		this.eoi = eoi;
	}
	
	public final int getEss() {
		return this.ess;
	}
	
	public final void setEss(int ess) {
		this.ess = ess;
	}

}
