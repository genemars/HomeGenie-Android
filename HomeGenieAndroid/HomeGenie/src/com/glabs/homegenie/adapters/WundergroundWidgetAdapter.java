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

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.glabs.homegenie.R;
import com.glabs.homegenie.client.data.Module;
import com.glabs.homegenie.client.data.ModuleParameter;

import java.text.SimpleDateFormat;

/**
 * Created by Gene on 05/01/14.
 */
public class WundergroundWidgetAdapter extends GenericWidgetAdapter {

    public WundergroundWidgetAdapter(Module module) {
        super(module);
    }

    @Override
    public View getView(LayoutInflater inflater) {
        View v = _module.View;
        if (v == null) {
            v = inflater.inflate(R.layout.widget_item_wunderground, null);
            _module.View = v;
            v.setTag(_module);
        } else {
            v = _module.View;
        }
        return v;
    }

    @Override
    public void updateViewModel() {

        if (_module.View == null) return;

        TextView title = (TextView) _module.View.findViewById(R.id.titleText);
        TextView subtitle = (TextView) _module.View.findViewById(R.id.subtitleText);
        TextView infotext = (TextView) _module.View.findViewById(R.id.infoText);

        title.setText(_module.getDisplayName());
        subtitle.setText(_module.getDisplayAddress());
        infotext.setVisibility(View.GONE);
        //
        ModuleParameter sunriseParam = _module.getParameter("Astronomy.Sunrise");
        String sunrise = "";
        if (sunriseParam != null) sunrise = sunriseParam.Value;
        _updatePropertyBox(_module.View, R.id.propSunrise, "Sunrise", sunrise);
        //
        ModuleParameter sunsetParam = _module.getParameter("Astronomy.Sunset");
        String sunset = "";
        if (sunsetParam != null) sunset = sunsetParam.Value;
        _updatePropertyBox(_module.View, R.id.propSunset, "Sunset", sunset);
        //
        ModuleParameter sensorTemperature = _module.getParameter("Conditions.TemperatureC");
        String temperature = "";
        if (sensorTemperature != null)
            temperature = Module.getFormattedNumber(sensorTemperature.Value);
        _updatePropertyBox(_module.View, R.id.propTemperature, "Temp.℃", temperature);
        //
        ModuleParameter sensorPressure = _module.getParameter("Conditions.PressureMb");
        String pressure = "";
        if (sensorPressure != null) pressure = Module.getFormattedNumber(sensorPressure.Value);
        _updatePropertyBox(_module.View, R.id.propPressure, "Press.mb", pressure);
        //
        ModuleParameter sensorPrecipitations = _module.getParameter("Conditions.PrecipitationHourMetric");
        String precipitations = "";
        if (sensorPrecipitations != null)
            precipitations = Module.getFormattedNumber(sensorPrecipitations.Value);
        _updatePropertyBox(_module.View, R.id.propPrecipitations, "Precip.h/m", precipitations);
        //
        ModuleParameter condLocation = _module.getParameter("Conditions.DisplayLocation");
        String location = "";
        if (condLocation != null) location = condLocation.Value;
        TextView tv1 = (TextView) _module.View.findViewById(R.id.condLocation);
        tv1.setText(location);
        //
        ModuleParameter condDescription = _module.getParameter("Conditions.Description");
        String description = "";
        if (condDescription != null) description = condDescription.Value;
        TextView tv2 = (TextView) _module.View.findViewById(R.id.condDescription);
        tv2.setText(description);

        String updateTimestamp = "";
        if (sunriseParam != null) {
            updateTimestamp = new SimpleDateFormat("MMM y E dd - HH:mm:ss").format(sunriseParam.UpdateTime);
            infotext.setText(updateTimestamp);
            infotext.setVisibility(View.VISIBLE);
        }

        ModuleParameter iconUrl = _module.getParameter("Conditions.IconUrl");
        int imageres = 0;
        if (iconUrl != null) {
            String fname = iconUrl.Value.substring(iconUrl.Value.lastIndexOf('/') + 1);
            fname = fname.replace(".gif", "");
            if (fname.startsWith("nt_")) {
                fname = "weather_night_" + fname.replace("nt_", ""); // + ".png";
            } else {
                fname = "weather_day_" + fname; // + ".png";
            }
            imageres = _module.View.getResources().getIdentifier(fname, "drawable", _module.View.getContext().getApplicationContext().getPackageName());
        }
        final ImageView image = (ImageView) _module.View.findViewById(R.id.iconImage);
        final String timestamp = updateTimestamp;
        if (imageres > 0 && (image.getTag() == null || !image.getTag().equals(timestamp))) {
            image.setImageResource(imageres);
            image.setTag(timestamp);
        }

    }

}
