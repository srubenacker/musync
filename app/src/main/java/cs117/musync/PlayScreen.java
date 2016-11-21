package cs117.musync;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class PlayScreen extends AppCompatActivity {
    // music playing
    int playButton_state = 0;   //0 is end state/not initialized, 1 is playing, 2 is paused
    ImageButton playButton;
    MediaPlayer mediaPlayer;
    ImageView   albumArt;
    static final int PICK_SONG_REQUEST = 5;
    TextView groupNameView;
    Button joinButton;
    TextView songDesp;

    private static final String TAG = "Musync";

    private Button libraryButton;
    private String mGroupName;
    private String mLastState;
    private String mLastTime;
    private int mSeekTime;

    private Handler pollHandler;
    private Handler stateHandler;

    private static final String serverAddress = "http://40.78.19.9/";

    // Called on initialization of activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_screen);

        playButton = (ImageButton) findViewById(R.id.playButton);
        libraryButton = (Button) findViewById(R.id.libraryButton);
        albumArt = (ImageView) findViewById(R.id.albumArtImage);
        groupNameView = (TextView) findViewById(R.id.groupNameView);
        joinButton = (Button) findViewById(R.id.joinButton);
        songDesp = (TextView) findViewById(R.id.album_string);
        mediaPlayer = MediaPlayer.create(PlayScreen.this, R.raw.song2);

        albumArt.setImageResource(R.drawable.art);

        //disable play button when not in a group
        playButton.setEnabled(false);

        //---------------get album art
       // TO DO

        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(playButton_state == 0 ) {        //haven't started
                    //mediaPlayer.start();
                    playButton.setImageResource(android.R.drawable.ic_media_pause);
                    playButton_state = 1;
                    updateGroup("PLAY", incrementTime(getCurrentTime(), 2), 0);
                }
                else if (playButton_state == 1){   //music is playing
                    //mediaPlayer.pause();
                    playButton.setImageResource(android.R.drawable.ic_media_play);
                    playButton_state = 2;
                    updateGroup("PAUSE", incrementTime(getCurrentTime(), 2), mediaPlayer.getCurrentPosition());
                }
                else{           //is paused
                    //mediaPlayer.start();
                    playButton.setImageResource(android.R.drawable.ic_media_pause);
                    playButton_state = 1;
                    updateGroup("PLAY", incrementTime(getCurrentTime(), 2), mediaPlayer.getCurrentPosition());
                }
            }
        });

        libraryButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(PlayScreen.this, MusicLibrary.class);
                startActivityForResult(i, PICK_SONG_REQUEST);   //request code is 5, defined above
            }
        });



        joinButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                joinGroup();
                playButton.setEnabled(true);
            }

        });

        mGroupName = "";
        mLastState = "PAUSE";
        mLastTime = "00:00:00";
        mSeekTime = 0;

        //new DownloadTask().execute("http://www.google.com/");
    }


    private class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String[] params) {
            //do your request in here so that you don't interrupt the UI thread

            try {
                return downloadContent(params[0]);
            } catch (IOException e) {
                System.out.println(e.toString());
                mediaPlayer.reset();
                return "Error reading music file";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            //Here you are done with the task
            String [] parts = result.split(" ");
            if (parts.length == 4) {
                System.out.println("Seek Time: " + parts[0]);
                System.out.println("State: " + parts[1]);
                System.out.println("Time: " + parts[2]);
                mSeekTime = Integer.parseInt(parts[0]);
                mLastState = parts[1];
                mLastTime = parts[2];
            }
            else {
                Toast.makeText(PlayScreen.this, result, Toast.LENGTH_LONG).show();
            }
        }
    }

    private String downloadContent(String myurl) throws IOException {
        InputStream is = null;
        int length = 500;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(TAG, "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = convertInputStreamToString(is, length);
            return contentAsString;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public String convertInputStreamToString(InputStream stream, int length) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[length];
        reader.read(buffer);
        return new String(buffer);
    }

    public void joinGroup() {
        mGroupName = groupNameView.getText().toString();
        // remove spaces so the url doesnt get messed up
        mGroupName = mGroupName.replaceAll(" ", "");

        joinButton.setEnabled(false);
        groupNameView.setEnabled(false);

        String newGroup = serverAddress + "newGroup.php?groupname=" + mGroupName;
        new DownloadTask().execute(newGroup);
        pollHandler = new Handler();
        pollHandler.postDelayed(pollState, 250);

        stateHandler = new Handler();
        stateHandler.postDelayed(pollPlay, 500);
    }

    public void getGroup() {
        String getGroup = serverAddress + "getGroup.php?groupname=" + mGroupName;
        new DownloadTask().execute(getGroup);
        System.out.println("State: " + mLastState + ", Time: " + mLastTime + ", Seek Time: " + mSeekTime);
    }

    public void updateGroup(String state, String time, int seekTime) {
        System.out.println("Updating to State " + state + " at time " + time + " with seek " + seekTime);
        String updateGroup = serverAddress + "updateGroup.php?groupname=" + mGroupName + "&state="
                + state + "&time=" + time + "&seekTime=" + seekTime;
        new DownloadTask().execute(updateGroup);
    }

    private Runnable pollState = new Runnable() {
        @Override
        public void run() {
            getGroup();
            pollHandler.postDelayed(this, 1000);
        }
    };

    private Runnable pollPlay = new Runnable() {
        @Override
        public void run() {
            togglePlay();
            stateHandler.postDelayed(this, 25);
        }
    };

    private String getCurrentTime() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    private boolean compareCurrentTime() {
        String currentTime = getCurrentTime();
        int currentHour = Integer.parseInt((currentTime.split(":")[0]).replaceAll("[\\D]", ""));
        int currentMin = Integer.parseInt((currentTime.split(":")[1]).replaceAll("[\\D]", ""));
        int currentSec = Integer.parseInt((currentTime.split(":")[2]).replaceAll("[\\D]", ""));

        int stateHour = Integer.parseInt((mLastTime.split(":")[0]).replaceAll("[\\D]", ""));
        int stateMin = Integer.parseInt((mLastTime.split(":")[1]).replaceAll("[\\D]", ""));
        int stateSec = Integer.parseInt((mLastTime.split(":")[2]).replaceAll("[\\D]", ""));

        if (currentHour <= stateHour) {
            if (currentMin <= stateMin) {
                if (currentSec <= stateSec) {
                    return false;
                }
            }
        }

        return true;
    }

    private String incrementTime(String currentTime, int incrementAmount) {
        int currentHour = Integer.parseInt(currentTime.split(":")[0]);
        int currentMin = Integer.parseInt(currentTime.split(":")[1]);
        int currentSec = Integer.parseInt(currentTime.split(":")[2]);

        currentSec += incrementAmount;
        if (currentSec >= 60) {
            currentSec -= 60;
            currentMin += 1;
            if (currentMin >= 60) {
                currentMin -= 60;
                currentHour += 1;
                if (currentHour >= 24) {
                    currentHour -= 24;
                }
            }
        }

        return "" + currentHour + ":" + currentMin + ":" + currentSec;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_SONG_REQUEST) {
            if(resultCode == Activity.RESULT_OK){

                Uri songPath = (Uri) data.getExtras().get("result");
                String desp = (String) data.getExtras().get("desp");
                String artPath = (String) data.getExtras().get("art");
                mediaPlayer.reset();

                try {
                    mediaPlayer.setDataSource(PlayScreen.this, songPath);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    Log.e("error loading song", "error");
                }

                Toast.makeText(PlayScreen.this, "Playing " + desp, Toast.LENGTH_LONG).show();
                //Toast.makeText(PlayScreen.this, "Playing " + artPath, Toast.LENGTH_LONG).show();


                if(artPath != null){

                    Bitmap bm= BitmapFactory.decodeFile(artPath);
                    albumArt.setImageBitmap(bm);
                    songDesp.setText(null);
                }
                else {
                    albumArt.setImageDrawable(null);
                    songDesp.setText(desp);
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
                return;
            }
        }
    }//onActivityResult
    private void togglePlay() {
        if (compareCurrentTime()) {
            if (mLastState.equalsIgnoreCase("PLAY") && !mediaPlayer.isPlaying()) {
                mediaPlayer.seekTo(mSeekTime);
                mediaPlayer.start();
                playButton.setImageResource(android.R.drawable.ic_media_pause);
                playButton_state = 1;
            }
            else if (mLastState.equalsIgnoreCase("PAUSE") && mediaPlayer.isPlaying()) {
                mediaPlayer.seekTo(mSeekTime);
                mediaPlayer.pause();
                playButton.setImageResource(android.R.drawable.ic_media_play);
                playButton_state = 2;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String deleteGroup = serverAddress + "deleteGroup.php?groupname=" + mGroupName;
        new DownloadTask().execute(deleteGroup);
    }
}
