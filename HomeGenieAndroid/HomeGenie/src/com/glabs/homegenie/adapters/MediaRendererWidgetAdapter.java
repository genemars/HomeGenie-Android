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
import com.glabs.homegenie.service.Control;
import com.glabs.homegenie.service.data.Module;
import com.glabs.homegenie.util.AsyncImageDownloadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

/**
 * Created by Gene on 02/02/14.
 */
public class MediaRendererWidgetAdapter extends GenericWidgetAdapter {


    private String _playbackstatus = "STOPPED";
    private String _currentmute = "0";
    private String _currenturi = "n/a";
    private String _currentposition = "00:00:00 / 00:00:00";

    public MediaRendererWidgetAdapter(Module module) {
        super(module);
    }


    @Override
    public View getView(LayoutInflater inflater) {
        View v = _module.View;
        if (v == null)
        {
            v = inflater.inflate(R.layout.widget_item_upnprenderer, null);
            _module.View = v;
            v.setTag(_module);
            //
            Button play = (Button)v.findViewById(R.id.playButton);
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!_playbackstatus.equals("PLAYING"))
                    {
                        _mediaPlay();
                    }
                    else
                    {
                        _mediaPause();
                    }
                }
            });
            Button stop = (Button)v.findViewById(R.id.stopButton);
            stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    _mediaStop();
                }
            });
            Button prev = (Button)v.findViewById(R.id.prevButton);
            prev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    _mediaPrev();
                }
            });
            Button next = (Button)v.findViewById(R.id.nextButton);
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    _mediaNext();
                }
            });
            Button mute = (Button)v.findViewById(R.id.muteButton);
            mute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (_currentmute.equals("1"))
                    {
                        _mediaSetMute(0);
                    }
                    else
                    {
                        _mediaSetMute(1);
                    }
                }
            });
            SeekBar volbar = (SeekBar)v.findViewById(R.id.volumeSlider);
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
        }
        else
        {
            v = _module.View;
        }
        return v;
    }

    @Override
    public void updateViewModel() {

        if (_module.View == null) return;

        _updateRendererDisplayData();

        TextView title = (TextView)_module.View.findViewById(R.id.titleText);
        TextView subtitle = (TextView)_module.View.findViewById(R.id.subtitleText);
        TextView infotext = (TextView)_module.View.findViewById(R.id.infoText);

        title.setText(_module.getDisplayName());
        infotext.setVisibility(View.GONE);

        subtitle.setText(Control.getUpnpDisplayName(_module));
        //
        if (_module.getParameter("UPnP.StandardDeviceType") != null && !_module.getParameter("UPnP.StandardDeviceType").Value.trim().equals(""))
        {
            infotext.setText(_module.getParameter("UPnP.StandardDeviceType").Value);
            infotext.setVisibility(View.VISIBLE);
        }
        //
        final ImageView image = (ImageView)_module.View.findViewById(R.id.iconImage);
        if (image.getTag() == null && !(image.getDrawable() instanceof AsyncImageDownloadTask.DownloadedDrawable))
        {
            AsyncImageDownloadTask asyncDownloadTask = new AsyncImageDownloadTask(image, true, new AsyncImageDownloadTask.ImageDownloadListener() {
                @Override
                public void imageDownloadFailed(String imageUrl) {
                }
                @Override
                public void imageDownloaded(String imageUrl, Bitmap downloadedImage) {
                    image.setTag("CACHED");
                }
            });
            asyncDownloadTask.download(Control.getHgBaseHttpAddress() + getModuleIcon(_module), image);
        }
    }


    private void _updateRendererDisplayData()
    {
        final String apibase = _module.Domain + "/" + _module.Address + "/";
        Control.callServiceApi(apibase + "AvMedia.GetTransportInfo", new Control.ServiceCallCallback() {
            @Override
            public void serviceCallCompleted(String response) {

                try {

                    JSONObject jtinfo = new JSONArray(response).getJSONObject(0);
                    String tstate = jtinfo.getString("CurrentTransportState");
                    _playbackstatus = tstate;

                    Control.callServiceApi(apibase + "AvMedia.GetVolume", new Control.ServiceCallCallback() {
                        @Override
                        public void serviceCallCompleted(String value) {

                            SeekBar volume = (SeekBar)_module.View.findViewById(R.id.volumeSlider);
                            volume.setProgress(Integer.parseInt(value));

                            Control.callServiceApi(apibase + "AvMedia.GetMute", new Control.ServiceCallCallback() {
                                @Override
                                public void serviceCallCompleted(String value) {
                                    _currentmute = (value.toLowerCase().equals("true") ? "1" : "0");
                                    _updateControlStatus();
                                }
                            });

                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });


        Control.callServiceApi(apibase + "AvMedia.GetPositionInfo", new Control.ServiceCallCallback() {
            @Override
            public void serviceCallCompleted(String response) {
                try {
                    JSONObject pinfo = new JSONArray(response).getJSONObject(0);
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
        });

    }


    private void _updateControlStatus()
    {
        if (_playbackstatus.equals("STOPPED"))
        {
            _module.View.findViewById(R.id.playButton).setBackgroundResource(R.drawable.ic_media_play);
            _module.View.findViewById(R.id.stopButton).setVisibility(View.GONE);
        }
        else if (_playbackstatus.equals("PAUSED_PLAYBACK"))
        {
            _module.View.findViewById(R.id.stopButton).setVisibility(View.VISIBLE);
            _module.View.findViewById(R.id.playButton).setBackgroundResource(R.drawable.ic_media_play);
        }
        else if (_playbackstatus.equals("PLAYING"))
        {
            _module.View.findViewById(R.id.stopButton).setVisibility(View.VISIBLE);
            _module.View.findViewById(R.id.playButton).setBackgroundResource(R.drawable.ic_media_pause);
        }
        //
        if (_currentmute.equals("1"))
        {
            _module.View.findViewById(R.id.muteButton).setBackgroundResource(android.R.drawable.ic_lock_silent_mode);
        }
        else
        {
            _module.View.findViewById(R.id.muteButton).setBackgroundResource(android.R.drawable.ic_lock_silent_mode_off);
        }
        //
        TextView trackuri = (TextView)_module.View.findViewById(R.id.mediaUri);
        TextView trackpos = (TextView)_module.View.findViewById(R.id.mediaPosition);
        trackuri.setText(_currenturi);
        trackpos.setText(_currentposition);
    }


    private void _mediaPlay()
    {
        _playbackstatus = "PLAYING";
        _updateControlStatus();
        String apibase = _module.Domain + "/" + _module.Address + "/";
        Control.callServiceApi(apibase + "AvMedia.Play", new Control.ServiceCallCallback() {
            @Override
            public void serviceCallCompleted(String value) {
            }
        });
    }
    private void _mediaPause()
    {
        _playbackstatus = "PAUSED_PLAYBACK";
        _updateControlStatus();
        String apibase = _module.Domain + "/" + _module.Address + "/";
        Control.callServiceApi(apibase + "AvMedia.Pause", new Control.ServiceCallCallback() {
            @Override
            public void serviceCallCompleted(String value) {
            }
        });
    }
    private void _mediaStop()
    {
        _playbackstatus = "STOPPED";
        _updateControlStatus();
        String apibase = _module.Domain + "/" + _module.Address + "/";
        Control.callServiceApi(apibase + "AvMedia.Stop", new Control.ServiceCallCallback() {
            @Override
            public void serviceCallCompleted(String value) {
            }
        });
    }
    private void _mediaNext()
    {
        String apibase = _module.Domain + "/" + _module.Address + "/";
        Control.callServiceApi(apibase + "AvMedia.Next", new Control.ServiceCallCallback() {
            @Override
            public void serviceCallCompleted(String value) {
            }
        });
    }
    private void _mediaPrev()
    {
        String apibase = _module.Domain + "/" + _module.Address + "/";
        Control.callServiceApi(apibase + "AvMedia.Prev", new Control.ServiceCallCallback() {
            @Override
            public void serviceCallCompleted(String value) {
            }
        });
    }

    private void _mediaSetVolume(int i) {
        String apibase = _module.Domain + "/" + _module.Address + "/";
        Control.callServiceApi(apibase + "AvMedia.SetVolume/" + i, new Control.ServiceCallCallback() {
            @Override
            public void serviceCallCompleted(String value) {
            }
        });
    }

    private void _mediaSetMute(int i) {
        _currentmute = String.valueOf(i);
        _updateControlStatus();
        String apibase = _module.Domain + "/" + _module.Address + "/";
        Control.callServiceApi(apibase + "AvMedia.SetMute/" + _currentmute, new Control.ServiceCallCallback() {
            @Override
            public void serviceCallCompleted(String value) {
            }
        });
    }

}
