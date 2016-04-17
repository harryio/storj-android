package com.harryio.storj;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class ImageGridAdapter extends ArrayAdapter<File> {
    private LayoutInflater layoutInflater;
    private Context context;

    public ImageGridAdapter(Context context, List<File> imageFileList) {
        super(context, 0, imageFileList);

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

    private static class ViewHolder {
        ImageView imageView;
    }
}
