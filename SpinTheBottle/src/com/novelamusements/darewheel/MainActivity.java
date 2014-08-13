package com.novelamusements.darewheel;

import com.novelamusements.darewheel.R;

import com.google.android.gms.ads.*;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.SoundEffectConstants;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.PowerManager;

public class MainActivity extends Activity {

	private String TAG = "MainActivity";
	
    protected PowerManager.WakeLock mWakeLock;
    private InterstitialAd interstitial;
    private Intent intent = null;    
    private boolean adShown = false;
    
    Button buttonDare;
    Button buttonTruth;
    
    MediaPlayer mPlayer = null;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_main);
		
	    // Create the interstitial.
	    interstitial = new InterstitialAd(this);
	    interstitial.setAdUnitId("ca-app-pub-3786246041885398/1200862466");

	    // Create ad request.
	    AdRequest adRequest = new AdRequest.Builder().build();

	    // Begin loading your interstitial.
	    interstitial.loadAd(adRequest);
	    interstitial.setAdListener(new AdListener() {
	    	  @Override
	    	  public void onAdClosed() {
	    		  	if (intent != null) {
		            	startActivity(intent);
		                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	    		  	}
	    	  }
	  	});		
	    
        buttonDare = (Button) findViewById(R.id.buttonDare);
        buttonDare.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	buttonDare.setEnabled(false);
            	intent = new Intent(MainActivity.this, WheelActivity.class);
            	intent.putExtra("mode","dare");
            	buttonDare.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (interstitial.isLoaded() && !adShown) {
                	adShown = true;
                    interstitial.show();
                } else {
	            	startActivity(intent);           
	                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);	
                }
            }
        });

        buttonTruth = (Button) findViewById(R.id.buttonTruth);
        buttonTruth.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	buttonTruth.setEnabled(false);
            	intent = new Intent(MainActivity.this, WheelActivity.class);
            	intent.putExtra("mode","truth");
            	buttonTruth.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (interstitial.isLoaded() && !adShown) {
                	adShown = true;
                    interstitial.show();
                } else {
	            	startActivity(intent);           
	                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }
        });
        		
    	buttonTruth.setEnabled(true);
    	buttonDare.setEnabled(true);

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        this.mWakeLock.acquire();
        
	    mPlayer = MediaPlayer.create(this, R.raw.song);
	    mPlayer.setLooping(true);
	    mPlayer.start();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	    if (mPlayer != null) mPlayer.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
    	buttonTruth.setEnabled(true);
    	buttonDare.setEnabled(true);
	    if (mPlayer != null) mPlayer.start();
	}

}
