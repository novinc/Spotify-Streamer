package nov.chang.spotifystreamer;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import nov.chang.spotifystreamer.adapters.SongsListAdapter;
import nov.chang.spotifystreamer.containers.ArtistContainer;
import nov.chang.spotifystreamer.containers.TrackContainer;
import nov.chang.spotifystreamer.cursors.SongsCursor;
import nov.chang.spotifystreamer.service.PlayerService;
import retrofit.RetrofitError;

public class SongsFragment extends ListFragment {

    final String DEBUG = "myDebug";
    ArrayList<TrackContainer> trackContainers;
    SongsListAdapter mAdapter;
    ArtistContainer artistContainer;
    SongsCursor mCursor;
    SpotifyService spotify;
    String[] columns = {"song", "album", "image", "_id"};
    String artistID;

    private void setArtist(ArtistContainer artist) {
        artistContainer = artist;
        if (artist != null) {
            ((MainActivity) getActivity()).setActionBarTitle("Top 10 Tracks", artist.name);
        } else {
            ((MainActivity) getActivity()).setActionBarTitle("Top 10 Tracks", "No internet connection");
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            artistID = getArguments().getString("artistID");
            spotify = new SpotifyApi().getService();
            CursorLoader loader = new CursorLoader(savedInstanceState);
            loader.execute();
        }
    }



    private void fillTrackContainers(String artistID) {
        Map<String, Object> options = new HashMap<>();
        options.put("country", "US");
        Tracks tracks = new Tracks();
        try {
            tracks = spotify.getArtistTopTrack(artistID, options);
        } catch (RetrofitError e) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            e.printStackTrace();
        }
        fillContainers(tracks);
    }

    public void fillContainers(Tracks tracks) {
        List<Track> tracks1 = tracks.tracks;
        if (tracks1 != null) {
            for (Track track : tracks1) {
                String url = null;
                if (!track.album.images.isEmpty()) {
                    url = track.album.images.get(0).url;
                }
                trackContainers.add(new TrackContainer(track.name, track.album.name, url, track.preview_url, track.duration_ms, track.artists));
            }
            for (int i = 0; i < trackContainers.size(); i++) {
                TrackContainer trackContainer = trackContainers.get(i);
                Object[] row = {trackContainer.name, trackContainer.album, trackContainer.image, i};
                mCursor.addRow(row);
            }
        }
        if (trackContainers.isEmpty()) {
            Object[] row = {"No Top Tracks", null, null, 0};
            mCursor.addRow(row);
        }
        int[] to = {R.id.song_name, R.id.song_album, R.id.song_image};
        mAdapter = new SongsListAdapter(getActivity(), R.layout.song, mCursor, columns, to);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (trackContainers.size() > 0) {
            getActivity().stopService(new Intent(getActivity(), PlayerService.class));
            TrackContainer selectedTrack = trackContainers.get(position);
            if (isTabletMode()) {
                PlayerFragment playerFragment = new PlayerFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable("track", selectedTrack);
                bundle.putParcelableArrayList("tracks", trackContainers);
                bundle.putInt("pos", position);
                playerFragment.setArguments(bundle);
                playerFragment.show(getFragmentManager(), "player");
            } else {
                Intent intent = new Intent(getActivity(), Player.class);
                intent.putExtra("track", selectedTrack);
                intent.putParcelableArrayListExtra("tracks", trackContainers);
                intent.putExtra("pos", position);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("artist", artistContainer);
        outState.putParcelableArrayList("trackContainers", trackContainers);
        outState.putSerializable("adapter", mAdapter);
    }

    private boolean isTabletMode() {
        return ((MainActivity) getActivity()).tabletMode;
    }

    private class CursorLoader extends AsyncTask<Void, Void, Void> {

        Bundle savedInstanceState;

        public CursorLoader(Bundle save) {
            savedInstanceState = save;
        }

        @Override
        protected Void doInBackground(Void... params) {
            ArtistContainer artist;
            if (savedInstanceState != null) {
                artist = savedInstanceState.getParcelable("artist");
            } else {
                Artist artistAPI = null;
                try {
                    artistAPI = spotify.getArtist(artistID);
                } catch (RetrofitError e) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    e.printStackTrace();
                }
                if (artistAPI != null) {
                    artist = new ArtistContainer(artistAPI.id, artistAPI.name, artistAPI.images);
                } else {
                    artist = null;
                }
            }
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
