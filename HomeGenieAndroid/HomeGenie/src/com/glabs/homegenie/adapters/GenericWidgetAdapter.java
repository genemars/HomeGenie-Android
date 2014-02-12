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

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.glabs.homegenie.R;
import com.glabs.homegenie.service.Control;
import com.glabs.homegenie.service.data.Module;
import com.glabs.homegenie.service.data.ModuleParameter;
import com.glabs.homegenie.util.AsyncImageDownloadTask;
import com.glabs.homegenie.widgets.ColorLightDialogFragment;
import com.glabs.homegenie.widgets.DimmerLightDialogFragment;
import com.glabs.homegenie.widgets.ModuleControlActivity;
import com.glabs.homegenie.widgets.ModuleDialogFragment;

import java.text.SimpleDateFormat;

/**
 * Created by Gene on 05/01/14.
 */
public class GenericWidgetAdapter {

    protected Module _module;

    public GenericWidgetAdapter() {
    }

    public GenericWidgetAdapter(Module module) {
        _module = module;
        module.Adapter = this;
    }

    public View getView(LayoutInflater inflater) {
        View v = _module.View;
        if (v == null)
        {
            v = inflater.inflate(R.layout.widget_item_generic, null);
            _module.View = v;
            v.setTag(_module);
        }
        else
        {
            v = _module.View;
        }
        return v;
    }

    public ModuleDialogFragment getControlFragment()
    {
        ModuleDialogFragment fmWidget = null;
        ModuleParameter widget = _module.getParameter("Widget.DisplayModule");
        Module.DeviceTypes devtype = Module.DeviceTypes.Generic;
        try
        {
            devtype = Enum.valueOf(Module.DeviceTypes.class, _module.DeviceType);
        }
        catch (Exception e)
        {
            // TODO handle exception
        }
        //
        if (widget != null && widget.Value.equals("homegenie/generic/colorlight"))
        {
            fmWidget = new ColorLightDialogFragment();
        }
        else if (devtype.equals(Module.DeviceTypes.Dimmer) || devtype.equals(Module.DeviceTypes.Siren) || devtype.equals(Module.DeviceTypes.Switch) || devtype.equals(Module.DeviceTypes.Light))
        {
            fmWidget = new DimmerLightDialogFragment();
        }

        return fmWidget;
    }


    public Intent getControlActivityIntent(Module module) {
        return null;
    }


