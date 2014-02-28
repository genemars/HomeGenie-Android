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

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.glabs.homegenie.R;
import com.glabs.homegenie.service.Control;
import com.glabs.homegenie.service.data.ModuleParameter;
import com.glabs.homegenie.util.AsyncImageDownloadTask;


/**
 * Created by Gene on 02/02/14.
 */
public class CameraControlActivity extends ModuleControlActivity {

    private boolean _ispaused = true;
    private ModuleParameter _imageurl;
    private ImageView _image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_control_camera);

        TextView title = (TextView) findViewById(R.id.title);
        title.setText(_module.getDisplayName());

        //
        // get Image.URL property
        _imageurl = _module.getParameter("Image.URL");
        _image = (ImageView) findViewById(R.id.image);
    }

    @Override
    public void onBackPressed() {
        this.finish();
        overridePendingTransition(R.anim.left_slide_in, R.anim.right_slide_out);
        return;
    }


    @Override
    public void onResume() {
        super.onResume();
        _ispaused = false;
        //
        if (_image != null && _imageurl != null) {
            refreshImage(Control.getHgBaseHttpAddress() + _imageurl.Value, _image);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        _ispaused = true;
    }


    private void refreshImage(final String url, final ImageView image) {
        if (_ispaused) return;
        //
        final Handler h = new Handler();
        final Runnable refresh = new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshImage(url, image);
                    }
                });
            }
        };
        AsyncImageDownloadTask asyncDownloadTask = new AsyncImageDownloadTask(image, false, new AsyncImageDownloadTask.ImageDownloadListener() {
            @Override
            public void imageDownloadFailed(String imageUrl) {
                h.postDelayed(refresh, 1000);
            }

            @Override
            public void imageDownloaded(String imageUrl, Bitmap downloadedImage) {
                h.postDelayed(refresh, 150);
            }
        });
        asyncDownloadTask.download(url, image);
    }
}
