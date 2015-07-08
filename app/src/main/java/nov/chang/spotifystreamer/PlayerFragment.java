package nov.chang.spotifystreamer;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class PlayerFragment extends DialogFragment {

    TrackContainer mTrack;
    List<TrackContainer> tracks;
    SeekBar seekBar;
    TextView currTimeBox;
    TextView trackLengthBox;
    TextView artistBox;
    TextView albumBox;
    ImageView albumArt;
    TextView songBox;
    ImageButton prev;
    ImageButton next;
    ImageButton play;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mTrack = getArguments().getParcelable("track");
        tracks = getArguments().getParcelableArrayList("tracks");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.player, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        artistBox = (TextView)view.findViewById(R.id.player_artist);
        albumBox = (TextView)view.findViewById(R.id.player_album);
        albumArt = (ImageView)view.findViewById(R.id.player_image);
        songBox = (TextView)view.findViewById(R.id.player_track);
        seekBar = (SeekBar)view.findViewById(R.id.seekBar);
        currTimeBox = (TextView)view.findViewById(R.id.curr_time);
        trackLengthBox = (TextView)view.findViewById(R.id.song_length);
        prev = (ImageButton)view.findViewById(R.id.player_back);
        next = (ImageButton)view.findViewById(R.id.player_next);
        play = (ImageButton)view.findViewById(R.id.player_play);
        String artists = "";
        for (ArtistContainer artistContainer : mTrack.artists) {
            artists += artistContainer.name + ", ";
        }
        artistBox.setText(artists.substring(0, artists.length() - 2));
        trackLengthBox.setText(getDuration(mTrack.duration));
        albumBox.setText(mTrack.album);
        songBox.setText(mTrack.name);
        Picasso.with(getActivity()).load(Uri.parse(mTrack.image)).into(albumArt);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
    private String getDuration(long duration) {
        int seconds = (int) (duration / 1000) % 60 ;
        int minutes = (int) ((duration / (1000*60)) % 60);
        int hours = (int) ((duration / (1000*60*60)) % 24);
        String ret = "";
        boolean hasHours = false;
        if (hours > 0) {
            ret += hours + ":";
            hasHours = true;
        }
        if (minutes < 10 && hasHours) {
            ret += "0";
        }
        ret += minutes + ":";
        if (seconds < 10) {
            ret += "0";
        }
        ret += seconds;
        return ret;
    }
}
