package com.harryio.storj.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.harryio.storj.R;
import com.harryio.storj.model.Bucket;
import com.harryio.storj.util.PrefUtils;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.harryio.storj.util.PrefUtils.KEY_DEFAULT_BUCKET_ID;

public class BucketAdapter extends BaseAdapter {
    private static final String TAG = "BucketAdapter";
    private LayoutInflater inflater;
    private List<Bucket> buckets;
    private String defaultBucketId;
    private Context context;

    public BucketAdapter(Context context, List<Bucket> buckets) {
        inflater = LayoutInflater.from(context);
        this.context = context;
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
        viewHolder.cloudImageView.setVisibility(defaultBucketId.equals(bucket.getId()) ? View.VISIBLE : View.GONE);

        return convertView;
    }

    public void addItem(Bucket bucket) {
        buckets.add(bucket);
        notifyDataSetChanged();
    }

    public void removeItem(Bucket bucket) {
        Log.i(TAG, "Size before removing: " + buckets.size());
        buckets.remove(bucket);
        Log.i(TAG, "Size after removing: " + buckets.size());

        if (bucket.getId().equals(defaultBucketId)) {
            final PrefUtils prefUtils = PrefUtils.instance(context);
            if (buckets.size() > 0) {
                defaultBucketId = buckets.get(0).getId();
                prefUtils.storeString(KEY_DEFAULT_BUCKET_ID, defaultBucketId);
            } else {
                prefUtils.storeString(KEY_DEFAULT_BUCKET_ID, null);
            }
        }
        notifyDataSetChanged();
    }

    public void setDefaultBucketId(String bucketId) {
        defaultBucketId = bucketId;
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
        @Bind(R.id.imageView_cloud)
        ImageView cloudImageView;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
