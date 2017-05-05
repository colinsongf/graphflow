Graphflow
=========

Graphflow is an in-memory graph database that supports continuous queries.
Graphflow's query language is based on [openCypher](http://opencypher.org).
Currently Graphflow supports a subset of queries expressible in openCypher.
In addition it supports several extensions to openCypher for expressing 
continuous queries. Throughout the code base and documentation, we refer to 
this extended version of openCypher as openCypher++.

A key feature of Graphflow is the use of the worst-case optimal 
[Generic Join algorithm](https://arxiv.org/abs/1310.3314) to do one-time 
and continuous match queries.

## Build steps

Graphflow requires Java 8, and uses Gradle as its build tool.

* Do a full clean build: `./gradlew clean build installDist`
* Subsequent builds: `./gradlew build installDist`
* Build without tests: `./gradlew build installDist -x test`

## Running the Server and CLI

Gradle creates helper scripts for running the server and the client.
Once the build is successful, run the scripts in two different terminals.

* Start Graphflow server:  
  `./build/install/graphflow/bin/graphflow-server`
* Start Graphflow CLI:  
  `./build/install/graphflow/bin/graphflow-cli`

The CLI can now be used to interact with the server using openCypher++ queries.

The Graphflow server logs to `stdout` by default. To change the logging mechanism, 
provide a custom log4j2 config file:
  ```bash
  export GRAPHFLOW_SERVER_OPTS="-Dlog4j.configurationFile=file:/path/to/log4j2.properties"  
  ./build/install/graphflow/bin/graphflow-server
  ```

## Licensing

Graphflow is an open source software under the Apache 2.0 license.
