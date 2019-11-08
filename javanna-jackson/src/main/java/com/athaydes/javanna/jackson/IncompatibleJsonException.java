package com.athaydes.javanna.jackson;

public class IncompatibleJsonException extends RuntimeException {
    public IncompatibleJsonException( String message ) {
        super( message );
    }
}
