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

package kieker.test.analysis.junit.plugin.reader.tcp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import kieker.analysis.AnalysisController;
import kieker.analysis.AnalysisControllerThread;
import kieker.analysis.IAnalysisController;
import kieker.analysis.exception.AnalysisConfigurationException;
import kieker.analysis.plugin.filter.forward.ListCollectionFilter;
import kieker.analysis.plugin.reader.filesystem.FSReader;
import kieker.analysis.plugin.reader.tcp.TCPReader;
import kieker.common.configuration.Configuration;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.controlflow.OperationExecutionRecord;
import kieker.monitoring.core.configuration.ConfigurationFactory;
import kieker.monitoring.core.controller.IMonitoringController;
import kieker.monitoring.core.controller.MonitoringController;
import kieker.monitoring.writer.tcp.TCPWriter;

import kieker.test.analysis.util.AssertHelper;
import kieker.test.common.junit.AbstractKiekerTest;

/**
 * @author Christian Wulf
 * @since 1.11
 */
public class TestTCPReader extends AbstractKiekerTest {

	private static final String CURRENT_TEST_DIR = "test/analysis/" + TestTCPReader.class.getPackage().getName().replaceAll("\\.", "/");

	private Configuration monitoringConfiguration;
	private String port1;
	private String port2;

	public TestTCPReader() {
		// Nothing to do
	}

	@Before
	public void before() throws IOException {
		this.monitoringConfiguration = new Configuration();

		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(TestTCPReader.CURRENT_TEST_DIR + "/kieker.monitoring.properties");
			this.monitoringConfiguration.load(inputStream);
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}

		this.monitoringConfiguration.setProperty(ConfigurationFactory.AUTO_SET_LOGGINGTSTAMP, "false");

		this.port1 = this.monitoringConfiguration.getPathProperty(TCPWriter.CONFIG_PORT1);
		this.port2 = this.monitoringConfiguration.getPathProperty(TCPWriter.CONFIG_PORT2);
	}

	@Test
	public void test() throws Exception {
		final IAnalysisController analysisController = new AnalysisController();

		final Configuration configuration = new Configuration();
		configuration.setProperty(TCPReader.CONFIG_PROPERTY_NAME_PORT1, this.port1);
		configuration.setProperty(TCPReader.CONFIG_PROPERTY_NAME_PORT2, this.port2);
		final TCPReader tcpReader = new TCPReader(configuration, analysisController);
		final ListCollectionFilter<IMonitoringRecord> collectionFilter = new ListCollectionFilter<IMonitoringRecord>(new Configuration(), analysisController);

		analysisController.connect(tcpReader, TCPReader.OUTPUT_PORT_NAME_RECORDS, collectionFilter, ListCollectionFilter.INPUT_PORT_NAME);

		final AnalysisControllerThread analysisControllerThread = new AnalysisControllerThread(analysisController);
		analysisControllerThread.start();

		final List<IMonitoringRecord> records = this.loadRecordsFromFilesystem();
		this.writeViaTcp(records);

		analysisControllerThread.join();

		Assert.assertEquals(AnalysisController.STATE.TERMINATED, analysisController.getState());
		final long expectedNumRecords = 10;
		Assert.assertEquals(expectedNumRecords, collectionFilter.getList().size());

		final IMonitoringRecord firstRecord = collectionFilter.getList().get(0);
		final OperationExecutionRecord castedFirstRecord = AssertHelper.assertInstanceOf(OperationExecutionRecord.class, firstRecord);
		Assert.assertEquals(1378814632852360525L, castedFirstRecord.getLoggingTimestamp());
		Assert.assertEquals("<no-session-id>", castedFirstRecord.getSessionId());
		Assert.assertEquals(1378814632849896821L, castedFirstRecord.getTin());
		Assert.assertEquals(1378814632852105483L, castedFirstRecord.getTout());
		Assert.assertEquals("myHost", castedFirstRecord.getHostname());
	}

	private List<IMonitoringRecord> loadRecordsFromFilesystem() throws AnalysisConfigurationException {
		final IAnalysisController analysisController = new AnalysisController();

		final Configuration configurationFSReader = new Configuration();
		final String inputDir = TestTCPReader.CURRENT_TEST_DIR + "/log-data";
		configurationFSReader.setProperty(FSReader.CONFIG_PROPERTY_NAME_INPUTDIRS, inputDir);
		final FSReader reader = new FSReader(configurationFSReader, analysisController);

		final ListCollectionFilter<IMonitoringRecord> collectionFilter = new ListCollectionFilter<IMonitoringRecord>(new Configuration(), analysisController);

		analysisController.connect(reader, FSReader.OUTPUT_PORT_NAME_RECORDS, collectionFilter, ListCollectionFilter.INPUT_PORT_NAME);

		analysisController.run();
		return collectionFilter.getList();
	}

	private void writeViaTcp(final List<IMonitoringRecord> records) throws Exception {
		final IMonitoringController monitoringController = MonitoringController.createInstance(this.monitoringConfiguration);

		for (final IMonitoringRecord record : records) {
			monitoringController.newMonitoringRecord(record);
		}

		monitoringController.terminateMonitoring();
	}
}