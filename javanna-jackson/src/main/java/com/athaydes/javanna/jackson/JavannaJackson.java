package com.athaydes.javanna.jackson;

import com.athaydes.javanna.JavaAnnotation;
import com.athaydes.javanna.Javanna;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavannaJackson {

    private final JsonFactory factory;

    public JavannaJackson() {
        factory = new JsonFactory();
    }

    public JavannaJackson( JsonFactory factory ) {
        this.factory = factory;
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
        Map<?, ?> map;
        try {
            JsonParser parser = factory.createParser( json );
            map = parse( parser, annotation );
        } catch ( IOException e ) {
            // there can be no IO from reading a String!!
            throw new RuntimeException( e );
        }
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
    public <A extends Annotation> A parse( Reader reader, Class<A> annotation ) throws IOException {
        JsonParser parser = factory.createParser( reader );
        Map<?, ?> map = parse( parser, annotation );
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
    public <A extends Annotation> A parse( File jsonFile, Class<A> annotation ) throws IOException {
        JsonParser parser = factory.createParser( jsonFile );
        Map<?, ?> map = parse( parser, annotation );
        return createAnnotation( annotation, map );
    }

    private static Map<String, ?> parse( JsonParser parser, Class<? extends Annotation> type ) throws IOException {
        JsonToken token = parser.nextToken();
        if ( token != JsonToken.START_OBJECT ) {
            throw new IncompatibleJsonException( "Not a JSON Object" );
        }
        return parseObject( parser, type );
    }

    private static Map<String, ?> parseObject( JsonParser parser, Class<? extends Annotation> type ) throws IOException {
        JavaAnnotation<?> annotation = Javanna.parseAnnotation( type );
        Map<String, Class<?>> typeByMember = annotation.getTypeByMember();

        Map<String, Object> result = new HashMap<>( typeByMember.size() );

        while ( !parser.isClosed() ) {
            JsonToken token = parser.nextToken();
            if ( token == JsonToken.FIELD_NAME ) {
                String member = parser.currentName();
                Class<?> memberType = typeByMember.get( member );
                if ( memberType == null ) {
                    throw new IncompatibleJsonException( "Unexpected field name: '" + member +
                            "'. Acceptable field names are: " + typeByMember.keySet() );
                }
                token = parser.nextToken();
                result.put( member, parseValue( parser, token, member, memberType ) );
            } else if ( token == JsonToken.END_OBJECT ) {
                break;
            } else {
                throw new IncompatibleJsonException( "Expected field name but found " + token );
            }
        }

        return result;
    }

    private static Object parseValue( JsonParser parser, JsonToken token,
                                      String member, Class<?> memberType ) throws IOException {
        switch ( token ) {
            case START_OBJECT:
                if ( Annotation.class.isAssignableFrom( memberType ) ) {
                    Class<? extends Annotation> annotationType = memberType.asSubclass( Annotation.class );
                    Map<?, ?> nestedObject = parseObject( parser, annotationType );
                    return createAnnotation( annotationType, nestedObject );
                } else {
                    throw new IncompatibleJsonException( "Expected member (" + member + ") to have value of type " +
                            memberType.getName() + " but found nested JSON Object at " + location( parser ) );
                }
            case VALUE_STRING:
                if ( String.class.equals( memberType ) ) {
                    return parser.getValueAsString();
                } else if ( char.class.equals( memberType ) ) {
                    char[] chars = parser.getValueAsString().toCharArray();
                    if ( chars.length == 1 ) {
                        return chars[ 0 ];
                    }
                    throw new IncompatibleJsonException( "Expected member (" + member + ") to have value of type " +
                            memberType.getName() + " but found String of length > 1 at " + location( parser ) );
                } else if ( memberType.isEnum() ) {
                    return Enum.valueOf( memberType.asSubclass( Enum.class ), parser.getValueAsString() );
                } else {
                    throw new IncompatibleJsonException( "Expected member (" + member + ") to have value of type " +
                            memberType.getName() + " but found String at " + location( parser ) );
                }
            case VALUE_NUMBER_INT:
                if ( int.class.equals( memberType ) ) {
                    return parser.getValueAsInt();
                } else if ( long.class.equals( memberType ) ) {
                    return parser.getValueAsLong();
                } else if ( short.class.equals( memberType ) ) {
                    return Integer.valueOf( parser.getValueAsInt() ).shortValue();
                } else if ( byte.class.equals( memberType ) ) {
                    return Integer.valueOf( parser.getValueAsInt() ).byteValue();
                } else {
                    throw new IncompatibleJsonException( "Expected member (" + member + ") to have value of type " +
                            memberType.getName() + " but found number at " + location( parser ) );
                }
            case VALUE_NUMBER_FLOAT:
                if ( float.class.equals( memberType ) ) {
                    return Double.valueOf( parser.getValueAsDouble() ).floatValue();
                } else if ( double.class.equals( memberType ) ) {
                    return parser.getValueAsDouble();
                } else {
                    throw new IncompatibleJsonException( "Expected member (" + member + ") to have value of type " +
                            memberType.getName() + " but found number at " + location( parser ) );
                }
            case VALUE_TRUE:
            case VALUE_FALSE:
                if ( boolean.class.equals( memberType ) ) {
                    return parser.getValueAsBoolean();
                } else {
                    throw new IncompatibleJsonException( "Expected member (" + member + ") to have value of type " +
                            memberType.getName() + " but found boolean at " + location( parser ) );
                }
            case START_ARRAY:
                if ( memberType.isArray() ) {
                    return parseArray( parser, member, memberType.getComponentType() );
                } else {
                    throw new IncompatibleJsonException( "Expected member (" + member + ") to have value of type " +
                            memberType.getName() + " but found array at " + location( parser ) );
                }
        }

        throw new IncompatibleJsonException( "Expected member (" + member + ") to have value of type " +
                memberType.getName() + " but found unexpected token (" + token + ") at " + location( parser ) );
    }

    private static Object parseArray( JsonParser parser, String member, Class<?> type ) throws IOException {
        List<Object> items = new ArrayList<>();
        while ( !parser.isClosed() ) {
            JsonToken token = parser.nextToken();
            if ( token == JsonToken.END_ARRAY ) {
                Object newArray = Array.newInstance( type, items.size() );
                for ( int i = 0; i < items.size(); i++ ) {
                    Array.set( newArray, i, items.get( i ) );
                }
                return newArray;
            } else {
                items.add( parseValue( parser, token, member, type ) );
            }
        }
        throw new RuntimeException( "Unexpected end of input while parsing array" );
    }

    private static String location( JsonParser parser ) {
        JsonLocation loc = parser.getCurrentLocation();
        return "line " + loc.getLineNr() + ", column " + loc.getColumnNr();
    }


    /**
     * Convert the given annotation instance to a JSON String.
     *
     * @param annotation annotation to read.
     * @return JSON String.
     */
    public String toJson( Annotation annotation ) {
        StringWriter writer = new StringWriter();
        try {
            toJson( annotation, factory.createGenerator( writer ) );
        } catch ( IOException e ) {
            // this can't happen as there's no IO being done
            throw new RuntimeException( e );
        }
        return writer.toString();
    }

    /**
     * Convert the given annotation instance to a JSON document and write it using the provided writer.
     *
     * @param annotation annotation to read.
     * @throws IOException if an error occurs while writing out
     */
    public void toJson( Annotation annotation, OutputStream writer ) throws IOException {
        toJson( annotation, factory.createGenerator( writer ) );
    }

    private static void toJson( Annotation annotation, JsonGenerator generator ) throws IOException {
        Map<String, Object> annotationMap = Javanna.getAnnotationValues( annotation, true );
        try {
            writeObject( annotationMap, generator );
        } finally {
            generator.close();
        }
    }

    private static void writeObject( Map<?, ?> map, JsonGenerator generator ) throws IOException {
        generator.writeStartObject();
        for ( Map.Entry<?, ?> entry : map.entrySet() ) {
            generator.writeFieldName( ( String ) entry.getKey() );
            writeValue( entry.getValue(), generator );
        }
        generator.writeEndObject();
    }

    private static void writeValue( Object value, JsonGenerator generator ) throws IOException {
        // we only need to handle the types allowed in annotations by the Java compiler
        if ( value instanceof Float ) {
            generator.writeNumber( ( float ) value );
        } else if ( value instanceof Double ) {
            generator.writeNumber( ( double ) value );
        } else if ( value instanceof Number ) {
            generator.writeNumber( ( ( Number ) value ).longValue() );
        } else if ( value instanceof String || value instanceof Character ) {
            generator.writeString( value.toString() );
        } else if ( value instanceof Boolean ) {
            generator.writeBoolean( ( boolean ) value );
        } else if ( value instanceof Enum<?> ) {
            generator.writeString( ( ( Enum<?> ) value ).name() );
        } else if ( value instanceof Map ) {
            writeObject( ( Map<?, ?> ) value, generator );
        } else if ( value != null && value.getClass().isArray() ) {
            writeArray( value, generator );
        } else {
            throw new IllegalArgumentException( value == null ? "Null value cannot be serialized" :
                    "Value of type which cannot be serialized to JSON: " + value.getClass() );
        }
    }

    private static void writeArray( Object value, JsonGenerator generator ) throws IOException {
        int length = Array.getLength( value );

        generator.writeStartArray( length );
        for ( int i = 0; i < length; i++ ) {
            writeValue( Array.get( value, i ), generator );
        }
        generator.writeEndArray();
    }

    private static <A extends Annotation> A createAnnotation(
            Class<A> annotation, Map<?, ?> map ) {
        Map<String, Object> typedMap = new HashMap<>( map.size() );
        for ( Map.Entry entry : map.entrySet() ) {
            typedMap.put( entry.getKey().toString(), entry.getValue() );
        }
        return Javanna.createAnnotation( annotation, typedMap );
    }


}
