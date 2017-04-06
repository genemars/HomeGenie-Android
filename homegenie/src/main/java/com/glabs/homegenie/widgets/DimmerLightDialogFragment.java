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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.glabs.homegenie.R;
import com.glabs.homegenie.client.Control;
import com.glabs.homegenie.client.data.ModuleParameter;

/**
 * Created by Gene on 01/01/14.
 */
public class DimmerLightDialogFragment extends ModuleDialogFragment {

    private View _view;
    private TextView _groupText;
    private TextView _levelText;
    private SeekBar _levelBar;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /** Inflating the layout for this fragment **/
        final Fragment _this = this;

        _view = inflater.inflate(R.layout.widget_control_dimmerlight, null);
        _groupText = (TextView) _view.findViewById(R.id.groupText);
        _levelText = (TextView) _view.findViewById(R.id.levelText);
        _levelBar = (SeekBar) _view.findViewById(R.id.levelBar);

        Button onButton = (Button) _view.findViewById(R.id.onButton);
        onButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _module.setParameter("Status.Level", "100");
                refreshView();
                _module.control("Control.On", null);
            }
        });

        Button offButton = (Button) _view.findViewById(R.id.offButton);
        offButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _module.setParameter("Status.Level", "0");
                refreshView();
                _module.control("Control.Off", null);
            }
        });

        _levelBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
                // the following "if" is to prevent occasional null pointer exception occurring on some devices, though "_module" shouldn't be null at this point
                if (_module == null)
                    return;
                _module.setParameter("Status.Level", String.valueOf((double) seekBar.getProgress() / 100D));
                refreshView();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                _module.setParameter("Status.Level", String.valueOf((double) seekBar.getProgress() / 100D));
                refreshView();
                _module.control("Control.Level/" + seekBar.getProgress(), null);
            }
        });

        if (_module != null) {
            if (_module.DeviceType.equals("Shutter")) {
                onButton.setText("Open");
                offButton.setText("Close");
            }
            refreshView();
        }

        return _view;
    }

    @Override
    public void refreshView() {
        super.refreshView();
        //
        _view.post(new Runnable() {
            @Override
            public void run() {
                if (!_module.DeviceType.equals("Dimmer") && !_module.DeviceType.equals("Siren") && !_module.DeviceType.equals("Shutter")) {
                    _levelBar.setVisibility(View.GONE);
                }
                _groupText.setText(_module.getDisplayAddress());
                ModuleParameter levelParam = _module.getParameter("Status.Level");
                if (levelParam != null) {
                    String level = _module.getDisplayLevel(levelParam.Value);
                    if (_module.DeviceType.equals("Shutter")) {
                        if (level.equals("OFF")) {
                            level = "Closed";
                        } else if (level.equals("ON")) {
                            level = "Open";
                        }
                    }
                    _levelText.setText(level);
                    _levelBar.setProgress((int) Math.round(_module.getDoubleValue(levelParam.Value) * 100));
                } else {
                    _levelText.setText("");
                }
            }
        });
    }

}
