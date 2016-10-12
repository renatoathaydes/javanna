# Javanna

A Java library to create and introspect annotations at runtime.

The whole library is just one class: `com.athaydes.javanna.Javanna`.

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
assertEquals( Complex.class, annotation.getAnnotationType() );
assertEquals( new HashMap<String, Object>() {{
    put( "name", "default-name" );
    put( "count", 2 );
}}, annotation.getDefaultValueByMember() );
assertEquals( annotation.getTypeByMember(), new HashMap<String, Class<?>>() {{
    put( "name", String.class );
    put( "count", int.class );
    put( "simple", Simple.class );
    put( "example", Example.class );
}} );
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
```

> It is an error to not provide mandatory values, or to give invalid members or values of the wrong type. All errors
  cause an `IllegalArgumentException` to be thrown.
