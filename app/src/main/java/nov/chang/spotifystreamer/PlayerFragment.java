package nov.chang.spotifystreamer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import nov.chang.spotifystreamer.containers.ArtistContainer;
import nov.chang.spotifystreamer.containers.TrackContainer;
import nov.chang.spotifystreamer.service.PlayerService;

public class PlayerFragment extends DialogFragment {

    private Handler mHandler = new Handler();
    PlayerService.LocalBinder mBinder;
    BroadcastReceiver receiver;
    BroadcastReceiver receiver2;
    boolean mBound = false;
    int pos;
    TrackContainer mTrack;
    ArrayList<TrackContainer> tracks;
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
        if (savedInstanceState == null) {
            mTrack = getArguments().getParcelable("track");
            tracks = getArguments().getParcelableArrayList("tracks");
            pos = getArguments().getInt("pos");
            Intent intentService = new Intent(getActivity(), PlayerService.class);
            intentService.setAction(PlayerService.ACTION_PLAY);
            intentService.putExtra("track", getArguments().getParcelable("track"));
            intentService.putParcelableArrayListExtra("tracks", getArguments().getParcelableArrayList("tracks"));
            intentService.putExtra("pos", pos);
            intentService.addFlags(ServiceInfo.FLAG_STOP_WITH_TASK);
            getActivity().bindService(intentService, mConnection, Context.BIND_AUTO_CREATE);
        } else {
            mTrack = savedInstanceState.getParcelable("track");
            tracks = savedInstanceState.getParcelableArrayList("tracks");
            pos = savedInstanceState.getInt("pos");
            Intent intentService = new Intent(getActivity(), PlayerService.class);
            intentService.putExtra("track", getArguments().getParcelable("track"));
            intentService.putParcelableArrayListExtra("tracks", getArguments().getParcelableArrayList("tracks"));
            intentService.putExtra("pos", pos);
            intentService.addFlags(ServiceInfo.FLAG_STOP_WITH_TASK);
            getActivity().bindService(intentService, mConnection, Context.BIND_AUTO_CREATE);
        }
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getStringExtra("direction").equals("next")) {
                    try {
                        mTrack = tracks.get(++pos);
                    } catch (IndexOutOfBoundsException e) {
                        mTrack = tracks.get(0);
                        pos = 0;
                    }
                } else if (intent.getStringExtra("direction").equals("prev")){
                    try {
                        mTrack = tracks.get(--pos);
                    } catch (IndexOutOfBoundsException e) {
                        mTrack = tracks.get(tracks.size() - 1);
                        pos = tracks.size() - 1;
                    }
                } else {
                    pos = intent.getIntExtra("direction", -1);
                    mTrack = tracks.get(pos);
                }
                set();
            }
        };
        receiver2 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                play.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_pause));
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, new IntentFilter(PlayerService.UPDATE));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver2, new IntentFilter(PlayerService.STARTED));
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
        set();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {

                    mBinder.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinder.prevTrack();
                play.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_pause));
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinder.nextTrack();
                play.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_pause));
            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBinder.getState().equals(PlayerService.State.started)) {
                    mBinder.pause();
                    play.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_play));
                } else if (mBinder.getState().equals(PlayerService.State.paused)) {
                    mBinder.play();
                    play.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_pause));
                }
            }
        });
        seekBar.setMax(30000);
    }

    @Override
    public void onResume() {
        super.onResume();
        play.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_pause));
        if (mBinder != null && mBinder.getState().equals(PlayerService.State.paused)) {
            play.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_play));
        }
    }

    private void set() {
        String artists = "";
        for (ArtistContainer artistContainer : mTrack.artists) {
            artists += artistContainer.name + ", ";
        }
        artistBox.setText(artists.substring(0, artists.length() - 2));
        trackLengthBox.setText(getDuration(mTrack.duration));
        albumBox.setText(mTrack.album);
        songBox.setText(mTrack.name);
        Picasso.with(getActivity()).load(Uri.parse(mTrack.image)).into(albumArt);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (PlayerService.LocalBinder) service;
            mBound = true;
            if (mBinder.getState().equals(PlayerService.State.paused)) {
                seekBar.setProgress(mBinder.getPlayer().getCurrentPosition());
                play.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_play));
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mBinder.getState().equals(PlayerService.State.started) && mBinder.getPlayer() != null) {
                        seekBar.setProgress(mBinder.getPlayer().getCurrentPosition());
                    }
                    mHandler.postDelayed(this, 50);
                }
            });
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mBinder.getState().equals(PlayerService.State.started) && mBinder.getPlayer() != null) {
                        String s = "0:";
                        if ((mBinder.getPlayer().getCurrentPosition() / 1000) < 10) {
                            s += "0";
                        }
                        s += (mBinder.getPlayer().getCurrentPosition() / 1000);
                        currTimeBox.setText(s);
                    }
                    mHandler.postDelayed(this, 1000);
                }
            });
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (pos != mBinder.getPosition()) {
                        pos = mBinder.getPosition();
                        mTrack = tracks.get(pos);
                        set();
                    }
                    mHandler.postDelayed(this, 1000);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver2);
        getActivity().startService(new Intent(getActivity(), PlayerService.class));
        if (mBound) {
            getActivity().unbindService(mConnection);
            mBound = false;
        }
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("track", mTrack);
        outState.putParcelableArrayList("tracks", tracks);
        outState.putInt("pos", pos);
    }

    private String getDuration(long duration) {
        /*int seconds = (int) (duration / 1000) % 60 ;
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
        ret += seconds;*/
        return "0:30";
    }
}
