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
package kieker.monitoring.writer.http;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import kieker.common.configuration.Configuration;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.io.TextValueSerializer;
import kieker.monitoring.core.controller.IMonitoringController;
import kieker.monitoring.core.controller.MonitoringController;
import kieker.monitoring.core.controller.ReceiveUnfilteredConfiguration;
import kieker.monitoring.registry.IRegistryListener;
import kieker.monitoring.registry.IWriterRegistry;
import kieker.monitoring.registry.WriterRegistry;
import kieker.monitoring.writer.AbstractMonitoringWriter;



/**
 * http writer
 *
 * @author Wassim DHIB
 *
 * @since 1.14
 */


@ReceiveUnfilteredConfiguration // required for using class KiekerLogFolder
public class httpWriter extends AbstractMonitoringWriter implements IRegistryListener<String> {

	public static final String PREFIX = httpWriter.class.getName() + ".";
		
	private String apmServer;

	private String appUuid;

		
	public httpWriter(final Configuration configuration) {
		super(configuration);
		
	}

	@Override
	public void onStarting() {
		// do nothing
	}

	@Override
	public void writeMonitoringRecord(final IMonitoringRecord record)  {
		
		apmServer = configuration.getStringProperty("kieker.monitoring.apmServer");
		appUuid = (System.getProperty("leadwire.agent.name") ==  null) ? "no.agent.name" : System.getProperty("leadwire.agent.name");
				
		HttpClient httpClient = HttpClientBuilder.create().build();
	    HttpPost request;
		request = new HttpPost("https://"+apmServer+"/"+appUuid+"/apm");
						
		StringEntity params =new StringEntity(record.toJson(), ContentType.APPLICATION_FORM_URLENCODED);
		request.setEntity(params);

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
			
	       

	
	}

	@Override
	public void onNewRegistryEntry(final String recordClassName, final int id) {

	}

	@Override
	public void onTerminating() {
		
	}




}

