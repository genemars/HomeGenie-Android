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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.glabs.homegenie.R;
import com.glabs.homegenie.StartActivity;
import com.glabs.homegenie.service.Control;


/**
 * Created by Gene on 04/01/14.
 */
public class SettingsFragment extends DialogFragment {

    private final String PREFS_NAME = "HomeGenieService";
    private EditText hg_address, hg_user, hg_pass;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /** Inflating the layout for this fragment **/
        getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        getDialog().setTitle("Settings");

        View v = inflater.inflate(R.layout.fragment_settings, null);

        hg_address = (EditText)v.findViewById(R.id.hg_service_address);
        hg_user = (EditText)v.findViewById(R.id.hg_service_user);
        hg_pass = (EditText)v.findViewById(R.id.hg_service_pass);

/*        Button connect = (Button)v.findViewById(R.id.btn_connect);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });*/


        // Restore preferences
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        hg_address.setText(settings.getString("serviceAddress", "127.0.0.1"));
        hg_user.setText(settings.getString("serviceUsername", "admin"));
        hg_pass.setText(settings.getString("servicePassword", ""));

        return v;
    }

    public void setHomeGenieAddress(String address)
    {
        hg_address.setText(address);
        hg_pass.requestFocus();
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("serviceAddress", hg_address.getText().toString().trim());
        editor.putString("serviceUsername", hg_user.getText().toString().trim());
        editor.putString("servicePassword", hg_pass.getText().toString().trim());

        // Commit the edits!
        editor.commit();

        // Set HG coordinates
        Control.setHgServer(
                settings.getString("serviceAddress", ""),
                settings.getString("serviceUsername", ""),
                settings.getString("servicePassword", "")
        );

        StartActivity sa = (StartActivity)getActivity();
        sa.showLogo();
        sa.updateGroups();

    }
}
