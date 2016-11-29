# The Nasa World Wind Project inside Eclipse

Working with NWW inside eclipse is as simple as:

* fire up the eclipse IDE
* select from the file menu **File->New->Other**
* in the resulting wizard select **Java Project from Existing Ant Buildfile**
* in the following tab push the **Browse...** button and select the **build.xml** file inside the NWW source code folder

That is it.

To be able to run the examples from eclipse, you need to extract
the proper native jogl and glugen libraries. For example on a linux 64 bit
machine you will extract (with any unzipping app, for example 7zip) the files:

* gluegen-rt-natives-linux-amd64.jar
* jogl-all-natives-linux-amd64.jar

which will lead to having in the source folder the following additional files:

* libgluegen-rt.so
* libjogl_desktop.so
* libjogl_mobile.so
* libnativewindow_awt.so
* libnativewindow_x11.so
* libnewt.so

Now the exapmple applications should work right away.

# Include the NWW master version in your maven project

To work with the latest master version of NWW in your own project, a few tweaks are necessary.

## Get gluegen and jogl from maven

The version to add to the pom, are the following:

```xml
<dependency>
    <groupId>org.jogamp.jogl</groupId>
    <artifactId>jogl-all</artifactId>
    <version>2.1.5-01</version>
</dependency>

<dependency>
	<groupId>org.jogamp.gluegen</groupId>
	<artifactId>gluegen-rt-main</artifactId>
	<version>2.1.5-01</version>
</dependency>
```

It seems there are no native jars available for those on maven central, so the native libs need to be
taken from the NWW source jars.

## Mavenize NWW and install it in your local maven repository

These are the steps to mavenize your NWW code starting from eclipse:

* right-click on the NWW eclipse project
* select the export action
* then in the **Export** wizard select **JAR file**
* leave the settings of the following tab as per default and use **Browse...** to define the output jar
* push **Next** and **Finish** to trigger the export

The resulting jar file can then be installed in the local maven repository with the following command:

```bash
# for linux
mvn install:install-file \
         -Dfile=./worldwind.jar \
         -DgroupId=gov.nasa \
         -DartifactId=worldwind \
         -Dversion=2.0.X \
         -Dpackaging=jar

# for windows
mvn install:install-file \
         -Dfile=absolute_path_to/worldwind.jar \
         -DgroupId="gov.nasa" \
         -DartifactId=worldwind \
         -Dversion="2.0.X" \
         -Dpackaging=jar
```

Where **2.0.X** is just a fake version of choice that can then be referenced in the application pom as:

```xml
<dependency>
    <groupId>gov.nasa</groupId>
    <artifactId>worldwind</artifactId>
    <version>2.0.X</version>
</dependency>
```

To check if everything worked properly, run the maven command to create the eclipse project of the application that needs to reference NWW:

```bash
mvn eclipse:eclipse
```
