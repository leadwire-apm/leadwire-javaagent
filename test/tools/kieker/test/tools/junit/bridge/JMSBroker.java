/***************************************************************************
 * Copyright 2014 Kieker Project (http://kieker-monitoring.net)
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
package kieker.test.tools.junit.bridge;

import org.apache.activemq.broker.BrokerService;
import org.junit.Assert;

import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;
import kieker.tools.bridge.connector.jms.JMSClientConnector;

import kieker.test.common.junit.AbstractKiekerTest;

/**
 * Provides a JMSBroker, presently realized though ActiveMQ for the JMSClientConnector test.
 * 
 * @author Reiner Jung
 * 
 * @since 1.8
 * 
 */
public class JMSBroker implements Runnable {

	private static final Log LOG;

	static {
		if (System.getProperty("kieker.common.logging.Log") == null) {
			System.setProperty("kieker.common.logging.Log", "JUNIT");
		}
		LOG = LogFactory.getLog(AbstractKiekerTest.class);
	}

	private final BrokerService broker;

	/**
	 * Empty constructor.
	 */
	public JMSBroker() {
		LOG.info("Create broker service");
		this.broker = new BrokerService();
	}

	/**
	 * Runnable run method for the test JMSBroker.
	 */
	@Override
	public void run() {

		// configure the broker
		this.broker.setBrokerName(JMSClientConnector.KIEKER_DATA_BRIDGE_READ_QUEUE);
		try {
			this.broker.addConnector(ConfigurationParameters.JMS_URI);
			LOG.info("Start broker");
			this.broker.start();
			LOG.info("Broker stopped");
		} catch (final Exception e) { // NOCS NOPMD -- the framework uses Exception :-(
			Assert.fail("Broker startup failed. " + e.getMessage());
		}
	}

	public boolean isRunning() {
		return this.broker.isStarted();
	}

}
