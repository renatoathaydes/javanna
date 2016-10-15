# Javanna-Gson

Java library to convert between JSON and Java annotations at runtime.

Leveraging [Javanna](https://github.com/renatoathaydes/javanna) to read and create annotations,
and [Gson](https://github.com/google/gson) to parse and write JSON documents,
Javanna-Gson allows you to map JSON to Java annotations.

## Parsing JSON into an annotation type

The whole library is one class: `com.athaydes.javanna.gson.JavannaGson`.

It has a default constructor that does not take any arguments, but may also be created with a
provided `Gson` instance.

Given a couple of Java annotation definitions such as the ones below:

```java
@Retention( RetentionPolicy.RUNTIME )
@interface Server {
    /**
     * @return the name of this server.
     */
    String name() default "-";

    /**
     * @return the port the server should listen to.
     */
    int port() default 80;

    /**
     * @return the location of the Server log file.
     */
    String logFile() default "/var/log/server.log";

    WhiteLists whiteLists() default @WhiteLists;
}

@Retention( RetentionPolicy.RUNTIME )
@interface WhiteLists {
    String[] ips() default { };

    int[] ports() default { };
}
```

The following code will parse a JSON document into an instance of `Server`:

```java
Server server = javannaGson.parse( jsonString, Server.class );
```

The JSON document may look like the following:

```json
{
  "name": "Super Server",
  "port": 43,
  "whiteLists": {
    "ips": [
      "192.168.10.1",
      "255.255.255.255"
    ],
    "ports": [
      60,
      90
    ]
  }
}
```

## Writing an annotation to JSON

To write an annotation to JSON, do the following:

```java
String json = javannaGson.toJson( annotation );
```

If you want to send it out over a `Writer` or `Appendable`:

```java
javannaGson.toJson( server, writer );
```

## Why map JSON to Java annotations instead of interfaces or classes?

Annotations have several properties that make them ideal to represent pure data:

* annotation instances are immutable.
* support for default values.
* no null anywhere, even within String arrays.
* no inheritance, only composition.
* no logic.

Also, annotations have no Collection or other custom types, mapping extremely well to JSON's types.
This limitation means you never need to worry about non-serializable types and `TypeAdapter`s.
