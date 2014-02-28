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

package com.glabs.homegenie.widgets;

import android.view.LayoutInflater;
import android.view.View;

import com.glabs.homegenie.adapters.CameraWidgetAdapter;
import com.glabs.homegenie.adapters.EarthToolsWidgetAdapter;
import com.glabs.homegenie.adapters.GenericWidgetAdapter;
import com.glabs.homegenie.adapters.MediaRendererWidgetAdapter;
import com.glabs.homegenie.adapters.MediaServerWidgetAdapter;
import com.glabs.homegenie.adapters.SecurityWidgetAdapter;
import com.glabs.homegenie.adapters.StatusWidgetAdapter;
import com.glabs.homegenie.adapters.WundergroundWidgetAdapter;
import com.glabs.homegenie.service.data.Module;
import com.glabs.homegenie.service.data.ModuleParameter;

/**
 * Created by Gene on 05/01/14.
 */
public class ModuleWidgetHolder {

    private GenericWidgetAdapter _adapter;

    public ModuleWidgetHolder(Module module) {
        ModuleParameter widgetParam = module.getParameter("Widget.DisplayModule");
        if (widgetParam != null && widgetParam.Value.equals("weather/earthtools/sundata")) {
            _adapter = new EarthToolsWidgetAdapter(module);
        } else if (widgetParam != null && widgetParam.Value.equals("weather/wunderground/conditions")) {
            _adapter = new WundergroundWidgetAdapter(module);
        } else if (widgetParam != null && widgetParam.Value.equals("homegenie/generic/securitysystem")) {
            _adapter = new SecurityWidgetAdapter(module);
        } else if (widgetParam != null && widgetParam.Value.equals("homegenie/generic/status")) {
            _adapter = new StatusWidgetAdapter(module);
        } else if (widgetParam != null && widgetParam.Value.equals("homegenie/generic/camerainput")) {
            _adapter = new CameraWidgetAdapter(module);
        } else if (widgetParam != null && widgetParam.Value.equals("homegenie/generic/mediaserver")) {
            _adapter = new MediaServerWidgetAdapter(module);
        } else if (widgetParam != null && widgetParam.Value.equals("homegenie/generic/mediareceiver")) {
            _adapter = new MediaRendererWidgetAdapter(module);
        } else {
            _adapter = new GenericWidgetAdapter(module);
        }
    }

    public View getView(LayoutInflater inflater) {

        return _adapter.getView(inflater);

    }

    public void renderView() {
        _adapter.updateViewModel();
    }

}
