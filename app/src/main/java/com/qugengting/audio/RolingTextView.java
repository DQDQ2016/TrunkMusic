package com.qugengting.audio;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class RolingTextView  extends android.support.v7.widget.AppCompatTextView {


    public RolingTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public RolingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public RolingTextView(Context context) {
        super(context);
    }
    @Override
    public boolean isFocused() {
        //就是把这里返回true即可
        return true;
    }

}
