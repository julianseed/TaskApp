package jp.techacademy.yasuhiko.tokushima.taskapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by tokushima on 2016/10/01.
 */

public class TaskAdapter extends BaseAdapter {
    private LayoutInflater mLayoutInFlater;
    private ArrayList<Task> mTaskArrayList;

    public TaskAdapter(Context context) {
        mLayoutInFlater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setTaskArrayList(ArrayList<Task> taskArrayList) {
        mTaskArrayList = taskArrayList;
    }

    @Override
    public int getCount() {
        return mTaskArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return mTaskArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return mTaskArrayList.get(i).getId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = mLayoutInFlater.inflate(android.R.layout.simple_list_item_2, null);
        }

        TextView textview1 = (TextView) view.findViewById(android.R.id.text1);
        TextView textview2 = (TextView) view.findViewById(android.R.id.text2);

        textview1.setText(mTaskArrayList.get(i).getTitle());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.JAPANESE);
        Date date = mTaskArrayList.get(i).getDate();
        textview2.setText(simpleDateFormat.format(date) + " － " + mTaskArrayList.get(i).getCategory());

        return view;
    }
}
