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

package com.glabs.homegenie.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;

import com.glabs.homegenie.StartActivity;
import com.glabs.homegenie.client.Control;
import com.glabs.homegenie.fragments.SettingsFragment;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.android.AndroidUpnpServiceImpl;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.registry.RegistryListener;

/**
 * Created by Gene on 10/01/14.
 */
public class UpnpManager {

    private StartActivity _hgcontext;
    private AndroidUpnpService upnpService;
    private RegistryListener registryListener = new BrowseRegistryListener();

    public UpnpManager(StartActivity hgref) {
        _hgcontext = hgref;

    }

    public void bind() {
        // TODO test UPnP discovery
        _hgcontext.getApplicationContext().bindService(
                new Intent(_hgcontext, AndroidUpnpServiceImpl.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );
    }

    public void unbind() {
        // TODO test UPnP unbind
        if (upnpService != null) {
            upnpService.getRegistry().removeListener(registryListener);
        }
        _hgcontext.getApplicationContext().unbindService(serviceConnection);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
//            Toast.makeText(_hgcontext.getApplicationContext(), "UPnP service active.", 2000).show();

            try {
                upnpService = (AndroidUpnpService) service;

                if (upnpService.getRegistry() != null) {
                    // Refresh the list with all known devices
                    //listAdapter.clear();
                    for (Device device : upnpService.getRegistry().getDevices()) {
                        //registryListener.deviceAdded(device);
//                        Toast.makeText(_hgcontext.getApplicationContext(), device.getDisplayString(), 2000).show();
                    }

                    // Getting ready for future device advertisements
                    upnpService.getRegistry().addListener(registryListener);
                }

                if (upnpService.getControlPoint() != null) {
                    // Search asynchronously for all devices
                    upnpService.getControlPoint().search();
                } else {
//                    Toast.makeText(_hgcontext.getApplicationContext(), "UPnP initialization error: Control Point is null!", 2000).show();
                }
            } catch (Exception e) {
//                Toast.makeText(_hgcontext.getApplicationContext(), e.getMessage(), 2000).show();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };


    class BrowseRegistryListener extends DefaultRegistryListener {

        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
            _hgcontext.runOnUiThread(new Runnable() {
                public void run() {
//                    Toast.makeText(_hgcontext.getApplicationContext(), "UpNP FAILED Discovery: " + device.getDisplayString(), 2000).show();
                }
            });
            deviceRemoved(device);
        }

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            deviceRemoved(device);
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
            deviceAdded(device);
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
            deviceRemoved(device);
        }

        public void deviceAdded(final Device device) {
            _hgcontext.runOnUiThread(new Runnable() {
                public void run() {
                    /*DeviceDisplay d = new DeviceDisplay(device);
                    int position = listAdapter.getPosition(d);
                    if (position >= 0) {
                        // Device already in the list, re-set new value at same position
                        listAdapter.remove(d);
                        listAdapter.insert(d, position);
                    } else {
                        listAdapter.add(d);
                    }*/
                    // Read preferences
                    SharedPreferences settings = _hgcontext.getSharedPreferences(_hgcontext.PREFS_NAME, 0);
                    // Set HG coordinates
                    Control.setServer(
                            settings.getString("serviceAddress", "127.0.0.1"),
                            settings.getString("serviceUsername", "admin"),
                            settings.getString("servicePassword", "")
                    );

                    if (settings.getString("serviceAddress", "").equals("") && device.getDetails().getModelDetails().getModelName().equals("HomeGenie")) {
//                        Toast.makeText(_hgcontext.getApplicationContext(), "UpNP: discovered " + device.getDisplayString() + " " + device.getDetails().getPresentationURI(), 2000).show();
                        //
                        String hg_address = device.getDetails().getPresentationURI().getHost();
                        int hg_port = device.getDetails().getPresentationURI().getPort();
                        //
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("serviceAddress", hg_address + ":" + hg_port);

                        // Commit the edits!
                        editor.commit();

                        FragmentManager fm = _hgcontext.getSupportFragmentManager();
                        if (fm.findFragmentByTag("SETTINGS") != null && fm.findFragmentByTag("SETTINGS").isVisible()) {
                            ((SettingsFragment) fm.findFragmentByTag("SETTINGS")).setHomeGenieAddress(hg_address + ":" + hg_port);
                        }

                    }
                }
            });
        }

        public void deviceRemoved(final Device device) {
            //runOnUiThread(new Runnable() {
            //    public void run() {
//                    Toast.makeText(getApplicationContext(), "UpNP: removed " + device.getDisplayString(), 2000).show();
            //listAdapter.remove(new DeviceDisplay(device));
            //    }
            //});
        }
    }

}
