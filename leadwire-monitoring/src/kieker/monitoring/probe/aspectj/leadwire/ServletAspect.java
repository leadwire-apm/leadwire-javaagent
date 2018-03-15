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

package kieker.monitoring.probe.aspectj.leadwire;



import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * @author Wassim DHIB
 * 
 * @since 1.13
 */
@Aspect
public class ServletAspect extends AbstractServletAspect {

	/**
	 * Default constructor.
	 */
	public ServletAspect() {
		// empty default constructor
	}



	

	@Override
	@Pointcut("execution(* javax.servlet.http.HttpServlet.service(..)) && args(request,response) ")
	public void monitoredOperation() {
		// Aspect Declaration (MUST be empty)
	}
	
	@Override
	@Pointcut("execution(* javax.servlet.http.HttpServlet.service(..)) && args(request,response)")
	public void monitoredServletservice(final javax.servlet.http.HttpServletRequest request, final javax.servlet.http.HttpServletResponse response) {
		// Aspect Declaration (MUST be empty)
	}
	
	

}
