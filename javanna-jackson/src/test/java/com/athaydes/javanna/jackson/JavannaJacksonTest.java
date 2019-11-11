package com.athaydes.javanna.jackson;

import com.athaydes.javanna.Javanna;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class JavannaJacksonTest {

    private final JavannaJackson javannaJackson = new JavannaJackson();

    @Test
    public void parseObjectWithAllTypesOfFields() {
        Hello hello = javannaJackson.parse( "{\"hello\": \"world\", \"num\": 23, \"yes\":true, " +
                        "\"lon\": 42,\"pi\": 3.14, \"d\":0.5, \"sh\": 252, \"by\": 32, \"key\": \"Z\", " +
                        "\"arr\": [1,2,3], \"name\": {\"value\":\"Test\"}, \"status\": \"ON\"}",
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
        assertEquals( hello.status(), Status.ON );
    }

    @Test
    public void generateJsonWithObjectWithAllTypesOfFields() throws JsonProcessingException {
        Name name = Javanna.createAnnotation( Name.class, Collections.singletonMap( "value", "foo" ) );
        Map<String, Object> map = new LinkedHashMap<>();
        map.put( "hello", "Joe" );
        map.put( "num", 4 );
        map.put( "yes", false );
        map.put( "lon", 43L );
        map.put( "pi", 0.434F );
        map.put( "d", 0.123D );
        map.put( "sh", ( short ) 44 );
        map.put( "by", ( byte ) 0x4A );
        map.put( "key", 'x' );
        map.put( "arr", new int[]{ 4, 2, 0 } );
        map.put( "name", name );
        map.put( "status", Status.OFF );

        Hello hello = Javanna.createAnnotation( Hello.class, map );
        String json = javannaJackson.toJson( hello );

        Hello parsedBackResult = javannaJackson.parse( json, Hello.class );

        assertEquals( parsedBackResult, hello );

        // make sure the numbers are not messed up
        assertThat( json, matches( "\"num\":4[,}]" ) );
        assertThat( json, matches( "\"lon\":43[,}]" ) );
        assertThat( json, matches( "\"pi\":0.434[,}]" ) );
        assertThat( json, matches( "\"d\":0.123[,}]" ) );
        assertThat( json, matches( "\"sh\":44[,}]" ) );
        assertThat( json, matches( "\"by\":74[,}]" ) );
    }

    @Test
    public void parseEmptyObject() {
        Name name = javannaJackson.parse( "{}", Name.class );
        assertEquals( "anonymous", name.value() );
    }

    @Test
    public void testSimpleJsonParsing() throws Exception {
        Server server = javannaJackson.parse( testResource( "/server.json" ), Server.class );

        assertEquals( "Super Server", server.name() );
        assertEquals( 43, server.port() );
        assertEquals( "/var/log/server.log", server.logFile() );

        WhiteLists whiteLists = server.whiteLists();

        assertEquals( Arrays.asList( "192.168.10.1", "255.255.255.255" ), Arrays.asList( whiteLists.ips() ) );

        List<Integer> ports = new ArrayList<>( whiteLists.ports().length );
        for ( int p : whiteLists.ports() ) {
            ports.add( p );
        }
        assertEquals( Arrays.asList( 60, 90 ), ports );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void testJsonCreation() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map expectedMap = mapper.readValue( testResource( "/server.json" ), Map.class );

        // the default value of logFile should be included in the JSON
        expectedMap.put( "logFile", "/var/log/server.log" );

        Server server = javannaJackson.parse( testResource( "/server.json" ), Server.class );

        String json = javannaJackson.toJson( server );

        // turn the JavannaJackson JSON String into a Map using just Jackson Mapper
        Map javannaJacksonMap = mapper.readValue( json, Map.class );

        assertEquals( expectedMap, javannaJacksonMap );
    }

    private InputStreamReader testResource( String resource ) {
        return new InputStreamReader( getClass().getResourceAsStream( resource ),
                StandardCharsets.UTF_8 );
    }

    private static Matcher<String> matches( String pattern ) {
        Pattern p = Pattern.compile( pattern );
        return new BaseMatcher<String>() {
            @Override
            public boolean matches( Object item ) {
                return p.matcher( item.toString() ).find();
            }

            @Override
            public void describeMismatch( Object item, Description mismatchDescription ) {
                mismatchDescription.appendText( "does not match: " ).appendValue( item );
            }

            @Override
            public void describeTo( Description description ) {
                description.appendText( "to match " ).appendValue( p );
            }
        };
    }
}

enum Status {ON, OFF}

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

    Status status();
}

@Retention( RetentionPolicy.RUNTIME )
@interface Server {
    /**
     * @return the name of this server.
     */
    String name() default "-";

    /**
     * @return the port the server should listen to.
     */
    int port() default 80;

    /**
     * @return the location of the Server log file.
     */
    String logFile() default "/var/log/server.log";

    WhiteLists whiteLists() default @WhiteLists;
}

@Retention( RetentionPolicy.RUNTIME )
@interface WhiteLists {
    String[] ips() default { };

    int[] ports() default { };
}
