package com.javahelps.com.javahelps.spidertask3;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private EditText etWord;
    private TextView tvEtymology;
    private TextView tvLoading;
    private TextView tvRootWord;
    private String word= null;
    private int option;
    MyDictonaryRequest myDictonaryRequest;
    DatabaseHelper myDatabaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        etWord=findViewById( R.id.et_word);
        tvEtymology=findViewById( R.id.tv_etymology);
        tvLoading= findViewById( R.id.tv_loading);
        tvRootWord= findViewById( R.id.tv_root_word);
        myDatabaseHelper= new DatabaseHelper(this);
        Button btnSearch = findViewById( R.id.btn_search );
        btnSearch.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etWord.getText().length()==0) {
                    Toast.makeText( MainActivity.this,"Enter the word",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    option=0;
                    word= etWord.getText().toString().toLowerCase();
                    MyDictonaryRequest myDictonaryRequest=new MyDictonaryRequest();
                    myDictonaryRequest.execute(Lemmas());
                }
            }
        } );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate( R.menu.menu,menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item) {
            Intent intent = new Intent( MainActivity.this, ListDataActivity.class );
            startActivity( intent );
            return true;
        }

        return super.onOptionsItemSelected( item );
    }

    private String Etymology() {

        final String language = "en-us";
        final String fields = "etymologies";
        final String strictMatch = "false";
        return "https://od-api.oxforddictionaries.com:443/api/v2/entries/" +
                language + "/" + word + "?" + "fields=" + fields + "&strictMatch=" + strictMatch;
    }

    private String Lemmas() {
        return "https://od-api.oxforddictionaries.com:443/api/v2/lemmas/" +
                "en" + "/" + word ;

    }

    private class MyDictonaryRequest extends AsyncTask<String, Integer ,String> {
        @Override
        protected void onPreExecute() {
            tvLoading.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            final String app_id = "6abba60f";
            final String app_key = "c96ad79de5ccbf3edbda4a14e06233dc";
            try {
                URL url = new URL(params[0]);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Accept","application/json");
                urlConnection.setRequestProperty("app_id",app_id);
                urlConnection.setRequestProperty("app_key",app_key);

                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();

                String line ;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }
                option++;

                return stringBuilder.toString();

            }
            catch (Exception e) {
                option++;
                return e.toString();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if(option==1) {
                try {
                    JSONObject json= new JSONObject( s);
                    JSONArray results= json.getJSONArray( "results");

                    JSONObject resultsObject= results.getJSONObject( 0);
                    JSONArray lexicalEntries= resultsObject.getJSONArray( "lexicalEntries");

                    JSONObject lexicalEntriesObject= lexicalEntries.getJSONObject( 0);
                    JSONArray inflectionOf= lexicalEntriesObject.getJSONArray( "inflectionOf");

                    JSONObject inflectionObject= inflectionOf.getJSONObject( 0);
                    word= inflectionObject.getString("text");

                    MyDictonaryRequest dictionaryRequest= new MyDictonaryRequest();
                    dictionaryRequest.execute(Etymology());
                }
                catch (JSONException e) {
                    tvLoading.setVisibility( View.INVISIBLE );
                    Toast.makeText( MainActivity.this,
                            "No such word found or Connection problem",
                            Toast.LENGTH_SHORT).show();
                }
            }

            if(option==2) {
                try {
                    myDictonaryRequest= new MyDictonaryRequest();
                    JSONObject json = new JSONObject( s );
                    JSONArray results = json.getJSONArray( "results" );

                    JSONObject resultsObject = results.getJSONObject( 0 );
                    JSONArray lexicalEntries = resultsObject.getJSONArray( "lexicalEntries" );

                    JSONObject lexicalEntriesObject = lexicalEntries.getJSONObject( 0 );
                    JSONArray entries = lexicalEntriesObject.getJSONArray( "entries" );

                    JSONObject entriesObject = entries.getJSONObject( 0 );
                    JSONArray etymologies = entriesObject.getJSONArray( "etymologies" );

                    tvEtymology.setText("Word Origin : "+etymologies.getString( 0 ) );
                    tvLoading.setVisibility( View.INVISIBLE );
                    tvRootWord.setText("Root Word : "+word);

                    String newEntry =(etWord.getText().toString());
                    AddData(newEntry);
                }
                catch (JSONException e) {
                    tvLoading.setVisibility( View.INVISIBLE );
                    Toast.makeText( MainActivity.this, "Request failed", Toast.LENGTH_SHORT ).show();
                }
            }
        }
    }


    public void AddData(String newEntry) {
        boolean insertData = myDatabaseHelper.addData(newEntry);
        if (!insertData) {
            Toast.makeText(MainActivity.this,"Failed to add Data",
                    Toast.LENGTH_SHORT).show();
        }
    }

}