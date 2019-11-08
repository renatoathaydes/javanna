package com.athaydes.javanna.jackson;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JavannaJacksonTest {

    private final JavannaJackson javannaJackson = new JavannaJackson();

    @Test
    public void parseObjectWithAllTypesOfFields() {
        Hello hello = javannaJackson.parse( "{\"hello\": \"world\", \"num\": 23, \"yes\":true, " +
                        "\"lon\": 42,\"pi\": 3.14, \"d\":0.5, \"sh\": 252, \"by\": 32, \"key\": \"Z\", " +
                        "\"arr\": [1,2,3], \"name\": {\"value\":\"Test\"}}",
                Hello.class );

        assertEquals( "world", hello.hello() );
        assertEquals( 23, hello.num() );
        assertTrue( hello.yes() );
        assertEquals( 42L, hello.lon() );
        assertEquals( 3.14f, hello.pi(), 0.00001f );
        assertEquals( 0.5d, hello.d(), 0.00001d );
        assertEquals( ( short ) 252, hello.sh() );
        assertEquals( ( byte ) 32, hello.by() );
        assertEquals( 'Z', hello.key() );
        assertArrayEquals( new int[]{ 1, 2, 3 }, hello.arr() );
        assertEquals( "Test", hello.name().value() );
    }

    @Test
    public void parseEmptyObject() {
        Name name = javannaJackson.parse( "{}", Name.class );
        assertEquals( "anonymous", name.value() );
    }
}

@interface Name {
    String value() default "anonymous";
}

@interface Hello {
    String hello();

    int num();

    boolean yes();

    long lon();

    float pi();

    double d();

    short sh();

    byte by();

    char key();

    int[] arr();

    Name name();
}