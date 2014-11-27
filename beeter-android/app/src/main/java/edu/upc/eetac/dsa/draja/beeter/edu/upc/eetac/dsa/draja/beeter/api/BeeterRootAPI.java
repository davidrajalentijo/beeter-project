package edu.upc.eetac.dsa.draja.beeter.edu.upc.eetac.dsa.draja.beeter.api;

/**
 * Created by david on 17/11/2014.
 */
import java.util.HashMap;
import java.util.Map;

public class BeeterRootAPI {
//mapa con la relacion de todos los enlaces
    private Map<String, Link> links;

    public BeeterRootAPI() {
        links = new HashMap<String, Link>();
    }

    public Map<String, Link> getLinks() {
        return links;
    }

}