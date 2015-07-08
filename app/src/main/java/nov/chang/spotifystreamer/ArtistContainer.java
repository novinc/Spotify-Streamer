package nov.chang.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

public class ArtistContainer implements Parcelable {
    public String id;
    public String name;
    public List<ImageContainer> images;

    public ArtistContainer(String id, String name, List<Image> images) {
        this.id = id;
        this.name = name;
        this.images = new ArrayList<>();
        if (images != null) {
            for (Image img : images) {
                this.images.add(new ImageContainer(img));
            }
        }
    }

    protected ArtistContainer(Parcel in) {
        id = in.readString();
        name = in.readString();
        if (in.readByte() == 0x01) {
            images = new ArrayList<>();
            in.readList(images, ImageContainer.class.getClassLoader());
        } else {
            images = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        if (images == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(images);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ArtistContainer> CREATOR = new Parcelable.Creator<ArtistContainer>() {
        @Override
        public ArtistContainer createFromParcel(Parcel in) {
            return new ArtistContainer(in);
        }

        @Override
        public ArtistContainer[] newArray(int size) {
            return new ArtistContainer[size];
        }
    };
}