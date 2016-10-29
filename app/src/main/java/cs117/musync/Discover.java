package cs117.musync;

import android.bluetooth.BluetoothAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class Discover extends AppCompatActivity {

    // Bluetooth adapter used to discover devices
    private BluetoothAdapter mBluetoothAdapter;

    // An array to display the discovered devices
    private ArrayAdapter<String> mDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);
    }
}
