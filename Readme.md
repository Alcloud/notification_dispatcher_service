# Notification Dispatcher Service

This is an automated generated Readme.md file. Describe your service here in more detail.
After you have created the project structure from the maven archetype you have to perform the following steps:

1. Copy your service description inside the api.yaml file or modify the file for your needs.
1. Execute `mvn clean compile` on the command line.
1. Copy the generated implementation stubs 
 - `target/generated-sources/swagger/src/main/java/{groupId}/{artifactId}/api/factories/*Factory.java`
 - `target/generated-sources/swagger/src/main/java/{groupId}/{artifactId}/api/impl/*Impl.java`
 in the `src/main/java/{groupId}/{artifactId}/api/factories` and `src/main/java/{groupId}/{artifactId}/api/impl` directory. From here on now you can start implementing the webservices in these files.

The project can be build with

```sh
mvn clean package
```

The executable jar is moved in the `target` subdirectory of the project.
The webservice can be executed with

```sh
java -jar target/notification-dispatcher-service.jar
```

The project ships with a docker configuration.
The dockerfile can be found in `./package/Dockerfile`.
The docker image can be build with the following command

```sh
docker build -t notification-dispatcher-service ./package
```

Then, the docker image can be executed with

```sh
docker run -p 8080:8080 notification-dispatcher-service
```

The specification of the endpoints and the data model can be found in `./api.yaml`.


The project is configured with the `swagger-codegen-maven-plugin` and the avro schema generation plugin.
Therefore, java classes for the data model and the endpoint implementation are generated from the api specification and are placed in the directory `./target/generated-sources/swagger/src/gen/java` and `./target/generated-sources/swagger/src/main/java`.
Eclipse needs these files in a `Source Folder` in order to proper configure its workspace.
This can be done by right click on the folder and select `Build Path -> Use as Source Folder`.
