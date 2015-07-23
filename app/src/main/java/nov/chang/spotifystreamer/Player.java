package nov.chang.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

import nov.chang.spotifystreamer.containers.TrackContainer;
import nov.chang.spotifystreamer.service.PlayerService;

public class Player extends AppCompatActivity {

    TrackContainer track;
    ArrayList<TrackContainer> tracks;
    int pos;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        PlayerFragment player = new PlayerFragment();
        Bundle b = new Bundle();
        track = getIntent().getParcelableExtra("track");
        tracks = getIntent().getParcelableArrayListExtra("tracks");
        pos = getIntent().getIntExtra("pos", -1);
        b.putParcelable("track", track);
        b.putParcelableArrayList("tracks", tracks);
        b.putInt("pos", pos);
        player.setArguments(b);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.player_frag, player);
        fragmentTransaction.commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        TrackContainer newTrack = intent.getParcelableExtra("track");
        ArrayList<TrackContainer> newTracks = intent.getParcelableArrayListExtra("tracks");
        int newPos = intent.getIntExtra("pos", -1);
        if (pos != newPos) {
            track = newTrack;
            tracks = newTracks;
            pos = newPos;
        }
        PlayerFragment player = new PlayerFragment();
        Bundle b = new Bundle();
        b.putParcelable("track", track);
        b.putParcelableArrayList("tracks", tracks);
        b.putInt("pos", pos);
        player.setArguments(b);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.player_frag, player);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("update", true);
        startActivity(intent);
        Intent i = new Intent(PlayerService.NOW_PLAYING);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }
}
