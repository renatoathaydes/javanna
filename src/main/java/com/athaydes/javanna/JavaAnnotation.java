package com.athaydes.javanna;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Java annotation.
 * <p>
 * This class provides information about an annotation that is not easy to extract from annotation
 * classes in general, such as its declared members and default values.
 */
public final class JavaAnnotation<A extends Annotation> {

    private final Class<A> annotationType;
    private final Map<String, Object> defaultValueByMember;
    private final Map<String, Class<?>> typeByMember;

    JavaAnnotation( Class<A> annotationType,
                    Map<String, Object> defaultValueByMember,
                    Map<String, Class<?>> typeByMember ) {
        this.annotationType = annotationType;
        this.defaultValueByMember = Collections.unmodifiableMap( defaultValueByMember );
        this.typeByMember = Collections.unmodifiableMap( typeByMember );
    }

    /**
     * @return the type of the annotation.
     */
    public Class<A> getAnnotationType() {
        return annotationType;
    }

    /**
     * @return the default value of each member which declares a default value.
     */
    public Map<String, Object> getDefaultValueByMember() {
        return defaultValueByMember;
    }

    /**
     * @return the type of each member.
     */
    public Map<String, Class<?>> getTypeByMember() {
        return typeByMember;
    }

    /**
     * @return the declared members of this annotation.
     */
    public Set<String> getMembers() {
        return typeByMember.keySet();
    }

    /**
     * Create an instance of this annotation with the provided values.
     *
     * @param values values of the annotation members. All mandatory values must be provided.
     * @return new instance of this annotation.
     * @throws IllegalArgumentException if a mandatory value is missing, a value has an invalid type or values are
     *                                  provided for non-existing members.
     */
    public A create( Map<String, ?> values ) {
        return Javanna.createAnnotation( this, values );
    }

    @Override
    public boolean equals( Object other ) {
        if ( this == other ) return true;
        if ( other == null || getClass() != other.getClass() ) return false;

        JavaAnnotation<?> that = ( JavaAnnotation<?> ) other;

        return annotationType.equals( that.annotationType );

    }

    @Override
    public int hashCode() {
        return annotationType.hashCode();
    }

    @Override
    public String toString() {
        return "JavaAnnotation{" +
                "annotationType=" + annotationType +
                ", defaultValueByMember=" + defaultValueByMember +
                ", typeByMember=" + typeByMember +
                '}';
    }

}
