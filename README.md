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
  * Main class: ca.waterloo.dsg.graphflow.server.GraphflowServer or
    ca.waterloo.dsg.graphflow.cli.GraphflowCli
  * Module: graphflow_main
  * Before launch configs:
    * remove 'Make'
    * add: Run Gradle Task -> Gradle project: graphflow, Tasks: installDist
