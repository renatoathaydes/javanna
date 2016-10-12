package com.athaydes.javanna.internal;

import com.athaydes.javanna.JavaAnnotation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

public final class JavannaInvocationHandler implements InvocationHandler {

    private final JavaAnnotation<?> annotation;
    private final Map<String, Object> values;

    public JavannaInvocationHandler( JavaAnnotation<?> annotation, Map<String, Object> values ) {
        this.annotation = annotation;
        this.values = values;
    }

    @Override
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
        final String member = method.getName();
        Object value = values.get( member );

        if ( value == null ) {
            value = annotation.getDefaultValueByMember().get( member );
        }

        return value;
    }
}
