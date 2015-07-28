package nov.chang.spotifystreamer.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import nov.chang.spotifystreamer.R;
import nov.chang.spotifystreamer.cursors.ArtistCursor;

public class ArtistsAdapter extends CursorAdapter {

    ArtistCursor cursor;
    onArtistSelectedListener callback;

    public ArtistsAdapter(Fragment fragment, Context context, Cursor c) {
        super(context, c, 0);
        cursor = (ArtistCursor)c;
        callback = (onArtistSelectedListener) fragment;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.search_result, parent, false);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        convertView = super.getView(position, convertView, parent);
        convertView.setActivated(false);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cursor.moveToPosition(position);
                callback.onArtistSelected(cursor.getString(cursor.getColumnIndexOrThrow("artistID")), v);
            }
        });
        return convertView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView imageView = (ImageView)view.findViewById(R.id.artist_image);
        Picasso.with(context).load(Uri.parse(cursor.getString(cursor.getColumnIndexOrThrow("artistImageUrl")))).into(imageView);
        TextView textView = (TextView)view.findViewById(R.id.artist_name);
        String artistName = cursor.getString(cursor.getColumnIndexOrThrow("artistName"));
        textView.setText(artistName);
    }

    public interface onArtistSelectedListener {
        void onArtistSelected(String artistID, View view);
    }
}
