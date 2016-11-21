package cs117.musync;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * adapter class for ListView to display the list of songs
 */
public class SongAdapter extends CursorAdapter{

    public SongAdapter (Context context, Cursor c) {
        super(context, c, 0);

    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView (Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.song, viewGroup, false);
    }

    @Override
    public void bindView (View view, Context context, Cursor cursor) {
        TextView songTitleField = (TextView) view.findViewById(R.id.song_title);
        TextView artistNameField = (TextView) view.findViewById(R.id.song_artist);
        int titleColumn = cursor.getColumnIndex
                (android.provider.MediaStore.Audio.Media.TITLE);
        int artistColumn = cursor.getColumnIndex
                (android.provider.MediaStore.Audio.Media.ARTIST);
        int songFile = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DATA);
        int fileID = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
        String songTitle = cursor.getString(titleColumn);
        String songArtist = cursor.getString(artistColumn);
        long id = cursor.getLong(fileID);
        Uri contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        String path = cursor.getString(songFile);
        view.setTag(contentUri);
        songTitleField.setText(songTitle);
        artistNameField.setText(songArtist);
    }


}
