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

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.widget.ListView;

import com.glabs.homegenie.R;
import com.glabs.homegenie.client.data.Group;
import com.glabs.homegenie.fragments.GroupFragment;
import com.viewpagerindicator.IconPagerAdapter;

import java.util.ArrayList;

public class GroupsFragmentAdapter extends FragmentPagerAdapter implements IconPagerAdapter {
    private ArrayList<Group> _groups = new ArrayList<Group>();
    private ArrayList<GroupFragment> _fragments = new ArrayList<GroupFragment>();

    public void setGroups(ArrayList<Group> groups) {
        this._groups = groups;
        for (int f = 0; f < _groups.size(); f++) {
            GroupFragment fragment = null;
            if (_fragments.size() > f) fragment = _fragments.get(f);
            if (fragment == null) {
                fragment = GroupFragment.newInstance();
                _fragments.add(fragment);
            } else {
                View v = fragment.getView();
                if (v != null) {
                    ListView lv = (ListView) v.findViewById(R.id.listView);
                    if (lv != null) lv.setAdapter(null);
                }
            }
        }
        notifyDataSetChanged();
    }

    public Group getGroup(int position) {
        return _groups.get(position);
    }

    public GroupsFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public GroupFragment getItem(int position) {
        return _fragments.get(position);
    }

    @Override
    public int getCount() {
        return _groups.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return getGroup(position).Name;
    }

    @Override
    public int getIconResId(int index) {
        return 0; //ICONS[index % ICONS.length];
    }

}