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
package kieker.monitoring.sampler.mxbean;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import kieker.common.record.IMonitoringRecord;
import kieker.common.record.jvm.TomcatJdbcConnectionPoolRecord;
import kieker.monitoring.core.controller.IMonitoringController;
import kieker.monitoring.core.signaturePattern.SignatureFactory;

/**
 * A sampler using the MBean interface to access information about the tomcat jdbc connection pool in the JVM. The sampler produces a {@link TomcatJdbcConnectionPoolRecord} each time the
 * {@code sample} method is called.
 * 
 * @author Wassim Dhib
 * 
 * @since 1.13
 */
public class TomcatJdbcConnectionPoolSampler extends AbstractMXBeanSampler {

	/**
	 * Create new TomcatJdbcConnectionPool.
	 */
	public TomcatJdbcConnectionPoolSampler() {
		// Empty default constructor
	}

	@Override
	protected IMonitoringRecord[] createNewMonitoringRecords(final long timestamp, final String hostname, final String vmName,
			final IMonitoringController monitoringCtr) {

		if (!monitoringCtr.isProbeActivated(SignatureFactory.createJVMThreadsSignature())) {
			return new IMonitoringRecord[] {};
		}

		  Integer InitialSize = 0;
		  Integer Size = 0;
		  Integer NumActive = 0;
		  Integer NumIdle = 0;
          

		 try {
	            MBeanServerConnection mbeanServerConnection = ManagementFactory.getPlatformMBeanServer();
	           
	          
	            
	            String[] attrNames = {"InitialSize","Size","NumActive","NumIdle"};
	            ObjectName objectName = new ObjectName("org.apache.tomcat.jdbc.pool.jmx:name=dataSourceMBean,type=ConnectionPool");
	            
	            AttributeList list = mbeanServerConnection.getAttributes(objectName, attrNames);
	           
	            for (Attribute a : list.asList()) {            	
	            	  switch (a.getName()) {
	                  case "InitialSize":  InitialSize = (Integer) a.getValue() ;
	                           break;
	                  case "Size":  Size = (Integer) a.getValue() ;
	                           break;
	                  case "NumActive":  NumActive = (Integer) a.getValue() ;
	                           break;
	                  case "NumIdle":  NumIdle = (Integer) a.getValue() ;
	                           break;
	              }
	            }
	 
	            
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		 
		 
		 
		 

		return new IMonitoringRecord[] { new TomcatJdbcConnectionPoolRecord(timestamp, hostname, vmName, InitialSize, Size, NumActive, NumIdle) };
	}

}
