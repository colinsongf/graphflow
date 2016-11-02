Graphflow
=========

Project to add support for active queries to a graph database.

## Build steps

Gradle is used as the build tool.

* Build without tests: `./gradlew installDist`
* Build with tests: `./gradlew installDist test`
* Full build with tests: `./gradlew clean build installDist`

## Running the server and cli

* Start Graphflow server:
  `./build/install/graphflow/bin/graphflow-server`
* Start Graphflow cli:
  `./build/install/graphflow/bin/graphflow-cli `
* Start Graphflow server with custom log4j2 config file:
  ```
  $ export GRAPHFLOW_SERVER_OPTS="-Dlog4j.configurationFile=file:/path/to/log4j2.properties"
  $ ./build/install/graphflow/bin/graphflow-server
  ```

## IntelliJ setup

* Import code into a new project -> Select the 'build.gradle' file in 
the root folder (so that IntelliJ identifies this as a Gradle project)
* To run/debug the server and cli in IntelliJ, add a run configuration:
  Run -> Edit Configurations -> add 'Application'
  * Name: Graphflow
  * Main class: ca.waterloo.dsg.graphflow.server.GraphflowServerRunner or
    ca.waterloo.dsg.graphflow.cli.GraphflowCliRunner
  * Module: graphflow_main
  * Before launch configs:
    * remove 'Make'
    * add: Run Gradle Task -> Gradle project: graphflow, Tasks: installDir
