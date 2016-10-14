package com.athaydes.javanna;

import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JavaAnnotationTest {

    @Retention( RetentionPolicy.RUNTIME )
    @interface Hello {
        String value();

        String language() default "english";
    }

    @Test
    public void annotationCanBeCreatedWithOnlyMandatoryValues() {
        JavaAnnotation<Hello> helloAnnotation = Javanna.parseAnnotation( Hello.class );

        Hello hello = helloAnnotation.create( Collections.singletonMap( "value", "Hi" ) );

        assertEquals( "Hi", hello.value() );
        assertEquals( "english", hello.language() );
    }

    @Test
    public void annotationCanBeCreatedWithAllValues() {
        JavaAnnotation<Hello> helloAnnotation = Javanna.parseAnnotation( Hello.class );

        Hello hello = helloAnnotation.create( new HashMap<String, Object>() {{
            put( "value", "Olá" );
            put( "language", "português" );
        }} );

        assertEquals( "Olá", hello.value() );
        assertEquals( "português", hello.language() );
    }

    @Test
    public void annotationCanBeCreatedFromNonLiteralClass() throws Exception {
        Annotation javannaAnnotation = Javanna.parseAnnotation( helloClass() )
                .create( new HashMap<String, Object>() {{
                    put( "value", "Olá" );
                    put( "language", "português" );
                }} );

        assertTrue( Hello.class.isInstance( javannaAnnotation ) );

        Hello hello = ( Hello ) javannaAnnotation;

        assertEquals( "Olá", hello.value() );
        assertEquals( "português", hello.language() );
    }

    @Test
    public void javannaAnnotationHasHashCode() {
        Annotation javannaAnnotation = Javanna.parseAnnotation( helloClass() )
                .create( new LinkedHashMap<String, Object>() {{
                    put( "value", "Olá" );
                    put( "language", "português" );
                }} );

        assertFalse( javannaAnnotation.hashCode() == 0 );
    }

    @Test
    public void javannaAnnotationHasToString() {
        Annotation javannaAnnotation = Javanna.parseAnnotation( helloClass() )
                .create( new LinkedHashMap<String, Object>() {{
                    put( "value", "Olá" );
                    put( "language", "português" );
                }} );

        assertEquals( Hello.class.getName() + "(value=Olá, language=português)", javannaAnnotation.toString() );
    }

    @Test
    public void javannaAnnotationWithArraysHasToString() {
        Annotation hasArrays = Javanna.createAnnotation( JavannaTest.HasArrays.class, new LinkedHashMap<String, Object>() {{
            put( "numbers", new int[]{ 10, 5, 0 } );
            put( "names", new String[]{ "hi", "bye" } );
            put( "states", new boolean[]{ true, true, false, true } );
        }} );

        assertEquals( JavannaTest.HasArrays.class.getName() +
                "(numbers={10, 5, 0}, names={hi, bye}, states={true, true, false, true})", hasArrays.toString() );
    }

    @Test
    public void javannaAnnotationClassIsAnnotation() {
        Annotation javannaAnnotation = Javanna.parseAnnotation( JavannaTest.Empty.class )
                .create( Collections.<String, Object>emptyMap() );

        assertEquals( JavannaTest.Empty.class, javannaAnnotation.annotationType() );
        assertTrue( javannaAnnotation.annotationType().isAnnotation() );
    }

    @Hello( value = "Privyet", language = "russian" )
    @Test
    public void annotationCreatedByJVMEqualsJavannaWithSameValue() throws Exception {
        Annotation jvmAnnotation = getClass().getMethod( "annotationCreatedByJVMEqualsJavannaWithSameValue" )
                .getAnnotation( helloClass() );
        Annotation javannaAnnotation = Javanna.createAnnotation( helloClass(), new LinkedHashMap<String, Object>() {{
            put( "value", "Privyet" );
            put( "language", "russian" );
        }} );

        assertTrue( jvmAnnotation.equals( javannaAnnotation ) );
        assertTrue( javannaAnnotation.equals( jvmAnnotation ) );
    }

    @Hello( "Hello" )
    @Test
    public void annotationCreatedByJVMDoesNotEqualJavannaWithDifferentValues() throws Exception {
        Annotation jvmAnnotation = getClass().getMethod( "annotationCreatedByJVMDoesNotEqualJavannaWithDifferentValues" )
                .getAnnotation( helloClass() );
        Annotation javannaAnnotation = Javanna.createAnnotation( helloClass(), new LinkedHashMap<String, Object>() {{
            put( "value", "Privyet" );
            put( "language", "russian" );
        }} );

        assertFalse( jvmAnnotation.equals( javannaAnnotation ) );
        assertFalse( javannaAnnotation.equals( jvmAnnotation ) );
    }

    @JavannaTest.HasArrays( numbers = { 4, 5 }, names = { "joe" }, states = { } )
    @Test
    public void annotationCreatedByJVMEqualsJavannaWithSameValueWithArray() throws Exception {
        Annotation jvmAnnotation = getClass().getMethod( "annotationCreatedByJVMEqualsJavannaWithSameValueWithArray" )
                .getAnnotation( JavannaTest.HasArrays.class );
        Annotation javannaAnnotation = Javanna.createAnnotation( JavannaTest.HasArrays.class, new LinkedHashMap<String, Object>() {{
            put( "numbers", new int[]{ 4, 5 } );
            put( "names", new String[]{ "joe" } );
            put( "states", new boolean[ 0 ] );
        }} );

        assertTrue( jvmAnnotation.equals( javannaAnnotation ) );
        assertTrue( javannaAnnotation.equals( jvmAnnotation ) );
    }

    @JavannaTest.HasArrays( numbers = { 4, 5 }, names = { "joe" }, states = { true } )
    @Test
    public void annotationCreatedByJVMDoesNotEqualJavannaWithDifferentValueWithArray() throws Exception {
        Annotation jvmAnnotation = getClass().getMethod( "annotationCreatedByJVMDoesNotEqualJavannaWithDifferentValueWithArray" )
                .getAnnotation( JavannaTest.HasArrays.class );
        Annotation javannaAnnotation = Javanna.createAnnotation( JavannaTest.HasArrays.class, new LinkedHashMap<String, Object>() {{
            put( "numbers", new int[]{ 4, 5 } );
            put( "names", new String[]{ "joe" } );
            put( "states", new boolean[]{ false } ); // this is different
        }} );

        assertFalse( jvmAnnotation.equals( javannaAnnotation ) );
        assertFalse( javannaAnnotation.equals( jvmAnnotation ) );
    }

    private static Class<? extends Annotation> helloClass() {
        return Hello.class;
    }

}
