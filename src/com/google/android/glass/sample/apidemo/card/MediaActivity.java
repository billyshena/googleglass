package com.google.android.glass.sample.apidemo.card;

import com.google.android.glass.sample.apidemo.R;
import com.google.android.glass.sample.apidemo.touchpad.TouchpadView;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.TextView;
import android.widget.VideoView;

public class MediaActivity extends Activity implements GestureDetector.ScrollListener{
    private GestureDetector mGestureDetector;

   @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        
//        mGestureDetector = createGestureDetector(this);
        
        Bundle extras = getIntent().getExtras(); 
//        String url = "http://ircadeits.vo.llnwd.net/v1/videos/websurg/vd01/en/vd01en3436.mp4";
//        String url = "http://aw1.fr/glass/video.php?id=vd01en3436";
        String url = extras.getString("url");    // This is the correct URL for production
//        String url = "http://www.w3.org/2010/05/video/mediaevents.html";
        setContentView(R.layout.webview);
  	  	final WebView webview  = (WebView) findViewById(R.id.webView1);
        final WebSettings settings = webview.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setPluginState(WebSettings.PluginState.ON);
        settings.setPluginState(PluginState.ON_DEMAND);
        webview.setWebViewClient(new WebViewClient() {
            // autoplay when finished loading via javascript injection
            public void onPageFinished(WebView view, String url) { webview.loadUrl("javascript:(function() { document.getElementsByTagName('video')[0].play(); })()"); }
        });
        webview.setWebChromeClient(new WebChromeClient());
        webview.loadUrl(url);
    }
   
   	// detect scroll event on the CardView
	@Override
	public boolean onScroll(float arg0, float arg1, float arg2) {
		Log.d("doing","touch");
		return false;
	}
	
	@Override
	protected void onDestroy(){
		System.out.println("The Destroy is called");
		super.onDestroy();
		final WebView webview = (WebView)findViewById(R.id.webView1);
		webview.loadData("", "text/html", "utf-8");
	}
	
	@Override
	protected void onPause(){
		System.out.println("onPause called");
		super.onPause();
	}
	
	@Override 
	protected void onResume(){
		System.out.println("onResume called");
		super.onResume();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		System.out.println("TouchEvent called");
	    // do your stuff here... the below call will make sure the touch also goes to the webview.

	    return super.onTouchEvent(event);
	}
	

	@Override
	public boolean onKeyDown (int keyCode, KeyEvent event) {
  	  	final WebView mWebView  = (WebView) findViewById(R.id.webView1);
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {

	        mWebView.goBack(); // go back in only the web view
	        return true;
	    }
	    System.out.println("onKeyDown called");
	    return super.onKeyDown(keyCode, event);
	}

//	
}
