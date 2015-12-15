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
import com.glabs.homegenie.data.ModuleHolder;
import com.glabs.homegenie.util.AsyncImageDownloadTask;
import com.glabs.homegenie.widgets.CameraControlActivity;

/**
 * Created by Gene on 31/01/14.
 */
public class CameraWidgetAdapter extends GenericWidgetAdapter {


    public CameraWidgetAdapter(ModuleHolder module) {
        super(module);
    }


    @Override
    public View getView(LayoutInflater inflater) {
        View v = _moduleHolder.View;
        if (v == null) {
            v = inflater.inflate(R.layout.widget_item_camera, null);
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
        TextView infoText = (TextView) _moduleHolder.View.findViewById(R.id.infoText);

        title.setText(_moduleHolder.Module.getDisplayName());
        subtitle.setText(_moduleHolder.Module.getDisplayAddress());
        infoText.setVisibility(View.GONE);
        //
        // get Image.URL property
        String imagePath = "/api/" + _moduleHolder.Module.Domain + "/" + _moduleHolder.Module.Address + "/Camera.GetPicture/";
        ModuleParameter imageUrl = _moduleHolder.Module.getParameter("Image.URL");
        if (imageUrl != null && !imageUrl.Value.equals(""))
        {
            imagePath = imageUrl.Value;
        }
        //
        final ImageView image = (ImageView) _moduleHolder.View.findViewById(R.id.iconImage);
        AsyncImageDownloadTask asyncDownloadTask = new AsyncImageDownloadTask(image, false, new AsyncImageDownloadTask.ImageDownloadListener() {
            @Override
            public void imageDownloadFailed(String imageUrl) {
            }

            @Override
            public void imageDownloaded(String imageUrl, Bitmap downloadedImage) {
            }
        });
        asyncDownloadTask.setCacheEnabled(false);
        asyncDownloadTask.download(Control.getHgBaseHttpAddress() + imagePath + System.currentTimeMillis(), image);

    }

    @Override
    public Intent getControlActivityIntent(Module module) {
        CameraControlActivity._module = module;
        return new Intent(_moduleHolder.View.getContext(), CameraControlActivity.class);
    }

}
