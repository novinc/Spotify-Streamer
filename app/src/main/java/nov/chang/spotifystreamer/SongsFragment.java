package nov.chang.spotifystreamer;


import android.app.Activity;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;

public class SongsFragment extends ListFragment {

    ArrayList<TrackContainer> trackContainers;
    SimpleCursorAdapter mAdapter;
    MatrixCursor mCursor;
    String[] columns = {"song", "album", "image", "_id"};
    String artistID;


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        artistID = getArguments().getString("artistID");
        fillTrackContainers(artistID);
        trackContainers = new ArrayList<>();
        trackContainers.add(new TrackContainer("Song 1", "album 1", null));
        trackContainers.add(new TrackContainer("Song 2", "album 2", null));
        trackContainers.add(new TrackContainer("Song 3", "album 3", null));
        mCursor = new MatrixCursor(columns);
        for (int i = 0; i < trackContainers.size(); i++) {
            TrackContainer trackContainer = trackContainers.get(i);
            Object[] row = {trackContainer.name, trackContainer.album, trackContainer.image, i};
            mCursor.addRow(row);
        }
        int[] to = {R.id.song_name, R.id.song_album};
        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.song, mCursor, columns, to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        setListAdapter(mAdapter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).setActionBarTitle("Top 10 Tracks", "Artist 1");
    }

    private void fillTrackContainers(String artistID) {

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            // restore state
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save the instance
    }
}
