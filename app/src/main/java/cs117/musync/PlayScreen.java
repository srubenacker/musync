package cs117.musync;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.media.MediaPlayer;
import android.widget.ImageButton;
import android.widget.ImageView;

public class PlayScreen extends AppCompatActivity {
    int playButton_state = 0;   //0 is end state/not initialized, 1 is playing, 2 is paused
    ImageButton playButton;
    MediaPlayer mediaPlayer;
    ImageView   albumArt;

    // Called on initialization of activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_screen);

        playButton = (ImageButton) findViewById(R.id.playButton);
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
        
    }

    public void switchToDiscoverView(View view) {
        Intent intent = new Intent(PlayScreen.this, BTConnect.class);
        startActivity(intent);
    }
}
