package cs117.musync;


import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import co.lujun.lmbluetoothsdk.BluetoothController;
import co.lujun.lmbluetoothsdk.base.BluetoothListener;
import co.lujun.lmbluetoothsdk.base.State;

public class PlayScreen extends AppCompatActivity {
    int playButton_state = 0;   //0 is end state/not initialized, 1 is playing, 2 is pause
    ImageButton playButton;
    MediaPlayer mediaPlayer;
    ImageView   albumArt;
    static final int PICK_SONG_REQUEST = 5;

    private BluetoothController mBTController;

    private Button sendButton, disconnectButton, libraryButton;
    private EditText sendContent;
    private TextView stateView, contentView, deviceView, macView;

    private int mConnectState;
    private String mMACAddress = "", mDeviceName = "";

    private static final String TAG = "Musync";


    // Called on initialization of activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_screen);

        playButton = (ImageButton) findViewById(R.id.playButton);
        libraryButton = (Button) findViewById(R.id.libraryButton);
        albumArt = (ImageView) findViewById(R.id.albumArtImage);

        //---------------get album art
       // TO DO

        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(playButton_state == 0 ) {        //haven't started
                    mediaPlayer = MediaPlayer.create(PlayScreen.this, R.raw.song2);
                    mediaPlayer.start();
                    playButton.setImageResource(android.R.drawable.ic_media_pause);
                    playButton_state = 1;
                }
                else if (playButton_state == 1){   //music is playing
                    mediaPlayer.pause();
                    playButton.setImageResource(android.R.drawable.ic_media_play);
                    playButton_state = 2;
                }
                else{           //is paused
                    mediaPlayer.start();
                    playButton.setImageResource(android.R.drawable.ic_media_pause);
                    playButton_state = 1;
                }
            }
        });

        libraryButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(PlayScreen.this, MusicLibrary.class);
                startActivityForResult(i, PICK_SONG_REQUEST);   //request code is 5, defined above
            }
        });

        initialize();
        
    }


    private void initialize() {
        mMACAddress = getIntent().getStringExtra("mac");
        mDeviceName = getIntent().getStringExtra("name");

        mBTController = BluetoothController.getInstance();
        mBTController.setBluetoothListener(new BluetoothListener() {

            @Override
            public void onReadData(final BluetoothDevice device, final byte[] data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String deviceName = device == null ? "" : device.getName();
                        contentView.append(deviceName + ": " + new String(data) + "\n");
                    }
                });
            }

            @Override
            public void onActionStateChanged(int preState, int state) {
                Toast.makeText(PlayScreen.this, "Bluetooth State: " + state, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onActionDiscoveryStateChanged(String discoveryState) {
                // do nothing
            }

            @Override
            public void onActionScanModeChanged(int preScanMode, int scanMode) {
                // do nothing
            }

            @Override
            public void onBluetoothServiceStateChanged(final int state) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mConnectState = state;
                        stateView.setText("Connection state: " + transConnStateAsString(state));
                    }
                });
            }

            @Override
            public void onActionDeviceFound(BluetoothDevice device) {
                // do nothing
            }
        });

        sendButton = (Button) findViewById(R.id.sendButton);
        disconnectButton = (Button) findViewById(R.id.disconnectButton);

        stateView = (TextView) findViewById(R.id.stateView);
        sendContent = (EditText) findViewById(R.id.sendContent);
        contentView = (TextView) findViewById(R.id.contentView);
        deviceView = (TextView) findViewById(R.id.deviceView);
        macView = (TextView) findViewById(R.id.macView);

        deviceView.setText("Device: " + mDeviceName);
        macView.setText("MAC Address: " + mMACAddress);
        stateView.setText("Connection State: " + transConnStateAsString(mBTController.getConnectionState()));

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = sendContent.getText().toString();

                if (TextUtils.isEmpty(message)) {
                    return;
                }
                mBTController.write(message.getBytes());
                contentView.append("This device: " + message + "/n");
                sendContent.setText("");
            }
        });

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConnectState == State.STATE_DISCONNECTED) {
                    mBTController.disconnect();
                }
                finish();
            }
        });

        if (!TextUtils.isEmpty(mMACAddress)) {
            mBTController.connect(mMACAddress);
        }
        else {
            if (mBTController.getConnectedDevice() == null) {
                return;
            }

            mDeviceName = mBTController.getConnectedDevice().getName();
            mMACAddress = mBTController.getConnectedDevice().getAddress();
            deviceView.setText("Device: " + mDeviceName);
            macView.setText("MAC Address: " + mMACAddress);
        }

    }

    public void switchToDiscoverView(View view) {
        Intent intent = new Intent(PlayScreen.this, BTConnect.class);
        startActivity(intent);
    }

    public static String transConnStateAsString(int state){
        String result;
        if (state == State.STATE_NONE) {
            result = "NONE";
        } else if (state == State.STATE_LISTEN) {
            result = "LISTEN";
        } else if (state == State.STATE_CONNECTING) {
            result = "CONNECTING";
        } else if (state == State.STATE_CONNECTED) {
            result = "CONNECTED";
        } else if (state == State.STATE_DISCONNECTED){
            result = "DISCONNECTED";
        } else{
            result = "UNKNOWN";
        }
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_SONG_REQUEST) {
            if(resultCode == Activity.RESULT_OK){
                String songName = data.getStringExtra("result");
                Toast.makeText(PlayScreen.this, "Bluetooth State: " + songName, Toast.LENGTH_SHORT).show();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
                return;
            }
        }
    }//onActivityResult
}
