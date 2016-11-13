package cs117.musync;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.UUID;

// Bluetooth Library Link
// https://android-arsenal.com/details/1/3071#!description
import co.lujun.lmbluetoothsdk.BluetoothController;
import co.lujun.lmbluetoothsdk.base.BluetoothListener;


public class BTConnect extends AppCompatActivity {

    // For classic bluetooth
    BluetoothController mBTController = BluetoothController.getInstance().build(this);

    private boolean server;
    private boolean client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.activity_btconnect);

        super.onCreate(savedInstanceState);
        UUID randomId = UUID.randomUUID();
        mBTController.setAppUuid(randomId);

        mBTController.setBluetoothListener(new BluetoothListener() {

            @Override
            public void onActionStateChanged(int preState, int state) {
                // Callback when bluetooth power state changed.
            }

            @Override
            public void onActionDiscoveryStateChanged(String discoveryState) {
                // Callback when local Bluetooth adapter discovery process state changed.
            }

            @Override
            public void onActionScanModeChanged(int preScanMode, int scanMode) {
                // Callback when the current scan mode changed.
            }

            @Override
            public void onBluetoothServiceStateChanged(int state) {
                // Callback when the connection state changed.
            }

            @Override
            public void onActionDeviceFound(BluetoothDevice device) {
                // Callback when found device.
            }

            @Override
            public void onReadData(final BluetoothDevice device, final byte[] data) {
                // Callback when remote device send data to current device.
            }
        });

        server = false;
        client = false;

        if (!initializeBluetooth()) {
            TextView statusTextView = (TextView) findViewById(R.id.statusTextView);
            statusTextView.setText("Unable to initialize Bluetooth! :(");
        }
    }


    public boolean initializeBluetooth() {

        if (!mBTController.isAvailable()) {
            System.out.println("Bluetooth is not available! :(");
            return false;
        }

        if (!mBTController.isEnabled()) {
            System.out.println("Bluetooth is not enabled! :(");
            return false;
        }

        if (!mBTController.openBluetooth()) {
            
        }


        return true;
    }


    public void startAsServer() {
        if (client == false) {
            server = true;
            client = false;
            mBTController.startAsServer();
            System.out.println("Starting as server...");
        }
    }


    public void setAsDiscoverable() {
        if (server == false) {
            server = false;
            client = true;
            if (mBTController.setDiscoverable(60)) {
                System.out.println("Setting as discoverable for 60 seconds...");
            }
            else {
                client = false;
            }
        }
    }

}
