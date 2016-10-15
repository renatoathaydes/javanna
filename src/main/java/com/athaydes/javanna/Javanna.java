package com.athaydes.javanna;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Java annotation parser/creator.
 * <p>
 * No instance of this class can be created. It only contains static methods.
 */
public final class Javanna {

    private Javanna() {
        // private
    }

    /**
     * Parse the annotation class.
     *
     * @param annotationType {@code @interface} of annotation to parse.
     * @param <A>            the type of the annotation
     * @return a {@link JavaAnnotation} representing the annotation.
     */
    public static <A extends Annotation> JavaAnnotation<A> parseAnnotation(
            Class<A> annotationType ) {
        Map<String, Object> defaultValueByMember = new LinkedHashMap<>();
        Map<String, Class<?>> typeByMember = new LinkedHashMap<>();

        Method[] methods = annotationType.getDeclaredMethods();
        for (Method method : methods) {
            try {
                String memberName = method.getName();
                Object memberDefaultValue = method.getDefaultValue();
                Class<?> memberType = method.getReturnType();
                typeByMember.put( memberName, memberType );

                if ( memberDefaultValue != null )
                    defaultValueByMember.put( memberName, memberDefaultValue );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }

        return new JavaAnnotation<>( annotationType, defaultValueByMember, typeByMember );
    }

    /**
     * Create an annotation of the given type with the provided values.
     * <p>
     * To find out what the members of the annotation of type A are, use
     * {@link #parseAnnotation(Class)}.
     *
     * @param annotationType annotation type
     * @param values         values of annotation members
     * @param <A>            type of the annotation
     * @return the annotation instance with the provided values.
     * @throws IllegalArgumentException if a mandatory value is missing, a value has an invalid type or values are
     *                                  provided for non-existing members.
     */
    public static <A extends Annotation> A createAnnotation(
            Class<A> annotationType,
            Map<String, ?> values ) {
        return createAnnotation( parseAnnotation( annotationType ), values );
    }

    /**
     * Create an annotation of the given type with the provided values.
     *
     * @param annotation parsed annotation
     * @param values     values of annotation members
     * @param <A>        type of the annotation
     * @return the annotation instance with the provided values.
     * @throws IllegalArgumentException if a mandatory value is missing, a value has an invalid type or values are
     *                                  provided for non-existing members.
     */
    @SuppressWarnings( "unchecked" )
    public static <A extends Annotation> A createAnnotation(
            JavaAnnotation<A> annotation,
            Map<String, ?> values ) {
        Map<String, ?> checkedValues = validateValues( annotation, values );

        try {
            return ( A ) Proxy.newProxyInstance( annotation.getAnnotationType().getClassLoader(),
                    new Class[]{ annotation.getAnnotationType() },
                    new JavannaInvocationHandler( annotation, checkedValues ) );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    /**
     * Get the values of this annotation's members.
     *
     * @param annotation annotation
     * @return a Map of the values of this annotation by its member names.
     */
    public static Map<String, Object> getAnnotationValues( Annotation annotation ) {
        Map<String, Object> result = new LinkedHashMap<>();
        Method[] methods = annotation.annotationType().getDeclaredMethods();

        try {
            for (Method method : methods) {
                result.put( method.getName(), method.invoke( annotation ) );
            }
            return result;
        } catch ( IllegalAccessException | InvocationTargetException e ) {
            throw new IllegalStateException( "Unexpected error invoking annotation member methods", e );
        }
    }

    private static Map<String, ?> validateValues( JavaAnnotation<?> annotation,
                                                  Map<String, ?> values ) {
        Set<String> mandatoryMembers = diff( annotation.getMembers(), annotation.getDefaultValueByMember().keySet() );
        Set<String> missingMembers = diff( mandatoryMembers, values.keySet() );

        if ( !missingMembers.isEmpty() ) {
            throw new IllegalArgumentException( String.format(
                    "Missing values for mandatory annotation members [%s]: %s",
                    annotation.getAnnotationType().getName(), missingMembers ) );
        }

        Set<String> notMembers = diff( values.keySet(), annotation.getMembers() );

        if ( !notMembers.isEmpty() ) {
            throw new IllegalArgumentException( String.format(
                    "Values provided for non-existing members [%s]: %s",
                    annotation.getAnnotationType().getName(), joinWith( ", ", notMembers ) ) );
        }

        Map<String, Class<?>> typeByMember = annotation.getTypeByMember();

        Map<String, Object> result = new LinkedHashMap<>( values.size() );
        List<String> errors = new ArrayList<>( 1 );

        for (Map.Entry<String, ?> entry : values.entrySet()) {
            String member = entry.getKey();
            Class<?> type = typeByMember.get( member );

            Either validationResult = checkValue( member, type, entry.getValue() );

            if ( validationResult.isSuccess() ) {
                if ( errors.isEmpty() ) { // if there's an error, result will be ignored
                    result.put( member, validationResult.getValidResult() );
                }
            } else {
                errors.add( validationResult.getFailure() );
            }
        }

        if ( errors.isEmpty() ) {
            return result;
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append( "Errors:" );
            for (String error : errors) {
                builder.append( "\n* " ).append( error );
            }
            throw new IllegalArgumentException( builder.toString() );
        }
    }

    private static Set<String> diff( Set<String> a, Set<String> b ) {
        Set<String> temp = new HashSet<>( a );
        temp.removeAll( b );
        return temp;
    }

    private static Class<?> boxedType( Class<?> primitiveType ) {
        // boolean, byte, char, short, int, long, float, and double.
        if ( primitiveType == Boolean.TYPE )
            return Boolean.class;
        if ( primitiveType == Byte.TYPE )
            return Byte.class;
        if ( primitiveType == Character.TYPE )
            return Character.class;
        if ( primitiveType == Short.TYPE )
            return Short.class;
        if ( primitiveType == Integer.TYPE )
            return Integer.class;
        if ( primitiveType == Long.TYPE )
            return Long.class;
        if ( primitiveType == Float.TYPE )
            return Float.class;
        if ( primitiveType == Double.TYPE )
            return Double.class;

        throw new IllegalStateException( "Not a primitive type: " + primitiveType );
    }

    private static Either checkValue( String member, Class<?> type, Object value ) {
        if ( value == null ) {
            return Either.failure( String.format( "member '%s' contains illegal null item.", member ) );
        }

        boolean isArrayValue = value.getClass().isArray();

        if ( ( isArrayValue && value.getClass().equals( type ) )
                || value instanceof Collection ) {
            if ( type.isArray() ) {
                int length;
                Iterator<?> iterator = null;

                if ( isArrayValue ) {
                    length = Array.getLength( value );
                } else {
                    Collection collection = ( Collection ) value;
                    length = collection.size();
                    iterator = collection.iterator();
                }

                Class<?> itemType = type.getComponentType();
                Object newArray = Array.newInstance( itemType, length );
                for (int i = 0; i < length; i++) {
                    Object item;
                    if ( isArrayValue ) {
                        item = Array.get( value, i );
                    } else {
                        item = iterator.next();
                    }
                    String indexedMember = String.format( "%s[%d]", member, i );
                    Either itemValidationResult = checkValue( indexedMember, itemType, item );
                    if ( !itemValidationResult.isSuccess() ) {
                        return itemValidationResult;
                    }
                    Array.set( newArray, i, itemValidationResult.getValidResult() );
                }
                return Either.success( newArray );
            } else {
                return Either.failure( String.format( "member '%s' has invalid type. Expected: %s. Found: %s.",
                        member, type.getName(), value.getClass().getName() ) );
            }
        } else {
            // simple values are boxed, so if the expected type is primitive, turn it into its boxed equivalent
            if ( type.isPrimitive() ) {
                type = boxedType( type );
            }

            if ( type.isInstance( value ) ) {
                return Either.success( value );
            } else {
                return TypeConverter.coerce( value, type, String.format( "member '%s' has invalid type. Expected: %s. Found: %s.",
                        member, type.getName(), value.getClass().getName() ) );
            }
        }
    }

    private static String joinWith( String separator, Collection<String> values ) {
        StringBuilder builder = new StringBuilder();
        Iterator<String> iterator = values.iterator();

        if ( iterator.hasNext() ) {
            builder.append( iterator.next() );
        }
        while ( iterator.hasNext() ) {
            builder.append( separator ).append( iterator.next() );
        }

        return builder.toString();
    }

}
