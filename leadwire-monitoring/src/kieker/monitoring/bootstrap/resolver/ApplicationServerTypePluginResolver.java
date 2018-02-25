/*
 * Copyright 2015 NAVER Corp.
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
 */

package kieker.monitoring.bootstrap.resolver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;
import kieker.monitoring.bootstrap.config.DefaultProfilerConfig;
import kieker.monitoring.bootstrap.config.ProfilerConfig;
import kieker.monitoring.bootstrap.plugin.ApplicationTypeDetector;
import kieker.monitoring.bootstrap.resolver.condition.MainClassCondition;
import kieker.monitoring.common.trace.ServiceType;
import kieker.monitoring.plugins.tomcat.TomcatConfig;
import kieker.monitoring.plugins.tomcat.TomcatDetector;

/**
 * This class attempts to resolve the current application type through {@link ApplicationTypeDetector}s.
 * The application type is resolved by checking the conditions defined in each of the loaded detector's {@code detect} method.
 * <p>
 * If no match is found, the application type defaults to {@code ServiceType.STAND_ALONE}
 * 
 * @author HyunGil Jeong
 */
public class ApplicationServerTypePluginResolver {

	static final Log LOG = LogFactory.getLog(MainClassCondition.class); // NOPMD package for inner class

    private final List<ApplicationTypeDetector> applicationTypeDetectors;
    
    private final ConditionProvider conditionProvider;
    
    private static final ServiceType DEFAULT_SERVER_TYPE = ServiceType.STAND_ALONE;
    
    public ApplicationServerTypePluginResolver(List<ApplicationTypeDetector> serverTypeDetectors) {
        this(serverTypeDetectors, ConditionProvider.DEFAULT_CONDITION_PROVIDER);
    }
    
    public ApplicationServerTypePluginResolver(List<ApplicationTypeDetector> serverTypeDetectors, ConditionProvider conditionProvider) {
        if (serverTypeDetectors == null) {
            throw new IllegalArgumentException("applicationTypeDetectors should not be null");
        }
        if (conditionProvider == null) {
            throw new IllegalArgumentException("conditionProvider should not be null");
        }
        this.applicationTypeDetectors = serverTypeDetectors;
        this.conditionProvider = conditionProvider;
    }
   
    
    
    public ServiceType resolve() {
        for (ApplicationTypeDetector currentDetector : this.applicationTypeDetectors) {
            String currentDetectorName = currentDetector.getClass().getName();
            LOG.info("Attempting to resolve using ["+currentDetectorName+"]");
            if (currentDetector.detect(this.conditionProvider)) {
                LOG.info("Match found using ["+currentDetectorName+"] : "+currentDetector.getApplicationType().getName());

                if (currentDetector.getApplicationType().getName().equals("TOMCAT" )) {
                Class<?> c;
				try {
					c = Class.forName("org.apache.catalina.util.ServerInfo");
	                Method method;
					method = c.getMethod("getServerInfo");
	                Object o = method.invoke(null);
	                LOG.info("org.apache.catalina.util.ServerInfo:"+(String)o);

				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
                   
                }
                
                return currentDetector.getApplicationType();
            } else {
                LOG.info("No match found using ["+currentDetectorName+"]");
            }
        }
        LOG.debug("Server type not resolved. Defaulting to "+DEFAULT_SERVER_TYPE.getName() );
        return DEFAULT_SERVER_TYPE;
    }
}
