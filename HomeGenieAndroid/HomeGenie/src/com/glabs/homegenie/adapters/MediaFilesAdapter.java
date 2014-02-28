/*
    This file is part of HomeGenie for Adnroid.

    HomeGenie for Adnroid is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    HomeGenie for Adnroid is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with HomeGenie for Adnroid.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
 *     Author: Generoso Martello <gene@homegenie.it>
 */

package com.glabs.homegenie.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.glabs.homegenie.R;
import com.glabs.homegenie.service.data.MediaEntry;

import java.util.List;

/**
 * Created by Gene on 01/02/14.
 */
public class MediaFilesAdapter extends ArrayAdapter<MediaEntry> {

    private List<MediaEntry> _entries;
    private LayoutInflater inflater;
    private int selectedIndex = -1;

    public MediaFilesAdapter(Context context, int resource, List<MediaEntry> objects) {
        super(context, resource, objects);
        _entries = objects;
        inflater = LayoutInflater.from(context);
    }

    public void setEntries(List<MediaEntry> objects) {
        _entries.clear();
        _entries.addAll(objects);
        selectedIndex = -1;
        this.notifyDataSetChanged();
    }

    public void setSelectedIndex(int ind) {
        selectedIndex = ind;
        this.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {

        MediaEntry mentry = getItem(position);

        ViewHolder holder;

        if (v == null) {
            v = inflater.inflate(R.layout.widget_control_mediaserver_item, parent, false);
            holder = new ViewHolder();
            holder.Title = (TextView) v.findViewById(R.id.title);
            holder.Icon = (ImageView) v.findViewById(R.id.icon);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        holder.Title.setText(mentry.Title);
        if (mentry.Class.indexOf("object.container") == 0) {
            holder.Title.setTypeface(Typeface.DEFAULT_BOLD);
            holder.Icon.setBackgroundResource(R.drawable.browser_folder);
        } else {
            holder.Title.setTypeface(Typeface.DEFAULT);
            if (mentry.Class.indexOf("object.item.videoItem") == 0) {
                holder.Icon.setBackgroundResource(R.drawable.browser_video);
            } else if (mentry.Class.indexOf("object.item.audioItem") == 0) {
                holder.Icon.setBackgroundResource(R.drawable.browser_audio);
            } else if (mentry.Class.indexOf("object.item.imageItem") == 0) {
                holder.Icon.setBackgroundResource(R.drawable.browser_image);
            }
        }


        if (selectedIndex != -1 && position == selectedIndex) {
            v.setBackgroundColor(getAttrVal(android.R.attr.colorPressedHighlight));
        } else {
            v.setBackgroundColor(Color.BLACK);
        }


        return v;
    }

    private int getAttrVal(int attr) {
        TypedValue Val = new TypedValue();
        getContext().getTheme().resolveAttribute(attr, Val, true);
        return Val.data;
    }

    private static class ViewHolder {
        ImageView Icon;
        TextView Title;
    }
}
