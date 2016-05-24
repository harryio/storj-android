package com.harryio.storj.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.harryio.storj.R;
import com.harryio.storj.model.Bucket;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class BucketGridAdapter extends ArrayAdapter<Bucket> {
    private LayoutInflater inflater;

    public BucketGridAdapter(Context context, List<Bucket> buckets) {
        super(context, 0, buckets);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.bucket_grid_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Bucket bucket = getItem(position);
        viewHolder.nameTextView.setText(bucket.getName());
        viewHolder.capacityTextView.setText(String.format("%1$dGB", bucket.getStorage()));
        viewHolder.dateTextView.setText(bucket.getFormattedDate());

        return convertView;
    }

    static class ViewHolder {
        @Bind(R.id.name_textView)
        TextView nameTextView;
        @Bind(R.id.capacity_textView)
        TextView capacityTextView;
        @Bind(R.id.date_textView)
        TextView dateTextView;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
