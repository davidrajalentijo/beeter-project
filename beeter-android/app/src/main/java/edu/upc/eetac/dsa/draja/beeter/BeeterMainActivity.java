package edu.upc.eetac.dsa.draja.beeter;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;

import edu.upc.eetac.dsa.draja.beeter.edu.upc.eetac.dsa.draja.beeter.api.AppException;
import edu.upc.eetac.dsa.draja.beeter.edu.upc.eetac.dsa.draja.beeter.api.BeeterAPI;
import edu.upc.eetac.dsa.draja.beeter.edu.upc.eetac.dsa.draja.beeter.api.Sting;
import edu.upc.eetac.dsa.draja.beeter.edu.upc.eetac.dsa.draja.beeter.api.StingCollection;
import edu.upc.eetac.dsa.draja.beeter.edu.upc.eetac.dsa.draja.beeter.api.StingAdapter;


public class BeeterMainActivity extends ListActivity {
    //clase anidada, clase parametrizada que tiene una serie de metodos cuyo tipo de los parametros puede ser cualquier cosa. Param(tipo de informacion para procesar la tarea, parametro de entrada por ej que sting quiero ir a buscar, progress tipo de informacion que tu pasas para indicar el resultado del progresso void para indicar que no sabes cuando va a tardar, result tipo de informacion que tu devuelves de la tarea
    private class FetchStingsTask extends
            AsyncTask<Void, Void, StingCollection> {
        private ProgressDialog pd;

        //se invoca antes de iniciar la tarea, nos devuelve todos los stings
        @Override
        protected StingCollection doInBackground(Void... params) {
            StingCollection stings = null;
            try {
                stings = BeeterAPI.getInstance(BeeterMainActivity.this)
                        .getStings();
            } catch (AppException e) {
                e.printStackTrace();
            }
            return stings;
        }
//cogo los stings y los pongo en un array list sacando por pantalla el id y el subject y cierro el progress dialog

        //mostra un post dialog para no dejar a nadie colgado sin saber que hacer
        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(BeeterMainActivity.this);
            pd.setTitle("Searching...");
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.show();
        }

        @Override
        protected void onPostExecute(StingCollection result) {
            addStings(result);
            if (pd != null) {
                pd.dismiss();
            }
        }
    }


    private void addStings(StingCollection stings) {
        stingsList.addAll(stings.getStings());
        adapter.notifyDataSetChanged();
    }


    private final static String TAG = BeeterMainActivity.class.toString();
    //elementos de la lista
    private ArrayList<Sting> stingsList;

    private StingAdapter adapter;
    //cada list view tiene asociado un adapter cuando los datos estan en un formato de array, contiene objetos del tipo string

    /**
     * Called when the activity is first created.
     */
//queda configurado para autenticarse con alicia alicia, hardcode login
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Sting sting = stingsList.get(position);
        Log.d(TAG, sting.getLinks().get("self").getTarget());

        Intent intent = new Intent(this, StingDetailActivity.class);
        intent.putExtra("url", sting.getLinks().get("self").getTarget());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        getMenuInflater().inflate(R.menu.menu_beeter_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.miWrite:
                Intent intent = new Intent(this, WriteStingActivity.class);
                startActivityForResult(intent, WRITE_ACTIVITY);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private final static int WRITE_ACTIVITY = 0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case WRITE_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    Bundle res = data.getExtras();
                    String jsonSting = res.getString("json-sting");
                    Sting sting = new Gson().fromJson(jsonSting, Sting.class);
                    stingsList.add(0, sting);
                    adapter.notifyDataSetChanged();
                }
                break;
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beeter_main);

        stingsList = new ArrayList<Sting>();
        adapter = new StingAdapter(this, stingsList);
        setListAdapter(adapter);

        SharedPreferences prefs = getSharedPreferences("beeter-profile",
                Context.MODE_PRIVATE);
        final String username = prefs.getString("username", null);
        final String password = prefs.getString("password", null);
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password
                        .toCharArray());
            }
        });
        (new FetchStingsTask()).execute();
    }
}