    public void updateViewModel() {
        if (_module.View == null) return;

        View convertView = _module.View;
        Module module = _module;

        TextView title = (TextView)convertView.findViewById(R.id.titleText);
        TextView subtitle = (TextView)convertView.findViewById(R.id.subtitleText);
        TextView infotext = (TextView)convertView.findViewById(R.id.infoText);

        title.setText(module.getDisplayName());
        subtitle.setText(module.getDisplayAddress());
        infotext.setVisibility(View.INVISIBLE);
        //
        String status = "";
        String updateTimestamp = "";
        ModuleParameter statusLevel = module.getParameter("Status.Level");
        if (statusLevel != null && statusLevel.Value != null && !statusLevel.Value.equals(""))
        {
            if (!statusLevel.Value.equals("0"))
            {
                status = "on";
                try
                {
                    Integer level = (int)(Double.parseDouble(statusLevel.Value) * 100);
                    if (level < 100)
                    {
                        status = level.toString() + "%";
                    }
                } catch (NumberFormatException nfe) {
                }
            }
            else
            {
                status = "off";
            }
            if (statusLevel != null)
            {
                updateTimestamp = new SimpleDateFormat("MMM y E dd - HH:mm:ss").format(statusLevel.UpdateTime);
                infotext.setText(updateTimestamp);
                infotext.setVisibility(View.VISIBLE);
            }
        }
        _updatePropertyBox(convertView, R.id.propLevel, "Status", status.toUpperCase());
        //
        View colorBox = convertView.findViewById(R.id.propColorHsb);
        colorBox.setVisibility(View.GONE);
        ModuleParameter statusColor = module.getParameter("Status.ColorHsb");
        if (statusColor != null && !statusColor.Value.equals(""))
        {
            try
            {
                String[] sHsb = statusColor.Value.split(",");
                float[] hsb = new float[3];
                hsb[0] = Float.parseFloat(sHsb[0]) * 360f;
                hsb[1] = Float.parseFloat(sHsb[1]);
                hsb[2] = Float.parseFloat(sHsb[2]);
                View colorView = colorBox.findViewById(R.id.propColor);
                colorView.setBackgroundColor(Color.HSVToColor(hsb));
                //
                colorBox.setVisibility(View.VISIBLE);
            } catch (Exception e) { }
        }
        //
        ModuleParameter doorwindowProp = module.getParameter("Sensor.DoorWindow");
        if (doorwindowProp != null && doorwindowProp.Value != null)
        {
            updateTimestamp = new SimpleDateFormat("MMM y E dd - HH:mm:ss").format(doorwindowProp.UpdateTime);
            infotext.setText(updateTimestamp);
            infotext.setVisibility(View.VISIBLE);
        }
        //
        ModuleParameter meterWatts = module.getParameter("Meter.Watts");
        String watts = "";
        if (meterWatts != null) watts = Module.getFormattedNumber(meterWatts.Value);
        _updatePropertyBox(convertView, R.id.propWatt, "Watt", watts);
        //
        ModuleParameter statusBattery = module.getParameter("Status.Battery");
        String battery = "";
        if (statusBattery != null) battery = Module.getFormattedNumber(statusBattery.Value);
        _updatePropertyBox(convertView, R.id.propBattery, "Bat.%", battery);
        //
        ModuleParameter sensorTemperature = module.getParameter("Sensor.Temperature");
        String temperature = "";
        if (sensorTemperature != null) temperature = Module.getFormattedNumber(sensorTemperature.Value);
        _updatePropertyBox(convertView, R.id.propTemperature, "Temp.℃", temperature);
        //
        ModuleParameter sensorHumidity = module.getParameter("Sensor.Humidity");
        String humidity = "";
        if (sensorHumidity != null) humidity = Module.getFormattedNumber(sensorHumidity.Value);
        _updatePropertyBox(convertView, R.id.propHumidity, "Hum.%", humidity);
        //
        ModuleParameter sensorLuminance = module.getParameter("Sensor.Luminance");
        String luminance = "";
        if (sensorLuminance != null) luminance = Module.getFormattedNumber(sensorLuminance.Value);
        _updatePropertyBox(convertView, R.id.propLuminance, "Lum.%", luminance);
        //
        ModuleParameter sensorDoorWindow = module.getParameter("Sensor.DoorWindow");
        String doorwindow = "";
        if (sensorDoorWindow != null && !sensorDoorWindow.Value.equals(""))
        {
            doorwindow = sensorDoorWindow.Value;
            if (doorwindow != null && !doorwindow.equals(""))
            {
                double dw = module.getDoubleValue(doorwindow);
                if (dw > 0)
                    doorwindow = "OPENED";
                else
                    doorwindow = "CLOSED";
            }
        }
        _updatePropertyBox(convertView, R.id.propDoorWindow, "Status", doorwindow, 12);
        //
        ModuleParameter sensorMotion = module.getParameter("Sensor.MotionDetect");
        String motiondetected = "";
        if (sensorMotion != null) motiondetected = Module.getDisplayLevel(sensorMotion.Value);
        _updatePropertyBox(convertView, R.id.propMotionDetect, "Motion", motiondetected);
        //
        final ImageView image = (ImageView)convertView.findViewById(R.id.iconImage);
        final String timestamp = updateTimestamp;
        if (image.getTag() == null || !image.getTag().equals(timestamp) && !(image.getDrawable() instanceof AsyncImageDownloadTask.DownloadedDrawable))
        {
            AsyncImageDownloadTask asyncDownloadTask = new AsyncImageDownloadTask(image, true, new AsyncImageDownloadTask.ImageDownloadListener() {
                @Override
                public void imageDownloadFailed(String imageUrl) {
                }
                @Override
                public void imageDownloaded(String imageUrl, Bitmap downloadedImage) {
                    image.setTag(timestamp);
                }
            });
            asyncDownloadTask.download(Control.getHgBaseHttpAddress() + getModuleIcon(module), image);
            //image.setTag(asyncDownloadTask);
        }

    }


