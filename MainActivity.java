package com.redcouagmail.newwayofphoto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.loopj.android.http.HttpGet;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;


public class MainActivity extends AppCompatActivity {

    public static GridView gridView;
    public static GridViewAdapter gridViewAdapter;

    static String url;

    static ArrayList<String> picURLS;

    static Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridView = (GridView)findViewById(R.id.imageGrid);
        gridViewAdapter = new GridViewAdapter(this);
        // gridView.setAdapter(gridViewAdapter);

        context = getApplicationContext();

        getActionBar().setTitle("Flickr");

        url = "https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=API KEY&per_page=50&user_id=52540720@N02&format=json&nojsoncallback=1";

        picURLS = new ArrayList<String>();

        new ParseJSON().execute();

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ImageActivity.class);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });

    }
    public static class JSONParser {
        static InputStream is = null;
        static JSONObject jObj = null;
        static String json = "";

        // constructor
        public JSONParser()
        {

        }

        public JSONObject getJSONFromUrl(String jsonUrl)
        {
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(jsonUrl);
            String responseBody = "DEFAULT_MSG_TEXT";
            int resCode = 0;

            try{

                HttpResponse response = client.execute(get);

                int responseCode = response.getStatusLine().getStatusCode();
                resCode = responseCode;

                switch(responseCode) {
                    case 200:
                        HttpEntity entity = response.getEntity();
                        if(entity != null) {
                            responseBody = EntityUtils.toString(entity);
                        }
                        break;
                }
            }
            catch(Exception ex){
                Log.e("Post Error",resCode + "\n Exception" + ex);
                responseBody = "DEFAULT_MSG_TEXT";
            }

            json = responseBody;

            // try to parse the string to a JSON object
            try
            {
                jObj = new JSONObject(json);
            }
            catch (JSONException e)
            {
                Log.e("JSON Parser", "Error parsing data " + e.toString());
            }

            // return JSON String
            return jObj;
        }
    }

    public static class ParseJSON extends AsyncTask<Void,Void,ArrayList> {
        @Override
        protected void onPreExecute()
        {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected ArrayList doInBackground(Void... params) {
            JSONParser jParser = new JSONParser();

            // get json from url here
            JSONObject json = jParser.getJSONFromUrl(url);
            System.out.println("JSON: " + json);

            try {
                // JSONArray dataArray = json.getJSONArray("photo");
                JSONObject photos = json.getJSONObject("photos");
                JSONArray dataArray = photos.getJSONArray("photo");
                int thumbnailsCount = dataArray.length();

                for (int i = 0; i < thumbnailsCount; i++) {
                    String farm = dataArray.getJSONObject(i).getString("farm");

                   // JSONObject farm = dataArray.getJSONObject(i).getJSONObject("farm");

                    JSONObject server = dataArray.getJSONObject(i).getJSONObject("server");
                    JSONObject id = dataArray.getJSONObject(i).getJSONObject("id");
                    JSONObject secret = dataArray.getJSONObject(i).getJSONObject("secret");
                    String picURL = String.format("http://farm%s.static.flickr.com/%s/%s_%s_b.jpg", farm, server, id, secret);
                    System.out.println(picURL);
                    picURLS.add(picURL);
                }
            }
            catch (Exception e) {
                e.getMessage().toString();
            }

            return picURLS;
        }

        @Override

        protected void onPostExecute(ArrayList result) {
            super.onPostExecute(result);
            gridView.setAdapter(gridViewAdapter);

            for (String thumb : picURLS) {
                System.out.println(thumb);
            }
        }
    }

    public class GridViewAdapter extends ArrayAdapter {

        Context context;

        public GridViewAdapter(Context context) {
            super(context, 0);
            this.context = context;
        }

        public int getCount() {
            return picURLS.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View row = convertView;
            ViewHolder holder;

            if(row == null) {
                LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                row = inflater.inflate(R.layout.grid_row, parent, false);

                holder = new ViewHolder();
                holder.imageView = (ImageView)row.findViewById(R.id.gridImageView);

                // ImageView gridImageView = (ImageView)row.findViewById(R.id.gridImageView);

                row.setTag(holder);
            }
            else {
                holder = (ViewHolder) row.getTag();
            }

            Picasso.with(context)
                    .load(picURLS.get(position))
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.imageView);

            return row;
        }
    }

    static class ViewHolder {
        ImageView imageView;
    }
}

