package com.athaydes.javanna;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith( Parameterized.class )
public class TypeConverterTest {

    @Parameterized.Parameters
    public static Collection<Number[]> data() {
        return Arrays.asList( new Number[][]{
                { ( byte ) 0, ( byte ) 0 }, { ( byte ) 1, ( short ) 1 }, { ( byte ) 2, 2 }, { ( byte ) 3, ( long ) 3 }, { ( byte ) 4, ( float ) 4 }, { ( byte ) 5, ( double ) 5 },
                { ( short ) 0, ( byte ) 0 }, { ( short ) 1, ( short ) 1 }, { ( short ) 2, 2 }, { ( short ) 3, ( long ) 3 }, { ( short ) 4, ( float ) 4 }, { ( short ) 5, ( double ) 5 },
                { 0, ( byte ) 0 }, { 1, ( short ) 1 }, { 2, 2 }, { 3, ( long ) 3 }, { 4, ( float ) 4 }, { 5, ( double ) 5 },
                { ( float ) 0, ( byte ) 0 }, { ( float ) 1, ( short ) 1 }, { ( float ) 2, 2 }, { ( float ) 3, ( long ) 3 }, { ( float ) 4, ( float ) 4 }, { ( float ) 5, ( double ) 5 },
                { ( long ) 0, ( byte ) 0 }, { ( long ) 1, ( short ) 1 }, { ( long ) 2, 2 }, { ( long ) 3, ( long ) 3 }, { ( long ) 4, ( float ) 4 }, { ( long ) 5, ( double ) 5 },
                { ( double ) 0, ( byte ) 0 }, { ( double ) 1, ( short ) 1 }, { ( double ) 2, 2 }, { ( double ) 3, ( long ) 3 }, { ( double ) 4, ( float ) 4 }, { ( double ) 5, ( double ) 5 },
        } );
    }

    private final Number input;
    private final Number expected;

    public TypeConverterTest( Number input, Number expected ) {
        this.input = input;
        this.expected = expected;
    }

    @Test
    public void numberConversionWorks() {
        Either result = TypeConverter.coerce( input, expected.getClass(), "ERROR" );

        assertTrue( result.isSuccess() );
        assertEquals( expected, result.getValidResult() );
    }

}
