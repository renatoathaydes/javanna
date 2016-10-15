package com.athaydes.javanna;

final class Either {
    private final Object validResult;
    private final String failure;

    static Either success( Object validResult ) {
        return new Either( validResult, null );
    }

    static Either failure( String error ) {
        if ( error == null ) {
            throw new IllegalArgumentException( "Error is null" );
        }
        return new Either( null, error );
    }

    private Either( Object validResult, String failure ) {
        this.validResult = validResult;
        this.failure = failure;
    }

    boolean isSuccess() {
        return failure == null;
    }

    public Object getValidResult() {
        if ( !isSuccess() ) {
            throw new IllegalStateException( "No valid result" );
        }
        return validResult;
    }

    public String getFailure() {
        if ( isSuccess() ) {
            throw new IllegalStateException( "Not a failure" );
        }
        return failure;
    }
}
