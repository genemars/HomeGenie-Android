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

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.glabs.homegenie.R;
import com.glabs.homegenie.service.data.ModuleParameter;

import afzkl.development.colorpickerview.view.ColorPickerView;

/**
 * Created by Dani on 22/02/14.
 */
public class ColorLightRGBDialogFragment extends ModuleDialogFragment {

    private View _view;
    private TextView _groupText;
    private TextView _levelText;
    private ColorPickerView _colorPicker;
    private View _colorPreview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /** Inflating the layout for this fragment **/
        final Fragment _this = this;

        _view = inflater.inflate(R.layout.widget_control_colorlightrgb, null);
        _groupText = (TextView) _view.findViewById(R.id.groupText);
        _levelText = (TextView) _view.findViewById(R.id.levelText);
        _colorPicker = (ColorPickerView) _view.findViewById(R.id.color_picker_view);
        _colorPreview = _view.findViewById(R.id.colorPreview);

        _colorPicker.setOnColorChangedListener(new ColorPickerView.OnColorChangedListener() {
            @Override
            public void onColorChanged(int newColor) {
                int red = Color.red(newColor);
                int green = Color.green(newColor);
                int blue = Color.blue(newColor);
                String rgbcolor = String.valueOf(red) + "," + String.valueOf(green) + "," + String.valueOf(blue);
                _module.setParameter("Status.ColorHsb", rgbcolor);
                refreshView();

            }
        });
        _colorPicker.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    String cmd = "Control.ColorHsb/" + _module.getParameter("Status.ColorHsb").Value;
                    _module.control(cmd, null);
                    return true;
                }
                return false;
            }
        });

        Button prg6Button = (Button) _view.findViewById(R.id.prg6Button);
        prg6Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _module.setParameter("Status.Level", "100");
                refreshView();
                _module.control("Control.ProgramRGB/6", null);
            }
        });

        Button prg7Button = (Button) _view.findViewById(R.id.prg7Button);
        prg7Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _module.setParameter("Status.Level", "100");
                refreshView();
                _module.control("Control.ProgramRGB/7", null);
            }
        });

        Button prg8Button = (Button) _view.findViewById(R.id.prg8Button);
        prg8Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _module.setParameter("Status.Level", "100");
                refreshView();
                _module.control("Control.ProgramRGB/8", null);
            }
        });

        Button prg9Button = (Button) _view.findViewById(R.id.prg9Button);
        prg9Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _module.setParameter("Status.Level", "100");
                refreshView();
                _module.control("Control.ProgramRGB/9", null);
            }
        });

        Button prg10Button = (Button) _view.findViewById(R.id.prg10Button);
        prg10Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _module.setParameter("Status.Level", "100");
                refreshView();
                _module.control("Control.ProgramRGB/10", null);
            }
        });

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

        refreshView();

        return _view;
    }

    @Override
    public void refreshView() {
        super.refreshView();
        //
        _view.post(new Runnable() {
            @Override
            public void run() {
                _groupText.setText(_module.getDisplayAddress());
                ModuleParameter levelParam = _module.getParameter("Status.Level");
                if (levelParam != null)
                    _levelText.setText(_module.getDisplayLevel(levelParam.Value));
                else
                    _levelText.setText("");
                ModuleParameter colorParam = _module.getParameter("Status.ColorHsb");
                if (colorParam != null && colorParam.Value != null) {
                    try {
                        String[] sRgb = colorParam.Value.split(",");
                        int red = Integer.parseInt(sRgb[0]);
                        int green = Integer.parseInt(sRgb[1]);
                        int blue = Integer.parseInt(sRgb[2]);
                        int color = Color.rgb(red, green, blue);
                        _colorPicker.setColor(color);
                        _colorPreview.setBackgroundColor(color);
                    } catch (Exception e) {
                        // TODO parsing errors
                    }
                }
//                _levelBar.setProgress((int)Math.round(_module.getDoubleValue(levelParam.Value) * 100));
            }
        });
    }
}
