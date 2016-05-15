package com.harryio.storj.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.harryio.storj.R;
import com.squareup.picasso.Picasso;

import java.io.File;

public class ImageGridAdapter extends ArrayAdapter<File> {
    File imageFolder;
    private LayoutInflater layoutInflater;
    private Context context;

    public ImageGridAdapter(Context context, File imageFolder) {
        super(context, 0);

        this.context = context;
        this.layoutInflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.imageFolder = imageFolder;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.image_list_view, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        File imageFile = getItem(position);
        Picasso.with(context)
                .load(imageFile)
                .fit()
                .into(viewHolder.imageView);

        return convertView;
    }

    @Override
    public int getCount() {
        final File[] files = imageFolder.listFiles();
        return files == null ? 0 : files.length;
    }

    @Override
    public File getItem(int position) {
        return imageFolder.listFiles()[position];
    }

    private static class ViewHolder {
        ImageView imageView;
    }
}
