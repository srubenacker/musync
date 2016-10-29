package cs117.musync;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

public class PlayScreen extends AppCompatActivity {

    // Called on initialization of activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_screen);
    }


    public void switchToDiscoverView(View view) {
        Intent intent = new Intent(PlayScreen.this, Discover.class);
        startActivity(intent);
    }

}
