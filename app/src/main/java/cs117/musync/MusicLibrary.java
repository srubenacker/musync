package cs117.musync;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MusicLibrary extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST = 6;
    private ListView songListView;
    //private ArrayList<Song> songList;
    // Identifies a particular Loader being used in this component
    private static final int MUSIC_LOADER = 0;
    private SongAdapter mySongAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_library);
        checkPermission();
        //songList = new ArrayList<Song>();
        songListView = (ListView) findViewById(R.id.song_list);

        mySongAdapter = new SongAdapter(MusicLibrary.this, null);
        getSupportLoaderManager().initLoader(MUSIC_LOADER, null, this);;
        songListView.setAdapter(mySongAdapter);

        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick (AdapterView<?> adapterView, View view, int i, long l) {
                Intent returnIntent = new Intent();

                Uri result = (Uri) view.getTag(R.string.song_tag1);
                String desp = (String) view.getTag(R.string.song_tag2);
                Long id = (Long) view.getTag(R.string.song_tag3);

                returnIntent.putExtra("result", result);
                returnIntent.putExtra("desp", desp);
                ContentResolver contentr = getContentResolver();
                Cursor tempCursor = contentr.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        new String[] {MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                        MediaStore.Audio.Albums._ID+ "=?",
                        new String[] {String.valueOf(id)},
                        null);


                if (tempCursor.moveToFirst()) {
                    String p = tempCursor.getString(tempCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                    returnIntent.putExtra("art", p);
                }

                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });


    }

    protected void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Toast.makeText(MusicLibrary.this, "Requesting permission to read external library", Toast.LENGTH_SHORT).show();
                }

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        READ_EXTERNAL_STORAGE_PERMISSION_REQUEST);
                return;
            }
        }
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * Callback that's invoked when the system has initialized the Loader and
     * is ready to start the query. This usually happens when initLoader() is
     * called. The loaderID argument contains the ID value passed to the
     * initLoader() call.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader (int id, Bundle args) {
        /*
     * Takes action based on the ID of the Loader that's being created
     */
        switch (id) {
            case MUSIC_LOADER:
                // Returns a new CursorLoader
                String[] mProjection =
                        {
                                MediaStore.Audio.AudioColumns.ARTIST,
                                MediaStore.Audio.AudioColumns.TITLE,
                                MediaStore.Audio.AudioColumns._ID,
                                MediaStore.Audio.Media.DATA,
                        };
                return new CursorLoader(
                        MusicLibrary.this,   // Parent activity context
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,        // Table to query
                        mProjection,     // Projection
                        null,            // No selection clause
                        null,            // No selection arguments
                        MediaStore.Audio.Media.TITLE + " ASC"             // sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    /**
     * Called when a previously created loader has finished its load.  Note
     * that normally an application is <em>not</em> allowed to commit fragment
     * transactions while in this call, since it can happen after an
     * activity's state is saved.  See {@linkFragmentManager#beginTransaction()
     * FragmentManager.openTransaction()} for further discussion on this.
     * <p/>
     * <p>This function is guaranteed to be called prior to the release of
     * the last data that was supplied for this Loader.  At this point
     * you should remove all use of the old data (since it will be released
     * soon), but should not do your own release of the data since its Loader
     * owns it and will take care of that.  The Loader will take care of
     * management of its data so you don't have to.  In particular:
     * <p/>
     * <ul>
     * <li> <p>The Loader will monitor for changes to the data, and report
     * them to you through new calls here.  You should not monitor the
     * data yourself.  For example, if the data is a {@link Cursor}
     * and you place it in a {@link CursorAdapter}, use
     * the {@link CursorAdapter#CursorAdapter(Context,
     * Cursor, int)} constructor <em>without</em> passing
     * in either {@link CursorAdapter#FLAG_AUTO_REQUERY}
     * or {@link CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER}
     * (that is, use 0 for the flags argument).  This prevents the CursorAdapter
     * from doing its own observing of the Cursor, which is not needed since
     * when a change happens you will get a new Cursor throw another call
     * here.
     * <li> The Loader will release the data once it knows the application
     * is no longer using it.  For example, if the data is
     * a {@link Cursor} from a {@link CursorLoader},
     * you should not call close() on it yourself.  If the Cursor is being placed in a
     * {@link CursorAdapter}, you should use the
     * {@link CursorAdapter#swapCursor(Cursor)}
     * method so that the old Cursor is not closed.
     * </ul>
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished (Loader<Cursor> loader, Cursor data) {
        mySongAdapter.changeCursor(data);

//        if(data!=null && data.moveToFirst()){
//            //get columns
//            int titleColumn = data.getColumnIndex
//                    (android.provider.MediaStore.Audio.Media.TITLE);
//            int idColumn = data.getColumnIndex
//                    (android.provider.MediaStore.Audio.Media._ID);
//            int artistColumn = data.getColumnIndex
//                    (android.provider.MediaStore.Audio.Media.ARTIST);
//            int songFile = data.getColumnIndex(android.provider.MediaStore.Audio.Media.DATA);
//            //add songs to list
//            do {
//                long thisId = data.getLong(idColumn);
//                String thisTitle = data.getString(titleColumn);
//                String thisArtist = data.getString(artistColumn);
//                songList.add(new Song(thisId, thisTitle, thisArtist));
//            }
//            while (data.moveToNext());
//        }

//        Collections.sort(songList, new Comparator<Song>(){
//            public int compare(Song a, Song b){
//                return a.getTitle().compareTo(b.getTitle());
//            }
//        });
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset (Loader<Cursor> loader) {
        mySongAdapter.changeCursor(null);
    }
}
