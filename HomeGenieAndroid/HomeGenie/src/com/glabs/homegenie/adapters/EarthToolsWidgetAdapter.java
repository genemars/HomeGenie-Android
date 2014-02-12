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

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.glabs.homegenie.R;
import com.glabs.homegenie.service.Control;
import com.glabs.homegenie.service.data.Module;
import com.glabs.homegenie.service.data.ModuleParameter;
import com.glabs.homegenie.util.AsyncImageDownloadTask;

import java.text.SimpleDateFormat;

/**
 * Created by Gene on 05/01/14.
 */
public class EarthToolsWidgetAdapter extends GenericWidgetAdapter {

    public EarthToolsWidgetAdapter(Module module) {
        super(module);
    }

    @Override
    public View getView(LayoutInflater inflater) {
        View v = _module.View;
        if (v == null)
        {
            v = inflater.inflate(R.layout.widget_item_earthtools, null);
            _module.View = v;
            v.setTag(_module);
        }
        else
        {
            v = _module.View;
        }
        return v;
    }

    @Override
    public void updateViewModel() {

        if (_module.View == null) return;

        TextView title = (TextView)_module.View.findViewById(R.id.titleText);
        TextView subtitle = (TextView)_module.View.findViewById(R.id.subtitleText);
        TextView infotext = (TextView)_module.View.findViewById(R.id.infoText);

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
        ModuleParameter latParam = _module.getParameter("ConfigureOptions.Latitude");
        String latitude = "";
        if (latParam != null) latitude = Module.getFormattedNumber(latParam.Value);
        _updatePropertyBox(_module.View, R.id.propLatitude, "Lat.", latitude);
        //
        ModuleParameter longParam = _module.getParameter("ConfigureOptions.Longitude");
        String longitude = "";
        if (longParam != null) longitude = Module.getFormattedNumber(longParam.Value);
        _updatePropertyBox(_module.View, R.id.propLongitude, "Long.", longitude);
        //
        if (sunriseParam != null)
        {
            String updateTimestamp = new SimpleDateFormat("MMM y E dd - HH:mm:ss").format(sunriseParam.UpdateTime);
            infotext.setText(updateTimestamp);
            infotext.setVisibility(View.VISIBLE);
        }
        //
        final ImageView image = (ImageView)_module.View.findViewById(R.id.iconImage);
        if (image.getTag() == null && !(image.getDrawable() instanceof AsyncImageDownloadTask.DownloadedDrawable))
        {
            AsyncImageDownloadTask asyncDownloadTask = new AsyncImageDownloadTask(image, true, new AsyncImageDownloadTask.ImageDownloadListener() {
                @Override
                public void imageDownloadFailed(String imageUrl) {
                }
                @Override
                public void imageDownloaded(String imageUrl, Bitmap downloadedImage) {
                    image.setTag("CACHED");
                }
            });
            asyncDownloadTask.download(Control.getHgBaseHttpAddress() + getModuleIcon(_module), image);
        }

    }

}