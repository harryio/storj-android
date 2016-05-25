package com.harryio.storj.ui.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.harryio.storj.R;

public class StateView extends LinearLayout {

    public StateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public StateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setGravity(Gravity.CENTER);
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.state_view, this, true);
        ImageView stateImageView = (ImageView) findViewById(R.id.state_imageView);
        TextView stateTextView = (TextView) findViewById(R.id.state_textView);

        if (attrs != null) {
            String stateText = "";
            int drawableId;

            TypedArray typedArray = context.getTheme()
                    .obtainStyledAttributes(attrs, R.styleable.State, 0, 0);
            try {
                drawableId = typedArray.getResourceId(R.styleable.State_imageId, 0);
                stateText = typedArray.getString(R.styleable.State_text);
            } finally {
                typedArray.recycle();
            }

            stateImageView.setImageResource(drawableId);
            stateTextView.setText(stateText);
        }
    }
}
