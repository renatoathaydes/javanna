package com.athaydes.javanna;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

final class JavannaInvocationHandler implements InvocationHandler {

    private static final Method EQUALS_METHOD;
    private static final Method HASHCODE_METHOD;
    private static final Method TO_STRING_METHOD;
    private static final Method ANNOTATION_TYPE_METHOD;

    static {
        try {
            EQUALS_METHOD = Object.class.getMethod( "equals", Object.class );
            HASHCODE_METHOD = Object.class.getMethod( "hashCode" );
            TO_STRING_METHOD = Object.class.getMethod( "toString" );
            ANNOTATION_TYPE_METHOD = Annotation.class.getMethod( "annotationType" );
        } catch ( NoSuchMethodException e ) {
            throw new IllegalStateException( "JVM does not provide expected method", e );
        }
    }

    private final JavaAnnotation<?> annotation;
    private final Map<String, ?> values;

    JavannaInvocationHandler( JavaAnnotation<?> annotation, Map<String, ?> values ) {
        this.annotation = annotation;
        this.values = values;
    }

    @Override
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
        if ( method.equals( EQUALS_METHOD ) ) {
            Object other = args[ 0 ];
            return isEqual( other );
        }
        if ( method.equals( HASHCODE_METHOD ) ) {
            return annotation.hashCode() + values.hashCode();
        }
        if ( method.equals( TO_STRING_METHOD ) ) {
            return asString();
        }
        if ( method.equals( ANNOTATION_TYPE_METHOD ) ) {
            return annotation.getAnnotationType();
        }

        final String member = method.getName();
        Object value = values.get( member );

        if ( value == null ) {
            value = annotation.getDefaultValueByMember().get( member );
        }

        return cloneIfArray( value );
    }

    private Boolean isEqual( Object other ) {
        Class<? extends Annotation> type = annotation.getAnnotationType();

        if ( type.isInstance( other ) ) {
            Annotation otherAnnotation = type.cast( other );

            Map<String, Object> otherValues = Javanna.getAnnotationValues( otherAnnotation );

            return mapsAreEqual( values, otherValues );
        }

        return false;
    }

    private String asString() {
        StringBuilder builder = new StringBuilder();
        builder.append( annotation.getAnnotationType().getName() );
        builder.append( "(" );

        final int lastIndex = values.size();
        int index = 0;

        for (Map.Entry<String, ?> entry : values.entrySet()) {
            builder.append( entry.getKey() )
                    .append( "=" )
                    .append( valueAsString( entry.getValue() ) );

            if ( ++index != lastIndex ) {
                builder.append( ", " );
            }
        }

        builder.append( ")" );

        return builder.toString();
    }

    private static String valueAsString( Object value ) {
        // handle array of any type (Arrays.toString() requires us to know the type)
        if ( value.getClass().isArray() ) {
            int length = Array.getLength( value );
            StringBuilder builder = new StringBuilder();
            builder.append( "{" );
            for (int i = 0; i < length - 1; i++) {
                Object element = Array.get( value, i );
                builder.append( valueAsString( element ) ).append( ", " );
            }

            // last element, if any
            if ( length > 0 ) {
                builder.append( Array.get( value, length - 1 ) );
            }

            return builder.append( "}" ).toString();
        }
        return value.toString();
    }

    private static boolean mapsAreEqual( Map<String, ?> values, Map<String, Object> otherValues ) {
        if ( !values.keySet().equals( otherValues.keySet() ) ) {
            return false;
        }

        // keys are equal, now check each value

        for (Map.Entry<String, ?> entry : values.entrySet()) {
            Object value1 = entry.getValue();
            Object value2 = otherValues.get( entry.getKey() );
            if ( !valuesAreEqual( value1, value2 ) ) {
                return false;
            }
        }

        return true;
    }

    private static boolean valuesAreEqual( Object first, Object second ) {
        if ( first.getClass().isArray() ) {
            if ( second.getClass().isArray() ) {
                // both are arrays
                int length1 = Array.getLength( first );
                int length2 = Array.getLength( second );
                if ( length1 != length2 ) {
                    return false;
                }
                for (int i = 0; i < length1; i++) {
                    Object child1 = Array.get( first, i );
                    Object child2 = Array.get( second, i );
                    if ( !valuesAreEqual( child1, child2 ) ) {
                        return false;
                    }
                }

                // all elements are the same
                return true;
            } else {
                return false;
            }
        } else if ( second.getClass().isArray() ) {
            return false;
        } else {
            // none is an array
            return Objects.equals( first, second );
        }
    }

    @SuppressWarnings( "SuspiciousSystemArraycopy" )
    private static Object cloneIfArray( Object value ) {
        if ( value.getClass().isArray() ) {
            Class<?> type = value.getClass().getComponentType();
            int length = Array.getLength( value );
            Object clone = Array.newInstance( type, length );
            System.arraycopy( value, 0, clone, 0, length );
            return clone;
        } else {
            return value;
        }
    }

}
