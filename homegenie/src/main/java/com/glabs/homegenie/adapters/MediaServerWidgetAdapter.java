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
import com.glabs.homegenie.client.Control;
import com.glabs.homegenie.data.ModuleHolder;
import com.glabs.homegenie.util.AsyncImageDownloadTask;
import com.glabs.homegenie.widgets.MediaServerDialogFragment;
import com.glabs.homegenie.widgets.ModuleDialogFragment;

/**
 * Created by Gene on 01/02/14.
 */
public class MediaServerWidgetAdapter extends GenericWidgetAdapter {

    public MediaServerWidgetAdapter(ModuleHolder module) {
        super(module);
    }


    @Override
    public View getView(LayoutInflater inflater) {
        View v = _moduleHolder.View;
        if (v == null) {
            v = inflater.inflate(R.layout.widget_item_upnpmedia, null);
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

        title.setText(_moduleHolder.Module.getDisplayName());
        infotext.setVisibility(View.GONE);

        subtitle.setText(Control.getUpnpDisplayName(_moduleHolder.Module));
        //
        if (_moduleHolder.Module.getParameter("UPnP.StandardDeviceType") != null && !_moduleHolder.Module.getParameter("UPnP.StandardDeviceType").Value.trim().equals("")) {
            infotext.setText(_moduleHolder.Module.getParameter("UPnP.StandardDeviceType").Value);
            infotext.setVisibility(View.VISIBLE);
        }
        //
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

    @Override
    public ModuleDialogFragment getControlFragment() {
        ModuleDialogFragment fmWidget = new MediaServerDialogFragment();
        return fmWidget;
    }


}
