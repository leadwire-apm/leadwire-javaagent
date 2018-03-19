## Features
* **Real User Monitroing** : The java agent injects a JavaScript plugin in html pages which measures the performance characteristicsof real-world page loads and interactions :
    * Measure a page’s perceived performance
    * Collect browser WebTiming information
    * Measure perceived performance of content loaded dynamically
    * Measure a user’s bandwidth along with page load time
    * Measure HTTP latency
    * Request/page tagging
    * Measure DNS latency
    * Measure a random sample of users instead of all users
* **System resources monitoring** throught the Sigar API which provides a portable interface for gathering system information such as :
   * System memory, swap, cpu, load average, uptime
   * Per-process memory, cpu, credential info, state, arguments, environment, open files
   * File system detection and metrics
   * Network interface configuration info and metrics 
   
   **The following platforms are currently supported :**
   
   os | arch | version
   ----- | ---- | ---------------------
    Linux | x86 | (2.2, 2.4, 2.6 kernels)
    Linux | amd64 | (2.6 kernel)
    Win32 | x86 | (NT 4.0 sp6, 2000 Pro/Server, 2003 Server, XP)
    Solaris | sparc | (2.6, 7, 8, 9, 10)
    Solaris | x86 | (8, 9, 10)
    HP/UX | PA-RISC | (11)
    AIX | PowerPC | (4.3, 5.1, 5.2, 5.3)
    FreeBSD | x86 |(4.x, 5.x, 6.x)
    Mac OS X | PowerPC | (10.4)
    
* **Jvm Monitoring (JMX)**
   * Memory 
   * Threads
   * Data Sources
   * Garbage Collection
   * Class Loading
   * Compilation
   * Uptime
* **Deep Dive into application source code**
Enable code package, class and method level throught aspectj library
```
<aspectj>
  <weaver>
    <include within="org.squashtest.csp.core.bugtracker.service.BugTrackersServiceImpl" /> 
    ...
```    
* **SQL monitoring for commercial and free databases which have endorsed the JDBC TM database access API and have built or are building JDBC-based products:**
   * H2
   * Mysql
   * Mariadb
   * Postgresql
   * Microsoft sqlserver
   * Oracle


## Integration

The integration of Lead Wire in your application is completely transparent, you do not have to change a single line of code. 
The only thing you have to do is to place the agent with your application and integrate it into your startup script.

The integration is as simple as adding the following to the startup of your application.

` -javaagent:[LEADWIRE_HOME]/leadwire-agent.jar -Dleadwire.agent.name=[AGENT_NAME] `

## Eclipse Setup for Developers
1. Get Gradle support by installing the Eclipse plugin "Buildship: ..." in version 2 or above.
1. If you have already imported leadwire-javaagent in Eclipse, delete it
1. Use git to clone leadwire-javaagent from the web url :  https://github.com/leadwire-apm/leadwire-javaagent.git
1. In Eclipse, right click button on the project, then "Configure" -> "Add Gradle nature"
1. Whenever you change a build.gradle file, regenerate the .project and .classpath files for Eclipse by using "Gradle->Refresh Gradle Project"