    public static String getModuleIcon(Module module)
    {
        ModuleParameter widgetProp = module.getParameter("Widget.DisplayModule");
        ModuleParameter statusLevel = module.getParameter("Status.Level");
        ModuleParameter doorwindowProp = module.getParameter("Sensor.DoorWindow");
        //
        // collects module fields from module.Properties
        //
        double level = 0;
        String statussuffix = "";
        String widget = "";
        double doorwindow = 0;
        if (widgetProp != null && widgetProp.Value != null)
        {
            widget = widgetProp.Value;
        }
        if (doorwindowProp != null && doorwindowProp.Value != null)
        {
            try
            {
                doorwindow = Double.parseDouble(doorwindowProp.Value);
            } catch (Exception e) { }
        }
        if (statusLevel != null && statusLevel.Value != null)
        {
            try
            {
                level = Double.parseDouble(statusLevel.Value);
            } catch (Exception e) { }
            if (level != 0D)
            {
                statussuffix = "on";
            }
            else
            {
                statussuffix = "off";
            }
        }

        String imageurl = "/hg/html/pages/control/widgets/homegenie/generic/images/program.png";
        if (!widget.equals(""))
        {

            if (widget.equals("weather/earthtools/sundata"))
            {
                imageurl = "/hg/html/pages/control/widgets/weather/earthtools/images/earthtools.png";
            }
            else if (widget.equals("weather/wunderground/conditions"))
            {
                ModuleParameter weathericon = null;
//                    try { weathericon = Properties.First(mp => mp.Name == "Conditions.IconUrl"); }
//                    catch { }
/*
                if (weathericon != null)
                {
                    string fname = weathericon.Value.Substring(weathericon.Value.LastIndexOf('/') + 1);
                    fname = fname.Replace(".gif", "");
                    if (fname.StartsWith("nt_"))
                    {
                        fname = "/Assets/WeatherIcons/night/" + fname.Replace("nt_", "") + ".png";
                    }
                    else
                    {
                        fname = "/Assets/WeatherIcons/day/" + fname + ".png";
                    }
                    imageurl = fname;
                }
*/

            }
            else if (widget.equals("homegenie/generic/status"))
            {
                imageurl = "/hg/html/pages/control/widgets/homegenie/generic/images/status.png";
            }
            else if (widget.equals("homegenie/generic/mediaserver"))
            {
                imageurl = "/hg/html/pages/control/widgets/homegenie/generic/images/media.png";
            }
            else if (widget.equals("homegenie/generic/mediareceiver"))
            {
                imageurl = "/hg/html/pages/control/widgets/homegenie/generic/images/media_receiver.png";
            }
            else if (widget.equals("homegenie/generic/sensor"))
            {
                imageurl = "/hg/html/pages/control/widgets/homegenie/generic/images/sensor.png";
            }
            else if (widget.equals("homegenie/generic/doorwindow"))
            {
                if (level != 0 || doorwindow != 0)
                    imageurl = "/hg/html/pages/control/widgets/homegenie/generic/images/door_open.png";
                else
                    imageurl = "/hg/html/pages/control/widgets/homegenie/generic/images/door_closed.png";
            }
            else if (widget.equals("homegenie/generic/siren"))
            {
                imageurl = "/hg/html/pages/control/widgets/homegenie/generic/images/siren.png";
            }
            else if (widget.equals("homegenie/generic/temperature"))
            {
                imageurl = "/hg/html/pages/control/widgets/homegenie/generic/images/temperature.png";
            }
            else if (widget.equals("homegenie/generic/securitysystem"))
            {
                imageurl = "/hg/html/pages/control/widgets/homegenie/generic/images/securitysystem.png";
            }
            else if (widget.equals("homegenie/generic/switch"))
            {
                imageurl = "/hg/html/pages/control/widgets/homegenie/generic/images/socket_" + statussuffix + ".png";
            }
            else if (widget.equals("homegenie/generic/light") || widget.equals("homegenie/generic/dimmer") || widget.equals("homegenie/generic/colorlight"))
            {
                imageurl = "/hg/html/pages/control/widgets/homegenie/generic/images/light_" + statussuffix + ".png";
            }
            else
            {
                //
            }
        }
        else
        {
            // no widget specified, device type fallback
            Module.DeviceTypes type = Module.DeviceTypes.Switch;
            try
            {
                type = Enum.valueOf(Module.DeviceTypes.class, module.DeviceType);
            } catch (Exception e) { }
            //
            switch (type)
            {
                case Light:
                case Dimmer:
                    imageurl = "/hg/html/pages/control/widgets/homegenie/generic/images/light_" + statussuffix + ".png";
                    break;
                case Switch:
                    imageurl = "/hg/html/pages/control/widgets/homegenie/generic/images/socket_" + statussuffix + ".png";
                    break;
                case Sensor:
                    imageurl = "/hg/html/pages/control/widgets/homegenie/generic/images/sensor.png";
                    break;
                case DoorWindow:
                    if (statussuffix == "on") statussuffix = "open"; else statussuffix = "closed";
                    imageurl = "/hg/html/pages/control/widgets/homegenie/generic/images/door_" + statussuffix + ".png";
                    break;
                case Thermostat:
                case Temperature:
                    imageurl = "/hg/html/pages/control/widgets/homegenie/generic/images/temperature.png";
                    break;
                case Siren:
                    imageurl = "/hg/html/pages/control/widgets/homegenie/generic/images/siren.png";
                    break;
                case Fan:
                    imageurl = "/hg/html/pages/control/widgets/homegenie/generic/images/fan.png";
                    break;
                case Shutter:
                    if (statussuffix == "on") statussuffix = "open"; else statussuffix = "closed";
                    imageurl = "/hg/html/pages/control/widgets/homegenie/generic/images/shutters_" + statussuffix + ".png";
                    break;
            }
        }

/*
        <select id="module_type" onchange="HG.WebApp.GroupModules.UpdateModuleType($(this).val())">
        <option value="">(select type)</option>
        <option data-locale-id="configure_module_typelight" value="Light">Light</option>
        <option data-locale-id="configure_module_typedimmer" value="Dimmer">Dimmer</option>
        <option data-locale-id="configure_module_typeswitch" value="Switch">Switch</option>
        <option data-locale-id="configure_module_typesensor" value="Sensor">Sensor</option>
        <option data-locale-id="configure_module_typetemperature" value="Temperature">Temperature</option>
        <option data-locale-id="configure_module_typesiren" value="Siren">Siren</option>
        <option data-locale-id="configure_module_typefan" value="Fan">Fan</option>
        <option data-locale-id="configure_module_typethermostat" value="Thermostat">Thermostat</option>
        <option data-locale-id="configure_module_typeshutter" value="Shutter">Shutter</option>
        <option data-locale-id="configure_module_typedoorwindow" value="DoorWindow">Door/Window</option>
        </select>*/

        return imageurl;
    }



    protected void _updatePropertyBox(View convertView, int boxResId, String label, String value) {
        _updatePropertyBox(convertView, boxResId, label, value, 0);
    }

    protected void _updatePropertyBox(View convertView, int boxResId, String label, String value, float fontsize) {
        View propBox = convertView.findViewById(boxResId);
        if (value == null || value.equals(""))
        {
            propBox.setVisibility(View.GONE);
        }
        else
        {
            TextView propLabel = (TextView)propBox.findViewById(R.id.propLabel);
            propLabel.setText(label);
            TextView propValue = (TextView)propBox.findViewById(R.id.propValue);
            if (fontsize != 0) propValue.setTextSize(fontsize);
            propValue.setText(value);
        }
    }


}
