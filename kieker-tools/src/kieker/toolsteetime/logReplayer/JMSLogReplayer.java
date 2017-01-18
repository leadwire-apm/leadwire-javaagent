/***************************************************************************
 * Copyright 2015 Kieker Project (http://kieker-monitoring.net)
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

package kieker.toolsteetime.logReplayer;

import java.util.concurrent.TimeUnit;

import kieker.analysisteetime.plugin.reader.AbstractReader;
import kieker.analysisteetime.plugin.reader.jms.JMSReader;
import kieker.monitoring.core.controller.IMonitoringController;

/**
 * An implementation of the {@link AbstractLogReplayer}, using the {@link JMSReader} to replay {@link kieker.common.record.IMonitoringRecord}s from a JMS queue.
 *
 * @author Andre van Hoorn
 *
 * @since 1.3
 */
public class JMSLogReplayer extends AbstractLogReplayer {

	private final String jmsProviderUrl;
	private final String jmsDestination;
	private final String jmsFactoryLookupName;

	/**
	 * Creates a new JMS log replayer.
	 *
	 * @param monitoringController
	 *            The {@link IMonitoringController}.
	 * @param monitoringConfigurationFile
	 *            The name of the {@code monitoring.properties} file.
	 * @param realtimeMode
	 *            Determines whether to use real time mode or not.
	 * @param realtimeTimeunit
	 *            The time unit to be used in realtime mode.
	 * @param realtimeAccelerationFactor
	 *            Determines whether to accelerate (value > 1.0) or slow down (<1.0) the replay in realtime mode by the given factor.
	 *            Choose a value of 1.0 for "real" realtime mode (i.e., no acceleration/slow down)
	 * @param realtimeWarnNegativeSchedTime
	 *            A time bound to configure a warning when a record is forwarded too late in realtime mode.
	 * @param ignoreRecordsBeforeTimestamp
	 *            The lower limit for the time stamps of the records.
	 * @param ignoreRecordsAfterTimestamp
	 *            The upper limit for the time stamps of the records.
	 * @param jmsProviderUrl
	 *            = for instance "tcp://127.0.0.1:3035/"
	 * @param jmsDestination
	 *            = for instance "queue1"
	 * @param jmsFactoryLookupName
	 *            = for instance "org.exolab.jms.jndi.InitialContextFactory" (OpenJMS)
	 * @param monitoringConfigurationFile
	 *            The path of the monitoring.properties file.
	 */
	public JMSLogReplayer(final IMonitoringController monitoringController, final String monitoringConfigurationFile, final boolean realtimeMode,
			final TimeUnit realtimeTimeunit,
			final double realtimeAccelerationFactor, final long realtimeWarnNegativeSchedTime, final long ignoreRecordsBeforeTimestamp,
			final long ignoreRecordsAfterTimestamp, final String jmsProviderUrl, final String jmsDestination, final String jmsFactoryLookupName) {
		super(monitoringController, monitoringConfigurationFile, realtimeMode, realtimeTimeunit, realtimeAccelerationFactor, realtimeWarnNegativeSchedTime,
				ignoreRecordsBeforeTimestamp,
				ignoreRecordsAfterTimestamp);
		this.jmsProviderUrl = jmsProviderUrl;
		this.jmsDestination = jmsDestination;
		this.jmsFactoryLookupName = jmsFactoryLookupName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AbstractReader createReader() {
		return new JMSReader(this.jmsProviderUrl, this.jmsDestination, this.jmsFactoryLookupName);
	}

}