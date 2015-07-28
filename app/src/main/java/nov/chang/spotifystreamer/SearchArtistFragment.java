package nov.chang.spotifystreamer;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import nov.chang.spotifystreamer.adapters.ArtistsAdapter;
import nov.chang.spotifystreamer.containers.ArtistContainer;
import nov.chang.spotifystreamer.containers.ImageContainer;
import nov.chang.spotifystreamer.cursors.ArtistCursor;
import retrofit.RetrofitError;


public class SearchArtistFragment extends Fragment implements ArtistsAdapter.onArtistSelectedListener {

    final String DEBUG = "myDebug";
    Bundle containersAndQuery;
    protected ArrayList<ArtistContainer> artistContainers;
    private SpotifyService spotify;
    private ArtistsAdapter adapter;
    private ArtistCursor cursor;
    private final String[] columns = {"_id", "artistName", "artistImageUrl", "artistID"};
    protected OnArtistSelectedListener mCallBack;
    View active;

    @Override
    public void onArtistSelected(String artistID, View view) {
        if (active != null) {
            active.setActivated(false);
        }
        view.setActivated(true);
        active = view;
        mCallBack.onArtistSelected(artistID);
    }


    public interface OnArtistSelectedListener {
        void onArtistSelected(String artistID);
    }

    public SearchArtistFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallBack = (OnArtistSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    " must implement OnArtistSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (savedInstanceState != null) {
            containersAndQuery.putInt("index", savedInstanceState.getInt("index"));
            containersAndQuery.putInt("top", savedInstanceState.getInt("top"));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("artists", artistContainers);
        outState.putSerializable("cursor", cursor);
        if (getView() != null) {
            outState.putCharSequence("query", ((SearchView)getView().findViewById(R.id.search_box)).getQuery());
            ListView mList = (ListView) getView().findViewById(R.id.search_results);
            int index = mList.getFirstVisiblePosition();
            View v = mList.getChildAt(0);
            int top = (v == null) ? 0 : (v.getTop() - mList.getPaddingTop());
            outState.putInt("index", index);
            outState.putInt("top", top);
            if (containersAndQuery != null) {
                containersAndQuery.putInt("index", -1);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_artist_search, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SpotifyApi api = new SpotifyApi();
        spotify = api.getService();
        final SearchView searchBox = (SearchView)view.findViewById(R.id.search_box);
        if (((MainActivity) getActivity()).tabletMode) {
            searchBox.setIconifiedByDefault(true);
        }
        if (savedInstanceState != null) {
            searchBox.setQuery(savedInstanceState.getCharSequence("search"), false);
        }
        final ListView resultsListView = (ListView) view.findViewById(R.id.search_results);
        if (containersAndQuery != null) {
            artistContainers = containersAndQuery.getParcelableArrayList("artistContainers");
            searchBox.setQuery(containersAndQuery.getCharSequence("query"), false);
        }
        final SearchArtistFragment fragment = this;
        final CursorLoader loader = new CursorLoader(this, savedInstanceState, resultsListView);
        loader.execute();
        searchBox.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                CursorLoader loader1 = new CursorLoader(fragment, savedInstanceState, resultsListView);
                loader1.execute(query);
                InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
                searchBox.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.app_name), null);
    }

    private void showArtists(List<Artist> artists) {
        for (int i = 0; i < artists.size(); i++) {
            Artist artist = artists.get(i);
            artistContainers.add(new ArtistContainer(artist.id, artist.name, artist.images));
            Object[] row = {i, artist.name, getUrl(artistContainers.get(i).images), artist.id};
            cursor.addRow(row);
        }
    }

    private String getUrl(List<ImageContainer> imageContainers) {
        String url = "http://www.gannett-cdn.com/-mm-/2e8a737f8467156ad3628027275ac8a58230bb14/r=300/https/d2b1xqaw2ss8na.cloudfront.net/static/img/defaultCoverL.png";
        if (!imageContainers.isEmpty()) {
            url = imageContainers.get(0).url;
        }
        return url;
    }

    private class CursorLoader extends AsyncTask<String, Void, Void> {

        Bundle savedInstanceState;
        SearchArtistFragment fragment;
        ListView listView;
        boolean newData = false;

        public CursorLoader(SearchArtistFragment fragment, Bundle save, ListView resultsListView) {
            savedInstanceState = save;
            this.fragment = fragment;
            listView = resultsListView;
        }

        @Override
        protected Void doInBackground(String... params) {
            if (params.length > 0) {
                artistContainers = new ArrayList<>();
                cursor = new ArtistCursor(columns);
                ArtistsPager artists;
                try {
                    artists = spotify.searchArtists(params[0]);
                } catch (RetrofitError e) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                    return null;
                }
                showArtists(artists.artists.items);
                newData = true;
            } else if (containersAndQuery != null) {
                cursor = new ArtistCursor(columns);
                for (int i = 0; i < artistContainers.size(); i++) {
                    ArtistContainer artist = artistContainers.get(i);
                    Object[] row = {i, artist.name, getUrl(artist.images), artist.id};
                    cursor.addRow(row);
                }
            } else if (savedInstanceState != null) {
                artistContainers = savedInstanceState.getParcelableArrayList("artists");
                cursor = (ArtistCursor)savedInstanceState.getSerializable("cursor");
            } else {
                artistContainers = new ArrayList<>();
                cursor = new ArtistCursor(columns);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter = new ArtistsAdapter(fragment, getActivity(), cursor);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            if (!newData) {
                if (containersAndQuery != null && containersAndQuery.getInt("index") != -1) {
                    listView.setSelectionFromTop(containersAndQuery.getInt("index"), containersAndQuery.getInt("top"));
                } else if (savedInstanceState != null) {
                    listView.setSelectionFromTop(savedInstanceState.getInt("index"), savedInstanceState.getInt("top"));
                }
            } else {
                if (containersAndQuery != null) {
                    containersAndQuery.putParcelableArrayList("artistContainers", artistContainers);
                }
            }
        }
    }
}
