package com.qugengting.audio;

import android.graphics.Bitmap;

public class UpdateUI {

    public UpdateUI(String data,int state)
    {
        this.data=data;
        this.state=state;
    }

    public UpdateUI(Bitmap bitmap, int state) {
        this.bitmap=bitmap;
        this.state=state;
    }

    public UpdateUI() {

    }


    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }


    private int state;

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    private Bitmap bitmap;
    private Object data;

}
