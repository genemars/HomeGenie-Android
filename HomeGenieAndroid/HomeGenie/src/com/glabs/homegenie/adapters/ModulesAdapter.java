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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.glabs.homegenie.client.data.Module;
import com.glabs.homegenie.widgets.ModuleWidgetHolder;

import java.util.List;

public class ModulesAdapter extends ArrayAdapter<Module> {

    private List<Module> _modules;

    public ModulesAdapter(Context context, int resource, List<Module> objects) {
        super(context, resource, objects);
        _modules = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Module module = _modules.get(position);
        ModuleWidgetHolder widget = new ModuleWidgetHolder(module);
        convertView = widget.getView(inflater);

        widget.renderView();

        return convertView;
    }

}
