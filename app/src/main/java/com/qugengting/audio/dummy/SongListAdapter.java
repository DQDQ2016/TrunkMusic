package com.qugengting.audio.dummy;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.qugengting.audio.R;
import com.qugengting.audio.SongItem;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SongListAdapter extends ArrayAdapter<SongItem> {


    private int resourceId;

    public SongListAdapter(Context context, int resource, List<SongItem> objects) {
        super(context, resource,objects);
        resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SongItem song = getItem(position);//获取当前项的实例
        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        ((TextView) view.findViewById(R.id.item_songName)).setText(song.getSongName());
        ((TextView) view.findViewById(R.id.item_artistName)).setText(song.getArtistName());

        return view;
    }

}
