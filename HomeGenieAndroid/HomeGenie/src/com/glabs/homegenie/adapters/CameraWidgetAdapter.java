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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.glabs.homegenie.R;
import com.glabs.homegenie.client.Control;
import com.glabs.homegenie.client.data.Module;
import com.glabs.homegenie.client.data.ModuleParameter;
import com.glabs.homegenie.util.AsyncImageDownloadTask;
import com.glabs.homegenie.widgets.CameraControlActivity;

/**
 * Created by Gene on 31/01/14.
 */
public class CameraWidgetAdapter extends GenericWidgetAdapter {


    public CameraWidgetAdapter(Module module) {
        super(module);
    }


    @Override
    public View getView(LayoutInflater inflater) {
        View v = _module.View;
        if (v == null) {
            v = inflater.inflate(R.layout.widget_item_camera, null);
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
        // get Image.URL property
        ModuleParameter imageurl = _module.getParameter("Image.URL");
        //
        final ImageView image = (ImageView) _module.View.findViewById(R.id.iconImage);
        if (imageurl != null)// && !(image.getDrawable() instanceof AsyncImageDownloadTask.DownloadedDrawable))
        {
            AsyncImageDownloadTask asyncDownloadTask = new AsyncImageDownloadTask(image, false, new AsyncImageDownloadTask.ImageDownloadListener() {
                @Override
                public void imageDownloadFailed(String imageUrl) {
                }

                @Override
                public void imageDownloaded(String imageUrl, Bitmap downloadedImage) {
                }
            });
            asyncDownloadTask.download(Control.getHgBaseHttpAddress() + imageurl.Value, image);
        }

    }

    @Override
    public Intent getControlActivityIntent(Module module) {
        CameraControlActivity._module = module;
        return new Intent(_module.View.getContext(), CameraControlActivity.class);
    }

}
