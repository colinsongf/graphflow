Graphflow
=========

Project that aims to be the "PostgreSQL of Graph Databases", with support for
active queries as its core feature.

## Build steps

Gradle is used as the build tool. To test the environment setup:

* Clone the repo: `git clone git@github.com:graphflow/graphflow-dev.git`
* Do a full clean build: `./gradlew clean build installDist`
* Subsequent builds: `./gradlew build installDist`
* Build without tests: `./gradlew build installDist -x test`

If you get a build error, it is possible that old artifacts are causing it.
Perform a full clean build as the first step to debugging build failures.

## Running the server and cli

Gradle creates helper scripts that can be used to run the server and the client.
Once the build is successful, run the scripts in two different terminals.

* Start Graphflow server:
  `./build/install/graphflow/bin/graphflow-server`
* Start Graphflow CLI:
  `./build/install/graphflow/bin/graphflow-cli`

The CLI can now be used to interact with the server using Cypher++ queries.
For now, see 'src/test/java/ca/waterloo/dsg/graphflow/query/parser/StructuredQueryParserTest.java'
for the list of supported queries.

You can also start the Graphflow server with a custom log4j2 config file:
  ```
  $ export GRAPHFLOW_SERVER_OPTS="-Dlog4j.configurationFile=file:/path/to/log4j2.properties"  
  $ ./build/install/graphflow/bin/graphflow-server
  ```

## IntelliJ setup

Before importing the project into IntelliJ, execute a clean build from the
command line. This helps setup the code properly.

### Import source

In IntelliJ:

* Either "Import project", or "File -> New -> Project from existing sources..."
* In the file browser, go the Graphflow source directory, and select the
'build.gradle' file. (This selection of the build.gradle file instead of just
the directory is important because it informs IntelliJ that this is a Gradle
project). In the 'Import Project from Gradle' dialog box, select 'use gradle
wrapper task configuration', and press 'OK'

This completes the basic setup of the project. You can browse the code. Follow
any IntelliJ prompts.

### Create run configurations

To run or debug the Server or the CLI in IntelliJ, we need to create Run
configurations.

* It is recommended that you link the provided configurations into the IntelliJ 
config directory, so that any changes made in the repo will be automatically 
applied by IntelliJ. From the root directory, run: `ln -s ../intellij_configs/runConfigurations .idea`
* Alternatively, copy the configurations: `cp -r intellij_configs/runConfigurations .idea`
* In IntelliJ, "Run -> Run... -> GraphflowServerRunner/GraphflowCliRunner"

To see/edit the actual configuration, go to "Run -> Edit Configurations..."

### Import style settings

Import the IntelliJ code style configuration for auto-formatting code.

* "File -> Settings -> Editor -> Code Style -> Java -> Manage... -> Import... -> IntelliJ IDEA code style XML"
* Browse to the source directory and select 'intellij_configs/graphflow_code_style_guide.xml'
* Press "OK" everywhere and exit the settings.

You can now use 'Alt + Shift + L' to format code automatically in IntelliJ.
