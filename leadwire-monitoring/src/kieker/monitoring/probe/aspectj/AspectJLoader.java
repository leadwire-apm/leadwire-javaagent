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

package kieker.monitoring.probe.aspectj;

import java.lang.instrument.Instrumentation;

import org.aspectj.weaver.loadtime.Agent;

import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;
	
/**
 * @author Nils Christian Ehmke, Jan Waller
 * 
 * @since 1.9
 */
public final class AspectJLoader {

	private static final String JBOSS_MODULES_SYSTEM_PKGS = "jboss.modules.system.pkgs";
	private static final String AGENT_BASE_PACKAGE = "kieker,shadow.org.aspectj";
	private static final String SHADOW_AOP_CONFIG = "META-INF/shadow-aop.xml";
	
	static final Log LOG = LogFactory.getLog(AspectJLoader.class); // NOPMD package for inner class

	private AspectJLoader() {
		// Avoid instantiation
	}

	/**
	 * JSR-163 preMain entry method.
	 * 
	 * @param options
	 *            for the weaver agent
	 * @param instrumentation
	 *            java API instrumentation object
	 */
	public static void premain(final String options, final Instrumentation instrumentation) {
		
		LOG.info(ServiceNameUtil.getDefaultServiceName());
		
		if (System.getProperty("shadow.org.aspectj.weaver.loadtime.configuration") == null) {
		System.setProperty("shadow.org.aspectj.weaver.loadtime.configuration", SHADOW_AOP_CONFIG);
		}
				
		/**
		 * Makes the kieker package visible from all modules
		 */
		final String systemPackages = System.getProperty(JBOSS_MODULES_SYSTEM_PKGS);
        if (systemPackages != null) {
            System.setProperty(JBOSS_MODULES_SYSTEM_PKGS, systemPackages + "," + AGENT_BASE_PACKAGE);
        } else {
            System.setProperty(JBOSS_MODULES_SYSTEM_PKGS, AGENT_BASE_PACKAGE);
        }	
		
		Agent.premain(options, instrumentation);
	}
	
	
   

}
