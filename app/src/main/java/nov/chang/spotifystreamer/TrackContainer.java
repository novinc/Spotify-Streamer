package nov.chang.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.ArtistSimple;

public class TrackContainer implements Parcelable {

    public String name;
    public String album;
    public String image;
    public String uri;
    public long duration;
    public List<ArtistContainer> artists;

    public TrackContainer(String name, String album, String image, String uri, long duration, List<ArtistSimple> artists) {
        this.name = name;
        this.album = album;
        this.image = image;
        this.uri = uri;
        this.duration = duration;
        this.artists = new ArrayList<>();
        for (ArtistSimple artist : artists) {
            this.artists.add(new ArtistContainer(artist.id, artist.name, null));
        }
    }

    protected TrackContainer(Parcel in) {
        name = in.readString();
        album = in.readString();
        image = in.readString();
        uri = in.readString();
        duration = in.readLong();
        if (in.readByte() == 0x01) {
            artists = new ArrayList<>();
            in.readList(artists, ArtistContainer.class.getClassLoader());
        } else {
            artists = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(album);
        dest.writeString(image);
        dest.writeString(uri);
        dest.writeLong(duration);
        if (artists == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(artists);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<TrackContainer> CREATOR = new Parcelable.Creator<TrackContainer>() {
        @Override
        public TrackContainer createFromParcel(Parcel in) {
            return new TrackContainer(in);
        }

        @Override
        public TrackContainer[] newArray(int size) {
            return new TrackContainer[size];
        }
    };
}