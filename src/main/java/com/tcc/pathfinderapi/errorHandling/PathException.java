package com.tcc.pathfinderapi.errorHandling;

public class PathException extends Exception {

    private String message;

    public PathException () { super("Unknown Path Error"); }

    public PathException (String message) {

        super(message);
        this.message = message;
    }


    @Override
    public String getMessage () { return this.message; }
}
