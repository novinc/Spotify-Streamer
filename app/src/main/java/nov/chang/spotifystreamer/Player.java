package nov.chang.spotifystreamer;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class Player extends AppCompatActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        PlayerFragment player = new PlayerFragment();
        Bundle b = new Bundle();
        b.putParcelable("track", getIntent().getParcelableExtra("track"));
        b.putParcelableArrayList("tracks", getIntent().getParcelableArrayListExtra("tracks"));
        b.putInt("pos", getIntent().getIntExtra("pos", -1));
        player.setArguments(b);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.player_frag, player);
        fragmentTransaction.commit();
    }
}
