package nov.chang.spotifystreamer;

import android.database.MatrixCursor;

import java.io.Serializable;

public class ArtistCursor extends MatrixCursor implements Serializable {
    public ArtistCursor(String[] columnNames) {
        super(columnNames);
    }
}
