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

package com.glabs.homegenie.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import com.glabs.homegenie.R;
import com.glabs.homegenie.StartActivity;

/**
 * Created by Gene on 05/01/14.
 */
public class ErrorDialogFragment extends DialogFragment {

    private boolean _loadgroups = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /** Inflating the layout for this fragment **/
        getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        getDialog().setTitle("Oh-oh!");

        android.view.View v = inflater.inflate(R.layout.fragment_errordialog, null);

        final ErrorDialogFragment _this = this;

        Button retry = (Button)v.findViewById(R.id.retry_button);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                _this.dismiss();

            }
        });

        Button options = (Button)v.findViewById(R.id.options_button);
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                StartActivity sa = (StartActivity)getActivity();
                //sa.showSettings();
                sa.showOptionsMenu();
                _loadgroups = false;
                _this.dismiss();

            }
        });

        return v;
    }

    @Override
    public void onStop() {
        super.onStop();

        if (_loadgroups)
        {
            StartActivity sa = (StartActivity)getActivity();
            sa.updateGroups();
        }
    }
}
