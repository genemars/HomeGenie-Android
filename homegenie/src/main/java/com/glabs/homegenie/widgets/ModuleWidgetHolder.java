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
import com.glabs.homegenie.adapters.OpenWeatherWidgetAdapter;
import com.glabs.homegenie.adapters.SecurityWidgetAdapter;
import com.glabs.homegenie.adapters.SolarAltitudeWidgetAdapter;
import com.glabs.homegenie.adapters.StatusWidgetAdapter;
import com.glabs.homegenie.adapters.WundergroundWidgetAdapter;
import com.glabs.homegenie.client.data.Module;
import com.glabs.homegenie.client.data.ModuleParameter;
import com.glabs.homegenie.data.ModuleHolder;

/**
 * Created by Gene on 05/01/14.
 */
public class ModuleWidgetHolder {

    private GenericWidgetAdapter _adapter;

    public ModuleWidgetHolder(ModuleHolder moduleHolder) {
        ModuleParameter widgetParam = moduleHolder.Module.getParameter("Widget.DisplayModule");
        if (widgetParam != null && widgetParam.Value.equals("weather/earthtools/sundata")) {
            _adapter = new EarthToolsWidgetAdapter(moduleHolder);
        } else if (widgetParam != null && widgetParam.Value.equals("jkUtils/SolarAltitude/SolarAltitude")) {
            _adapter = new SolarAltitudeWidgetAdapter(moduleHolder);
        } else if (widgetParam != null && widgetParam.Value.equals("jkUtils/OpenWeatherMap/OpenWeatherMap")) {
            _adapter = new OpenWeatherWidgetAdapter(moduleHolder);
        } else if (widgetParam != null && widgetParam.Value.equals("weather/wunderground/conditions")) {
            _adapter = new WundergroundWidgetAdapter(moduleHolder);
        } else if (widgetParam != null && widgetParam.Value.equals("homegenie/generic/securitysystem")) {
            _adapter = new SecurityWidgetAdapter(moduleHolder);
        } else if (widgetParam != null && widgetParam.Value.equals("homegenie/generic/status")) {
            _adapter = new StatusWidgetAdapter(moduleHolder);
        } else if (widgetParam != null && widgetParam.Value.equals("homegenie/generic/camerainput")) {
            _adapter = new CameraWidgetAdapter(moduleHolder);
        } else if (widgetParam != null && widgetParam.Value.equals("homegenie/generic/mediaserver")) {
            _adapter = new MediaServerWidgetAdapter(moduleHolder);
        } else if (widgetParam != null && widgetParam.Value.equals("homegenie/generic/mediareceiver")) {
            _adapter = new MediaRendererWidgetAdapter(moduleHolder);
        } else {
            _adapter = new GenericWidgetAdapter(moduleHolder);
        }
    }

    public View getView(LayoutInflater inflater) {
        return _adapter.getView(inflater);
    }

    public void renderView() {
        _adapter.updateViewModel();
    }

}
