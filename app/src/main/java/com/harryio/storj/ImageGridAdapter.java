package com.harryio.storj;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

public class ImageGridAdapter extends ArrayAdapter<File> {
    private LayoutInflater layoutInflater;
    private Context context;

    public ImageGridAdapter(Context context, File[] images) {
        super(context, 0, images);

        this.context = context;
        this.layoutInflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.image_list_view, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        File imageFile = getItem((getCount() - 1) - position);
        Picasso.with(context)
                .load(imageFile)
                .fit()
                .into(viewHolder.imageView);

        return convertView;
    }

    private static class ViewHolder {
        ImageView imageView;
    }
}
