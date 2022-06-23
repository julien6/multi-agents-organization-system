# multi-agents-organization-system

The intent of this repository is to be a workspace for multi-agent organization testing

## Installation and configuration

### Install JADE
1. In https://jade.tilab.com/download/jade/license/jade-download/?x=40&y=12 download "jadeBin"
2. Put the JAR file "jade.jar" somewhere in your file system, like "C:\jade"
3. Create an environment variable to link it : "export CLASSPATH=/path/to/your/jar/file.jar"
4. Check it is properly installed with : "java jade.Boot -gui", the jade interface window should be displayed

### Import project and configuration
1. Clone repo somewhere (for instance in "eclipse-workspace")
2. In Eclipse select the project folder : File -> Open Projects from File System... -> Directory
3. Righ-click on root project folder icon in Eclipse -> Build Path -> Add External Archives... -> [Select the jade.jar file...]
4. Right-click on Main.java -> Run As -> Java Application
5. In Eclipse : Run -> Run Configurations... -> Click on "Java Application" -> Select the "Main" created configuration -> Arguments -> type "-gui Tester:main.Main" if you want to have the jade interface window displayed
