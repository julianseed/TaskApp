package jp.techacademy.yasuhiko.tokushima.taskapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by tokushima on 2016/10/08.
 */

public class CategoryAdapter extends BaseAdapter {
    private LayoutInflater mLayoutInFlater;
    private ArrayList<Category> mCategoryArrayList;

    public CategoryAdapter(Context context) {
        mLayoutInFlater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setCategoryArrayList(ArrayList<Category> categoryArrayList) {
        mCategoryArrayList = categoryArrayList;
    }

    @Override
    public int getCount() {
        return mCategoryArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return mCategoryArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return mCategoryArrayList.get(i).getCategory_id();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = mLayoutInFlater.inflate(android.R.layout.simple_list_item_1, null);
        }

        TextView textView1 = (TextView) view.findViewById(android.R.id.text1);
        textView1.setText(mCategoryArrayList.get(i).getCategory());

        return view;
    }
}
