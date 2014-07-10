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
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.DropBoxManager.Entry;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import junit.framework.Assert;

/**
 * Creates a card scroll view with examples of different image layout cards.
 */
public final class ResultActivity extends Activity implements AsyncResponse{

    private CardScrollView mCardScroller;
    private ProgressDialog pDialog;
    private static final String TAG = ApiDemoActivity.class.getSimpleName();
    ArrayList<HashMap<String, String>> arl = new ArrayList<HashMap<String,String>>();
    ArrayList<HashMap<Integer,String>> results = new ArrayList<HashMap<Integer,String>>();

    private ImageView downloadedImg;
	public Bitmap bitmap = null;
	
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        arl = (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("arraylist");
        ArrayList<Card> cards = new ArrayList<Card>();
        int j = 0;
        
        // Loop on the Hashmap to get video information and store them into another array
        for (HashMap<String, String> map : arl){
            HashMap<Integer,String> vid = new HashMap<Integer,String>();
            String authors = map.get("authors").replace("\u00a0",""); // Escape and remove &nbsp
            authors = authors.replace("&nbsp;"," ");
            int duration = ( Integer.parseInt(map.get("duration")) / 60 ) ; // Convert duration in seconds to minutes
            String title = map.get("title");
            if(title.length() > 40){
            	title = title.substring(0,40) + "...";
            }

            Card c = new Card(getApplicationContext())
            .setImageLayout(ImageLayout.LEFT)
            .setText("Title : " + " " + title + "\n\n" + "Authors : " +  authors)
            .setFootnote("Duration : " + " " + duration);
           
            // New AynscTask
//            downloadedImg = new ImageView(getApplicationContext());
//            new ImageDownloader(c).execute("http://www.websurg.com/packages/img/experts/642.png");
            
            int img = getDrawable(getApplicationContext(), "image" + map.get("img"));
            c.addImage(img);           
            vid.put(j,map.get("video"));
            results.add(j,vid);
            cards.add(c);
            j++;   

        }
        
        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(new CardAdapter(cards));
        setContentView(mCardScroller);
        setCardScrollerListener();
        
    }
    

    public static int getDrawable(Context context, String name)
    {
        Assert.assertNotNull(context);
        Assert.assertNotNull(name);

        return context.getResources().getIdentifier(name,
                "drawable", context.getPackageName());
    }

    /**
     * Create list of cards that showcase different type of {@link Card} API.
     */
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
    
    private void setCardScrollerListener() {
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	String url = "http://aw1.fr/glass/video.php?id="+results.get(position).get(position);
            	Intent i = new Intent(ResultActivity.this,MediaActivity.class);
            	i.putExtra("url",url);
            	startActivity(i);
            }
        });
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
    
    
    
    
    /** ASYNC TASK TO PERFORM THE BITMAP OF THE SPECIFIED URL **/
    private class ImageDownloader extends AsyncTask<String,Void,Bitmap> {
    	public AsyncResponse delegate=null;
		@Override
		protected Bitmap doInBackground(String... param) {
			// TODO Auto-generated method stub
			return downloadBitmap(param[0]);
		}

		@Override
		protected void onPreExecute() {
			Log.i("Async-Example", "onPreExecute Called");
			
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			downloadedImg.setImageBitmap(result);
			if (downloadedImg.getDrawable() instanceof BitmapDrawable) {
			    bitmap = ((BitmapDrawable) downloadedImg.getDrawable()).getBitmap();
			} else {
			    Drawable d = downloadedImg.getDrawable();
			    bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
			}


			
		}

		private Bitmap downloadBitmap(String url) {
			// initilize the default HTTP client object
			final DefaultHttpClient client = new DefaultHttpClient();

			//forming a HttoGet request 
			final HttpGet getRequest = new HttpGet(url);
			try {

				HttpResponse response = client.execute(getRequest);

				//check 200 OK for success
				final int statusCode = response.getStatusLine().getStatusCode();

				if (statusCode != HttpStatus.SC_OK) {
					Log.w("ImageDownloader", "Error " + statusCode + 
							" while retrieving bitmap from " + url);
					return null;

				}

				final HttpEntity entity = response.getEntity();
				if (entity != null) {
					InputStream inputStream = null;
					try {
						// getting contents from the stream 
						inputStream = entity.getContent();

						// decoding stream data back into image Bitmap that android understands
						final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

						return bitmap;
					} finally {
						if (inputStream != null) {
							inputStream.close();
						}
						entity.consumeContent();
					}
				}
			} catch (Exception e) {
				// You Could provide a more explicit error message for IOException
				getRequest.abort();
				Log.e("ImageDownloader", "Something went wrong while" +
						" retrieving bitmap from " + url + e.toString());
			} 

			return null;
		}
	}





	@Override
	public Bitmap processFinish(Bitmap result) {
		// TODO Auto-generated method stub
		return result;
	}
    

    
    
}
