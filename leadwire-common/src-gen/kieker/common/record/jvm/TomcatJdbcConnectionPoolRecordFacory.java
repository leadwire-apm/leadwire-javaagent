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


import kieker.common.record.factory.IRecordFactory;
import kieker.common.record.io.IValueDeserializer;

/**
 * @author Wassim Dhib
 * 
 * @since 1.13
 */
public final class TomcatJdbcConnectionPoolRecordFacory implements IRecordFactory<TomcatJdbcConnectionPoolRecord> {
	
	
	@Override
	public TomcatJdbcConnectionPoolRecord create(final IValueDeserializer deserializer) {
		return new TomcatJdbcConnectionPoolRecord(deserializer);
	}
	
	@Override
	@Deprecated
	public TomcatJdbcConnectionPoolRecord create(final Object[] values) {
		return new TomcatJdbcConnectionPoolRecord(values);
	}
	
	public int getRecordSizeInBytes() {
		return TomcatJdbcConnectionPoolRecord.SIZE;
	}
}
