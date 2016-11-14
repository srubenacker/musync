package cs117.musync;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Bluetooth Library Link
// https://android-arsenal.com/details/1/3071#!description
import co.lujun.lmbluetoothsdk.BluetoothController;
import co.lujun.lmbluetoothsdk.base.BluetoothListener;
import co.lujun.lmbluetoothsdk.base.State;


public class BTConnect extends AppCompatActivity {

    // For classic bluetooth
    private BluetoothController mBTController;// = BluetoothController.getInstance().build(this);

    private Button setDiscoverableButton, scanButton, startServerButton, openButton;
    private TextView stateTextView;
    private ListView devicesView;

    private List<String> mList;
    private BaseAdapter mFoundAdapter;
    private String mMACAddress;

    private static final String TAG = "MUSYNC";
    private static final int DISCOVERY_TIME = 60;

    private BluetoothListener mListener = new BluetoothListener() {
        @Override
        public void onActionStateChanged(int preState, int state) {
            // Callback when bluetooth power state changed.
            stateTextView.setText("State: " + translateState(state));
        }

        @Override
        public void onActionDiscoveryStateChanged(String discoveryState) {
            // Callback when local Bluetooth adapter discovery process state changed.
            if (discoveryState.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                Toast.makeText(BTConnect.this, "Discovery scan starting...", Toast.LENGTH_SHORT).show();
            }
            else if (discoveryState.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                Toast.makeText(BTConnect.this, "Discovery scan finsished!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onActionScanModeChanged(int preScanMode, int scanMode) {
            // Callback when the current scan mode changed.
            System.out.println(TAG + " | preScanMode: " + preScanMode + " | scanMode: " + scanMode);
        }

        @Override
        public void onBluetoothServiceStateChanged(int state) {
            // Callback when the connection state changed.
            System.out.println(TAG + " | State: " + translateState(state));

            if (state == State.STATE_CONNECTED) {
                // switch to other activity
                Intent intent = new Intent(BTConnect.this, PlayScreen.class);
                startActivityForResult(intent, 4);
            }
        }

        @Override
        public void onActionDeviceFound(BluetoothDevice device) {
            // Callback when found device.
            mList.add(device.getName() + "@" + device.getAddress());
            mFoundAdapter.notifyDataSetChanged();
        }

        @Override
        public void onReadData(final BluetoothDevice device, final byte[] data) {
            // Callback when remote device send data to current device.
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btconnect);

        //UUID randomId = UUID.randomUUID();
        //mBTController.setAppUuid(randomId);



        initialize();
    }


    private void initialize() {

        mList = new ArrayList<String>();
        mFoundAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mList);

        setDiscoverableButton = (Button) findViewById(R.id.startDiscoverableButton);
        scanButton = (Button) findViewById(R.id.scanButton);
        openButton = (Button) findViewById(R.id.openButton);
        startServerButton = (Button) findViewById(R.id.startServerButton);

        stateTextView = (TextView) findViewById(R.id.statusTextView);
        devicesView = (ListView) findViewById(R.id.devicesList);

        devicesView.setAdapter(mFoundAdapter);

        initializeBluetooth();


        setDiscoverableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mBTController.setDiscoverable(DISCOVERY_TIME);
                }
                catch (Exception e) {
                    Toast.makeText(BTConnect.this, e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    int permission = ActivityCompat.checkSelfPermission(BTConnect.this, Manifest.permission.ACCESS_COARSE_LOCATION);
                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(BTConnect.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                    }
                }

                mList.clear();
                mFoundAdapter.notifyDataSetChanged();

                if (!mBTController.startScan()) {
                    Toast.makeText(BTConnect.this, "Failed to Start a Scan! :(", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(BTConnect.this, "Starting a scan...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBTController.isEnabled()) {
                    mBTController.openBluetooth();
                }
                else {
                    Toast.makeText(BTConnect.this, "Bluetooth opened!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        startServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mBTController.startAsServer();
                    Toast.makeText(BTConnect.this, "Started as a server!", Toast.LENGTH_SHORT).show();
                }
                catch (Exception e) {
                    Toast.makeText(BTConnect.this, e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });

        devicesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemStr = mList.get(position);
                mMACAddress = itemStr.substring(itemStr.length() - 17);
                stateTextView.setText("MAC Address: " + mMACAddress);

                // transition to music playing page
                Intent intent = new Intent(BTConnect.this, PlayScreen.class);
                intent.putExtra("name", itemStr.substring(0, itemStr.length() - 18));
                intent.putExtra("mac", mMACAddress);
                startActivityForResult(intent, 4);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 4) {
            if (mBTController != null) {
                mBTController.release();
            }
            mBTController.build(this);
            mBTController.setBluetoothListener(mListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBTController.release();
    }


    public void initializeBluetooth() {

        mBTController = BluetoothController.getInstance().build(this);

        // change this if it doesn't work
        mBTController.setAppUuid(UUID.randomUUID());

        mBTController.setBluetoothListener(mListener);

        // set state text (optional)
    }

    public static String translateState(int state){
        String result = "UNKNOWN";
        if (state == BluetoothAdapter.STATE_TURNING_ON) {
            result = "TURNING_ON";
        } else if (state == BluetoothAdapter.STATE_ON) {
            result = "ON";
        } else if (state == BluetoothAdapter.STATE_TURNING_OFF) {
            result = "TURNING_OFF";
        }else if (state == BluetoothAdapter.STATE_OFF) {
            result = "OFF";
        }
        return result;
    }

}
