Graphflow
=========

Adding support for active queries to graph database.

## Build and run steps

Gradle is used to parse the ANTLR4 grammar files, generate corresponding
Java code, and compile them.

* Build code: `./gradlew installDist`
* Start Graphflow server: `./build/install/graphflow/bin/graphflow-server`
* Start Graphflow cli: `./build/install/graphflow/bin/graphflow-cli `

## IntelliJ setup

* Import code into a new project -> Select the 'build.gradle' file
in the root folder (so that IntelliJ identifies this as a Gradle project)
* To run/debug the server and cli in IntelliJ, add a run configuration:
  Run -> Edit Configurations -> add 'Application'
  * Name: Graphflow
  * Main class: ca.waterloo.dsg.graphflow.server.GraphflowServerRunner or
    ca.waterloo.dsg.graphflow.cli.GraphflowCliRunner
  * Module: graphflow_main
  * Before launch configs:
    * remove 'Make'
    * add: Run Gradle Task -> Gradle project: graphflow, Tasks: build

## Configuring log4j

* Set path to new log4j2 config file: `export GRAPHFLOW_SERVER_OPTS=
  "-Dlog4j.configurationFile=file:/path/to/log4j2.properties"`
* Start Graphflow server: `./build/install/graphflow/bin/graphflow-server`
