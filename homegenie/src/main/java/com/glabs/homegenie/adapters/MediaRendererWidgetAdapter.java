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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.glabs.homegenie.R;
import com.glabs.homegenie.client.Control;
import com.glabs.homegenie.data.ModuleHolder;
import com.glabs.homegenie.util.AsyncImageDownloadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Gene on 02/02/14.
 */
public class MediaRendererWidgetAdapter extends GenericWidgetAdapter {

    private String _playbackstatus = "STOPPED";
    private String _currentmute = "0";
    private String _currenturi = "n/a";
    private String _currentposition = "00:00:00 / 00:00:00";

    public MediaRendererWidgetAdapter(ModuleHolder module) {
        super(module);
    }

    @Override
    public View getView(LayoutInflater inflater) {
        View v = _moduleHolder.View;
        if (v == null) {
            v = inflater.inflate(R.layout.widget_item_upnprenderer, null);
            _moduleHolder.View = v;
            v.setTag(_moduleHolder);
            //
            Button play = (Button) v.findViewById(R.id.playButton);
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!_playbackstatus.equals("PLAYING")) {
                        _mediaPlay();
                    } else {
                        _mediaPause();
                    }
                }
            });
            Button stop = (Button) v.findViewById(R.id.stopButton);
            stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    _mediaStop();
                }
            });
            Button prev = (Button) v.findViewById(R.id.prevButton);
            prev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    _mediaPrev();
                }
            });
            Button next = (Button) v.findViewById(R.id.nextButton);
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    _mediaNext();
                }
            });
            Button mute = (Button) v.findViewById(R.id.muteButton);
            mute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (_currentmute.equals("1")) {
                        _mediaSetMute(0);
                    } else {
                        _mediaSetMute(1);
                    }
                }
            });
            SeekBar volbar = (SeekBar) v.findViewById(R.id.volumeSlider);
            volbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    _mediaSetVolume(i);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        } else {
            v = _moduleHolder.View;
        }
        return v;
    }

    @Override
    public void updateViewModel() {

        if (_moduleHolder.View == null) return;

        _updateRendererDisplayData();

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

    private void _updateRendererDisplayData() {
        final String apibase = _moduleHolder.Module.Domain + "/" + _moduleHolder.Module.Address + "/";
        Control.apiRequest(apibase + "AvMedia.GetTransportInfo", new Control.ApiRequestCallback() {
            @Override
            public void onRequestSuccess(Control.ApiRequestResult apiRequestResult) {

                try {

                    JSONObject jtinfo = new JSONObject(apiRequestResult.ResponseBody);
                    String tstate = jtinfo.getString("CurrentTransportState");
                    _playbackstatus = tstate;

                    Control.apiRequest(apibase + "AvMedia.GetVolume", new Control.ApiRequestCallback() {
                        @Override
                        public void onRequestSuccess(Control.ApiRequestResult apiRequestResult) {

                            SeekBar volume = (SeekBar) _moduleHolder.View.findViewById(R.id.volumeSlider);
                            String volumeValue = "0";
                            try {
                                JSONObject jsonResponse = new JSONObject(apiRequestResult.ResponseBody);
                                volumeValue = jsonResponse.getString("ResponseValue");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            volume.setProgress(Integer.parseInt(volumeValue));

                            Control.apiRequest(apibase + "AvMedia.GetMute", new Control.ApiRequestCallback() {
                                @Override
                                public void onRequestSuccess(Control.ApiRequestResult apiRequestResult) {

                                    String volumeMute = "False";
                                    try {
                                        JSONObject jsonResponse = new JSONObject(apiRequestResult.ResponseBody);
                                        volumeMute = jsonResponse.getString("ResponseValue");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    _currentmute = (volumeMute.toLowerCase().equals("true") ? "1" : "0");
                                    _updateControlStatus();
                                }

                                @Override
                                public void onRequestError(Control.ApiRequestResult apiRequestResult) {

                                }
                            });

                        }

                        @Override
                        public void onRequestError(Control.ApiRequestResult apiRequestResult) {

                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onRequestError(Control.ApiRequestResult apiRequestResult) {

            }
        });

        Control.apiRequest(apibase + "AvMedia.GetPositionInfo", new Control.ApiRequestCallback() {
            @Override
            public void onRequestSuccess(Control.ApiRequestResult apiRequestResult) {
                try {
                    JSONObject pinfo = new JSONObject(apiRequestResult.ResponseBody);
                    String trackuri = pinfo.getString("TrackURI");
                    String trackduration = pinfo.getString("TrackDuration");
                    String relposition = pinfo.getString("RelTime");
                    //String absposition = pinfo.getString("AbsTime");
                    //
                    _currenturi = trackuri;
                    _currentposition = relposition + " / " + trackduration;
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            @Override
            public void onRequestError(Control.ApiRequestResult apiRequestResult) {

            }
        });

    }

    private void _updateControlStatus() {
        if (_playbackstatus.equals("STOPPED")) {
            _moduleHolder.View.findViewById(R.id.playButton).setBackgroundResource(R.drawable.ic_media_play);
            _moduleHolder.View.findViewById(R.id.stopButton).setVisibility(View.GONE);
        } else if (_playbackstatus.equals("PAUSED_PLAYBACK")) {
            _moduleHolder.View.findViewById(R.id.stopButton).setVisibility(View.VISIBLE);
            _moduleHolder.View.findViewById(R.id.playButton).setBackgroundResource(R.drawable.ic_media_play);
        } else if (_playbackstatus.equals("PLAYING")) {
            _moduleHolder.View.findViewById(R.id.stopButton).setVisibility(View.VISIBLE);
            _moduleHolder.View.findViewById(R.id.playButton).setBackgroundResource(R.drawable.ic_media_pause);
        }
        //
        if (_currentmute.equals("1")) {
            _moduleHolder.View.findViewById(R.id.muteButton).setBackgroundResource(android.R.drawable.ic_lock_silent_mode);
        } else {
            _moduleHolder.View.findViewById(R.id.muteButton).setBackgroundResource(android.R.drawable.ic_lock_silent_mode_off);
        }
        //
        TextView trackuri = (TextView) _moduleHolder.View.findViewById(R.id.mediaUri);
        TextView trackpos = (TextView) _moduleHolder.View.findViewById(R.id.mediaPosition);
        trackuri.setText(_currenturi);
        trackpos.setText(_currentposition);
    }


    private void _mediaPlay() {
        _playbackstatus = "PLAYING";
        _updateControlStatus();
        String apibase = _moduleHolder.Module.Domain + "/" + _moduleHolder.Module.Address + "/";
        Control.apiRequest(apibase + "AvMedia.Play", null);
    }

    private void _mediaPause() {
        _playbackstatus = "PAUSED_PLAYBACK";
        _updateControlStatus();
        String apibase = _moduleHolder.Module.Domain + "/" + _moduleHolder.Module.Address + "/";
        Control.apiRequest(apibase + "AvMedia.Pause", null);
    }

    private void _mediaStop() {
        _playbackstatus = "STOPPED";
        _updateControlStatus();
        String apibase = _moduleHolder.Module.Domain + "/" + _moduleHolder.Module.Address + "/";
        Control.apiRequest(apibase + "AvMedia.Stop", null);
    }

    private void _mediaNext() {
        String apibase = _moduleHolder.Module.Domain + "/" + _moduleHolder.Module.Address + "/";
        Control.apiRequest(apibase + "AvMedia.Next", null);
    }

    private void _mediaPrev() {
        String apibase = _moduleHolder.Module.Domain + "/" + _moduleHolder.Module.Address + "/";
        Control.apiRequest(apibase + "AvMedia.Prev", null);
    }

    private void _mediaSetVolume(int i) {
        String apibase = _moduleHolder.Module.Domain + "/" + _moduleHolder.Module.Address + "/";
        Control.apiRequest(apibase + "AvMedia.SetVolume/" + i, null);
    }

    private void _mediaSetMute(int i) {
        _currentmute = String.valueOf(i);
        _updateControlStatus();
        String apibase = _moduleHolder.Module.Domain + "/" + _moduleHolder.Module.Address + "/";
        Control.apiRequest(apibase + "AvMedia.SetMute/" + _currentmute, null);
    }

}
