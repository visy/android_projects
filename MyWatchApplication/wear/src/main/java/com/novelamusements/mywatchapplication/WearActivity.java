package com.novelamusements.mywatchapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.wearable.activity.InsetActivity;
import android.support.wearable.view.WatchViewStub;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;


public class WearActivity extends InsetActivity implements DataApi.DataListener {

    private int TICK_INTERVAL = 950;
    private IntentFilter timeTickFilter;
    private IntentFilter timeChangedFilter;
    private IntentFilter timeZoneChangedFilter;

    private BroadcastReceiver timeUpdateReceiver;

    private Context mContext;

    private AnalogClock1 clock;
    private TextView mTextView;
    private String TAG = "WearActivity";
    private long startTime = 0;

    int currentBg = 0;
    int currentDial = 0;
    int currentHandSet = 0;

    int bgColor = 0xFFFFFFFF;

    Bitmap mBitmap;
    boolean isRound = true;

    Bitmap mHourHand;
    Bitmap mMinuteHand;
    Bitmap mSecondHand;
    Bitmap mDial;

    int handRecCount = 0;

    GoogleApiClient mGoogleApiClient;
    boolean isConnected = false;

    public void savePng(Bitmap bmp, String filename) {
        FileOutputStream out = null;
        String fname = new File(getFilesDir(), filename).getAbsolutePath();

        try {
            out = new FileOutputStream(fname);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try{
                if (out != null) out.close();
            } catch(Throwable ignore) {}
        }
    }

