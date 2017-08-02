package com.example.popescu.bookapi;

import android.app.LoaderManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();
    private ListView lv;
    ArrayList<HashMap<String, String>> booksVolumes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        booksVolumes = new ArrayList<>();
        lv = (ListView) findViewById(R.id.list);
        new GetItems().execute();
    }

    private class GetItems extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "Json Data is downloading", Toast.LENGTH_LONG).show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String url = "https://www.googleapis.com/books/v1/volumes?q={search%20terms}";
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray items = jsonObj.getJSONArray("items");

                    // looping through All Volumes
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject c = items.getJSONObject(i);
                        String kind = c.getString("kind");
                        String id = c.getString("id");
                        String etag = c.getString("etag");
                        String selfLink = c.getString("selfLink");
                        // Phone node is JSON Object
                        JSONObject volumeInfo = c.getJSONObject("volumeInfo");
                        String title = volumeInfo.getString("title");
                        String subtitle = volumeInfo.getString("subtitle");
                        //JSONArray authors= volumeInfo.getJSONArray("authors");
                        if (volumeInfo.has("authors")) {
                            String authors = volumeInfo.getString("authors");
                            // parse the authors field
                        } else {
                            Toast.makeText(MainActivity.this, "No Book found, search again", Toast.LENGTH_SHORT).show();
                            // Authors placeholder text (e.g. "Author N/A")
                        }
                        // tmp hash map for single volume
                        HashMap<String, String> item = new HashMap<>();

                        // adding each child node to HashMap key => value
                        item.put("kind", kind);
                        item.put("id", id);
                        item.put("title", title);
                        item.put("subtitle", subtitle);
                        //item.put("authors", authors);

                        // adding contact to volume list
                        booksVolumes.add(item);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                }

            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            ListAdapter adapter = new SimpleAdapter(MainActivity.this, booksVolumes,
                    R.layout.list_item, new String[]{"id", "title"},
                    new int[]{R.id.id, R.id.title});
            lv.setAdapter(adapter);
        }
        private boolean isNetworkAvailable() {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }
}

