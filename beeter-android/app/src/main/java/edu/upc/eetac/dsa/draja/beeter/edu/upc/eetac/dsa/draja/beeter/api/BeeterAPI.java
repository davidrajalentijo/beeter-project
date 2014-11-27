package edu.upc.eetac.dsa.draja.beeter.edu.upc.eetac.dsa.draja.beeter.api;

/**
 * Created by david on 17/11/2014.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class BeeterAPI {
    private final static String TAG = BeeterAPI.class.getName();
    private static BeeterAPI instance = null;
    //es un singleton, ni dios lo puede instaciar tiene que haber un metodo publico que permita acceder
    //a la instancia de la clase
    private URL url;

    private BeeterRootAPI rootAPI = null;

    private BeeterAPI(Context context) throws IOException, AppException {
        //carga el asset config.proporties a traves del asset manager que se coge a travaes del contexto que es una actividad
        super();

        AssetManager assetManager = context.getAssets();
        Properties config = new Properties();
        config.load(assetManager.open("config.properties"));
        String urlHome = config.getProperty("beeter.home"); //obtiene el valor de la propiedad beeter_home
        url = new URL(urlHome);

        Log.d("LINKS", url.toString());
        getRootAPI(); //llama a este metodo
    }

    public final static BeeterAPI getInstance(Context context) throws AppException {
        if (instance == null)
            try {
                instance = new BeeterAPI(context);
            } catch (IOException e) {
                throw new AppException(
                        "Can't load configuration file");
            }
        return instance;
    }

    private void getRootAPI() throws AppException {
        Log.d(TAG, "getRootAPI()");
        rootAPI = new BeeterRootAPI(); //modelo de la respueste a la llamda de la root de la api, instancia el modelo qye guarda la respuesta a la raiz del servicio
        HttpURLConnection urlConnection = null; //para hacer conexiones http
        try {
            urlConnection = (HttpURLConnection) url.openConnection(); //abrir conexion
            urlConnection.setRequestMethod("GET"); //indicas el metodo
            urlConnection.setDoInput(true); //vas a leer la respuesta
            urlConnection.connect(); //hace el envio la peticion
        } catch (IOException e) {
            throw new AppException(
                    "Can't connect to Beeter API Web Service");
        }

        BufferedReader reader; //leer la respuesta
        try {
            reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream()));
            StringBuilder sb = new StringBuilder(); //se guarda la respuesta, string de la respuesta
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JSONObject jsonObject = new JSONObject(sb.toString()); //se procesa el json de respuesta, crear un objeto json a partir de la respuesta
            JSONArray jsonLinks = jsonObject.getJSONArray("links"); //el atributo links era un array por lo tanto lo obtines que te devuelve un json array
            parseLinks(jsonLinks, rootAPI.getLinks());
        } catch (IOException e) {
            throw new AppException(
                    "Can't get response from Beeter API Web Service");
        } catch (JSONException e) {
            throw new AppException("Error parsing Beeter Root API");
        }

    }

    //Metodo que devuelve todos los stings
    public StingCollection getStings() throws AppException {
        Log.d(TAG, "getStings()");
        StingCollection stings = new StingCollection();

        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL(rootAPI.getLinks()
                    .get("stings").getTarget()).openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.connect();
        } catch (IOException e) {
            throw new AppException(
                    "Can't connect to Beeter API Web Service");
        }

        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JSONObject jsonObject = new JSONObject(sb.toString());
            JSONArray jsonLinks = jsonObject.getJSONArray("links");
            parseLinks(jsonLinks, stings.getLinks());

            stings.setNewestTimestamp(jsonObject.getLong("newestTimestamp"));
            stings.setOldestTimestamp(jsonObject.getLong("oldestTimestamp"));
            JSONArray jsonStings = jsonObject.getJSONArray("stings");
            for (int i = 0; i < jsonStings.length(); i++) {
                Sting sting = new Sting();
                JSONObject jsonSting = jsonStings.getJSONObject(i);
                sting.setAuthor(jsonSting.getString("author"));
                sting.setStingid(jsonSting.getInt("stingid"));
                sting.setLastModified(jsonSting.getLong("lastModified"));
                sting.setCreationTimestamp(jsonSting.getLong("creationTimestamp"));
                sting.setSubject(jsonSting.getString("subject"));
                sting.setUsername(jsonSting.getString("username"));
                jsonLinks = jsonSting.getJSONArray("links");
                parseLinks(jsonLinks, sting.getLinks());
                stings.getStings().add(sting);
            }
        } catch (IOException e) {
            throw new AppException(
                    "Can't get response from Beeter API Web Service");
        } catch (JSONException e) {
            throw new AppException("Error parsing Beeter Root API");
        }

        return stings;
    }

    private void parseLinks(JSONArray jsonLinks, Map<String, Link> map)
        //le pasamos un array y un mapa donde vamos a guardar los links
            throws AppException, JSONException {
        //te devuele la longitud de este array
        for (int i = 0; i < jsonLinks.length(); i++) {
            Link link = null;
            try {
                //parseo cada uno de los elemento sde cada arrary
                link = SimpleLinkHeaderParser
                        .parseLink(jsonLinks.getString(i));
            } catch (Exception e) {
                throw new AppException(e.getMessage());
            }
            String rel = link.getParameters().get("rel");
            //puede tener varias relaciones separadas por espcacios
            String rels[] = rel.split("\\s");
            for (String s : rels)
                map.put(s, link);
        }
    }


    private Map<String, Sting> stingsCache = new HashMap<String, Sting>();

    public Sting getSting(String urlSting) throws AppException {
        Sting sting = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urlSting);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);

            sting = stingsCache.get(urlSting);
            //si la cache esta vacia devuelve null, se obtiene el etag sino el etag vale el valor de la cache
            String eTag = (sting == null) ? null : sting.geteTag();
            if (eTag != null)
                //si no es null puedo crear la cabecera if none match
                urlConnection.setRequestProperty("If-None-Match", eTag);
            urlConnection.connect();
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                Log.d(TAG, "CACHE");
                return stingsCache.get(urlSting);
                //lo tengo en cahce y devuelve el sting que tengo almacenado en la cache
            }
            //si no es esto, tengo que crear el sting, obtener el etag de la cabecera etag, asociarlo al sting, guardarlo en la cache y leer el sting
            Log.d(TAG, "NOT IN CACHE");
            sting = new Sting();
            eTag = urlConnection.getHeaderField("ETag");
            sting.seteTag(eTag);
            stingsCache.put(urlSting, sting);
            //ir a qui siempre para no almacenar una cosa erronia en el stingscache

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            JSONObject jsonSting = new JSONObject(sb.toString());
            sting.setAuthor(jsonSting.getString("author"));
            sting.setStingid(jsonSting.getInt("stingid"));
            sting.setLastModified(jsonSting.getLong("lastModified"));
            sting.setCreationTimestamp(jsonSting.getLong("creationTimestamp"));
            sting.setSubject(jsonSting.getString("subject"));
            sting.setContent(jsonSting.getString("content"));
            sting.setUsername(jsonSting.getString("username"));
            JSONArray jsonLinks = jsonSting.getJSONArray("links");
            parseLinks(jsonLinks, sting.getLinks());
        } catch (MalformedURLException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new AppException("Bad sting url");
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new AppException("Exception when getting the sting");
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new AppException("Exception parsing response");
        }

        return sting;
    }

    public Sting createSting(String subject, String content) throws AppException {
        Sting sting = new Sting();
        sting.setSubject(subject);
        sting.setContent(content);
        HttpURLConnection urlConnection = null;
        try {
            JSONObject jsonSting = createJsonSting(sting);
            //obtenemos la url a partir de hateoas
            URL urlPostStings = new URL(rootAPI.getLinks().get("create-stings")
                    .getTarget());

            urlPostStings = new URL("http://10.89.102.111:8080/beeter-api/stings");
            //creamos la conexion
            urlConnection = (HttpURLConnection) urlPostStings.openConnection();
            String mediaType = rootAPI.getLinks().get("create-stings").getParameters().get("type");
            urlConnection.setRequestProperty("Accept",
                    mediaType);
            urlConnection.setRequestProperty("Content-Type",
                    mediaType);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.connect();


            PrintWriter writer = new PrintWriter(
                    urlConnection.getOutputStream());
            writer.println(jsonSting.toString());
            writer.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            reader.close();
            writer.close();
            jsonSting = new JSONObject(sb.toString());

            sting.setAuthor(jsonSting.getString("author"));
            sting.setStingid(jsonSting.getInt("stingid"));
            sting.setLastModified(jsonSting.getLong("lastModified"));
            sting.setSubject(jsonSting.getString("subject"));
            sting.setContent(jsonSting.getString("content"));
            sting.setUsername(jsonSting.getString("username"));
            JSONArray jsonLinks = jsonSting.getJSONArray("links");
            parseLinks(jsonLinks, sting.getLinks());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new AppException("Error parsing response");
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new AppException("Error getting response");
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
        return sting;
    }

    private JSONObject createJsonSting(Sting sting) throws JSONException {
        JSONObject jsonSting = new JSONObject();
        jsonSting.put("subject", sting.getSubject());
        jsonSting.put("content", sting.getContent());

        return jsonSting;
    }


}