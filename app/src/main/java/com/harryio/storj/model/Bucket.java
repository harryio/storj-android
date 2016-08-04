package com.harryio.storj.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Bucket implements Parcelable {
    public static final Parcelable.Creator<Bucket> CREATOR = new Parcelable.Creator<Bucket>() {
        @Override
        public Bucket createFromParcel(Parcel source) {
            return new Bucket(source);
        }

        @Override
        public Bucket[] newArray(int size) {
            return new Bucket[size];
        }
    };
    int storage;
    int transfer;
    String status;
    String[] pubkeys;
    String user;
    String name;
    String created;
    String id;

    public Bucket() {
    }

    protected Bucket(Parcel in) {
        this.storage = in.readInt();
        this.transfer = in.readInt();
        this.status = in.readString();
        this.pubkeys = in.createStringArray();
        this.user = in.readString();
        this.name = in.readString();
        this.created = in.readString();
        this.id = in.readString();
    }

    public int getStorage() {
        return storage;
    }

    public int getTransfer() {
        return transfer;
    }

    public String getStatus() {
        return status;
    }

    public String[] getPubkeys() {
        return pubkeys;
    }

    public String getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public String getCreated() {
        return created;
    }

    public String getId() {
        return id;
    }

    public String getFormattedDate() {
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = simpleDateFormat.parse(created);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            return sdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "";
    }

    @Override
    public String toString() {
        return "Bucket{" +
                "storage=" + storage +
                ", transfer=" + transfer +
                ", status='" + status + '\'' +
                ", pubkeys=" + Arrays.toString(pubkeys) +
                ", user='" + user + '\'' +
                ", name='" + name + '\'' +
                ", created='" + created + '\'' +
                ", id='" + id + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.storage);
        dest.writeInt(this.transfer);
        dest.writeString(this.status);
        dest.writeStringArray(this.pubkeys);
        dest.writeString(this.user);
        dest.writeString(this.name);
        dest.writeString(this.created);
        dest.writeString(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bucket bucket = (Bucket) o;

        return id != null ? id.equals(bucket.id) : bucket.id == null;

    }
}
