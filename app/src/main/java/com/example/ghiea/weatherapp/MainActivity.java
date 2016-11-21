package com.example.ghiea.weatherapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.R.attr.country;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;

public class MainActivity extends AppCompatActivity {

    String APIKEY;
    String imageURLString;
    String cityNameText;
    String countryCodeText;
    ArrayList<String> mainAndDescription;
    ArrayList<Bitmap> weatherIcon;
    TextView locationText;
    EditText editText;

    public void onSearch(View view) {
        if(!editText.getText().equals(null)) {
            String cityName = editText.getText().toString();
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute("http://api.openweathermap.org/data/2.5/weather?q=" + cityName.replace(" ", "%20") + "&APPID=" + APIKEY);

            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } else {

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainAndDescription = new ArrayList<>();
        weatherIcon = new ArrayList<>();
        editText = (EditText) findViewById(R.id.editText);
        locationText = (TextView) findViewById(R.id.locationTextView);


        //DownloadTask downloadTask = new DownloadTask();
        APIKEY = "a7085fa4f4ccb6812b8c78fe24ea4bf6";

//        String cityName = "Los Angeles, US";
//
//        downloadTask.execute("http://api.openweathermap.org/data/2.5/weather?q=" + cityName.replace(" ", "%20") + "&APPID=" + APIKEY);

    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.connect();
                InputStream inputStream = httpURLConnection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);

                return myBitmap;

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onPostExecute(Bitmap result) {
            //celebImage.setImageBitmap(result);

            //set image
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);

                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                int data = inputStreamReader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;

                    data = inputStreamReader.read();
                }

                return result;

            } catch (Exception e) {
                e.printStackTrace();
            }


            return "Failed";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.i("JSON", s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                String weatherInfo = jsonObject.getString("weather");
                String cityInfo = jsonObject.getString("name");
                String countryInfo = jsonObject.getString("sys");
                countryInfo = new JSONObject(countryInfo).getString("country");
                Log.i("Weather content", weatherInfo);
                Log.i("City name", cityInfo);
                Log.i("Country code", countryInfo);

                cityNameText = cityInfo;
                countryCodeText = countryInfo;
                locationText.setText(cityNameText + ", " + countryCodeText);
                JSONArray jsonArray = new JSONArray(weatherInfo);
                mainAndDescription.clear();
                weatherIcon.clear();
                for(int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonPart = jsonArray.getJSONObject(i);
                    Log.i("main", jsonPart.getString("main"));
                    Log.i("description", jsonPart.getString("description"));
                    Log.i("icon", jsonPart.getString("icon"));


                    mainAndDescription.add(jsonPart.getString("main") + ":\n" + jsonPart.getString("description").toUpperCase().charAt(0) + jsonPart.getString("description").substring(1));
                    ImageDownloader imageDownloader = new ImageDownloader();
                    imageURLString = "http://openweathermap.org/img/w/";
                    weatherIcon.add(imageDownloader.execute(imageURLString + jsonPart.getString("icon") + ".png").get());

                }

                Snackbar.make(findViewById(R.id.editText),"Found city",Snackbar.LENGTH_LONG).show();

            } catch (JSONException e) {
                e.printStackTrace();
                Snackbar.make(findViewById(R.id.editText),"City not found",Snackbar.LENGTH_LONG).show();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            CustomList customList = new CustomList(MainActivity.this, mainAndDescription, weatherIcon);
            ListView myList = (ListView) findViewById(R.id.myList);
            myList.setAdapter(customList);


        }
    }
}
