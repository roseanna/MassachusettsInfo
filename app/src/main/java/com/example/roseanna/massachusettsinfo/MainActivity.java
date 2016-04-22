package com.example.roseanna.massachusettsinfo;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    Spinner spinner;
    ProgressBar progressBar;
    TextView name, population, population2, change;
    String url;
    SQLiteDatabase sampleDB = null;
    ArrayList<Town> towns = null;
    ArrayList<String> townNames = null;
    String tableName = "Mass Population";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner     = (Spinner) findViewById(R.id.spinner);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        name        = (TextView) findViewById(R.id.name);
        population  = (TextView) findViewById(R.id.population);
        population2 = (TextView) findViewById(R.id.population2);
        change      = (TextView) findViewById(R.id.change);
        townNames   = new ArrayList();

        url         = "https://malegislature.gov/District/CensusData";
        createDatabase();
        startView();
    }
    public void onResume(){
        super.onResume();
        this.deleteDatabase("NAME");
        createDatabase();
    }
    public void createDatabase(){
        try{
            sampleDB = openOrCreateDatabase("NAME", MODE_PRIVATE, null);
            createTable();
        }catch(SQLiteException se) {
            Log.e(getClass().getSimpleName(), "Could not create or Open the database");
        }
    }

    private void createTable() {
        sampleDB.execSQL("CREATE TABLE IF NOT EXISTS " + tableName +
                " (City VARCHAR, " +
                "  Population NUMBER );");
        Log.i("Created Table", "Done");
    }

    private void insertIntoDB(String city, int pop){
        ContentValues contentValues = new ContentValues();
        contentValues.put("City", city);
        contentValues.put("Population", pop);
        sampleDB.insert(tableName, null, contentValues);
    }
    public void displayInfo(String city){
        name.setText(city);
    }
    private void insertData(String title, String desc, String date) {
        ContentValues values = new ContentValues();
        values.put("Title", title);
        values.put("Desc", desc);
        values.put("Date", date);
        Log.i("Insert Data", title);
        sampleDB.insert(tableName, null, values);
    }

    public void setArray(ArrayList generated){
        towns = generated;
        update();
    }

    public void update(){
        townNames.clear();
        for (Town town: towns){
            townNames.add(town.name);
        }
        spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, townNames));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                name.setText("For the city of " + townNames.get(position));
                Town selected = null;
                for (Town x : towns) {
                    if (x.name == townNames.get(position))
                        selected = x;
                    if (selected != null) {
                        population.setText("The population in 2000 is " + String.valueOf(selected.population));
                        population2.setText("The population in 2010 is " + String.valueOf(selected.population2));
                        change.setText("Change: " + selected.getChange() + "%");
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public void startView(){
        GetData getData = new GetData();
        getData.execute(url);
    }

    public class GetData extends AsyncTask<String, String, ArrayList> {
        HttpURLConnection urlConnection;
        String regex, regex2;
        Pattern pattern, pattern2;
        Matcher m, m2;

        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);

            regex = "(<td scope=\"row\">)(\\w{2,})(</td>)";
            regex2 = "( <td class=\"number\">)(.*)(</td>)";
            if (pattern == null)
                pattern = Pattern.compile(regex);
            if (pattern2 == null)
                pattern2 = Pattern.compile(regex2);
            towns = new ArrayList();
        }

        @Override
        protected ArrayList doInBackground(String... params) {
            String urlString = params[0];
            Log.i("do in background", urlString);

            try {
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                InputStreamReader reader = new InputStreamReader(in);
                BufferedReader br = new BufferedReader(reader);

                String line = br.readLine();
                ArrayList<Integer> temp = new ArrayList();
                while ((line = br.readLine()) != null) {
                    m = pattern.matcher(line);
                    if (m.find()) {
                        String newName = m.group(2);
                        Log.i("town", newName);
                        int i = 0;
                        while (i < 2){
                            line = br.readLine();
                            m2 = pattern2.matcher(line);
                            if (m2.find()){
                                String pop = m2.group(2);
                                if (pop.contains(","))
                                    pop = pop.replace(",","");
                                int popInt = Integer.valueOf(pop);
                                Log.i(String.valueOf(i), String.valueOf(popInt));
                                temp.add(popInt);
                                i++;
                            }
                        }
                        if (temp.size() == 2) {
                            Town newTown = new Town(newName, temp.get(0), temp.get(1));
                            towns.add(newTown);
                            temp.clear();
                        }
                        else{
                            Log.i("temp size not 2", "wtf");
                        }
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
            return towns;
        }

        public void onPostExecute(ArrayList towns){
            progressBar.setVisibility(View.GONE);
            setArray(towns);
        }

    }


}



