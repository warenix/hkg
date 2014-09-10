package org.dyndns.warenix.abs.activity;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.dyndns.warenix.hkg.R;

/**
 * adapter of the list navigation of ABSActionbarActivity
 *
 * @author warenix
 *
 */
public class SwitchPageAdapter extends ArrayAdapter<String> {

    private String mThreadTitle = "";

    private int mSelectedPosition = 0;

    public SwitchPageAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public void setTitle(String title) {
        mThreadTitle = title;
    }

    public void setPageCount(int pageCount) {
        mPageCount = pageCount;
    }

    int mPageCount;

    @Override
    public int getCount() {
        return mPageCount;
    }

    @Override
    public String getItem(int position) {
        return super.getItem(position);
    }

    // return views of drop down items
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View v = getView(position, convertView, parent);
        return v;
    }

    // return header view of drop down
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.switch_page_dropdown, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.site = (TextView) view.findViewById(R.id.label);
            viewHolder.icon = (ImageView) view.findViewById(R.id.icon);
            view.setTag(viewHolder);
        }

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String siteLink = position == 0 ? mThreadTitle : "\t\t"
                                                         + (position + 1) + "\t";
        TextView site = viewHolder.site;
        if (position == mSelectedPosition) {
            site.setTextColor(Color.YELLOW);
        } else {
            site.setTextColor(Color.WHITE);
        }
        site.setText(siteLink);

        // TODO mark visited
        ImageView icon = viewHolder.icon;
        boolean visited = position == 0;
        if (!visited) {
            icon.setImageResource(R.drawable.rectangle);
        } else {
            icon.setImageBitmap(null);
        }

        return view;
    }

    public void setSelectedPosition(int selectedItemIndex) {
        mSelectedPosition = selectedItemIndex;
    }

    static class ViewHolder {
        TextView site;
        ImageView icon;
    }
}
