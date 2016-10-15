package com.athaydes.javanna;

import java.util.HashMap;
import java.util.Map;

final class TypeConverter {

    private static final Map<Class<? extends Number>, Number> MAX_INTEGRAL_VALUES =
            new HashMap<Class<? extends Number>, Number>() {{
                put( Byte.class, Byte.MAX_VALUE );
                put( Short.class, Short.MAX_VALUE );
                put( Integer.class, Integer.MAX_VALUE );
                put( Long.class, Long.MAX_VALUE );
            }};

    private TypeConverter() {
        // private
    }

    static Either coerce( Object value, Class<?> type, String errorMessage ) {
        if ( Number.class.isAssignableFrom( type ) && Number.class.isInstance( value ) ) {
            return coerceNumber( ( Number ) value, type.asSubclass( Number.class ), errorMessage );
        } else {
            return Either.failure( errorMessage );
        }
    }

    private static Either coerceNumber( Number value, Class<? extends Number> type, String errorMessage ) {
        double d = value.doubleValue();
        if ( isIntegral( type ) ) {
            boolean isIntegralValue = ( Math.rint( d ) == d );
            if ( isIntegralValue && d < MAX_INTEGRAL_VALUES.get( type ).doubleValue() ) {
                return Either.success( safeIntegralCast( type, value ) );
            }
        } else {
            // non-integral types
            if ( type.equals( Float.class ) ) {
                if ( d < Float.MAX_VALUE ) {
                    return Either.success( value.floatValue() );
                }
            } else {
                // must be a double
                return Either.success( d );
            }
        }

        return Either.failure( errorMessage );
    }

    private static boolean isIntegral( Class<? extends Number> type ) {
        return !( type.equals( Float.class ) || type.equals( Double.class ) );
    }

    private static Number safeIntegralCast( Class<? extends Number> type, Number value ) {
        if ( type.equals( Byte.class ) ) {
            return value.byteValue();
        }
        if ( type.equals( Short.class ) ) {
            return value.shortValue();
        }
        if ( type.equals( Integer.class ) ) {
            return value.intValue();
        }
        if ( type.equals( Long.class ) ) {
            return value.longValue();
        }
        throw new IllegalStateException( "Not a integral number type: " + type );
    }

}
