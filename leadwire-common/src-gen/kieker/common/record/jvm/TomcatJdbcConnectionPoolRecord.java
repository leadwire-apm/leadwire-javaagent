/***************************************************************************
 * Copyright 2018 LeadWire (https://leadwire.io)
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
package kieker.common.record.jvm;

import java.nio.BufferOverflowException;

import kieker.common.record.io.IValueDeserializer;
import kieker.common.record.io.IValueSerializer;
import kieker.common.util.registry.IRegistry;


/**
 * @author Wassim Dhib
 * 
 * @since 1.13
 */
public class TomcatJdbcConnectionPoolRecord extends AbstractJVMRecord  {
	private static final long serialVersionUID = -9176980438135391329L;

	/** Descriptive definition of the serialization size of the record. */
	public static final int SIZE = TYPE_SIZE_LONG // AbstractJVMRecord.timestamp
			 + TYPE_SIZE_STRING // AbstractJVMRecord.hostname
			 + TYPE_SIZE_STRING // AbstractJVMRecord.vmName
			 + TYPE_SIZE_LONG // TomcatJdbcConnectionPoolRecord.InitialSize
			 + TYPE_SIZE_LONG // TomcatJdbcConnectionPoolRecord.Size
			 + TYPE_SIZE_LONG // TomcatJdbcConnectionPoolRecord.NumActive
			 + TYPE_SIZE_LONG // TomcatJdbcConnectionPoolRecord.NumIdle
	;
	
	public static final Class<?>[] TYPES = {
		long.class, // AbstractJVMRecord.timestamp
		String.class, // AbstractJVMRecord.hostname
		String.class, // AbstractJVMRecord.vmName
		long.class, // TomcatJdbcConnectionPoolRecord.InitialSize
		long.class, // TomcatJdbcConnectionPoolRecord.Size
		long.class, // TomcatJdbcConnectionPoolRecord.NumActive
		long.class, // TomcatJdbcConnectionPoolRecord.NumIdle
	};
	
	
	
	/** property name array. */
	private static final String[] PROPERTY_NAMES = {
		"timestamp",
		"hostname",
		"vmName",
		"InitialSize",
		"Size",
		"NumActive",
		"NumIdle",
	};
	
	/** property declarations. */
	private final Integer InitialSize;
	private final Integer Size;
	private final Integer NumActive;
	private final Integer NumIdle;
	
	/**
	 * Creates a new instance of this class using the given parameters.
	 * 
	 * @param timestamp
	 *            timestamp
	 * @param hostname
	 *            hostname
	 * @param vmName
	 *            vmName
	 * @param InitialSize
	 *            InitialSize
	 * @param Size
	 *            Size
	 * @param NumActive
	 *            NumActive
	 * @param NumIdle
	 *            NumIdle
	 */
	public TomcatJdbcConnectionPoolRecord(final long timestamp, final String hostname, final String vmName, final Integer InitialSize, final Integer Size, final Integer NumActive, final Integer NumIdle) {
		super(timestamp, hostname, vmName);
		this.InitialSize = InitialSize;
		this.Size = Size;
		this.NumActive = NumActive;
		this.NumIdle = NumIdle;
	}

	/**
	 * This constructor converts the given array into a record.
	 * It is recommended to use the array which is the result of a call to {@link #toArray()}.
	 * 
	 * @param values
	 *            The values for the record.
	 *
	 * @deprecated since 1.13. Use {@link #TomcatJdbcConnectionPoolRecord(IValueDeserializer)} instead.
	 */
	@Deprecated
	public TomcatJdbcConnectionPoolRecord(final Object[] values) { // NOPMD (direct store of values)
		super(values, TYPES);
		this.InitialSize = (Integer) values[3];
		this.Size = (Integer) values[4];
		this.NumActive = (Integer) values[5];
		this.NumIdle = (Integer) values[6];
	}

	/**
	 * This constructor uses the given array to initialize the fields of this record.
	 * 
	 * @param values
	 *            The values for the record.
	 * @param valueTypes
	 *            The types of the elements in the first array.
	 *
	 * @deprecated since 1.13. Use {@link #TomcatJdbcConnectionPoolRecord(IValueDeserializer)} instead.
	 */
	@Deprecated
	protected TomcatJdbcConnectionPoolRecord(final Object[] values, final Class<?>[] valueTypes) { // NOPMD (values stored directly)
		super(values, valueTypes);
		this.InitialSize = (Integer) values[3];
		this.Size = (Integer) values[4];
		this.NumActive = (Integer) values[5];
		this.NumIdle = (Integer) values[6];
	}

	
	/**
	 * @param deserializer
	 *            The deserializer to use
	 */
	public TomcatJdbcConnectionPoolRecord(final IValueDeserializer deserializer) {
		super(deserializer);
		this.InitialSize = deserializer.getInt();
		this.Size = deserializer.getInt();
		this.NumActive = deserializer.getInt();
		this.NumIdle = deserializer.getInt();
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * @deprecated since 1.13. Use {@link #serialize(IValueSerializer)} with an array serializer instead.
	 */
	@Override
	@Deprecated
	public Object[] toArray() {
		return new Object[] {
			this.getTimestamp(),
			this.getHostname(),
			this.getVmName(),
			this.getInitialSize(),
			this.getPoolSize(),
			this.getNumActive(),
			this.getNumIdle()
		};
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerStrings(final IRegistry<String> stringRegistry) {	// NOPMD (generated code)
		stringRegistry.get(this.getHostname());
		stringRegistry.get(this.getVmName());
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void serialize(final IValueSerializer serializer) throws BufferOverflowException {
		//super.serialize(serializer);
		serializer.putLong(this.getTimestamp());
		serializer.putString(this.getHostname());
		serializer.putString(this.getVmName());
		serializer.putLong(this.getInitialSize());
		serializer.putLong(this.getPoolSize());
		serializer.putLong(this.getNumActive());
		serializer.putLong(this.getNumIdle());
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
		if (obj == null) return false;
		if (obj == this) return true;
		if (obj.getClass() != this.getClass()) return false;
		
		final TomcatJdbcConnectionPoolRecord castedRecord = (TomcatJdbcConnectionPoolRecord) obj;
		if (this.getLoggingTimestamp() != castedRecord.getLoggingTimestamp()) return false;
		if (this.getTimestamp() != castedRecord.getTimestamp()) return false;
		if (!this.getHostname().equals(castedRecord.getHostname())) return false;
		if (!this.getVmName().equals(castedRecord.getVmName())) return false;
		if (this.getInitialSize() != castedRecord.getInitialSize()) return false;
		if (this.getPoolSize() != castedRecord.getPoolSize()) return false;
		if (this.getNumActive() != castedRecord.getNumActive()) return false;
		if (this.getNumIdle() != castedRecord.getNumIdle()) return false;
		return true;
	}
	
	public final long getInitialSize() {
		return this.InitialSize;
	}
	
	
	public final long getPoolSize() {
		return this.Size;
	}
	
	
	public final long getNumActive() {
		return this.NumActive;
	}
	
	
	public final long getNumIdle() {
		return this.NumIdle;
	}
	
}
