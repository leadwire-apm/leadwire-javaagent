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
package kieker.monitoring.writer.elasticapm;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;

import kieker.common.configuration.Configuration;
import kieker.common.record.IMonitoringRecord;
import kieker.monitoring.core.controller.ReceiveUnfilteredConfiguration;
import kieker.monitoring.registry.IRegistryListener;
import kieker.monitoring.writer.AbstractMonitoringWriter;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;


/**
 * http/json data writer which sends monitoring records to a Elastic Apm Server.
 *
 * @author Wassim DHIB
 *
 * @since 1.13
 */


@ReceiveUnfilteredConfiguration // required for using class KiekerLogFolder
public class javaBeatWriter extends AbstractMonitoringWriter implements IRegistryListener<String> {

	public static final String PREFIX = javaBeatWriter.class.getName() + ".";

	/** The name of the configuration determining whether to flush upon each incoming registry entry. */
	public static final String CONFIG_FLUSH_MAPFILE = PREFIX + "flushMapfile";

	public javaBeatWriter(final Configuration configuration) {
		super(configuration);
	}

	@Override
	public void onStarting() {
		// do nothing
	}

	@Override
	public void writeMonitoringRecord(final IMonitoringRecord record) {

		  String payload = "data={" +
	                "\"username\": \"admin\", " +
	                "\"first_name\": \"System\", " +
	                "\"last_name\": \"Administrator\"" +
	                "}";
	        StringEntity entity = new StringEntity(payload,
	                ContentType.APPLICATION_FORM_URLENCODED);

	        HttpClient httpClient = HttpClientBuilder.create().build();
	        HttpPost request = new HttpPost("http://145.239.158.168/rum");
	        request.setEntity(entity);

	        HttpResponse response = null;
			try {
				response = httpClient.execute(request);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        System.out.println(response.getStatusLine().getStatusCode());
	        
	
	}

	@Override
	public void onNewRegistryEntry(final String recordClassName, final int id) {

	}

	@Override
	public void onTerminating() {
		
	}




}

