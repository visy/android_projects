package com.novelamusements.darewheel;

import java.util.ArrayList;
import java.util.Random;

import com.novelamusements.darewheel.R;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
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

public class WheelActivity extends Activity implements SensorEventListener {

	private String TAG = "WheelActivity";

	private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;
    
    int sensorCoolOffTime = 500;
    
    protected PowerManager.WakeLock mWakeLock;
    
    ImageView mWheelView = null;
    TextView mVerbView = null;
    RelativeLayout mWheelVerbGroup;
    ImageView mArrowHandView = null;
    ImageView mPhoneView = null;
    
    MediaPlayer mPlayer;

    RelativeLayout mTicketGroup;
    RelativeLayout mTruthOrDareGroup;
    RelativeLayout mInfoGroup;
    
    Button mDareButton;
    Button mTruthButton;
    
    boolean pressedBack = false;
    boolean dareMode = false;
    boolean showingText = false;
    boolean showingTod = false;
    
    String[] truthWheelSector = {
    		"dare",
    		"truth",
    		"truthordare",
    		"dare",
    		"truth",
    		"truthordare",
    		"dare",
    		"truth",
    		"pass",
    		"truthordare",
    		"dare",
    		"truth",
    		"truthordare",
    		"truth",
    		"dare",
    		"truth",
    		"pass",
    		"truthordare"
    };

