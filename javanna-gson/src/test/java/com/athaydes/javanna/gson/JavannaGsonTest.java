package com.athaydes.javanna.gson;

import com.google.gson.Gson;
import org.junit.Test;

import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class JavannaGsonTest {

    private final JavannaGson javannaGson = new JavannaGson();

    @Test
    public void testSimpleJsonParsing() throws Exception {
        Server server = javannaGson.parse( testResource( "/server.json" ), Server.class );

        assertEquals( "Super Server", server.name() );
        assertEquals( 43, server.port() );
        assertEquals( "/var/log/server.log", server.logFile() );

        WhiteLists whiteLists = server.whiteLists();

        assertEquals( Arrays.asList( "192.168.10.1", "255.255.255.255" ), Arrays.asList( whiteLists.ips() ) );

        List<Integer> ports = new ArrayList<>( whiteLists.ports().length );
        for (int p : whiteLists.ports()) {
            ports.add( p );
        }
        assertEquals( Arrays.asList( 60, 90 ), ports );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void testJsonCreation() throws Exception {
        Gson gson = new Gson();
        Map expectedMap = gson.fromJson( testResource( "/server.json" ), Map.class );

        // the default value of logFile should be included in the JSON
        expectedMap.put( "logFile", "/var/log/server.log" );

        Server server = javannaGson.parse( testResource( "/server.json" ), Server.class );

        String json = javannaGson.toJson( server );

        // turn the JavannaGson JSON String into a Map using just Gson
        Map javannaGsonMap = gson.fromJson( json, Map.class );

        assertEquals( expectedMap, javannaGsonMap );
    }

    private InputStreamReader testResource( String resource ) {
        return new InputStreamReader( getClass().getResourceAsStream( resource ),
                StandardCharsets.UTF_8 );
    }
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
