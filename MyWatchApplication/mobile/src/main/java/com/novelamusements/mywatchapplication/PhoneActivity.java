package com.novelamusements.mywatchapplication;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import at.technikum.mti.fancycoverflow.FancyCoverFlow;


public class PhoneActivity extends Activity implements ColorPicker.OnColorChangedListener {

    public static int currentColor = 0;
    public static Context mContext;
    public static Activity mActivity;

    @Override
    public void onColorChanged(int color) {
        //gives the color when it's changed
        currentColor = color;
        picker.setOldCenterColor(color);
        FancyCoverFlow fcf = (FancyCoverFlow) findViewById(R.id.coverFlow);
        fcf.setBackgroundColor(color);
    }

    private boolean isConnected = false;

    private ColorPicker picker;
    private SVBar svBar;
    private OpacityBar opacityBar;
    private Button button;
    private TextView text;
    private TextView bgTitle;

    int currentBg = 0;
    int[] bgImages = {};
    Bitmap[] bgBitmaps = {};
    public static ArrayList<String> bgNames;
    ArrayList<String> bgNamesToDownload = null;
    public int downloadedFileCount = 0;
    int bgCount = 0;
    int resLimit = 0;

    int currentDial = 0;
    int[] dialImages = {};
    int dialCount = 0;

    int handSetCount;
    int currentHandSet = 0;

    int count = 0;

    boolean isRound = false;

    private int[] handSets = {
            R.drawable.hand_01,R.drawable.hand_02,R.drawable.hand_03,
            R.drawable.hand_04,R.drawable.hand_05,R.drawable.hand_06,
            R.drawable.hand_07,R.drawable.hand_08,R.drawable.hand_09,
            R.drawable.hand_10,R.drawable.hand_11,R.drawable.hand_12,
            R.drawable.hand_01,R.drawable.hand_02,R.drawable.hand_14,
            R.drawable.hand_04,R.drawable.hand_05,R.drawable.hand_13,
            R.drawable.hand_07,R.drawable.hand_08,R.drawable.hand_15,
            R.drawable.hand_10,R.drawable.hand_11,R.drawable.hand_16,
            R.drawable.hand_17,R.drawable.hand_18,R.drawable.hand_06,
            R.drawable.hand_19,R.drawable.hand_20,R.drawable.hand_12
    };

    private static String TAG = "PhoneActivity";

    GoogleApiClient mGoogleApiClient;

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    public void findNodes() {
        Log.i(TAG, "calling findNodes");
        // Send the RPC
        PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);

        nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult result) {
                for (int i = 0; i < result.getNodes().size(); i++) {
                    Node node = result.getNodes().get(i);
                    String nName = node.getDisplayName();
                    String nId = node.getId();
                    Log.d(TAG, "Node name and ID: " + nName + " | " + nId);

                    isConnected = true;
                    Button b = (Button) findViewById(R.id.previewButton);
                    b.setEnabled(true);


                    Wearable.MessageApi.addListener(mGoogleApiClient, new MessageApi.MessageListener() {
                        @Override
                        public void onMessageReceived(MessageEvent messageEvent) {
                            Log.d(TAG, "Message received: " + messageEvent);
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        mActivity = this;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_phone);

        // init assets

        bgImages = new int[1000];
        dialImages = new int[1000];
        bgNames = new ArrayList<String>();

        final R.drawable drawableResources = new R.drawable();
        final Class<R.drawable> c = R.drawable.class;
        final Field[] fields = c.getDeclaredFields();

        handSetCount = handSets.length / 3;

        // all bgs
        int j = 0;
        for (int i = 0, max = fields.length; i < max; i++) {
            final int resourceId;
            try {
                String name = fields[i].getName();
                resourceId = fields[i].getInt(drawableResources);
                if (name.contains("bg_")) {
                    bgImages[j] = resourceId;
                    bgNames.add(j,name);
                    j++;
                }
            } catch (Exception e) {
                continue;
            }
        }
        bgCount = j;
        resLimit = j;
        Log.i(TAG, "Loaded bg:" + bgCount);

        // all downloaded bgs

        j = 0;

        int dnCount = 0;

        File bgFiles = mContext.getFilesDir();
        for (String strFile : bgFiles.list())
        {
            if (strFile.contains("bg_")) {
                dnCount++;
            }
        }

        bgBitmaps = new Bitmap[dnCount];

        for (String strFile : bgFiles.list())
        {
            if (strFile.contains("bg_")) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                String fname=new File(getFilesDir(), strFile).getAbsolutePath();
                bgBitmaps[j] = BitmapFactory.decodeFile(fname, options);
                bgNames.add(bgCount+j,fname);
                j++;
            }
        }

        bgCount+=j;

        // all dials
        j = 0;
        for (int i = 0, max = fields.length; i < max; i++) {
            final int resourceId;
            try {
                String name = fields[i].getName();
                resourceId = fields[i].getInt(drawableResources);
                String searchString = "dial_";
                if (name.contains(searchString)) dialImages[j++] = resourceId;
            } catch (Exception e) {
                continue;
            }
        }
        dialCount = j;
        Log.i(TAG, "Loaded dials:" + dialCount);

        // init UI

        picker = (ColorPicker) findViewById(R.id.colorPicker);
        svBar = (SVBar) findViewById(R.id.svbar);
        opacityBar = (OpacityBar) findViewById(R.id.opacitybar);

        picker.setShowOldCenterColor(false);
        picker.addSVBar(svBar);
        picker.addOpacityBar(opacityBar);
        picker.setOnColorChangedListener(this);

        FancyCoverFlow fcf = (FancyCoverFlow) findViewById(R.id.coverFlow);

        CoverFlowAdapter cfa = new CoverFlowAdapter();
        cfa.setImages(bgImages);
        cfa.setCount(bgCount);
        cfa.setResLimit(resLimit);
        cfa.setBitmaps(bgBitmaps);
        fcf.setAdapter(cfa);

        picker.setColor(0x222222);

        fcf.setBackgroundColor(0x222222);
        fcf.setUnselectedAlpha(1.0f);
        fcf.setUnselectedSaturation(0.0f);
        fcf.setUnselectedScale(0.5f);
        fcf.setSpacing(50);
        fcf.setMaxRotation(0);
        fcf.setScaleDownGravity(0.2f);
        fcf.setActionDistance(FancyCoverFlow.ACTION_DISTANCE_AUTO);

        if (dnCount > 0) {
            fcf.setSelection(bgCount-1);
        }

        FancyCoverFlow fcf2 = (FancyCoverFlow) findViewById(R.id.coverFlow2);

        CoverFlowAdapter cfa2 = new CoverFlowAdapter();
        cfa2.setImages(dialImages);
        cfa2.setCount(dialImages.length);
        cfa2.setResLimit(dialImages.length);

        fcf2.setAdapter(cfa2);

        fcf2.setBackgroundColor(0x222222);

        fcf2.setUnselectedAlpha(1.0f);
        fcf2.setUnselectedSaturation(0.0f);
        fcf2.setUnselectedScale(0.5f);
        fcf2.setSpacing(50);
        fcf2.setMaxRotation(0);
        fcf2.setScaleDownGravity(0.2f);
        fcf2.setActionDistance(FancyCoverFlow.ACTION_DISTANCE_AUTO);


        Spinner spinner = (Spinner) findViewById(R.id.handSelectionSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.hands_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        Button b2 = (Button) findViewById(R.id.findButton);

        b2.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isConnected) {
                    findNodes();
                }
                return false;
            }
        });

        Button b = (Button) findViewById(R.id.previewButton);
        b.setEnabled(false);

        b.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isConnected) {
                    Button b = (Button) findViewById(R.id.previewButton);
                    b.setEnabled(false);

                    Runnable mRunnable;
                    Handler mHandler=new Handler();

                    mRunnable=new Runnable() {
                        @Override
                        public void run() {
                            Button b = (Button) findViewById(R.id.previewButton);
                            b.setEnabled(true);
                        }
                    };

                    mHandler.postDelayed(mRunnable, 3000);

                    syncWithWear();
                }
                return false;
            }
        });

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
                        Button b = (Button) findViewById(R.id.previewButton);
                        b.setEnabled(false);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(TAG, "onConnectionFailed: " + result);
                        isConnected = false;
                        Button b = (Button) findViewById(R.id.previewButton);
                        b.setEnabled(false);
                    }
                })
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();

        // sync with update server
        updateSync();
    }

    private void updateSync() {
        downloadedFileCount = 0;
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://laserti.me/prettywear/update.php", new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"

                String str = "";

                try {
                    str = new String(response, "UTF-8");
                } catch (Exception e) {}

                if (str.length() > 0) {
                    bgNamesToDownload = new ArrayList<String>();
                    String[] files = str.split(",");
                    List<String> newList = Arrays.asList(files);

                    ArrayList<String> downloadedList = new ArrayList<String>();

                    File bgFiles = mContext.getFilesDir();
                    for (String strFile : bgFiles.list()) {
                        downloadedList.add(strFile);
                    }

                    if (newList.size() > 0) {
                        for (int i = 0; i < newList.size(); i++) {
                            String newName = newList.get(i);
                            String[] noSuffixParts = newName.split("\\.");
                            if (noSuffixParts.length > 0) {

                                String noSuffix = noSuffixParts[0];
                                if (!bgNames.contains(noSuffix) && !downloadedList.contains(newName))
                                    bgNamesToDownload.add(newName);
                            }
                        }

                        if (bgNamesToDownload.size() > 0) {
                            Toast.makeText(PhoneActivity.mContext, "Downloading " + bgNamesToDownload.size() + " update(s)!", Toast.LENGTH_LONG).show();
                            downloadNewFiles();
                        } else {
                        //    Toast.makeText(PhoneActivity.mContext, "No updates!", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                bgNamesToDownload = null;
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });
    }

    public abstract class MyAsyncHttpResponseHandler extends AsyncHttpResponseHandler {
        public String myUrl = "";

        MyAsyncHttpResponseHandler(String url) {
            myUrl = url;

        }
    }

    public void downloadNewFiles() {
        for (int i = 0; i < bgNamesToDownload.size(); i++) {
            String fn = bgNamesToDownload.get(i);

            AsyncHttpClient client = new AsyncHttpClient();
            client.get("http://laserti.me/prettywear/" + fn, new MyAsyncHttpResponseHandler(fn) {

                @Override
                public void onStart() {
                    // called before request is started
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                    // called when response HTTP status is "200 OK"

                    FileOutputStream outputStream;
                    try {
                        outputStream = openFileOutput(myUrl, Context.MODE_PRIVATE);
                        outputStream.write(response);
                        outputStream.close();
                    } catch (Exception e) {
                        Toast.makeText(PhoneActivity.mContext, "Failed updating...", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    } finally {
                        downloadedFileCount++;
                    }

                    if (downloadedFileCount >= bgNamesToDownload.size()) {
                        Toast.makeText(PhoneActivity.mContext, "Update complete!", Toast.LENGTH_LONG).show();

                        updateComplete();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                }

                @Override
                public void onRetry(int retryNo) {
                    // called when request is retried
                }
            });
        }
    }

    public void updateComplete() {
        downloadedFileCount = 0;
        bgNamesToDownload = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.phone, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void syncWithWear() {
        FancyCoverFlow fcf = (FancyCoverFlow) findViewById(R.id.coverFlow);
        FancyCoverFlow fcf2 = (FancyCoverFlow) findViewById(R.id.coverFlow2);
        Bitmap bitmap;
        Asset asset;

        if (fcf.getSelectedItemPosition() < resLimit) {
            bitmap = BitmapFactory.decodeResource(getResources(), bgImages[fcf.getSelectedItemPosition()]);
            asset = createAssetFromBitmap(bitmap);
        } else {
            bitmap = bgBitmaps[(fcf.getSelectedItemPosition()-resLimit)];
            asset = createAssetFromBitmap(bitmap);
        }

        Spinner spinner = (Spinner) findViewById(R.id.handSelectionSpinner);

        int handIndex = spinner.getSelectedItemPosition();

        bitmap = BitmapFactory.decodeResource(getResources(), handSets[(handIndex*3)]);
        Asset asset2 = createAssetFromBitmap(bitmap);
        bitmap = BitmapFactory.decodeResource(getResources(), handSets[(handIndex*3)+1]);
        Asset asset3 = createAssetFromBitmap(bitmap);
        bitmap = BitmapFactory.decodeResource(getResources(), handSets[(handIndex*3)+2]);
        Asset asset4 = createAssetFromBitmap(bitmap);
        bitmap = BitmapFactory.decodeResource(getResources(), dialImages[fcf2.getSelectedItemPosition()]);
        Asset asset5 = createAssetFromBitmap(bitmap);

        new AsyncTask<Asset, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Asset... assets) {
                ConnectionResult result =
                        mGoogleApiClient.blockingConnect(
                                1000, TimeUnit.MILLISECONDS);
                if (!result.isSuccess()) {
                    return null;
                }
                PutDataMapRequest request = PutDataMapRequest.create("/bg");

                DataMap map = request.getDataMap();
                map.putLong("time", new Date().getTime()); // MOST IMPORTANT LINE FOR TIMESTAMP
                map.putAsset("bg", assets[0]);
                map.putInt("bgcolor", picker.getColor());

                Log.d(TAG, "Calling putDataItem");
                Wearable.DataApi.putDataItem(mGoogleApiClient, request.asPutDataRequest());
                Log.d(TAG, "Calling putDataItem done.");

                return null;
            }
        }.execute(asset);


        new AsyncTask<Asset, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Asset... assets) {
                ConnectionResult result =
                        mGoogleApiClient.blockingConnect(
                                1000, TimeUnit.MILLISECONDS);
                if (!result.isSuccess()) {
                    return null;
                }
                PutDataMapRequest request = PutDataMapRequest.create("/hourhand");

                DataMap map = request.getDataMap();
                map.putLong("time", new Date().getTime()); // MOST IMPORTANT LINE FOR TIMESTAMP
                map.putAsset("hourhand", assets[0]);

                Log.d(TAG, "Calling putDataItem");
                Wearable.DataApi.putDataItem(mGoogleApiClient, request.asPutDataRequest());
                Log.d(TAG, "Calling putDataItem done.");

                return null;
            }
        }.execute(asset2);

        new AsyncTask<Asset, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Asset... assets) {
                ConnectionResult result =
                        mGoogleApiClient.blockingConnect(
                                1000, TimeUnit.MILLISECONDS);
                if (!result.isSuccess()) {
                    return null;
                }
                PutDataMapRequest request = PutDataMapRequest.create("/minutehand");

                DataMap map = request.getDataMap();
                map.putLong("time", new Date().getTime()); // MOST IMPORTANT LINE FOR TIMESTAMP
                map.putAsset("minutehand", assets[0]);

                Log.d(TAG, "Calling putDataItem");
                Wearable.DataApi.putDataItem(mGoogleApiClient, request.asPutDataRequest());
                Log.d(TAG, "Calling putDataItem done.");

                return null;
            }
        }.execute(asset3);

        new AsyncTask<Asset, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Asset... assets) {
                ConnectionResult result =
                        mGoogleApiClient.blockingConnect(
                                1000, TimeUnit.MILLISECONDS);
                if (!result.isSuccess()) {
                    return null;
                }
                PutDataMapRequest request = PutDataMapRequest.create("/secondhand");

                DataMap map = request.getDataMap();
                map.putLong("time", new Date().getTime()); // MOST IMPORTANT LINE FOR TIMESTAMP
                map.putAsset("secondhand", assets[0]);

                Log.d(TAG, "Calling putDataItem");
                Wearable.DataApi.putDataItem(mGoogleApiClient, request.asPutDataRequest());
                Log.d(TAG, "Calling putDataItem done.");

                return null;
            }
        }.execute(asset4);

        new AsyncTask<Asset, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Asset... assets) {
                ConnectionResult result =
                        mGoogleApiClient.blockingConnect(
                                1000, TimeUnit.MILLISECONDS);
                if (!result.isSuccess()) {
                    return null;
                }
                PutDataMapRequest request = PutDataMapRequest.create("/dial");

                DataMap map = request.getDataMap();
                map.putLong("time", new Date().getTime()); // MOST IMPORTANT LINE FOR TIMESTAMP
                map.putAsset("dial", assets[0]);

                Log.d(TAG, "Calling putDataItem");
                Wearable.DataApi.putDataItem(mGoogleApiClient, request.asPutDataRequest());
                Log.d(TAG, "Calling putDataItem done.");

                return null;
            }
        }.execute(asset5);
    }
}