    String[] truthTexts = {
    		"Worst gift you have ever received?",
    		"Have you ever peed in a pool?",
    		"What's your wildest fantasy?",
    		"Which was the most embarrassing moment of your life?",
    		"What is the one quality or feature you would like to change about yourself?",
    		"Do you have a crush on any of your friend's significant other?",
    		"Do you think your significant other is marriage material?",
    		"What was the craziest thing that happened to you at a mall?",
    		"What is the meanest thing that you have done in your life?",
    		"Would you ever cheat on your boyfriend?",
    		"Have you ever lied to your partner to avoid an intimate moment?",
    		"What is the worst rumor that you have participated in intentionally?",
    		"Describe the strangest dream you have ever had in your life.",
    		"In your opinion, what is the most offensive word?",
    		"When has a lie come back to bite you?",
    		"When have you been wrongly accused of something?",
    		"What would it take for you to become romantically involved with a coworker?",
    		"Would you rather dump someone or get dumped by someone? Why?",
    		"When was the last time someone saw you naked?",
    		"What was the nastiest joke you ever played on someone?",
    		"When have you been caught picking your nose?",
    		"What is one question that you do not what to hear the answer to?",
    		"What is the meanest thing you have done on a date?",
    		"When have you seen sexual harassment in the work placement?",
    		"What is more important than money?",
    		"Who is there in your life that you would take a bullet for?",
    		"When in your life have you 'gamed the system'?",
    		"When have you had a run in with the law?",
    		"If you could see 24 hours into the future what would you do with this ability?",
    		"When have you seen someone else steel something?",
    		"What is your best physical attribute?",
    		"Who in the room do you think would be a bad date?",
    		"Assuming every man/woman has their price, what is yours?",
    		"What talent do you have that is embarrassing to share?",
    		"If you significant other said it was alright would you cheat on them?",
    		"What prejudice do you harbor?",
    		"Who in this room do you most trust?",
    		"When where you embarrassed getting caught in the middle of something?",
    		"What is something you stole?",
    		"Who will be the next person you will kiss?",
    		"What is the worst thing about being a grown-up?",
    		"What fear keeps you up at night?",
    		"In what way are you inadequate?",
    		"When have you lost you dignity?",
    		"What are you afraid of?",
    		"Who in this room would be the worst to be trapped in an elevator with?",
    		"What is the longest you have gone without taking a bath or shower?",
    		"What do you do to let someone that you are interested in know that you like them?",
    		"What TV show are you embarrassed about watching?",
    		"What things are you shallow about?",
    		"What music are you embarrassed about listing too?",
    		"Why did you break up with your first partner?",
    		"If you were the opposite sex for one day, what would you do?",
    		"What action from you past would put you in jail if law enforcement ever found out?",
    		"What is the worst sin you've ever committed?",
    		"What makes you feel insecure?",
    		"How much money did you make last year?",
    		"If you could go out on a date with anyone in this room who would it be?",
    		"When have you been fired from a job?",
    		"If you could kiss anyone one in this room who would it be?",
    		"What celebrity do you have a crush on?",
    		"Would you still love your significant other if they gained 100 pounds?",
    		"When you are trying to impress people what personality trait do you hide?",
    		"What flaw is enough to cause you to break off a relationship?",
    		"What is the first physical feature you look for in someone you are attracted to?",
    		"What feature of yours makes you self-conscious?",
    		"When have you broken your mother's heart?",
    		"When have you loved someone who has not loved you back?",
    		"Who was your first crush with?",
    		"What was the last lie you told?",
    		"What was the most painful break up you ever had?",
    		"When you lie about your income do you round up or down? By how much?",
    		"Have you ever been unfaithful to a partner?",
    		"When was the last time you were caught in a lie?",
    		"What is the biggest thing you have stolen?",
    		"When have you seen a romantic mood die?",
    		"What was your first kiss like?",
    		"Who would you consider the worst date in your school?",
    		"Under what circumstances would you steal a friend's date?",
    		"Who is a movie villain you find attractive?",
    		"What is the worst thing about being your gender?",
    		"What is a rumor you spread that you knew was not true?" ,
    		"In what way have you grown up too quickly?",
    		"What would make you cheat on your boyfriend/girlfriend?",
    		"What is the meanest thing that you have done in your life?",
    		"If you could be invisible for one day what would you do?",
    		"When have you killed a romantic mood on purpose?",
    		"How do you want to be proposed to?",
    		"What movie character do you identify with?",
    		"Who is/was the 'hot teacher' at your school?",
    		"If you had 2 days with no parental supervision, what would you do?",
    		"What was the most romantic gift you have given?",
    		"What dream/goal do you have that you have not shared with anyone else?",
    		"What do you think the best thing would be about being the opposite sex?",
    		"What would cause you to marry your ex-boyfriend/ex-girlfriend?",
    		"Describe your perfect date.",
    		"What do you want to do but are too young for?",
    		"When is it acceptable to lose your temper?",
    		"When was the first time you had your heart broken?",
    		"What was your worst day at school?",
    		"When was an embarrassing time that you farted?",
    		"When is it alright for your boyfriend/girlfriend to lie to you?",
    		"When is it alright to mix love and business?",
    		"If you could trade bodies with another player who would it be?",
    		"If you could trade lives with another person you know for one week who would it be?",
    		"What are you most likely to become famous for?",
    		"What was the most awkward place you were when you realized you had to pee?",
    		"Have you ever fooled around with anyone in the room? Who was it?",
    		"Have you ever been in love with two people at the same time?",
    		"If you had to, whom in the group would you vote off of the island?",
    		"What is the worst punishment your parents used?",
    		"Who in the room do you think will be the most successful 20 years from now?",
    		"How do you think you are going to die?",
    		"What did you do to get in the most trouble with your parents?",
    		"What is a bad habit you have that would you like to break?",
    		"When did you almost die?",
    		"What do you thing the afterlife is like?",
    		"Who was your mother's favorite child?",
    		"What unresolved issues do you have with your parents?",
    		"When was the last time you went to the bathroom somewhere other than the toilet?",
    		"What embarrassing job does your mom/dad still do for you?",
    		"When have you taken someone's photograph without them knowing?",
    		"What is the lamest song you know every word to?",
    		"When are you happy when you see someone else fail?",
    		"On a scale of 1-10 how physically attractive are you?",
    		"What is the most romantic/loving moment of your life?",
    		"If you could trade lives with another player for one day who would it be? Why?"
    		};
    		
