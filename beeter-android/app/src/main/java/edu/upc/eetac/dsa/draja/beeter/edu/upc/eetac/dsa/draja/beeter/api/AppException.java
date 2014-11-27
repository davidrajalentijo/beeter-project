package edu.upc.eetac.dsa.draja.beeter.edu.upc.eetac.dsa.draja.beeter.api;

/**
 * Created by david on 17/11/2014.
 */
public class AppException extends Exception {
    //hereda de exceptions
    public AppException() {
        super();
    }

    public AppException(String detailMessage) {
        super(detailMessage);
    }
}