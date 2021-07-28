# Waveform Manager

An set of tools and applications for managing HDF files storing large waveforms.

The HDF File format supported is as follows.

## Service

A simple indexing service which allows the addition of tags and properties to a list of hdf waveform files.

## Viewer

A graphic user interface to view the waveform from the hdf files.

## Build

### Requirements
 - [JDK11 or later, suggested is OpenJDK](https://download.java.net/java/GA/jdk11/9/GPL/openjdk-11.0.2_linux-x64_bin.tar.gz).
 - [maven 2.x](https://maven.apache.org/)

## Building with maven

Define the JAVA_HOME environment variable to point to your Java installation directory.
Linux:
```
export JAVA_HOME=/opt/jvm/jdk-11.0.2
```

Mac OS:
```
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-11.0.2/Contents/Home
```
Verify through:
```
$JAVA_HOME/bin/java -version
```

Make sure your PATH environment variable includes JAVA_HOME and the path to the Maven executable.

### Build

To build the entire waveform-manager stack

```
mvn clean install
```
