package nov.chang.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SearchArtistFragment.OnArtistSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        SearchArtistFragment searchArtistFragment = new SearchArtistFragment();
        ft.add(R.id.fragment, searchArtistFragment);
        ft.commit();
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
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        SongsFragment songsFragment = new SongsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("ArtistID", artistID);
        songsFragment.setArguments(bundle);
        ft.replace(R.id.fragment, songsFragment);
        ft.addToBackStack(null);
        ft.commit();
        Toast.makeText(this, "made new fragment with " + artistID, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onArtistSelected(String artistID) {
        showSongsFragment(artistID);
    }

    public void setActionBarTitle(String title, String subtitle) {
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setSubtitle(subtitle);
    }
}
