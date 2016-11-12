package cs117.musync;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.UUID;

import co.lujun.lmbluetoothsdk.BluetoothController;
import co.lujun.lmbluetoothsdk.base.BluetoothListener;

public class BTConnect extends AppCompatActivity {

    // For classic bluetooth
    BluetoothController mBTController = BluetoothController.getInstance().build(this);

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
    }
}
