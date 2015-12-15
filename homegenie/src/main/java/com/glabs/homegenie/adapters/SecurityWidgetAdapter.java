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
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.glabs.homegenie.R;
import com.glabs.homegenie.client.Control;
import com.glabs.homegenie.client.data.ModuleParameter;
import com.glabs.homegenie.data.ModuleHolder;
import com.glabs.homegenie.util.AsyncImageDownloadTask;

/**
 * Created by Gene on 05/01/14.
 */
public class SecurityWidgetAdapter extends GenericWidgetAdapter {

    public SecurityWidgetAdapter(ModuleHolder module) {
        super(module);
    }

    @Override
    public View getView(LayoutInflater inflater) {
        View v = _moduleHolder.View;
        if (v == null) {
            v = inflater.inflate(R.layout.widget_item_security, null);
            _moduleHolder.View = v;
            v.setTag(_moduleHolder);
        } else {
            v = _moduleHolder.View;
        }
        return v;
    }

    @Override
    public void updateViewModel() {

        if (_moduleHolder.View == null) return;

        TextView title = (TextView) _moduleHolder.View.findViewById(R.id.titleText);
        TextView subtitle = (TextView) _moduleHolder.View.findViewById(R.id.subtitleText);
        TextView infotext = (TextView) _moduleHolder.View.findViewById(R.id.infoText);
        ToggleButton armbutton = (ToggleButton) _moduleHolder.View.findViewById(R.id.armDisarm);

        title.setText(_moduleHolder.Module.getDisplayName());
        subtitle.setText(_moduleHolder.Module.getDisplayAddress());
        infotext.setVisibility(View.GONE);
        //
        String updateTimestamp;
        ModuleParameter statusLevel = _moduleHolder.Module.getParameter("Status.Level");
        if (statusLevel != null && statusLevel.Value != null && !statusLevel.Value.equals("")) {
            ///updateTimestamp = new SimpleDateFormat("MMM y E dd - HH:mm:ss").format(statusLevel.UpdateTime);
            updateTimestamp = DateFormat.getDateFormat(_moduleHolder.View.getContext()).format(statusLevel.UpdateTime) + " " +
                    DateFormat.getTimeFormat(_moduleHolder.View.getContext()).format(statusLevel.UpdateTime);

            infotext.setText(updateTimestamp);
            infotext.setVisibility(View.VISIBLE);
            if (statusLevel.Value.equals("1")) {
                armbutton.setChecked(true);
            } else {
                armbutton.setChecked(false);
            }
        }

        String armedstat = "";
        ModuleParameter armedStatus = _moduleHolder.Module.getParameter("HomeGenie.SecurityArmed");
        if (armedStatus != null && armedStatus.Value != null && !armedStatus.Value.equals("")) {
            armedstat = (armedStatus.Value.equals("1") ? "Armed" : (statusLevel.Value.equals("1") ? "Arming" : "Disarmed"));
        }
        _updatePropertyBox(_moduleHolder.View, R.id.propArmedStatus, "Status", armedstat);

        armbutton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean armed) {
                _moduleHolder.Module.setParameter("Status.Level", armed ? "1" : "0");
                //refreshView();
                _moduleHolder.Module.control("Control." + (armed ? "On" : "Off"), null);
            }
        });

        final ImageView image = (ImageView) _moduleHolder.View.findViewById(R.id.iconImage);
        if (image.getTag() == null && !(image.getDrawable() instanceof AsyncImageDownloadTask.DownloadedDrawable)) {
            AsyncImageDownloadTask asyncDownloadTask = new AsyncImageDownloadTask(image, true, new AsyncImageDownloadTask.ImageDownloadListener() {
                @Override
                public void imageDownloadFailed(String imageUrl) {
                }

                @Override
                public void imageDownloaded(String imageUrl, Bitmap downloadedImage) {
                    image.setTag("CACHED");
                }
            });
            asyncDownloadTask.download(Control.getHgBaseHttpAddress() + getModuleIcon(_moduleHolder.Module), image);
        }

    }

}
