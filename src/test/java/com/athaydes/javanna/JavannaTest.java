package com.athaydes.javanna;

import org.junit.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class JavannaTest {

    @Retention( RetentionPolicy.RUNTIME )
    @interface Empty {
    }

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

    @Test
    public void canParseEmptyAnnotation() throws Exception {
        JavaAnnotation<Empty> annotation = Javanna.parseAnnotation( Empty.class );

        assertEquals( Empty.class, annotation.getAnnotationType() );
        assertEquals( Collections.emptyMap(), annotation.getDefaultValueByMember() );
        assertEquals( Collections.emptyMap(), annotation.getTypeByMember() );
        assertEquals( Collections.emptySet(), annotation.getMembers() );
    }

    @Test
    public void canParseSimpleAnnotation() throws Exception {
        JavaAnnotation<Simple> annotation = Javanna.parseAnnotation( Simple.class );

        assertEquals( Simple.class, annotation.getAnnotationType() );
        assertEquals( Collections.emptyMap(), annotation.getDefaultValueByMember() );
        assertEquals( Collections.singletonMap( "value", String.class ), annotation.getTypeByMember() );
        assertEquals( Collections.singleton( "value" ), annotation.getMembers() );
    }

    @Test
    public void canParseComplexAnnotation() throws Exception {
        JavaAnnotation<Complex> annotation = Javanna.parseAnnotation( Complex.class );

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
    }

    @Test
    public void canCreateEmptyAnnotation() throws Exception {
        Empty empty = Javanna.createAnnotation( Empty.class, Collections.<String, Object>emptyMap() );

        assertNotNull( empty );
    }

    @Test
    public void canCreateSimpleAnnotation() throws Exception {
        Simple simple = Javanna.createAnnotation( Simple.class, new HashMap<String, Object>() {{
            put( "value", "the-simple-one" );
        }} );

        assertEquals( "the-simple-one", simple.value() );
    }

    @Test
    public void canCreateComplexAnnotation() throws Exception {
        final Simple simple = Javanna.createAnnotation( Simple.class, new HashMap<String, Object>() {{
            put( "value", "the-simple-one" );
        }} );

        Complex complex = Javanna.createAnnotation( Complex.class, new HashMap<String, Object>() {{
            put( "name", "hello" );
            put( "count", 6 );
            put( "simple", simple );
            put( "example", Example.LARGE );
        }} );

        assertEquals( "hello", complex.name() );
        assertEquals( 6, complex.count() );
        assertEquals( "the-simple-one", complex.simple().value() );
        assertEquals( Example.LARGE, complex.example() );
    }

    @Test
    public void canCreateComplexAnnotationWithPartialValues() throws Exception {
        final Simple simple = Javanna.createAnnotation( Simple.class, new HashMap<String, Object>() {{
            put( "value", "the-simple-one" );
        }} );

        Complex complex = Javanna.createAnnotation( Complex.class, new HashMap<String, Object>() {{
            put( "simple", simple );
            put( "example", Example.LARGE );
        }} );

        assertEquals( "default-name", complex.name() );
        assertEquals( 2, complex.count() );
        assertEquals( "the-simple-one", complex.simple().value() );
        assertEquals( Example.LARGE, complex.example() );
    }

    @Test
    public void cannotCreateSimpleAnnotationWithNonExistingMemberValues() throws Exception {
        try {
            Javanna.createAnnotation( Simple.class, new HashMap<String, Object>() {{
                put( "value", "ok" );
                put( "example", "wrong" );
                put( "hi", "wrong" );
            }} );

            fail( "Should have failed" );
        } catch ( IllegalArgumentException e ) {
            String actualMessage = e.getMessage();
            String expectedMessagePrefix = "Values provided for non-existing members [com.athaydes.javanna.JavannaTest$Simple]: ";
            Set<String> acceptedSuffixes = new HashSet<>();
            acceptedSuffixes.add( "hi, example" );
            acceptedSuffixes.add( "example, hi" );

            assertEquals( expectedMessagePrefix, actualMessage.substring( 0, expectedMessagePrefix.length() ) );
            assertTrue( acceptedSuffixes.contains( actualMessage.substring( expectedMessagePrefix.length() ) ) );
        }
    }

    @Test( expected = IllegalArgumentException.class )
    public void cannotCreateComplexAnnotationWithMissingMandatoryValues() throws Exception {
        Javanna.createAnnotation( Complex.class, new HashMap<String, Object>() {{
            put( "example", Example.LARGE );
        }} );
    }

    @Test
    public void cannotCreateComplexAnnotationWithValueOfWrongType() throws Exception {
        final Simple simple = Javanna.createAnnotation( Simple.class, new HashMap<String, Object>() {{
            put( "value", "the-simple-one" );
        }} );

        try {
            Javanna.createAnnotation( Complex.class, new LinkedHashMap<String, Object>() {{
                put( "name", 'a' ); // should be String
                put( "count", 6 );
                put( "simple", simple );
                put( "example", 10.01f ); // should be Example
            }} );

            fail( "Should have failed" );
        } catch ( IllegalArgumentException e ) {
            assertEquals( "Type errors:\n" +
                    "* Type of member 'name' has invalid type. Expected: java.lang.String. Found: java.lang.Character\n" +
                    "* Type of member 'example' has invalid type. Expected: com.athaydes.javanna.JavannaTest$Example. " +
                    "Found: java.lang.Float", e.getMessage() );
        }
    }

}