    		String[] dareTexts = {
    		"Kiss the first person you come across.",
    		"Go out and flirt with the first person you come across.",
    		"Exchange any item of clothing another player.",
    		"Perform a lap dance on your person of choice in the room.",
    		"Wear your underpants over your pair of trousers.",
    		"Have another player style your hair.",
    		"Talk for 90 seconds without stopping or using the word 'um'.",
    		"Shake a strangers hand and refuse to let go.",
    		"Brush another player's teeth.",
    		"Put your fingers in your ear and lick them.",
    		"Sing everything you say for the next 5 minutes.",
    		"Eat three bites of dog or cat food.",
    		"Put your pants on backwards.",
    		"Lick the floor.",
    		"Do the macarena.",
    		"Do a cartwheel.",
    		"Do a handstand.",
    		"Yodel.",
    		"Perform 'I'm a Little Tea Pot'.",
    		"Wear your underwear on your head.",
    		"Make on obscene phone call.",
    		"Take a shot of ketchup.",
    		"Take a shot of syrup.",
    		"Dance like a ballerina.",
    		"Moonwalk across the room.",
    		"Break dance.",
    		"Do the robot dance.",
    		"Sing a song.",
    		"Wear all your clothes inside out.",
    		"Wear all your clothes in reverse order.",
    		"Wear a toilet paper turban.",
    		"Lick the bottom of your own foot.",
    		"Blow a raspberry on the back of someone's neck.",
    		"Put on someone else's bra.",
    		"Attempt to stand on your head.",
    		"Try to itch your armpit with your big toe.",
    		"Kiss someone on the cheek.",
    		"Pick your nose and eat it.",
    		"Show how you would flirt with someone.",
    		"Share something that makes you laugh.",
    		"Do a death scene.",
    		"Skip backwards around the group while singing.",
    		"Sing a nursery rhyme.",
    		"Make a funny face.",
    		"Lick the food of your choice off the back of someone's neck.",
    		"Say the words 'in bed' after everything you say for the next 5 min.",
    		"Give the player to the left of you a piggy back ride.",
    		"Sing the national anthem.",
    		"Put an ice cube down your shirt.",
    		"Bite your big toe.",
    		"Change the national anthem and sing it.",
    		"Do your best famous person or famous character impression.",
    		"Do a hula dance.",
    		"Tell us your best joke.",
    		"Dip your hands in the toilet.",
    		"Do the can-can.",
    		"Do the chicken dance.",
    		"Keep an ice cube down your pants till it melts.",
    		"Try to put your feet behind your head.",
    		"Do 25 push ups.",
    		"Do 50 sit ups.",
    		"Do 25 squats."
    	};

