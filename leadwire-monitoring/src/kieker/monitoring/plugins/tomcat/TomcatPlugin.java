/*
 * Copyright 2014 NAVER Corp.
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
package kieker.monitoring.plugins.tomcat;

import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;
import kieker.monitoring.bootstrap.plugin.ProfilerPlugin;
import kieker.monitoring.bootstrap.plugin.ProfilerPluginSetupContext;
import kieker.monitoring.bootstrap.resolver.condition.MainClassCondition;

/**
 * @author Jongho Moon
 * @author jaehong.kim
 *
 */
public class TomcatPlugin implements ProfilerPlugin {

	static final Log LOG = LogFactory.getLog(MainClassCondition.class); // NOPMD package for inner class

    @Override
    public void setup(ProfilerPluginSetupContext context) {

        final TomcatConfig config = new TomcatConfig(context.getConfig());
        if (LOG.isDebugEnabled()) {
            LOG.debug("TomcatPlugin config:"+config );
        }
        if (!config.isTomcatEnable()) {
            LOG.info("TomcatPlugin disabled");
            return;
        }

        TomcatDetector tomcatDetector = new TomcatDetector(config.getTomcatBootstrapMains());
        context.addApplicationTypeDetector(tomcatDetector);

    
    }
    
    
}

   
