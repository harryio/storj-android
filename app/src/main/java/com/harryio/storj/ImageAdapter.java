package com.harryio.storj;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private File[] imageFiles;
    private LayoutInflater layoutInflater;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public ImageAdapter(Context context, File[] imageFiles) {
        this.context = context;
        this.imageFiles = imageFiles;
        this.layoutInflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(View itemView, final OnItemClickListener onItemClickListener) {
            super(itemView);
            imageView = (ImageView) itemView;

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(getAdapterPosition());
                }
            });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.image_list_view, parent, false);
        return new ViewHolder(view, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        File imageFile = imageFiles[(getItemCount() - 1) - position];

        Picasso.with(context)
                .load(imageFile)
                .fit()
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageFiles.length;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    interface OnItemClickListener {
        void onItemClick(int position);
    }
}
