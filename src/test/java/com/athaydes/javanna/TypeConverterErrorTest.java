package com.athaydes.javanna;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith( Parameterized.class )
public class TypeConverterErrorTest {

    @Parameterized.Parameters
    public static Collection<Number[]> data() {
        return Arrays.asList( new Number[][]{
                { ( short ) 256, ( byte ) 0 },
                { 256, ( byte ) 0 }, { 66000, ( short ) 1 },
                { 256f, ( byte ) 0 }, { 0.1f, ( byte ) 0 }, { 66000f, ( byte ) 4 }, { 0.2f, ( short ) 1 }, { 0.3f, 3 },
                { 256L, ( byte ) 0 }, { 66000L, ( short ) 1 }, { 5_000_000_000L, 5 },
                { 256D, ( byte ) 0 }, { 66000D, ( short ) 1 }, { 5_000_000_000D, 2 }, { Double.MAX_VALUE, ( long ) 3 }, { 10E45, ( float ) 4 }
        } );
    }

    private final Number input;
    private final Number nonConvertable;

    public TypeConverterErrorTest( Number input, Number nonConvertable ) {
        this.input = input;
        this.nonConvertable = nonConvertable;
    }

    @Test
    public void badNumberConversionsDoNotWork() {
        Either result = TypeConverter.coerce( input, nonConvertable.getClass(), "ERROR" );

        assertFalse( String.format( "Should have failed: (%s, %s)", input, nonConvertable ),
                result.isSuccess() );

        assertEquals( "ERROR", result.getFailure() );
    }

}
