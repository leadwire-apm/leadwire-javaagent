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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.CharBuffer;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;

import kieker.common.configuration.Configuration;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.io.TextValueSerializer;
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
	
	private final IWriterRegistry<String> writerRegistry;
	private final TextValueSerializer serializer;
	private final CharBuffer buffer = CharBuffer.allocate(65535);

	private String httpServer;


	/** The name of the configuration determining whether to flush upon each incoming registry entry. */
	public static final String HTTP_SERVER = PREFIX + "httpServer";

	
	public httpWriter(final Configuration configuration) {
		super(configuration);
		
		httpServer = configuration.getStringProperty(HTTP_SERVER, "UTF-8");

		this.serializer = TextValueSerializer.create(this.buffer);
		this.writerRegistry = new WriterRegistry(this);
	}

	@Override
	public void onStarting() {
		// do nothing
	}

	@Override
	public void writeMonitoringRecord(final IMonitoringRecord record)  {

		final String recordClassName = record.getClass().getName();
		this.writerRegistry.register(recordClassName);

		StringBuilder _sb = new StringBuilder('$');

		
		this.buffer.clear();
		
		_sb.append(this.writerRegistry.getId(recordClassName));
		_sb.append(';');
		_sb.append(record.getLoggingTimestamp());

		record.serialize(this.serializer);

		this.buffer.flip();
		_sb.append(this.buffer.toString());
		
			
		    HttpClient httpClient = HttpClientBuilder.create().build();
		    
		   
		    
	        HttpPost request;
			try {
				request = new HttpPost("http://"+this.httpServer+"/?"+ URLEncoder.encode(_sb.toString(), "UTF-8"));
		

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
			
		    // System.out.println(_sb.toString());
		        
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			
	       

	
	}

	@Override
	public void onNewRegistryEntry(final String recordClassName, final int id) {

	}

	@Override
	public void onTerminating() {
		
	}




}

