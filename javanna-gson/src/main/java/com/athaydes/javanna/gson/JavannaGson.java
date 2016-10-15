package com.athaydes.javanna.gson;

import com.athaydes.javanna.Javanna;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * A <a href="https://github.com/renatoathaydes/javanna">Javanna</a> extension
 * to create annotation instances from JSON and vice-versa.
 * <p>
 * JSON parsing/creation is delegated to the <a href="https://github.com/google/gson">Gson</a> library.
 */
public final class JavannaGson {

    private final Gson gson;

    /**
     * Create a new instance of {@link JavannaGson}.
     *
     * @param gson the Gson instance to use to parse and create JSON.
     *             Not type adapters are required (as annotations can only have constant members).
     */
    public JavannaGson( Gson gson ) {
        this.gson = gson;
    }

    /**
     * Create a new instance of {@link JavannaGson} using a default {@link Gson} instance to parse
     * and create JSON.
     */
    public JavannaGson() {
        this.gson = new Gson();
    }

    /**
     * Parse the given JSON String into an instance of {@code A} (annotation type).
     *
     * @param json       JSON String to parse.
     * @param annotation annotation type.
     * @param <A>        type of the annotation.
     * @return an annotation instance that is equivalent to the JSON provided.
     */
    public <A extends Annotation> A parse( String json, Class<A> annotation ) {
        Map<?, ?> map = gson.fromJson( json, Map.class );
        return createAnnotation( annotation, map );
    }

    /**
     * Parse a JSON document using the provided reader into an instance of {@code A} (annotation type).
     *
     * @param reader     reader of the JSON document.
     * @param annotation annotation type.
     * @param <A>        type of the annotation.
     * @return an annotation instance that is equivalent to the JSON provided.
     */
    public <A extends Annotation> A parse( Reader reader, Class<A> annotation ) {
        Map<?, ?> map = gson.fromJson( reader, Map.class );
        return createAnnotation( annotation, map );
    }

    /**
     * Parse the JSON document in the given file into an instance of {@code A} (annotation type).
     *
     * @param jsonFile   file containing the JSON document to parse.
     * @param annotation annotation type.
     * @param <A>        type of the annotation.
     * @return an annotation instance that is equivalent to the JSON provided.
     */
    public <A extends Annotation> A parse( File jsonFile, Class<A> annotation ) {
        try {
            Map<?, ?> map = gson.fromJson( new FileReader( jsonFile ), Map.class );
            return createAnnotation( annotation, map );
        } catch ( FileNotFoundException e ) {
            throw new IllegalArgumentException( "File does not exist: " + jsonFile );
        }
    }

    /**
     * Convert the given annotation instance to a JSON String.
     *
     * @param annotation annotation to read.
     * @return JSON String.
     */
    public String toJson( Annotation annotation ) {
        return gson.toJson( Javanna.getAnnotationValues( annotation, true ) );
    }

    /**
     * Convert the given annotation instance to a JSON document and write it using the provided writer.
     *
     * @param annotation annotation to read.
     * @throws JsonIOException if Gson throws.
     */
    public void toJson( Annotation annotation, Appendable writer )
            throws JsonIOException {
        gson.toJson( Javanna.getAnnotationValues( annotation, true ), writer );
    }

    private static <A extends Annotation> A createAnnotation(
            Class<A> annotation, Map<?, ?> map ) {
        Map<String, Object> typedMap = new HashMap<>( map.size() );
        for (Map.Entry entry : map.entrySet()) {
            typedMap.put( entry.getKey().toString(), entry.getValue() );
        }
        return Javanna.createAnnotation( annotation, typedMap );
    }

}
