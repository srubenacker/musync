package cs117.musync;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.Set;

public class Discover extends AppCompatActivity {

    // Bluetooth adapter used to discover devices
    private BluetoothAdapter mBluetoothAdapter;

    // An array to display the discovered devices
    private ArrayAdapter<String> mDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

        // Create the scan button so we can discover devices
        Button scanButton = (Button) findViewById(R.id.scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                discoverMe();
                v.setVisibility(View.GONE);
            }
        });

        // initialize array adapters for devices
        mDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.activity_discover);

        // set the list view to display the devices in the adapter
        ListView devicesListView = (ListView) findViewById(R.id.devices_view);
        devicesListView.setAdapter((mDevicesArrayAdapter));
        // set on click item listener

        // register for broadcasts when a new device has been discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // register for broadcasts when a discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // get the singleton bluetooth adapter for this android device
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // get a set containing paired devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        // add paired devices to the list adapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device: pairedDevices) {
                mDevicesArrayAdapter.add(device.getName() + ": " + device.getAddress());
            }
        }
        else {
            mDevicesArrayAdapter.add("No paired devices.");
        }
    }


    /*
        Behavior on destruction of this activity
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // ensure that we are not discovering before we destroy
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // unregister any of our broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    /*
        Use the Bluetooth Adapter to start device discovery
     */
    private void discoverMe() {
        System.out.println("Starting discovery...");

        // stop discovering if it is already in progress
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // start discovering
        mBluetoothAdapter.startDiscovery();
    }



    /*
        This BroadcastReceiver listens for discovered devices as well as
        when discovery has finished.
     */

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // check what action just completed, either a device was discovered
            // or discovery has completed

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // get the BluetoothDevice object from the Intent object
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // don't display this device on the list if it is already paired
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mDevicesArrayAdapter.add(device.getName() + ": " + device.getAddress());
                }
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                // stop the progress bar
                // if no devices were found, add this to the list
                if (mDevicesArrayAdapter.getCount() == 0) {
                    mDevicesArrayAdapter.add("No devices found.");
                }
            }

        }
    };


}
