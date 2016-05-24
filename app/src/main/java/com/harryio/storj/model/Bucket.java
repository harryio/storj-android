package com.harryio.storj.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Bucket {
    int storage;
    int transfer;
    String status;
    String[] pubkeys;
    String user;
    String name;
    String created;
    String id;

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
}
