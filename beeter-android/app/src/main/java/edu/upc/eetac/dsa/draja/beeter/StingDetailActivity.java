package edu.upc.eetac.dsa.draja.beeter;

/**
 * Created by david on 19/11/2014.
 */
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;

import edu.upc.eetac.dsa.draja.beeter.edu.upc.eetac.dsa.draja.beeter.api.AppException;
import edu.upc.eetac.dsa.draja.beeter.edu.upc.eetac.dsa.draja.beeter.api.BeeterAPI;
import edu.upc.eetac.dsa.draja.beeter.edu.upc.eetac.dsa.draja.beeter.api.Sting;

public class StingDetailActivity extends Activity {

    private class FetchStingTask extends AsyncTask<String, Void, Sting> {
        private ProgressDialog pd;

        @Override
        //cualquier numero de strings ...
        protected Sting doInBackground(String... params) {
            Sting sting = null;
            try {
                sting = BeeterAPI.getInstance(StingDetailActivity.this)
                        .getSting(params[0]);
            } catch (AppException e) {
                Log.d(TAG, e.getMessage(), e);
            }
            return sting;
        }

        @Override
        protected void onPostExecute(Sting result) {
            loadSting(result);
            if (pd != null) {
                pd.dismiss();
            }
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(StingDetailActivity.this);
            pd.setTitle("Loading...");
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.show();
        }

    }



    private final static String TAG = StingDetailActivity.class.getName();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sting_detail_layout);
        String urlSting = (String) getIntent().getExtras().get("url");
        (new FetchStingTask()).execute(urlSting);
    }

    private void loadSting(Sting sting) {
        //pasado un sting recupera los textviews del layout y les da valor
        TextView tvDetailSubject = (TextView) findViewById(R.id.tvDetailSubject);
        TextView tvDetailContent = (TextView) findViewById(R.id.tvDetailContent);
        TextView tvDetailUsername = (TextView) findViewById(R.id.tvDetailUsername);
        TextView tvDetailDate = (TextView) findViewById(R.id.tvDetailDate);

        tvDetailSubject.setText(sting.getSubject());
        tvDetailContent.setText(sting.getContent());
        tvDetailUsername.setText(sting.getUsername());
        tvDetailDate.setText(SimpleDateFormat.getInstance().format(
                sting.getLastModified()));
    }

}