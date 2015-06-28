package nov.chang.spotifystreamer;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

public class SongsFragment extends ListFragment {

    final String DEBUG = "myDebug";
    ArrayList<TrackContainer> trackContainers;
    SongsListAdapter mAdapter;
    ArtistContainer artistContainer;
    SongsCursor mCursor;
    SpotifyService spotify;
    String[] columns = {"song", "album", "image", "_id"};
    String artistID;
    Artist artist;

    private void setArtist(Artist artist) {
        this.artist = artist;
        artistContainer = new ArtistContainer(artist.id, artist.name, artist.images);
        ((MainActivity) getActivity()).setActionBarTitle("Top 10 Tracks", artist.name);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        artistID = getArguments().getString("artistID");
        spotify = new SpotifyApi().getService();
        CursorLoader loader = new CursorLoader(savedInstanceState);
        loader.execute();
    }

    private void fillTrackContainers(String artistID) {
        Map<String, Object> options = new HashMap<>();
        options.put("country", "US");
        Tracks tracks = spotify.getArtistTopTrack(artistID, options);
        fillContainers(tracks);
    }

    public void fillContainers(Tracks tracks) {
        List<Track> tracks1 = tracks.tracks;
        for (Track track : tracks1) {
            String url = null;
            if (!track.album.images.isEmpty()) {
                url = track.album.images.get(0).url;
            }
            trackContainers.add(new TrackContainer(track.name, track.album.name, url));
        }
        if (trackContainers.isEmpty()) {
            Object[] row = {"No Top Tracks", null, null, 0};
            mCursor.addRow(row);
        }
        for (int i = 0; i < trackContainers.size(); i++) {
            TrackContainer trackContainer = trackContainers.get(i);
            Object[] row = {trackContainer.name, trackContainer.album, trackContainer.image, i};
            mCursor.addRow(row);
        }
        int[] to = {R.id.song_name, R.id.song_album, R.id.song_image};
        mAdapter = new SongsListAdapter(getActivity(), R.layout.song, mCursor, columns, to);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("trackContainers", trackContainers);
        outState.putSerializable("adapter", mAdapter);
    }

    private class CursorLoader extends AsyncTask<Void, Void, Void> {

        Bundle savedInstanceState;

        public CursorLoader(Bundle save) {
            savedInstanceState = save;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Artist artist = spotify.getArtist(artistID);
            setArtist(artist);
            mCursor = new SongsCursor(columns);
            if (savedInstanceState != null) {
                trackContainers = savedInstanceState.getParcelableArrayList("trackContainers");
                mAdapter = (SongsListAdapter) savedInstanceState.getSerializable("adapter");
            } else {
                trackContainers = new ArrayList<>();
                fillTrackContainers(artistID);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (savedInstanceState == null) {
                setListAdapter(mAdapter);
            }
        }
    }
}