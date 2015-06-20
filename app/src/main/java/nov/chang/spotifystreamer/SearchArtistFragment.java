package nov.chang.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class SearchArtistFragment extends Fragment {

    ArrayList<ArtistContainer> artistContainers;
    private SpotifyService spotify;
    private CursorAdapter adapter;
    private MatrixCursor cursor;
    private final String[] columns = {"_id", "artistName", "artistImageUrl", "artistID"};
    private OnArtistSelectedListener mCallBack;

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
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            artistContainers = savedInstanceState.getParcelableArrayList("artists");
            cursor = new MatrixCursor(columns);
            for (int i = 0; i < artistContainers.size(); i++) {
                ArtistContainer artist = artistContainers.get(i);
                Object[] row = {i, artist.name, getUrl(artist.images), artist.id};
                cursor.addRow(row);
            }
            adapter.swapCursor(cursor);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (artistContainers.size() > 0) {
            outState.putParcelableArrayList("artists", artistContainers);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_artist_search, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final SearchView searchBox = (SearchView)view.findViewById(R.id.search_box);
        artistContainers = new ArrayList<>();
        cursor = new MatrixCursor(columns);
        adapter = new CursorAdapter(getActivity(), cursor, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(R.layout.search_result, parent, false);
            }

            public View getView(final int position, View convertView, ViewGroup parent) {
                convertView = super.getView(position, convertView, parent);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cursor.moveToPosition(position);
                        mCallBack.onArtistSelected(cursor.getString(cursor.getColumnIndexOrThrow("artistID")));
                    }
                });
                return convertView;
            }

            @Override
            protected void onContentChanged() {
                super.onContentChanged();
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                ImageView imageView = (ImageView)view.findViewById(R.id.artist_image);
                Picasso.with(getActivity()).load(Uri.parse(cursor.getString(cursor.getColumnIndexOrThrow("artistImageUrl")))).into(imageView);
                TextView textView = (TextView)view.findViewById(R.id.artist_name);
                String artistName = cursor.getString(cursor.getColumnIndexOrThrow("artistName"));
                textView.setText(artistName);
            }
        };
        ListView resultsListView = (ListView) view.findViewById(R.id.search_results);
        resultsListView.setAdapter(adapter);
        SpotifyApi api = new SpotifyApi();
        spotify = api.getService();
        searchBox.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchSpotify(query);
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

    private List<Artist> searchSpotify(String search) {
        spotify.searchArtists(search, new Callback<ArtistsPager>() {
            @Override
            public void success(final ArtistsPager artistsPager, Response response) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showArtists(artistsPager.artists.items);
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getActivity(), "Failed to access spotify", Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        });

        return null;
    }

    private void showArtists(List<Artist> artists) {
        cursor = new MatrixCursor(columns);
        adapter.swapCursor(cursor);
        artistContainers.clear();
        for (int i = 0; i < artists.size(); i++) {
            Artist artist = artists.get(i);
            artistContainers.add(new ArtistContainer(artist.id, artist.name, artist.images));
            Object[] row = {i, artist.name, getUrl(artistContainers.get(i).images), artist.id};
            cursor.addRow(row);
        }
        adapter.notifyDataSetChanged();
    }

    private String getUrl(List<ImageContainer> imageContainers) {
        String url = "http://www.gannett-cdn.com/-mm-/2e8a737f8467156ad3628027275ac8a58230bb14/r=300/https/d2b1xqaw2ss8na.cloudfront.net/static/img/defaultCoverL.png";
        if (!imageContainers.isEmpty()) {
            url = imageContainers.get(0).url;
        }
        return url;
    }
}
