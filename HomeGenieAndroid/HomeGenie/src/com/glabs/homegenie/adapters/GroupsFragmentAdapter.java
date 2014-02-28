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

import com.glabs.homegenie.fragments.GroupFragment;
import com.glabs.homegenie.service.data.Group;
import com.viewpagerindicator.IconPagerAdapter;

import java.util.ArrayList;

public class GroupsFragmentAdapter extends FragmentPagerAdapter implements IconPagerAdapter {
    private ArrayList<Group> _groups = new ArrayList<Group>();
    private ArrayList<GroupFragment> _fragments = new ArrayList<GroupFragment>();

    public void setGroups(ArrayList<Group> groups) {
        boolean changed = false;
        //this._groups = groups;
        //this._fragments.clear();
        for (Group g : groups) {
            boolean exists = false;
            for (Group eg : _groups) {
                if (g.Name.equals(eg.Name)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                changed = true;
                _groups.add(_groups.size(), g);
                _fragments.add(new GroupFragment());
            }
        }
        if (changed) notifyDataSetChanged();

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

    public ArrayList<Group> getGroups() {
        return _groups;
    }
}