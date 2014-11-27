package edu.upc.eetac.dsa.draja.beeter.edu.upc.eetac.dsa.draja.beeter.api;

/**
 * Created by david on 17/11/2014.
 */
import java.util.HashMap;
import java.util.Map;

public class Link {

    private String target; //apunta a la url del servicio que queremos acceder
    private Map<String, String> parameters;

    public Link() {
        parameters = new HashMap<String, String>();
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }
}