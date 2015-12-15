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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.glabs.homegenie.R;
import com.glabs.homegenie.adapters.MediaFilesAdapter;
import com.glabs.homegenie.client.Control;
import com.glabs.homegenie.client.data.Module;
import com.glabs.homegenie.client.data.ModuleParameter;
import com.glabs.homegenie.data.MediaEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;


/**
 * Created by Gene on 01/02/14.
 */
public class MediaServerDialogFragment extends ModuleDialogFragment {

    private final String PARENT_FOLDER = "<< parent folder";
    private View _view;
    private AlertDialog _dialog;
    private MediaFilesAdapter mAdapter;
    private static LinkedList<String> navigationStack = new LinkedList<String>();
    private ArrayList<Module> renderers = new ArrayList<Module>();
    private MediaEntry _selectedMedia = null;
    private Module _selectedMediaRender = null;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        _view = getActivity().getLayoutInflater().inflate(R.layout.widget_control_mediaserver, null);
        builder.setView(_view);

        builder.setMessage(_module.getDisplayName())
                .setPositiveButton("Play", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Play to...
                        playMediaTo();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        mAdapter = new MediaFilesAdapter(_view.getContext(), R.layout.widget_control_mediaserver_item, new ArrayList<MediaEntry>());
        final ListView lv = (ListView) _view.findViewById(R.id.filesList);
        lv.setAdapter(mAdapter);
        //
        if (navigationStack.size() == 0) {
            navigationStack.add("0");
        }
        //
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MediaEntry clicked = mAdapter.getItem(i);
                mAdapter.setSelectedIndex(i);
                _selectedMedia = null;
                //
                if (clicked.Class.indexOf("object.container") == 0) {
                    // browse to folder
                    _dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    if (clicked.Title.equals(PARENT_FOLDER)) {
                        navigationStack.removeLast();
                    } else {
                        navigationStack.add(clicked.Id);
                    }
                    browseMediaFolder(lv);
                } else {
                    // set current media file
                    _selectedMedia = clicked;
                    _dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });
        //

        Control.getGroupModules("", new Control.GroupModulesRequestCallback() {
            @Override
            public void onRequestSuccess(ArrayList<Module> modules) {
                ArrayAdapter<CharSequence> playtoitems = new ArrayAdapter<CharSequence>(_view.getContext(), android.R.layout.simple_spinner_item);
                //
                playtoitems.add("This device");
                //
                for (int m = 0; m < modules.size(); m++) {
                    Module module = modules.get(m);
                    ModuleParameter devtype = module.getParameter("UPnP.StandardDeviceType");
                    if (devtype != null && devtype.Value.equals("MediaRenderer")) {
                        renderers.add(module);
                        playtoitems.add(Control.getUpnpDisplayName(module));
                    }
                }
                //
                // Media Renderer Spinner Select
                Spinner playto = (Spinner) _view.findViewById(R.id.playto);
                playto.setAdapter(playtoitems);
                playto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        _selectedMediaRender = null;
                        if (i > 0) {
                            _selectedMediaRender = renderers.get(i - 1);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

            }

            @Override
            public void onRequestError(Control.ApiRequestResult apiRequestResult) {

            }
        });

        browseMediaFolder(lv);

        // Create the AlertDialog object and return it
        _dialog = builder.create();
        _dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                _dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        });
        return _dialog;
    }

    @Override
    public void refreshView() {
//        super.refreshView();
    }

    private void playMediaTo() {
        // _selectedMedia
        String apicall = _module.Domain + "/" + _module.Address + "/AvMedia.GetUri/" + Uri.encode(_selectedMedia.Id) + "/";
        Control.apiRequest(apicall, new Control.ApiRequestCallback() {
            @Override
            public void onRequestSuccess(Control.ApiRequestResult apiRequestResult) {
                String mediauri = apiRequestResult.ResponseBody;
                try {
                    JSONObject jsonResponse = new JSONObject(apiRequestResult.ResponseBody);
                    mediauri = jsonResponse.getString("ResponseValue");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // send to currently selected media renderer
                if (_selectedMediaRender == null) // send to this device
                {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(mediauri));
                    if (mediauri.endsWith(".mp3") ||
                            mediauri.endsWith(".m4a") ||
                            mediauri.endsWith(".wav")) {
                        i.setType("audio/*");
                    } else if (mediauri.endsWith(".jpeg") ||
                            mediauri.endsWith(".jpg") ||
                            mediauri.endsWith(".png") ||
                            mediauri.endsWith(".gif") ||
                            mediauri.endsWith(".tiff")) {
                        // use default browser, internal image gallery won't work for external files
                        //i.setType("image/*");
                    } else if (mediauri.endsWith(".m4v") ||
                            mediauri.endsWith(".3gp") ||
                            mediauri.endsWith(".wmv") ||
                            mediauri.endsWith(".mp4") ||
                            mediauri.endsWith(".mpeg") ||
                            mediauri.endsWith(".mpg") ||
                            mediauri.endsWith(".avi") ||
                            mediauri.endsWith(".ogg")) {
                        i.setType("video/*");
                    }
                    try {
                        _view.getContext().startActivity(i);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    String apicall;
                    apicall = _selectedMediaRender.Domain + "/" + _selectedMediaRender.Address + "/AvMedia.SetUri/" + Uri.encode(mediauri) + "/";
                    Control.apiRequest(apicall, new Control.ApiRequestCallback() {
                        @Override
                        public void onRequestSuccess(Control.ApiRequestResult apiRequestResult) {
                            // should play and popup current renderer controls dialog
                            Control.apiRequest(_selectedMediaRender.Domain + "/" + _selectedMediaRender.Address + "/AvMedia.Play/", null);
                        }
                        @Override
                        public void onRequestError(Control.ApiRequestResult apiRequestResult) {

                        }
                    });
                }
            }

            @Override
            public void onRequestError(Control.ApiRequestResult apiRequestResult) {

            }
        });

    }


    private void browseMediaFolder(final ListView lv) {
        String apicall = _module.Domain + "/" + _module.Address + "/AvMedia.Browse/" + Uri.encode(navigationStack.getLast()) + "/";
        Control.apiRequest(apicall, new Control.ApiRequestCallback() {
            @Override
            public void onRequestSuccess(Control.ApiRequestResult apiRequestResult) {

                String jsonString = apiRequestResult.ResponseBody;
                if (jsonString == null || jsonString.equals("")) return;
                //
                ArrayList<MediaEntry> entries = new ArrayList<MediaEntry>();
                if (navigationStack.size() > 1) {
                    MediaEntry prevfolder = new MediaEntry();
                    prevfolder.Id = navigationStack.get(navigationStack.size() - 2);
                    prevfolder.Title = PARENT_FOLDER;
                    prevfolder.Class = "object.container";
                    entries.add(prevfolder);
                }
                try {
                    JSONArray jitems = new JSONArray(jsonString);
                    for (int g = 0; g < jitems.length(); g++) {
                        JSONObject jg = (JSONObject) jitems.get(g);
                        MediaEntry entry = new MediaEntry();
                        entry.Id = jg.getString("Id");
                        entry.Title = jg.getString("Title");
                        entry.Class = jg.getString("Class");
                        entries.add(entry);

                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                mAdapter.setEntries(entries);
            }
            @Override
            public void onRequestError(Control.ApiRequestResult apiRequestResult) {

            }
        });
    }

}
