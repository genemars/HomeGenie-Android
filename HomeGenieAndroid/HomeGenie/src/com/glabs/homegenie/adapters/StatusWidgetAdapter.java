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
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.glabs.homegenie.R;
import com.glabs.homegenie.components.FlowLayout;
import com.glabs.homegenie.service.Control;
import com.glabs.homegenie.service.data.Module;
import com.glabs.homegenie.service.data.ModuleParameter;
import com.glabs.homegenie.util.AsyncImageDownloadTask;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Gene on 31/01/14.
 */
public class StatusWidgetAdapter extends GenericWidgetAdapter {

    public StatusWidgetAdapter(Module module) {
        super(module);
    }


    @Override
    public View getView(LayoutInflater inflater) {
        View v = _module.View;
        if (v == null) {
            v = inflater.inflate(R.layout.widget_item_status, null);
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
        FlowLayout propscontainer = (FlowLayout) _module.View.findViewById(R.id.propsContainer);

        title.setText(_module.getDisplayName());
        subtitle.setText(_module.getDisplayAddress());
        infotext.setVisibility(View.GONE);
        //
        Date lastUpdate = null;
        LayoutInflater inflater = (LayoutInflater) _module.View.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (ModuleParameter p : _module.Properties) {
            if (p.Name.startsWith("StatusWidget.")) {
                View propView = null;
                for (int c = 0; c < propscontainer.getChildCount(); c++) {
                    Object tag = propscontainer.getChildAt(c).getTag();
                    if (tag != null && tag.equals(p.Name)) {
                        propView = propscontainer.getChildAt(c);
                        break;
                    }
                }
                if (propView == null) {
                    propView = inflater.inflate(R.layout.widget_fragment_property_small, null);
                    propView.setTag(p.Name);
                    propscontainer.addView(propView);
                }
                TextView pname = (TextView) propView.findViewById(R.id.propLabel);
                pname.setText(p.Name.replace("StatusWidget.", ""));
                TextView ptext = (TextView) propView.findViewById(R.id.propValue);
                ptext.setText(p.Value);
                //
                if (lastUpdate == null || lastUpdate.getTime() < p.UpdateTime.getTime()) {
                    lastUpdate = p.UpdateTime;
                }
            }
        }

        if (lastUpdate != null) {
            String updateTimestamp;
            updateTimestamp = new SimpleDateFormat("MMM y E dd - HH:mm:ss").format(lastUpdate);
            infotext.setText(updateTimestamp);
            infotext.setVisibility(View.VISIBLE);
        }

        final ImageView image = (ImageView) _module.View.findViewById(R.id.iconImage);
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
            asyncDownloadTask.download(Control.getHgBaseHttpAddress() + getModuleIcon(_module), image);
        }

    }

}
