/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.glass.sample.apidemo.card;

import com.google.android.glass.app.Card;
import com.google.android.glass.app.Card.ImageLayout;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.sample.apidemo.ApiDemoActivity;
import com.google.android.glass.sample.apidemo.R;
import com.google.android.glass.sample.apidemo.opengl.OpenGlService;
import com.google.android.glass.sample.apidemo.theming.ThemingActivity;
import com.google.android.glass.sample.apidemo.touchpad.SelectGestureDemoActivity;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.MediaController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Creates a card scroll view with examples of different image layout cards.
 */
public final class SearchActivity extends Activity {

    private CardScrollView mCardScroller;
    private static final String TAG = ApiDemoActivity.class.getSimpleName();
    private JSONParser jParser = new JSONParser();
    private ProgressDialog pDialog;
    private static String url_all_products = "http://test.websurg.com/googleglass?keyword=leroy";
    
    
    // JSON Node names
    private static final String TAG_SUCCESS = "title";
    private static final String TAG_PRODUCTS = "results";
    private static final String TAG_PID = "id";

    ArrayList<Card> myCards = new ArrayList<Card>();
    ArrayList<HashMap<String,String>> videos = new ArrayList<HashMap<String,String>>();
    JSONArray products = null;
    @Override
    protected void onCreate(Bundle bundle) {
        
        super.onCreate(bundle);
        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(new CardAdapter(createCards(this)));


        
        // Loading products in Background Thread
        new LoadAllProducts().execute();
        
        
    }
    
    private List<Card> createCards(Context context) {
        ArrayList<Card> cards = new ArrayList<Card>();
        cards.add(new Card(context)
                .setImageLayout(ImageLayout.LEFT)
                .setText(R.string.text_left_images_layout_card).addImage(R.drawable.video1));
        cards.add(new Card(context)
                .setImageLayout(ImageLayout.LEFT)
                .setText(R.string.text_full_images_layout_card).addImage(R.drawable.video2));
        return cards;
    }
    

    
    private Card getImagesCard(Context context) {
        Card card = new Card(context);
        card.addImage(R.drawable.video1);
        card.addImage(R.drawable.video2);
        return card;
    }
    

    @Override
    protected void onResume() {
        super.onResume();
        mCardScroller.activate();
    }

    @Override
    protected void onPause() {
        mCardScroller.deactivate();
        super.onPause();
    }
    
    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class LoadAllProducts extends AsyncTask<String, String, String> {
 
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SearchActivity.this);
            pDialog.setMessage("Loading videos. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
 
        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
//            JSONObject json = jParser.makeHttpRequest(url_all_products, "GET", params);
            ArrayList<HashMap<String, String>> results = new ArrayList<HashMap<String,String>>();
            
            
            
            ServiceHandler sh = new ServiceHandler();
            
            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url_all_products, ServiceHandler.GET);

            
            // Check your log cat for JSON reponse
            try {
                	JSONObject json = new JSONObject(jsonStr);
	                Iterator<?> keys = json.keys();

	                // Loop trough the JSON object received from the PHP script API
	                while( keys.hasNext() ){
	                    String key = (String)keys.next();
	                    if( json.get(key) instanceof JSONObject ){
	                    	JSONObject c = (JSONObject) json.get(key);
	                        HashMap<String, String> map = new HashMap<String, String>();
	                        
	                        // Initialize variables we need to display on each result card
	                    	String id = c.getString("id");
	                    	String title = c.getString("title");
	                    	String authors = c.getString("authors");
	                    	String duration = c.getString("duree");
	                    	String img = c.getString("img");
	                    	
	                    	// Put each variable in the HashMap
	                    	map.put("video",id);
	                    	map.put("id",key);
	                    	map.put("title",title);
	                    	map.put("authors",authors);
	                    	map.put("duration",duration);
	                    	map.put("img",img);
	                    	
	                    
	                    	map.put(key,id);
	                    	results.add(map);
	                    }
	                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
 
            videos = results;
            return null;
        }
 
        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
            
            Intent res = new Intent(SearchActivity.this,ResultActivity.class);
            res.putExtra("arraylist", videos);
            startActivity(res);
            finish();
 
        }
 
    }
    
}