    public void saveWatchFiles() {
        savePng(mBitmap,"bg.png");
        savePng(mHourHand,"hour.png");
        savePng(mMinuteHand,"minute.png");
        savePng(mSecondHand,"second.png");
        savePng(mDial,"dial.png");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.i(TAG, "onDataChanged");

        if (!mGoogleApiClient.isConnected()) {
            ConnectionResult connectionResult = mGoogleApiClient
                    .blockingConnect(30, TimeUnit.SECONDS);
            if (!connectionResult.isSuccess()) {
                Log.e(TAG, "Service failed to connect to GoogleApiClient.");
                return;
            }
        }

        for (DataEvent event : dataEvents) {


                                     /*

                            bgColor = dataMapItem.getDataMap().getInt("bgColor");


*/
            // hands & dial
            if (event.getType() == DataEvent.TYPE_CHANGED && (
                    event.getDataItem().getUri().getPath().equals("/hourhand") ||
                            event.getDataItem().getUri().getPath().equals("/minutehand") ||
                            event.getDataItem().getUri().getPath().equals("/dial") ||
                            event.getDataItem().getUri().getPath().equals("/secondhand"))
            ) {
                Log.i(TAG, "onDataChanged: hand " + (++handRecCount) + " received");

                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());

                Asset profileAsset = null;
                if (event.getDataItem().getUri().getPath().equals("/hourhand")) profileAsset = dataMapItem.getDataMap().getAsset("hourhand");
                if (event.getDataItem().getUri().getPath().equals("/minutehand")) profileAsset = dataMapItem.getDataMap().getAsset("minutehand");
                if (event.getDataItem().getUri().getPath().equals("/secondhand")) profileAsset = dataMapItem.getDataMap().getAsset("secondhand");
                if (event.getDataItem().getUri().getPath().equals("/dial")) profileAsset = dataMapItem.getDataMap().getAsset("dial");
                Bitmap bitmap = loadBitmapFromAsset(profileAsset);
                if (event.getDataItem().getUri().getPath().equals("/hourhand")) mHourHand = bitmap;
                if (event.getDataItem().getUri().getPath().equals("/minutehand")) mMinuteHand = bitmap;
                if (event.getDataItem().getUri().getPath().equals("/secondhand")) mSecondHand = bitmap;
                if (event.getDataItem().getUri().getPath().equals("/dial")) mDial = bitmap;

                if (handRecCount == 4)  {
                    handRecCount = 0;
                    saveWatchFiles();
                    WearActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            RelativeLayout rl = (RelativeLayout) findViewById(R.id.layout);

                            clock = (AnalogClock1) findViewById(R.id.analogClock);

                            rl.removeView(clock);
                            clock = null;

                            clock = new AnalogClock1(WearActivity.this,null,0);
                            clock.mSecondHand = new BitmapDrawable(getResources(),mSecondHand);
                            clock.mMinuteHand = new BitmapDrawable(getResources(),mMinuteHand);
                            clock.mHourHand = new BitmapDrawable(getResources(),mHourHand);
                            clock.mDial = new BitmapDrawable(getResources(),mDial);
                            clock.setId(R.id.analogClock);
                            rl.addView(clock);
                        }

                    });
                }
            }

            // bg
            if (event.getType() == DataEvent.TYPE_CHANGED &&
                    event.getDataItem().getUri().getPath().equals("/bg")) {
                Log.i(TAG, "onDataChanged: bg received");

                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());

                Asset profileAsset = dataMapItem.getDataMap().getAsset("bg");
                Bitmap bitmap = loadBitmapFromAsset(profileAsset);
                mBitmap = bitmap;
                bgColor  = dataMapItem.getDataMap().getInt("bgcolor");

                if (bitmap != null) {

                    WearActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            ImageView bgImageView = (ImageView) findViewById(R.id.bgImageView);
                            bgImageView.setImageBitmap(mBitmap);
                            bgImageView.setBackgroundColor(bgColor);

                        }

                    });
                }
            }
            ////////////////////////////////////////////////////////////////////////////

        }
    }

    public Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }

        if (isConnected) {
            // convert asset into a file descriptor and block until it's ready
            InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                    mGoogleApiClient, asset).await().getInputStream();
            mGoogleApiClient.disconnect();

            if (assetInputStream == null) {
                Log.w(TAG, "Requested an unknown Asset.");
                return null;
            }
            // decode the stream into a bitmap
            return BitmapFactory.decodeStream(assetInputStream);
        } else return null;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
    }

    public void initUI() {
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        TextView mTextView_round = (TextView) findViewById(R.id.textView_round);

        if (mTextView_round != null) {
            isRound = true;
        }

        clock = (AnalogClock1) findViewById(R.id.analogClock);

        ImageView bgImageView = (ImageView) stub.findViewById(R.id.bgImageView);
        bgImageView.setImageResource(R.drawable.bg_01);

        initGoogle();

        initWatchface();
    }

    public Bitmap loadPng(String filename) {

        String fname = getFilesDir().getAbsolutePath()+"/"+filename;
        Bitmap bmp = null;

        File file = new File(fname);
        if(file.exists()) {
            FileInputStream fin = null;
            try {
                fin = new FileInputStream(fname);
                byte fileContent[] = new byte[(int)file.length()];
                fin.read(fileContent);

                bmp = BitmapFactory.decodeByteArray(fileContent, 0, fileContent.length);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fin != null) {
                    try {
                        fin.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return bmp;
    }

    public void initWatchface() {
        File[] list = getFilesDir().listFiles();

        mBitmap = loadPng("bg.png");
        mHourHand = loadPng("hour.png");
        mMinuteHand = loadPng("minute.png");
        mSecondHand = loadPng("second.png");
        mDial = loadPng("dial.png");

        ImageView bgImageView = (ImageView) findViewById(R.id.bgImageView);
        if (mBitmap != null) bgImageView.setImageBitmap(mBitmap);

        if (mHourHand != null && mMinuteHand != null && mSecondHand != null && mDial != null ) {
            RelativeLayout rl = (RelativeLayout) findViewById(R.id.layout);

            clock = (AnalogClock1) findViewById(R.id.analogClock);

            rl.removeView(clock);
            clock = null;

            clock = new AnalogClock1(WearActivity.this, null, 0);
            clock.mSecondHand = new BitmapDrawable(getResources(), mSecondHand);
            clock.mMinuteHand = new BitmapDrawable(getResources(), mMinuteHand);
            clock.mHourHand = new BitmapDrawable(getResources(), mHourHand);
            clock.mDial = new BitmapDrawable(getResources(), mDial);
            clock.setId(R.id.analogClock);
            rl.addView(clock);
        }
    }

    public void initGoogle() {
        // init Wear connection

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d(TAG, "onConnected: " + connectionHint);
                        // Now you can use the data layer API
                        isConnected = true;
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d(TAG, "onConnectionSuspended: " + cause);
                        isConnected = false;
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(TAG, "onConnectionFailed: " + result);
                        isConnected = false;
                    }
                })
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();

        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onReadyForContent() {
        setContentView(R.layout.activity_wear);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                initUI();
            }
        });
    }


    public static class AnalogClock1 extends View {
        public AnalogClock1(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
        }

        private Time mCalendar;

        public Drawable mHourHand;
        public Drawable mMinuteHand;
        public Drawable mSecondHand;
        public Drawable mDial;

        private int mDialWidth;
        private int mDialHeight;

        private boolean mAttached;

        private final Handler mHandler = new Handler();
        private float mMinutes;
        private float mHour;
        private boolean mChanged;


        Context mContext;
        public AnalogClock1(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public AnalogClock1(Context context, AttributeSet attrs,
                            int defStyle) {
            super(context, attrs, defStyle);
            Resources r = context.getResources();
            mContext=context;

            mDial = r.getDrawable(R.drawable.dial_01);
            mDial.mutate();

            mHourHand = r.getDrawable(R.drawable.hand_01);
            mHourHand.mutate();
            mMinuteHand = r.getDrawable(R.drawable.hand_02);
            mMinuteHand.mutate();
            mSecondHand = r.getDrawable(R.drawable.hand_03);
            mSecondHand.mutate();

            mCalendar = new Time();

            mDialWidth = mDial.getIntrinsicWidth();
            mDialHeight = mDial.getIntrinsicHeight();
        }


        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();

            if (!mAttached) {
                mAttached = true;
                IntentFilter filter = new IntentFilter();

                filter.addAction(Intent.ACTION_TIME_TICK);
                filter.addAction(Intent.ACTION_TIME_CHANGED);
                filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

                getContext().registerReceiver(mIntentReceiver, filter, null, mHandler);
            }

            // NOTE: It's safe to do these after registering the receiver since the receiver always runs
            // in the main thread, therefore the receiver can't run before this method returns.

            // The time zone may have changed while the receiver wasn't registered, so update the Time
            mCalendar = new Time();

            // Make sure we update to the current time
            onTimeChanged();
            counter.start();
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            if (mAttached) {
                counter.cancel();
                getContext().unregisterReceiver(mIntentReceiver);
                mAttached = false;
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSize =  MeasureSpec.getSize(widthMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSize =  MeasureSpec.getSize(heightMeasureSpec);

            float hScale = 1.0f;
            float vScale = 1.0f;

            if (widthMode != MeasureSpec.UNSPECIFIED && widthSize < mDialWidth) {
                hScale = (float) widthSize / (float) mDialWidth;
            }

            if (heightMode != MeasureSpec.UNSPECIFIED && heightSize < mDialHeight) {
                vScale = (float )heightSize / (float) mDialHeight;
            }

            float scale = Math.min(hScale, vScale);

            setMeasuredDimension(resolveSize((int) (mDialWidth * scale), widthMeasureSpec),
                    resolveSize((int) (mDialHeight * scale), heightMeasureSpec));
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            mChanged = true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            boolean changed = mChanged;
            if (changed) {
                mChanged = false;
            }
            boolean seconds = mSeconds;
            if (seconds ) {
                mSeconds = false;
            }
            int availableWidth = getWidth();
            int availableHeight = getHeight();

            int x = availableWidth / 2;
            int y = availableHeight / 2;

            final Drawable dial = mDial;
            int w = dial.getIntrinsicWidth();
            int h = dial.getIntrinsicHeight();

            boolean scaled = false;

            if (availableWidth < w || availableHeight < h) {
                scaled = true;
                float scale = Math.min((float) availableWidth / (float) w,
                        (float) availableHeight / (float) h);
                canvas.save();
                canvas.scale(scale, scale, x, y);
            }

            if (changed) {
                dial.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
            }
            dial.draw(canvas);

            canvas.save();
            canvas.rotate(mMinutes / 60.0f * 360.0f, x, y);

            final Drawable hourHand = mHourHand;
            if (changed) {
                w = hourHand.getIntrinsicWidth();
                h = hourHand.getIntrinsicHeight();
                hourHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
            }
            mMinuteHand.draw(canvas);
            canvas.restore();

            canvas.save();
            canvas.rotate(mSecond, x, y);
            //canvas.rotate(mSecond, x, y);
            final Drawable minuteHand = mMinuteHand;
            if (changed) {
                w = minuteHand.getIntrinsicWidth();
                h = minuteHand.getIntrinsicHeight();
                minuteHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
            }
            mSecondHand.draw(canvas);
            canvas.restore();
            canvas.save();
            canvas.rotate(mHour / 12.0f * 360.0f, x, y);

            //minuteHand = mMinuteHand;
            if (seconds) {
                w = mSecondHand.getIntrinsicWidth();
                h = mSecondHand.getIntrinsicHeight();
                mSecondHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
            }
            hourHand.draw(canvas);

            canvas.restore();
            if (scaled) {
                canvas.restore();
            }
        }
        MyCount counter = new MyCount(100, 100);
        public class MyCount extends CountDownTimer {
            public MyCount(long millisInFuture, long countDownInterval) {
                super(millisInFuture, countDownInterval);
            }

            @Override
            public void onFinish() {
                counter.start();
            }

            @Override
            public void onTick(long millisUntilFinished) {
                mCalendar.setToNow();

                int hour = mCalendar.hour;
                int minute = mCalendar.minute;
                int second = mCalendar.second;

                mSecond=6.0f*second;
                mSeconds=true;

                AnalogClock1.this.invalidate();
            }
        }
        boolean mSeconds=false;
        float mSecond=0;
        private void onTimeChanged() {
            mCalendar.setToNow();

            int hour = mCalendar.hour;
            int minute = mCalendar.minute;
            int second = mCalendar.second;

            mMinutes = minute + second / 60.0f;
            mHour = hour + mMinutes / 60.0f;
            mChanged = true;
        }

        private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                    String tz = intent.getStringExtra("time-zone");
                    mCalendar = new Time(TimeZone.getTimeZone(tz).getID());
                }

                onTimeChanged();

                invalidate();
            }
        };
    }

}