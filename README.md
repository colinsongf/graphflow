Graphflow
=========

Adding support for active queries to graph database.

## Build steps

Gradle is used to parse the ANTLR4 grammar files, generate corresponding Java code, and compile them.

$ `./gradlew build`

## IntelliJ setup

* Import code into a new project -> Select the 'build.gradle' file in root folder (so that IntelliJ
adds this project as a Gradle project)
* Run -> Edit Configurations -> add 'Application'
  * Name: Graphflow
  * Main class: ca.waterloo.dsg.graphflow.Graphflow
  * Program Arguments: src/test/java/ca/waterloo/dsg/graphflow/testinput.txt
  * Module: graphflow_test
  * Before launch configs:
    * remove 'Make'
    * add: Run Gradle Task -> Gradle project: graphflow, Tasks: build
