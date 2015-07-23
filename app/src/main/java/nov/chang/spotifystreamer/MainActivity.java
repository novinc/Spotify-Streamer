package nov.chang.spotifystreamer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;

import nov.chang.spotifystreamer.containers.TrackContainer;
import nov.chang.spotifystreamer.service.PlayerService;

public class MainActivity extends AppCompatActivity implements SearchArtistFragment.OnArtistSelectedListener {

    private String artistID;
    final String DEBUG = "myDebug";
    ArrayList<TrackContainer> playingTracks;
    private int pos = -1;
    BroadcastReceiver receiver;
    BroadcastReceiver receiver2;
    private Menu menu;
    boolean enableMenu = false;
    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            pos = ((PlayerService.LocalBinder) service).getPosition();
            playingTracks = ((PlayerService.LocalBinder) service).getTracks();
            if (pos == -1) {
                enableMenu = false;
            } else {
                enableMenu = true;
                enableNowPlaying();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            SearchArtistFragment searchArtistFragment = new SearchArtistFragment();
            ft.add(R.id.fragment, searchArtistFragment, "artistFragment");
            ft.commit();
        } else {
            playingTracks = savedInstanceState.getParcelableArrayList("tracks");
            pos = savedInstanceState.getInt("pos", -1);
            if (pos != -1) {
                enableMenu = true;
            }
        }
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                playingTracks = intent.getParcelableArrayListExtra("tracks");
                pos = intent.getIntExtra("pos", -1);
            }
        };
        receiver2 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                playingTracks = intent.getParcelableArrayListExtra("tracks");
                pos = intent.getIntExtra("pos", -1);
                if (playingTracks != null) {
                    enableNowPlaying();
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getIntent() != null && getIntent().getBooleanExtra("update", false)) {
            enableMenu = true;
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(PlayerService.UPDATE_MAIN));
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver2, new IntentFilter(PlayerService.NOW_PLAYING));
        bindService(new Intent(getApplicationContext(), PlayerService.class), mServiceConnection, 0);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("ID", artistID);
        outState.putParcelableArrayList("tracks", playingTracks);
        outState.putInt("pos", pos);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        if (enableMenu && playingTracks != null) {
            enableNowPlaying();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == 111) {
            Intent intent = new Intent(getApplicationContext(), Player.class);
            intent.putParcelableArrayListExtra("tracks", playingTracks);
            intent.putExtra("pos", pos);
            try {
                intent.putExtra("track", playingTracks.get(pos));
            } catch (NullPointerException e) {
                disableNowPlaying();
                enableMenu = false;
                return super.onOptionsItemSelected(item);
            }
            startActivity(intent);
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

    private void enableNowPlaying() {
        if (menu != null && menu.findItem(111) == null) {
            menu.add(0, 111, 1, "Now Playing").setIcon(android.R.drawable.ic_media_play).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
    }

    private void disableNowPlaying() {
        if (menu != null) {
            menu.removeItem(111);
        }
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
            stopService(new Intent(getApplicationContext(), PlayerService.class));
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        unbindService(mServiceConnection);
        enableMenu = false;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver2);
        enableMenu = false;
        super.onDestroy();
    }
}
