/***************************************************************************
 * Copyright 2017 Lead Wire (http://leadwire-apm.com)
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

package kieker.monitoring.probe.aspectj.operationExecution;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponseWrapper;


/**
 * @author Wassim Dhib
 * 
 * @since 1.13
 */ 


public class HtmlResponseWrapper extends HttpServletResponseWrapper {

	private final ByteArrayOutputStream capture;
	private javax.servlet.ServletOutputStream output;
	private PrintWriter writer;
	
	public HtmlResponseWrapper(Object rep) {		
		super((javax.servlet.http.HttpServletResponse) rep);
		capture = new ByteArrayOutputStream(((javax.servlet.http.HttpServletResponse) rep).getBufferSize());
	}

	public HtmlResponseWrapper(javax.servlet.http.HttpServletResponse rep) {		
		super(rep);
		capture = new java.io.ByteArrayOutputStream(rep.getBufferSize());
	}

	@Override
	public javax.servlet.ServletOutputStream getOutputStream() {
		if (writer != null) {
			throw new java.lang.IllegalStateException(
					"getWriter() has already been called on this response.");
	}

	if (output == null) {
		output = new javax.servlet.ServletOutputStream() {
			@Override
			public void write(int b) throws java.io.IOException {
				capture.write(b);
			}

			@Override
			public void flush() throws java.io.IOException {
				capture.flush();
			}

			@Override
			public void close() throws java.io.IOException {
				capture.close();
			}
		};
	}

	return output;
}

@Override
public java.io.PrintWriter getWriter() throws java.io.IOException {
	if (output != null) {
		throw new java.lang.IllegalStateException("getOutputStream() has already been called on this response.");
		}

		if (writer == null) {
			writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(capture,getCharacterEncoding()));
		}

		return writer;
	}

	@Override
	public void flushBuffer() throws java.io.IOException {
		super.flushBuffer();

		if (writer != null) {
			writer.flush();
		} else if (output != null) {
			output.flush();
		}
	}

	public byte[] getCaptureAsBytes() throws java.io.IOException {
		if (writer != null) {
			writer.close();
		} else if (output != null) {
			output.close();
		}

		return capture.toByteArray();
	}

	public String getCaptureAsString() throws java.io.IOException {
		return new String(getCaptureAsBytes(), getCharacterEncoding());
	}

}
