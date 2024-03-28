## ML-datamover

It is a SpringBoot(v2.4.2) based microservice, which performs all the Job and Sub-Job related operations. 
We are storing all the job related information within the MongoDB.


### What you’ll need to Install
+ An IDE for SpringBoot Application
+ Java (Oracle JDK 15 or OpenJDK 15)
+ Gradle (v6.7)
+ Kafka (v2.7.0)
+ MongoDB (v4.4.4)



#### To Install JAVA
+ For Oracle JDK 15, Please visit this link  https://www.oracle.com/java/technologies/javase-jdk15-downloads.html 
+ For OpenJDK 15, Please visit this link https://jdk.java.net/15/



#### To Install Gradle
+ For Gradle (v6.7), Please visit this link https://docs.gradle.org/6.7/userguide/installation.html


#### To Install Kafka
+ For Kafka (v2.7.0) Please visit this link https://kafka.apache.org/quickstart
+ Kafka should run on 9092 port, otherwise you need to change in the environment variable.

#### To Install MongoDB
+ For MongoDB (v4.4.4) Please visit this link https://docs.mongodb.com/manual/installation/
+ MongoDB should run on 27017 Port, otherwise you need to change in the application.properties


### Plugins used in IDE ( IntelliJ )
+ **google-java-format** to format files consistently across our codebase.
+ **Lombok** which adds first-class support for Project Lombok.


### Enable Annotation Processing
+ **For IntelliJ IDEA** 
   > File >> Settings >> Build, Execution, Deployment >> Compiler >> Annotation Processors >> Enable Annotation Processing 

<br />
<br />

### High level folder Structure for ML-datamover
==================================================

 A typical top-level directory layout for ML-datamover

    .
    |-- gradle/wrapper          
    |-- src     
    |    |-- main
    |    |     |-- java/com/xperi/datamover
    |    |     |          |
    |    |     |          |-- advice               # rest controller exception handler          
    |    |     |          |-- config               # configuration classes               
    |    |     |          |-- constants            # constants     
    |    |     |          |-- controller           # rest controllers
    |    |     |          |-- dto                  # data transfer objects
    |    |     |          |-- entity               # entity classes     
    |    |     |          |-- exception            # exception classes       
    |    |     |          |-- repository           # repository classes      
    |    |     |          |-- service              # service classes
    |    |     |          |-- util                 # utility classes
    |    |     |          |    
    |    |     |          |-- MlDatamoverApplication.java
    |    |     |    
    |    |     |-- resources
    |    |           |-- application.properties    
    |    |                 
    |    |-- test  
    |        |-- java/com/xperi/datamover
    |                 |
    |                 |-- controller               # controller unit test cases         
    |                 |-- service                  # service unit test cases               
    |                 |-- util                     # utility unit test cases
    |                 |    
    |                 |-- MlDatamoverApplicationTests.java
    |             
    |-- .gitignore                     
    |-- build.gradle                   
    |-- gradlew                 
    |-- gradlew.bat
    |-- settings.gradle
    |-- README.md


<br />
<br />

### Setup Environment Variables for Kafka
+ KAFKA_BOOTSTRAP_SERVERS=127.0.0.1:9092
+ KAFKA_CONSUMER_GROUP=xperi
+ KAFKA_MINIO_EVENTS_TOPIC=bucketevents

