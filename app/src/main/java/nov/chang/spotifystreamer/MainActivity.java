package nov.chang.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;

import nov.chang.spotifystreamer.service.PlayerService;

public class MainActivity extends AppCompatActivity implements SearchArtistFragment.OnArtistSelectedListener {

    private String artistID;
    final String DEBUG = "myDebug";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            SearchArtistFragment searchArtistFragment = new SearchArtistFragment();
            ft.add(R.id.fragment, searchArtistFragment, "artistFragment");
            ft.commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("ID", artistID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showSongsFragment(String artistID) {
        this.artistID = artistID;
        SearchArtistFragment searchArtistFragment = (SearchArtistFragment) getSupportFragmentManager().findFragmentByTag("artistFragment");
        searchArtistFragment.containersAndQuery = new Bundle();
        Bundle b = searchArtistFragment.containersAndQuery;
        b.putParcelableArrayList("artistContainers", searchArtistFragment.artistContainers);
        b.putCharSequence("query", ((SearchView)searchArtistFragment.getView().findViewById(R.id.search_box)).getQuery());
        ListView mList = (ListView) searchArtistFragment.getView().findViewById(R.id.search_results);
        int index = mList.getFirstVisiblePosition();
        View v = mList.getChildAt(0);
        int top = (v == null) ? 0 : (v.getTop() - mList.getPaddingTop());
        b.putInt("index", index);
        b.putInt("top", top);
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        SongsFragment songsFragment = new SongsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("artistID", artistID);
        songsFragment.setArguments(bundle);
        ft.replace(R.id.fragment, songsFragment, "songs");
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onArtistSelected(String artistID) {
        showSongsFragment(artistID);
    }

    public void setActionBarTitle(final String title, final String subtitle) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(title);
                    getSupportActionBar().setSubtitle(subtitle);
                }
            }
        });
    }

    @Override
    protected void onStop() {
        if (!PlayerService.playerState.equals(PlayerService.State.started) &&
                !PlayerService.playerState.equals(PlayerService.State.prepared) &&
                !PlayerService.playerState.equals(PlayerService.State.preparing)) {
            Log.v(DEBUG, PlayerService.playerState.toString());
            stopService(new Intent(getApplicationContext(), PlayerService.class));
        }
        super.onStop();
    }
}
