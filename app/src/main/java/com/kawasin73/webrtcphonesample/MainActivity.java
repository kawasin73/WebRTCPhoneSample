package com.kawasin73.webrtcphonesample;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import io.skyway.Peer.Browser.Navigator;
import io.skyway.Peer.OnCallback;
import io.skyway.Peer.Peer;
import io.skyway.Peer.PeerOption;

public class MainActivity extends AppCompatActivity {
    private static final int RECORD_AUDIO_REQUEST_ID = 1;
    private String TAG = getClass().getSimpleName();
    private Peer peer;
    private String currentId;
    private TextView idTextView;
    private ListView listView;
    private MyAdapter adapter;
    private List<String> idList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkAudioPermission();

        PeerOption options = new PeerOption();
        options.key = BuildConfig.SKYWAY_API_KEY;
        options.domain = BuildConfig.SKYWAY_HOST;
        peer = new Peer(this, options);
        Navigator.initialize(peer);

        idTextView = (TextView) findViewById(R.id.id_textview);
        listView = (ListView) findViewById(R.id.listview);

        adapter = new MyAdapter(this, 0, idList);
        listView.setAdapter(adapter);

        Button refreshBtn = (Button) findViewById(R.id.refresh_btn);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshPeerList();
            }
        });

        showCurrentPeerId();

        refreshPeerList();
    }

    private void checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Manifest.permission.RECORD_AUDIO is not GRANTED");
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                Log.d(TAG, "shouldShowRequestPermissionRationale = false");
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                Log.d(TAG, "request Permission RECORD_AUDIO");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        RECORD_AUDIO_REQUEST_ID);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RECORD_AUDIO_REQUEST_ID: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "request Permission RECORD_AUDIO GRANTED!");
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    Log.d(TAG, "request Permission RECORD_AUDIO DENIED!");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void showCurrentPeerId() {
        peer.on(Peer.PeerEventEnum.OPEN, new OnCallback() {
            @Override
            public void onCallback(Object o) {
                if (o instanceof String) {
                    currentId = (String) o;
                    Log.d(TAG, "currentId: " + currentId);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            idTextView.setText("ID: " + currentId);
                        }
                    });
                }
            }
        });
    }

    private void refreshPeerList() {
        Log.d(TAG, "Refreshing");
        peer.listAllPeers(new OnCallback() {
            @Override
            public void onCallback(Object o) {
                if (o instanceof JSONArray) {
                    JSONArray array = (JSONArray) o;
                    idList.clear();
                    for (int i = 0; i < array.length(); i++) {
                        try {
                            String id = array.getString(i);
                            idList.add(id);
                            Log.d(TAG, "Fetched PeerId: " + id);
                        } catch (JSONException e) {
                            Log.e(TAG, "Parse ListAllPeer", e);
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    private class MyAdapter extends ArrayAdapter<String> {
        private LayoutInflater inflater;

        MyAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<String> objects) {
            super(context, resource, objects);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = inflater.inflate(R.layout.list_item, null, false);
            TextView textView = (TextView) view.findViewById(R.id.item_textview);
            String name = getItem(position);
            textView.setText(name);
            return view;
        }
    }
}
