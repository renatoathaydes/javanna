package com.athaydes.javanna;

import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
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

    private enum Example {SMALL, MEDIUM, LARGE, XXX}

    @Retention( RetentionPolicy.RUNTIME )
    @interface Complex {
        String name() default "default-name";

        int count() default 2;

        Simple simple();

        Example example();
    }

    @Retention( RetentionPolicy.RUNTIME )
    @interface HasArrays {
        int[] numbers();

        String[] names();

        boolean[] states() default { true, false };
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
        assertEquals( new HashMap<String, Class<?>>() {{
            put( "name", String.class );
            put( "count", int.class );
            put( "simple", Simple.class );
            put( "example", Example.class );
        }}, annotation.getTypeByMember() );
        assertEquals( new HashSet<>( Arrays.asList( "name", "count", "simple", "example" ) ),
                annotation.getMembers() );
    }

    @Test
    public void canParseAnnotationWithArrayMembers() {
        JavaAnnotation<HasArrays> annotation = Javanna.parseAnnotation( HasArrays.class );

        assertEquals( HasArrays.class, annotation.getAnnotationType() );

        boolean[] expectedStates = { true, false };

        Map<String, Object> defaultValues = annotation.getDefaultValueByMember();
        assertEquals( Arrays.toString( expectedStates ),
                Arrays.toString( ( boolean[] ) defaultValues.get( "states" ) ) );
        assertEquals( 1, defaultValues.size() );

        assertEquals( new LinkedHashMap<String, Class<?>>() {{
            put( "numbers", int[].class );
            put( "names", String[].class );
            put( "states", boolean[].class );
        }}, annotation.getTypeByMember() );

        assertEquals( new HashSet<>( Arrays.asList( "numbers", "names", "states" ) ), annotation.getMembers() );
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
    public void canCreateComplexAnnotationWithNumbersOfConvertibleTypes() throws Exception {
        final Simple simple = Javanna.createAnnotation( Simple.class, new HashMap<String, Object>() {{
            put( "value", "the-simple-one" );
        }} );

        Complex complex = Javanna.createAnnotation( Complex.class, new HashMap<String, Object>() {{
            put( "name", "hello" );
            put( "count", 42.0f );
            put( "simple", simple );
            put( "example", Example.LARGE );
        }} );

        assertEquals( 42, complex.count() );
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
            assertEquals( "Errors:\n" +
                    "* member 'name' has invalid type. Expected: java.lang.String. Found: java.lang.Character.\n" +
                    "* member 'example' has invalid type. Expected: com.athaydes.javanna.JavannaTest$Example. " +
                    "Found: java.lang.Float.", e.getMessage() );
        }
    }

    @Test
    public void canCreateAnnotationWithArrayValues() throws Exception {
        HasArrays hasArrays = Javanna.createAnnotation( HasArrays.class, new HashMap<String, Object>() {{
            put( "numbers", new int[]{ 10, 5, 0 } );
            put( "names", new String[]{ "hi", "bye" } );
            put( "states", new boolean[]{ true, true, false, true } );
        }} );

        assertEquals( Arrays.toString( new int[]{ 10, 5, 0 } ), Arrays.toString( hasArrays.numbers() ) );
        assertEquals( Arrays.toString( new String[]{ "hi", "bye" } ), Arrays.toString( hasArrays.names() ) );
        assertEquals( Arrays.toString( new boolean[]{ true, true, false, true } ), Arrays.toString( hasArrays.states() ) );
    }

    @Test
    public void canCreateAnnotationWithPartialArrayValues() throws Exception {
        HasArrays hasArrays = Javanna.createAnnotation( HasArrays.class, new HashMap<String, Object>() {{
            put( "numbers", new int[]{ 10, 5, 0 } );
            put( "names", new String[]{ "hi", "bye" } );
        }} );

        assertEquals( Arrays.toString( new int[]{ 10, 5, 0 } ), Arrays.toString( hasArrays.numbers() ) );
        assertEquals( Arrays.toString( new String[]{ "hi", "bye" } ), Arrays.toString( hasArrays.names() ) );
        assertEquals( Arrays.toString( new boolean[]{ true, false } ), Arrays.toString( hasArrays.states() ) );
    }

    @Test
    public void cannotCreateAnnotationWithArrayValuesOfWrongType() throws Exception {
        try {
            Javanna.createAnnotation( HasArrays.class, new HashMap<String, Object>() {{
                put( "numbers", new int[]{ 10, 5, 0 } );
                put( "names", new String[]{ "hi", "bye" } );
                put( "states", new int[]{ 2 } );
            }} );

            fail( "Should have failed" );
        } catch ( IllegalArgumentException e ) {
            assertEquals( "Errors:\n" +
                            "* member 'states' has invalid type. Expected: [Z. Found: [I.",
                    e.getMessage() );
        }
    }

    @Test
    public void cannotCreateSimpleAnnotationWithNullValue() throws Exception {
        try {
            Javanna.createAnnotation( Simple.class, new HashMap<String, Object>() {{
                put( "value", null );
            }} );

            fail( "Should have failed" );
        } catch ( IllegalArgumentException e ) {
            assertEquals( "Errors:\n* member 'value' contains illegal null item.", e.getMessage() );
        }
    }

    @Test
    public void cannotCreateAnnotationWithNullArrayValue() throws Exception {
        try {
            Javanna.createAnnotation( HasArrays.class, new HashMap<String, Object>() {{
                put( "numbers", new int[]{ 10, 5, 0 } );
                put( "names", new String[]{ "hi", null } );
                put( "states", new boolean[]{ true, false } );
            }} );

            fail( "Should have failed" );
        } catch ( IllegalArgumentException e ) {
            assertEquals( "Errors:\n* member 'names[1]' contains illegal null item.", e.getMessage() );
        }
    }

    @Simple( "hi" )
    @Test
    public void canReadSimpleAnnotationValues() throws Exception {
        Annotation simple = getClass().getMethod( "canReadSimpleAnnotationValues" ).getAnnotation( Simple.class );

        assertEquals( Collections.singletonMap( "value", "hi" ), Javanna.getAnnotationValues( simple ) );
    }

    @Simple( "hi" )
    @Complex( name = "hello", count = 6, simple = @Simple( "hi" ), example = Example.SMALL )
    @Test
    public void canReadComplexAnnotationValues() throws Exception {
        final Annotation simple = getClass().getMethod( "canReadComplexAnnotationValues" ).getAnnotation( Simple.class );
        final Annotation complex = getClass().getMethod( "canReadComplexAnnotationValues" ).getAnnotation( Complex.class );

        Map<String, Object> expectedValues = new LinkedHashMap<String, Object>() {{
            put( "name", "hello" );
            put( "count", 6 );
            put( "simple", simple );
            put( "example", Example.SMALL );
        }};

        assertEquals( expectedValues, Javanna.getAnnotationValues( complex ) );
    }

}
