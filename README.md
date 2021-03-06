# Javanna

A Java library to create and introspect annotations at runtime.

The whole library is just one class: `com.athaydes.javanna.Javanna`.

> To map between Java annotations and JSON, see [Javanna-Gson](javanna-gson).

## Getting started

Javanna is on [JCenter](https://bintray.com/search?query=javanna) and
[Maven Central](http://search.maven.org/#search%7Cga%7C1%7Ccom.athaydes.javanna).

### Gradle

```groovy
dependencies {
    compile "com.athaydes.javanna:javanna:1.1"
}
```

### Maven

```xml
<dependency>
  <groupId>com.athaydes.javanna</groupId>
  <artifactId>javanna</artifactId>
  <version>1.1</version>
</dependency>
```

## Parse an annotation class

Given the following example definitions:

```java
@Retention( RetentionPolicy.RUNTIME )
@interface Simple {
    String value();
}

enum Example {SMALL, MEDIUM, LARGE, XXX}

@Retention( RetentionPolicy.RUNTIME )
@interface Complex {
    String name() default "default-name";

    int count() default 2;

    Simple simple();

    Example example();
}
```


You can parse the annotations like this:

```java
JavaAnnotation<Complex> annotation = Javanna.parseAnnotation( Complex.class );
```

And then, inspect its members types and defaults (from the unit tests):

```java
// check the annotation actual type
assertEquals( Complex.class, annotation.getAnnotationType() );

// check the default values Map
assertEquals( new HashMap<String, Object>() {{
    put( "name", "default-name" );
    put( "count", 2 );
}}, annotation.getDefaultValueByMember() );

// check the annotation member types
assertEquals( new HashMap<String, Class<?>>() {{
    put( "name", String.class );
    put( "count", int.class );
    put( "simple", Simple.class );
    put( "example", Example.class );
}}, annotation.getTypeByMember() );

// check the annotation members
assertEquals( new HashSet<>( Arrays.asList( "name", "count", "simple", "example" ) ), annotation.getMembers() );
```

## Create an annotation instance

An annotation instance can be created from a `Class` or `JavaAnnotation`:

```java
final Simple simple = Javanna.createAnnotation( Simple.class, new HashMap<String, Object>() {{
    put( "value", "the-simple-one" );
}} );

Complex complex = Javanna.createAnnotation( Complex.class, new HashMap<String, Object>() {{
    put( "name", "hello" );
    put( "count", 6 );
    put( "simple", simple );
    put( "example", Example.LARGE );
}} );

// use the annotation as if it were a normal annotation instance
assertEquals( "hello", complex.name() );
assertEquals( 6, complex.count() );
assertEquals( "the-simple-one", complex.simple().value() );
assertEquals( Example.LARGE, complex.example() );
```

> It is an error to not provide mandatory values, or to give invalid members or values of the wrong type. All errors
  cause an `IllegalArgumentException` to be thrown by the `createAnnotation` method.

## Read the values of an annotation instance as a Map

To read all values of an annotation as a Map, use the `getAnnotationValues` method:

```java
Map<String, Object> values = Javanna.getAnnotationValues( annotation );
```

Full example (from the unit tests):

```java
@Simple( "hi" )
@Complex( name = "hello", count = 6, simple = @Simple( "hi" ), example = Example.SMALL )
public void canReadComplexAnnotationValues() throws Exception {
    // get the annotations on this method
    final Annotation simple = getClass().getMethod( "canReadComplexAnnotationValues" ).getAnnotation( Simple.class );
    final Annotation complex = getClass().getMethod( "canReadComplexAnnotationValues" ).getAnnotation( Complex.class );

    // expected values of the @Complex annotation
    Map<String, Object> expectedValues = new LinkedHashMap<String, Object>() {{
        put( "name", "hello" );
        put( "count", 6 );
        put( "simple", simple );
        put( "example", Example.SMALL );
    }};

    // read the annotation values as a Map
    Map<String, Object> actualValues = Javanna.getAnnotationValues( complex );

    assertEquals( expectedValues, actualValues );
}
```

To extract annotation values recursively (eg. get the value of the `@Simple` annotation as a Map in the example above),
just use the `getAnnotationValues(Annotation a, boolean recursive)` method:

```java
Map<String, Object> actualValues = Javanna.getAnnotationValues( complex, true );
```

In this case, the resulting Map would be:

```java
Map<String, Object> expectedValues = new LinkedHashMap<String, Object>() {{
    put( "name", "hello" );
    put( "count", 6 );
    put( "simple", new LinkedHashMap<String, Object>() {{
        put( "value", "hi" );
    }} );
    put( "example", Example.SMALL );
}};
```
