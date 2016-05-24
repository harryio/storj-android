package com.harryio.storj.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.harryio.storj.R;
import com.harryio.storj.model.Bucket;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class BucketGridAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<Bucket> buckets;

    public BucketGridAdapter(Context context, List<Bucket> buckets) {
        inflater = LayoutInflater.from(context);
        this.buckets = buckets;
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

        Bucket bucket = (Bucket) getItem(position);
        viewHolder.nameTextView.setText(bucket.getName());
        viewHolder.capacityTextView.setText(String.format("%1$dGB", bucket.getStorage()));
        viewHolder.dateTextView.setText(bucket.getFormattedDate());

        return convertView;
    }

    public void addItem(Bucket bucket) {
        buckets.add(bucket);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return buckets.size();
    }

    @Override
    public Object getItem(int position) {
        return buckets.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
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
