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
public class JavaAnnotation<A extends Annotation> {

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
}
