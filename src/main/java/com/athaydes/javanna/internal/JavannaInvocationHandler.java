package com.athaydes.javanna.internal;

import com.athaydes.javanna.JavaAnnotation;
import com.athaydes.javanna.Javanna;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

public final class JavannaInvocationHandler implements InvocationHandler {

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

    public JavannaInvocationHandler( JavaAnnotation<?> annotation, Map<String, ?> values ) {
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

        return value;
    }

    private Boolean isEqual( Object other ) {
        Class<? extends Annotation> type = annotation.getAnnotationType();

        if ( type.isInstance( other ) ) {
            Annotation otherAnnotation = type.cast( other );

            Map<String, Object> otherValues = Javanna.getAnnotationValues( otherAnnotation );

            return values.equals( otherValues );
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
            builder.append( entry.getKey() ).append( "=" ).append( entry.getValue() );
            if ( ++index != lastIndex ) {
                builder.append( ", " );
            }
        }

        builder.append( ")" );

        return builder.toString();
    }

}