    @Override
    public void onBackPressed() {
    	pressedBack = true;
    	super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    
    public void onWindowFocusChanged (boolean hasFocus) {
	    mWheelView.setTranslationX(mWheelView.getMeasuredWidth()/2+(mWheelView.getMeasuredWidth()*0.13f));
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_wheel);
		
	    mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	    accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	    
	    mWheelView = (ImageView)findViewById(R.id.wheelView);
	        
	    mWheelView.setScaleX(2.6f);
	    mWheelView.setScaleY(2.6f);
        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        
        mArrowHandView = (ImageView)findViewById(R.id.arrowHandView);
        
        mTicketGroup = (RelativeLayout)findViewById(R.id.ticketGroup);
        mTruthOrDareGroup = (RelativeLayout)findViewById(R.id.truthOrDareGroup);
        mInfoGroup =(RelativeLayout)findViewById(R.id.infoGroup);

        mInfoGroup.setVisibility(View.VISIBLE);
        zoomInView(mInfoGroup);
        showingText = true;
        showingTod = false;
        
        mPhoneView = (ImageView)findViewById(R.id.phoneView);
        rotateView(mPhoneView);
        
        mInfoGroup.setOnTouchListener(new OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_UP){
		        	zoomOutViewSpinOn(mInfoGroup);
		        	mInfoGroup.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

		            return true;
		        }
		        return false;
		    }
		});
        
        Typeface tf = Typeface.createFromAsset(getAssets(),"FHANicholsonFrench.ttf");  

		TextView tv = (TextView)findViewById(R.id.ticketTextView);
		tv.setTypeface(tf);

		tv = (TextView)findViewById(R.id.ticketCaptionView);
		tv.setTypeface(tf);

		tv = (TextView)findViewById(R.id.todCaption);
		tv.setTypeface(tf);

		tv = (TextView)findViewById(R.id.orText);
		tv.setTypeface(tf);

		Button b = (Button)findViewById(R.id.truthButton);
		b.setTypeface(tf);

		b = (Button)findViewById(R.id.dareButton);
		b.setTypeface(tf);
		
		tv = (TextView)findViewById(R.id.infoText);
		tv.setTypeface(tf);
		tv.setText("TOUCH TO START!");

        Intent intent = getIntent();
        if(intent.getStringExtra("mode").equals("truth")) dareMode = false;
        else dareMode = true;
        
        mPhoneView = (ImageView)findViewById(R.id.phoneView);
		mPhoneView.setVisibility(View.VISIBLE);
                
        if (!dareMode) {
        	mWheelView.setImageResource(R.drawable.truthwheel);
        }
        
        spinning = false;
    	handler.postDelayed(runnable, sensorCoolOffTime);
        
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        this.mWakeLock.acquire();
        
        pressedBack = false;
	}
	
	protected void onResume() {
	    super.onResume();
	    mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
	    mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
	}

	protected void onPause() {
	    super.onPause();
	    mSensorManager.unregisterListener(this);
	    try {
	    	if (mPlayer != null) mPlayer.pause();
	    } catch (Exception e) {}
  	}

	float[] mGravity;
	float[] mGeomagnetic;
	float prevAzimut = 0.0f;
	boolean spinning = false;
	boolean spinStart = false;

	private Animator mCurrentAnimator;
	private Animator mCurrentAnimator2;

	private void handJiggle() {

		AnimatorSet set = new AnimatorSet();

		ObjectAnimator a1 = ObjectAnimator.ofFloat(mArrowHandView, View.TRANSLATION_X, 0.0f, 16.0f);
		ObjectAnimator a2 = ObjectAnimator.ofFloat(mArrowHandView, View.TRANSLATION_X, 16.0f, 0.0f);
		
	    set.playSequentially(a1,a2);

	    set.setDuration(850);
	    set.setInterpolator(new BounceInterpolator());
	    
	    set.addListener(new AnimatorListenerAdapter() {
	        @Override
	        public void onAnimationEnd(Animator animation) {
	        	Log.i(TAG, "onAnimationEnd hand");
	        	mCurrentAnimator = null;
	        	mPlayer.release();
	        }

	        @Override
	        public void onAnimationCancel(Animator animation) {
	        	Log.i(TAG, "onAnimationCancel hand");
	        	mCurrentAnimator = null;
	        	mPlayer.release();
	        }
	    });

	    mCurrentAnimator = set;
	    
	    if (!pressedBack) {
		    mPlayer = MediaPlayer.create(this, R.raw.bell);
		    
	        handler.postDelayed(new Runnable(){
	            @Override
	            public void run() {
	    			if (dareMode) mPhoneView.setVisibility(View.VISIBLE);
	            }
	        }, 2000);
		    

		    mPlayer.start();
		    set.start();
	    }
	}
	
    // The system "short" animation time duration, in milliseconds. This
    // duration is ideal for subtle animations or animations that occur
    // very frequently.
    int mShortAnimationDuration;
    Handler handler = new Handler();
    Runnable runnable = new Runnable(){
        public void run() {
            spinning = false;
        }
    };
	
    float prevSpinEnd = 0;
    
    float currentSpinTarget;
    
    AnimatorSet currentRotateSet;
    
    private void rotateView(View view) {
    	if (currentRotateSet != null) currentRotateSet.end();
    	currentRotateSet = null;
    	
        AnimatorSet set = new AnimatorSet();   
	    set.playSequentially(
	    		ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f),
	    		ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f)
	    		);
	    set.setDuration(2000);
	    set.setInterpolator(new DecelerateInterpolator());

	    set.addListener(new AnimatorListenerAdapter() {
	        @Override
	        public void onAnimationEnd(Animator animation) {
	            super.onAnimationEnd(animation);
	            currentRotateSet = null;
	        }
	    });
	    
	    currentRotateSet = set;
	    set.start();
    }

    
    AnimatorSet mZoomerSet;
    View zoomingView;
    
    private void zoomInView(View view) {
    	showingText = true;
        AnimatorSet set = new AnimatorSet();   
	    set.playTogether(ObjectAnimator.ofFloat(view, View.SCALE_X, 0f, 1f), ObjectAnimator.ofFloat(view, View.SCALE_Y, 0f, 1f));
	    set.setDuration(300);
	    set.setInterpolator(new DecelerateInterpolator());

	    set.addListener(new AnimatorListenerAdapter() {
	        @Override
	        public void onAnimationEnd(Animator animation) {
	        	mZoomerSet = null;
	        }
	    });
	    
	    mZoomerSet = set;
	    set.start();
    }
   
    private void zoomOutView(View view) {
        AnimatorSet set = new AnimatorSet();   
        zoomingView = view;
	    set.playTogether(ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 0f), ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 0f));
	    set.setDuration(300);
	    set.setInterpolator(new DecelerateInterpolator());

	    set.addListener(new AnimatorListenerAdapter() {
	        @Override
	        public void onAnimationEnd(Animator animation) {
	        	mZoomerSet = null;
	        	zoomingView.setVisibility(View.GONE);
	        }
	    });
	    
	    mZoomerSet = set;
	    set.start();
    }

    private void zoomOutViewSpinOn(View view) {
        AnimatorSet set = new AnimatorSet();   
        zoomingView = view;
	    set.playTogether(ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 0f), ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 0f));
	    set.setDuration(300);
	    set.setInterpolator(new DecelerateInterpolator());

	    set.addListener(new AnimatorListenerAdapter() {
	        @Override
	        public void onAnimationEnd(Animator animation) {
	        	mZoomerSet = null;
	        	zoomingView.setVisibility(View.GONE);
	        	showingText = false;
	        }
	    });
	    
	    mZoomerSet = set;
	    set.start();
    }

	private void spinLogic() {
		if (spinning) {
			if (spinStart) {
				// trigger animation
			    if (mCurrentAnimator != null) {
			    	return;
			    }
			    
			    Log.i(TAG, "trigger animation");
			    
			    AnimatorSet set = new AnimatorSet();
			    AnimatorSet set2 = new AnimatorSet();
			    			    
			    int spinRotationCount = 10*(4+(int)(Math.random() * (20)));

			    float spinTarget = 10f*spinRotationCount;
			    
			    currentSpinTarget = prevSpinEnd+spinTarget;
			    
			    set.play(ObjectAnimator.ofFloat(mWheelView, View.ROTATION, prevSpinEnd, currentSpinTarget));
	
			    set2.playSequentially(
			    		ObjectAnimator.ofFloat(mArrowHandView, View.ROTATION, 270, 265.0f),
			    		ObjectAnimator.ofFloat(mArrowHandView, View.ROTATION, 270, 265.0f),
			    		ObjectAnimator.ofFloat(mArrowHandView, View.ROTATION, 270, 265.0f),
			    		ObjectAnimator.ofFloat(mArrowHandView, View.ROTATION, 270, 265.0f),
			    		ObjectAnimator.ofFloat(mArrowHandView, View.ROTATION, 270, 265.0f),
			    		ObjectAnimator.ofFloat(mArrowHandView, View.ROTATION, 270, 265.0f),
			    		ObjectAnimator.ofFloat(mArrowHandView, View.ROTATION, 270, 265.0f),
			    		ObjectAnimator.ofFloat(mArrowHandView, View.ROTATION, 270, 265.0f),
			    		ObjectAnimator.ofFloat(mArrowHandView, View.ROTATION, 270, 265.0f),
			    		ObjectAnimator.ofFloat(mArrowHandView, View.ROTATION, 270, 266.0f),
			    		ObjectAnimator.ofFloat(mArrowHandView, View.ROTATION, 270, 266.0f),
			    		ObjectAnimator.ofFloat(mArrowHandView, View.ROTATION, 270, 266.0f),
			    		ObjectAnimator.ofFloat(mArrowHandView, View.ROTATION, 270, 267.0f),
			    		ObjectAnimator.ofFloat(mArrowHandView, View.ROTATION, 270, 267.0f)
			    		);
			    
			    set2.setDuration(300);
			    set2.setInterpolator(new BounceInterpolator());
			    
			    prevSpinEnd = prevSpinEnd+spinTarget;

			    set.setDuration(5000);
			    set.setInterpolator(new DecelerateInterpolator());
			    
			    set.addListener(new AnimatorListenerAdapter() {
			        @Override
			        public void onAnimationEnd(Animator animation) {
			        	Log.i(TAG, "onAnimationEnd wheel");
			        	
			        	int sectorIndex = (int)(currentSpinTarget/20f) % 18;

			        	// truth or dare mode
			        	
			        	if (!dareMode) {
			        		if (truthWheelSector[sectorIndex].equals("dare")) {
			        			showingText = true;
			        			TextView tvc = (TextView)findViewById(R.id.ticketCaptionView);
			        			tvc.setText("DARE!");

			        			TextView tv = (TextView)findViewById(R.id.ticketTextView);
			        			Random random = new Random();
			        			tv.setText(dareTexts[random.nextInt(dareTexts.length)]);

			        			mTicketGroup.setVisibility(View.VISIBLE);
			        			zoomInView(mTicketGroup);
			        			
			        			mTicketGroup.setOnTouchListener(new OnTouchListener() {
			        			    @Override
			        			    public boolean onTouch(View v, MotionEvent event) {
			        			        if(event.getAction() == MotionEvent.ACTION_UP){
			        			        	mTicketGroup.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
			        			        	zoomOutView(mTicketGroup);
			        			    		TextView tv = (TextView)findViewById(R.id.infoText);
			        			    		tv.setText("NEXT PLAYER!");
			        			    		mInfoGroup.setVisibility(View.VISIBLE);
			        			    		zoomInView(mInfoGroup);
			        			            mPhoneView = (ImageView)findViewById(R.id.phoneView);
			        			    		mPhoneView.setVisibility(View.VISIBLE);
			        			            rotateView(mPhoneView);

			        			            return true;
			        			        }
			        			        return false;
			        			    }
			        			});

			        		}
			        		else if (truthWheelSector[sectorIndex].equals("truth")) {
			        			showingText = true;
			        			TextView tvc = (TextView)findViewById(R.id.ticketCaptionView);
			        			tvc.setText("TRUTH!");

			        			TextView tv = (TextView)findViewById(R.id.ticketTextView);
			        			Random random = new Random();
			        			tv.setText(truthTexts[random.nextInt(truthTexts.length)]);
			        			
			        			mTicketGroup.setVisibility(View.VISIBLE);
			        			zoomInView(mTicketGroup);
			        			
			        			mTicketGroup.setOnTouchListener(new OnTouchListener() {
			        			    @Override
			        			    public boolean onTouch(View v, MotionEvent event) {
			        			        if(event.getAction() == MotionEvent.ACTION_UP){
			        			        	mTicketGroup.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
			        			        	zoomOutView(mTicketGroup);
			        			    		TextView tv = (TextView)findViewById(R.id.infoText);
			        			    		tv.setText("NEXT PLAYER!");
			        			    		mInfoGroup.setVisibility(View.VISIBLE);
			        			    		zoomInView(mInfoGroup);
			        			            mPhoneView = (ImageView)findViewById(R.id.phoneView);
			        			    		mPhoneView.setVisibility(View.VISIBLE);
			        			            rotateView(mPhoneView);

			        			            return true;
			        			        }
			        			        return false;
			        			    }
			        			});
			        		}
			        		else if (truthWheelSector[sectorIndex].equals("truthordare")) {
			        			showingText = true;
			        			showingTod = true;

			        			mTruthOrDareGroup.setVisibility(View.VISIBLE);
			        			zoomInView(mTruthOrDareGroup);
			        						        			
			        			 mTruthButton = (Button)findViewById(R.id.truthButton);
			        			 mDareButton = (Button)findViewById(R.id.dareButton);
			        			
			        			mTruthButton.setOnTouchListener(new OnTouchListener() {
								    @Override
								    public boolean onTouch(View v, MotionEvent event) {
								    	mTruthButton.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

								    	mTruthOrDareGroup.setVisibility(View.GONE);
					        			TextView tvc = (TextView)findViewById(R.id.ticketCaptionView);
					        			tvc.setText("TRUTH!");
					        			showingTod = false;

					        			TextView tv = (TextView)findViewById(R.id.ticketTextView);
					        			Random random = new Random();
					        			tv.setText(truthTexts[random.nextInt(truthTexts.length)]);
					        			
					        			mTicketGroup.setVisibility(View.VISIBLE);
					        			zoomInView(mTicketGroup);
					        			
					        			mTicketGroup.setOnTouchListener(new OnTouchListener() {
					        			    @Override
					        			    public boolean onTouch(View v, MotionEvent event) {
					        			        if(event.getAction() == MotionEvent.ACTION_UP){
					        			        	mTicketGroup.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

								        			zoomOutView(mTicketGroup);
					        			    		TextView tv = (TextView)findViewById(R.id.infoText);
					        			    		tv.setText("NEXT PLAYER!");
					        			    		mInfoGroup.setVisibility(View.VISIBLE);
					        			    		zoomInView(mInfoGroup);
					        			            mPhoneView = (ImageView)findViewById(R.id.phoneView);
					        			    		mPhoneView.setVisibility(View.VISIBLE);
					        			            rotateView(mPhoneView);

					        			            return true;
					        			        }
					        			        return false;
					        			    }
					        			});

										return true;
								    }
			        			});

			        			mDareButton.setOnTouchListener(new OnTouchListener() {
								    @Override
								    public boolean onTouch(View v, MotionEvent event) {
								    	mDareButton.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

								    	mTruthOrDareGroup.setVisibility(View.GONE);
					        			TextView tvc = (TextView)findViewById(R.id.ticketCaptionView);
					        			tvc.setText("DARE!");
					        			
					        			showingTod = false;

					        			TextView tv = (TextView)findViewById(R.id.ticketTextView);
					        			Random random = new Random();
					        			tv.setText(dareTexts[random.nextInt(dareTexts.length)]);
					        			
					        			mTicketGroup.setVisibility(View.VISIBLE);
		        			        	zoomInView(mTicketGroup);

					        			mTicketGroup.setOnTouchListener(new OnTouchListener() {
					        			    @Override
					        			    public boolean onTouch(View v, MotionEvent event) {
					        			        if(event.getAction() == MotionEvent.ACTION_UP){
					        			        	mTicketGroup.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
								        			zoomOutView(mTicketGroup);
					        			    		TextView tv = (TextView)findViewById(R.id.infoText);
					        			    		tv.setText("NEXT PLAYER!");
					        			    		mInfoGroup.setVisibility(View.VISIBLE);
					        			    		zoomInView(mInfoGroup);
					        			            mPhoneView = (ImageView)findViewById(R.id.phoneView);
					        			    		mPhoneView.setVisibility(View.VISIBLE);
					        			            rotateView(mPhoneView);

					        			    		return true;
					        			        }
					        			        return false;
					        			    }
					        			});

										return true;
								    }
			        			});

			        		}
			        		else if (truthWheelSector[sectorIndex].equals("pass")) {
        			    		TextView tv = (TextView)findViewById(R.id.infoText);
        			    		tv.setText("NEXT PLAYER!");
        			    		mInfoGroup.setVisibility(View.VISIBLE);
        			    		zoomInView(mInfoGroup);
        			            mPhoneView = (ImageView)findViewById(R.id.phoneView);
        			    		mPhoneView.setVisibility(View.VISIBLE);
        			            rotateView(mPhoneView);				        		
			        		}
			        	}
			        		
			        	handler.postDelayed(runnable, sensorCoolOffTime);
					    mPlayer.release();
			            mCurrentAnimator = null;
			            mCurrentAnimator2 = null;
			        	handJiggle();
			        }

			        @Override
			        public void onAnimationCancel(Animator animation) {
			        	Log.i(TAG, "onAnimationCancel wheel");
					    mPlayer.release();
			            mCurrentAnimator = null;
			            mCurrentAnimator2 = null;
			        }
			    });
			    mPlayer = MediaPlayer.create(this, R.raw.wheel);
			    mPlayer.start();
			    set.start();
			    set2.start();
			    mCurrentAnimator = set;
			    mCurrentAnimator2 = set2;
				
				spinStart = false;
			}
		}
	}
	
	boolean leftSpin = false;

	@Override
	public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			  mGravity = event.values;
			if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
			  mGeomagnetic = event.values;
			if (mGravity != null && mGeomagnetic != null) {
			  float R[] = new float[9];
			  float I[] = new float[9];
			  boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
			  if (success) {
			    float orientation[] = new float[3];
			    SensorManager.getOrientation(R, orientation);
	
			    float azimut = orientation[0]; // orientation contains: azimut, pitch and roll
	
			    
			    if (Math.abs(prevAzimut - azimut) > 0.15 && !spinStart && !spinning && !showingText) {
				    Log.i(TAG, "azimut var: " + (Math.abs(prevAzimut - azimut)));
			    	spinning = true;
			    	spinStart = true;

		    		mPhoneView.setVisibility(View.GONE);

			    	leftSpin = (prevAzimut-azimut) < 0.0;
			    	
			    	prevAzimut = azimut;
			    	return;
			    }
			    
			    prevAzimut = orientation[0];
			    
			  }
			}

		if (spinning) { spinLogic(); mPhoneView.setVisibility(View.GONE);}


	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
}
