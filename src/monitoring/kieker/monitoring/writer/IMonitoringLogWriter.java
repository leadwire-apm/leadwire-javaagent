package kieker.monitoring.writer;

import kieker.common.record.IMonitoringRecordReceiver;

/* ==================LICENCE=========================
 * Copyright 2006-2011 Kieker Project
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
 * ==================================================
 */

/**
 * @author Andre van Hoorn, Jan Waller
 */
public interface IMonitoringLogWriter extends IMonitoringRecordReceiver {

	/**
	 * Called by the Monitoring Controller to announce the start of monitoring.
	 * Writers should start threads, etc. in this method.
	 */
	public void start();
	
	/**
	 * Called by the Monitoring Controller to announce a shutdown of monitoring.
	 * Writers should return as soon as it is safe to terminate Kieker.
	 */
	public void terminate();

	/**
	 * Returns a human-readable information string about the writer's configuration and state.
	 * 
	 * @return the information string.
	 */
	public String getInfoString();
}
