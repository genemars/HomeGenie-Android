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

import java.text.SimpleDateFormat;
import java.util.List;

import com.glabs.homegenie.R;
import com.glabs.homegenie.service.data.Module;
import com.glabs.homegenie.service.data.Module.DeviceTypes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.glabs.homegenie.service.data.ModuleParameter;
import com.glabs.homegenie.util.AsyncImageDownloadTask;
import com.glabs.homegenie.widgets.ModuleWidgetHolder;

public class ModulesAdapter extends ArrayAdapter<Module> {
	
	private List<Module> _modules;

	public ModulesAdapter(Context context, int resource, List<Module> objects) {
		super(context, resource, objects);
		_modules = objects;
	}
	
	public void setModules(List<Module> objects)
	{
		for(Module nm : objects)
		{
			boolean exists = false;
			for(Module m : _modules)
			{
				if (m.Domain.equals(nm.Domain) && m.Address.equals(nm.Address))
				{
					exists = true;
					//TODO: update properties
                    m.Properties = nm.Properties;
					break;
				}
			}
            ModuleParameter widgetParam = nm.getParameter("Widget.DisplayModule");
            String widget = "";
            if (widgetParam != null) widget = widgetParam.Value;
			if (!exists && !widget.equals("homegenie/generic/program"))
			{
				_modules.add(nm);
			}
		}
		//
		this.notifyDataSetChanged();
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
