package com.harryio.storj.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.harryio.storj.R;
import com.harryio.storj.model.StorjFile;
import com.harryio.storj.util.FileUtils;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FileAdapter extends ArrayAdapter<StorjFile> {
    public static final String MIME_TYPE_AUDIO = "audio/";
    public static final String MIME_TYPE_IMAGE = "image/";
    public static final String MIME_TYPE_VIDEO = "video/";


    private LayoutInflater layoutInflater;

    public FileAdapter(Context context, List<StorjFile> objects) {
        super(context, 0, objects);
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FileHolder fileHolder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.file_grid_item, parent, false);
            fileHolder = new FileHolder(convertView);
            convertView.setTag(fileHolder);
        } else {
            fileHolder = (FileHolder) convertView.getTag();
        }

        StorjFile storjFile = getItem(position);
        fileHolder.nameTextView.setText(storjFile.getFilename());
        fileHolder.capacityTextView.setText(FileUtils.getReadableFileSize(storjFile.getSize()));

        String mimeType = storjFile.getMimetype();
        int drawableId;
        if (mimeType.contains(MIME_TYPE_AUDIO)) {
            drawableId = R.drawable.ic_audio;
        } else if (mimeType.contains(MIME_TYPE_IMAGE)) {
            drawableId = R.drawable.ic_image;
        } else if (mimeType.contains(MIME_TYPE_VIDEO)) {
            drawableId = R.drawable.ic_video;
        } else {
            drawableId = R.drawable.ic_file;
        }

        fileHolder.imageView.setImageResource(drawableId);

        return convertView;
    }

    static class FileHolder {
        @Bind(R.id.name_textView)
        TextView nameTextView;
        @Bind(R.id.capacity_textView)
        TextView capacityTextView;
        @Bind(R.id.image)
        ImageView imageView;

        FileHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
