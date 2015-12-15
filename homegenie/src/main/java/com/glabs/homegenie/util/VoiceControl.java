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

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import com.glabs.homegenie.R;
import com.glabs.homegenie.StartActivity;
import com.glabs.homegenie.client.Control;
import com.glabs.homegenie.client.data.Group;
import com.glabs.homegenie.client.data.Module;
import com.glabs.homegenie.client.httprequest.HttpRequest;
import com.glabs.homegenie.client.httprequest.HttpRequest.HttpRequestException;

import org.apache.http.client.HttpResponseException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Gene on 09/01/14.
 */
public class VoiceControl implements RecognitionListener {


    private StartActivity _hgcontext;
    private SpeechRecognizer _recognizer;

    private String _currentInput = "";
    private LingoData _lingodata;

    private static String handleResponse(HttpRequest request) {
        if (request.code() < 200 || request.code() >= 300) {
            try {
                throw new HttpResponseException(request.code(), request.message());
            } catch (HttpResponseException e) {
                e.printStackTrace();
            }
        }
        return request.body();
    }

    class RetrieveLingoDataTask extends AsyncTask<Void, Void, String> {

        private Exception exception;

        protected String doInBackground(Void... noargs) {
            try {
                HttpRequest request = Control.getHttpGetRequest(Control.getHgBaseHttpAddress() + "hg/html/locales/" + Locale.getDefault().getLanguage() + ".lingo.json");
                String data = handleResponse(request);
                if (data.trim().equals(""))
                {
                    request = Control.getHttpGetRequest(Control.getHgBaseHttpAddress() + "hg/html/locales/en.lingo.json");
                    data = handleResponse(request);
                }
                return data;
            } catch (HttpRequestException e) {
                this.exception = e;
                return "";
            }
        }

        protected void onPostExecute(String data) {
            try {
                // Parse the data into jsonobject to get original data in form of json.
                JSONObject jobject = new JSONObject(data);
                JSONArray jtypes = jobject.getJSONArray("Types");
                JSONArray jcommands = jobject.getJSONArray("Commands");
                //
                for (int i = 0; i < jtypes.length(); i++) {
                    JSONObject type = jtypes.getJSONObject(i);
                    LingoType ltype = new LingoType();
                    ltype.Type = type.getString("Type");
                    JSONArray aliases = type.getJSONArray("Aliases");
                    for (int a = 0; a < aliases.length(); a++) {
                        ltype.Aliases.add(aliases.getString(a));
                    }
                    _lingodata.Types.add(ltype);
                }
                for (int i = 0; i < jcommands.length(); i++) {
                    JSONObject command = jcommands.getJSONObject(i);
                    LingoCommand lcmd = new LingoCommand();
                    lcmd.Command = command.getString("Command");
                    JSONArray aliases = command.getJSONArray("Aliases");
                    for (int a = 0; a < aliases.length(); a++) {
                        lcmd.Aliases.add(aliases.getString(a));
                    }
                    _lingodata.Commands.add(lcmd);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public VoiceControl(StartActivity hgactivity) {
        _hgcontext = hgactivity;

        //find out whether speech recognition is supported
        PackageManager packManager = _hgcontext.getPackageManager();
        List<ResolveInfo> intActivities = packManager.queryIntentActivities(new Intent(RecognizerIntent.ACTION_WEB_SEARCH), 0);
        if (intActivities.size() != 0) {
            // TODO ok speech recognition supported
        } else {
            //speech recognition not supported, disable button and output message
            Toast.makeText(_hgcontext, "Oops - Speech recognition not supported!", Toast.LENGTH_LONG).show();
        }

        _lingodata = new LingoData();
        new RetrieveLingoDataTask().execute();
    }


    public void startListen() {
        _recognizer = getSpeechRecognizer();
        _recognizer.setRecognitionListener(this);
        //
        //speech recognition is supported - detect user button clicks
        //start the speech recognition intent passing required data
        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //indicate package
        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        //message to display while listening
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Your wish is my command!");
        //set speech model
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //specify number of results to retrieve
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        //start listening
        //startActivityForResult(listenIntent, VR_REQUEST);
        //startActivityForResult(recognizerIntent, VR_REQUEST);
        _recognizer.startListening(recognizerIntent);
    }


    public void interpretInput(String sentence) {
        _currentInput = sentence;
        Handler hnd = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                return false;
            }
        });
        hnd.postDelayed(new Runnable() {
            @Override
            public void run() {
                _doInterpretInput();
            }
        }, 100);
    }

    private void _doInterpretInput() {
        boolean continueparsing = true;
        while (continueparsing) {
            continueparsing = false;
            //
            String command = searchCommandMatch();
            LingoMatch nextcommand = getCommandMatch();
            String type = searchTypeMatch(false);
            String group = searchGroupMatch(nextcommand.StartIndex);
            //
            if (!command.equals("") && !type.equals("")) {
                String[] types = type.split(",");
                ArrayList<Module> groupmodules = getGroupModules(group);
                //
                for (Module module : groupmodules) {
                    for (int t = 0; t < types.length; t++) {

                        if (module.DeviceType != null && types[t].toLowerCase().equals(module.DeviceType.toLowerCase())) {
                            module.control(command, null);
                            continueparsing = true;
                            //
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
            } else {
                Module module = searchSubjectMatch(group, nextcommand.StartIndex);
                //
                if (module != null && !command.equals("")) {
                    module.control(command, null);
                    continueparsing = true;
                    //
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //alert(group + ' ' + command + ' ' + module.Name);
            }
            //
        }

    }


    public String searchTypeMatch(boolean keepsentence) {
        String result = "";
        LingoMatch curmatch = new LingoMatch("", -1);
        for (LingoType t : getTypes()) {
            for (int c = 0; c < t.Aliases.size(); c++) {
                LingoMatch res = findMatchingInput(t.Aliases.get(c));
                if (res.StartIndex != -1 && (res.StartIndex < curmatch.StartIndex || curmatch.StartIndex == -1)) {
                    result = t.Type;
                    curmatch = res;
//	                            break;
                }
            }
        }
        //
        if (!keepsentence && curmatch.StartIndex != -1) {
            removeInputMatch(curmatch);
        }
        return result;

    }

    public LingoMatch getCommandMatch() {
        LingoMatch curmatch = new LingoMatch("", -1);
        for (LingoCommand cmd : getCommands()) {
            for (int c = 0; c < cmd.Aliases.size(); c++) {
                LingoMatch res = findMatchingInput(cmd.Aliases.get(c));
                if (res.StartIndex != -1 && (res.StartIndex < curmatch.StartIndex || curmatch.StartIndex == -1)) {
                    curmatch = res;
//	                            break;
                }
            }
        }
        //
        return curmatch;
    }

    public String searchCommandMatch() {
        String result = "";
        LingoMatch curmatch = new LingoMatch("", -1);
        for (LingoCommand cmd : getCommands()) {
            for (int c = 0; c < cmd.Aliases.size(); c++) {
                LingoMatch res = findMatchingInput(cmd.Aliases.get(c));
                if (res.StartIndex != -1 && (res.StartIndex < curmatch.StartIndex || curmatch.StartIndex == -1)) {
                    result = cmd.Command;
                    curmatch = res;
//	                            break;
                }
            }
        }
        //
        if (curmatch.StartIndex != -1) {
            removeInputMatch(curmatch);
        }
        return result;
    }

    public String searchGroupMatch(int limitindex) {
        String result = "";
        LingoMatch curmatch = new LingoMatch("", -1);
        for (Group g : Control.getGroups()) {
            LingoMatch res = findMatchingInput(g.Name);
            if (res.StartIndex != -1 && (res.StartIndex < limitindex || limitindex == -1) && (res.StartIndex < curmatch.StartIndex || curmatch.StartIndex == -1)) {
                result = g.Name;
                curmatch = res;
                //break;
            }
        }
        if (curmatch.StartIndex != -1) {
            removeInputMatch(curmatch);
        }
        return result;

    }

    public Module searchSubjectMatch(String group, int limitindex) {
        Module result = null;
        ArrayList<Module> groupmodules;
        groupmodules = getGroupModules(group);
        // try finding a module name / address
        LingoMatch curmatch = new LingoMatch("", -1);
        for (Module module : groupmodules) {
            LingoMatch res = findMatchingInput(module.Name);
            if (res.StartIndex == -1) res = findMatchingInput(module.Address);
            //
            if (res.StartIndex != -1 &&
                    (res.Words.length() >= curmatch.Words.length()) &&
                    (res.StartIndex < limitindex || limitindex == -1) &&
                    (res.StartIndex <= curmatch.StartIndex || curmatch.StartIndex == -1)) {
                result = module;
                curmatch = res;
                //break;
            }
        }
        if (curmatch.StartIndex != -1) {
            removeInputMatch(curmatch);
        }
        return result;
    }

    public void removeInputMatch(LingoMatch wordsmatch) {
        if (wordsmatch.StartIndex > -1 && wordsmatch.Words.length() > 0) {
            _currentInput = _currentInput.substring(0, wordsmatch.StartIndex) + ' ' + _currentInput.substring(wordsmatch.StartIndex + wordsmatch.Words.length() - 1);
        }
    }


    public LingoMatch findMatchingInput(String words) {
        LingoMatch wordsmatch = new LingoMatch(words, -1);
        words = ' ' + words.toLowerCase() + ' ';
        int idx = (' ' + _currentInput.toLowerCase() + ' ').indexOf(words);
        if (idx >= 0 && !words.trim().equals("")) {
            wordsmatch.StartIndex = idx;
            return wordsmatch;
        }
        return wordsmatch;
    }


    private ArrayList<LingoCommand> getCommands() {
        return _lingodata.Commands;
    }

    private ArrayList<LingoType> getTypes() {
        return _lingodata.Types;
    }

    private ArrayList<Module> getGroupModules(String group) {
        ArrayList<Module> modules = new ArrayList<Module>();
        if (group == null || group.equals("")) {
            modules = Control.getModules();
        } else
            for (Group g : Control.getGroups()) {
                if (g.Name.toLowerCase().equals(group.toLowerCase())) {
                    for (Module m : g.Modules) {
                        if (m != null) {
                            for (Module im : Control.getModules()) {
                                if (m.Domain.equals(im.Domain) && m.Address.equals(im.Address)) {
                                    modules.add(im);
                                }
                            }
                        }
                    }
                    break;
                }
            }
        return modules;
    }

    /*
    */

    /**
     * lazy initialize the speech recognizer
     */
    private SpeechRecognizer getSpeechRecognizer() {
        if (_recognizer == null) {
            _recognizer = SpeechRecognizer.createSpeechRecognizer(_hgcontext);
            _recognizer.setRecognitionListener(this);
        }
        return _recognizer;
    }


    public class LingoData {
        public ArrayList<LingoCommand> Commands = new ArrayList<LingoCommand>();
        public ArrayList<LingoType> Types = new ArrayList<LingoType>();
    }


    public class LingoCommand {
        public String Command;
        public ArrayList<String> Aliases = new ArrayList<String>();
    }

    public class LingoType {
        public String Type;
        public ArrayList<String> Aliases = new ArrayList<String>();
    }


    public class LingoMatch {
        public String Words = "";
        public int StartIndex = -1;

        public LingoMatch(String words, int idx) {
            this.Words = words;
            this.StartIndex = idx;
        }
    }


    @Override
    public void onReadyForSpeech(Bundle bundle) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int i) {
        String message;
        switch (i) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                //restart = false;
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                //restart = false;
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Not recognised";
                break;
        }
        Toast.makeText(_hgcontext.getApplicationContext(), "Recognizer: " + message, Toast.LENGTH_SHORT).show();
        _recognizer.destroy();
        _recognizer = null;
    }

    @Override
    public void onResults(Bundle results) {
        if ((results != null)
                && results.containsKey(SpeechRecognizer.RESULTS_RECOGNITION)) {
            List<String> heard =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            float[] scores =
                    results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
            String msg = "";
            for (String s : heard) {
                Toast.makeText(_hgcontext.getApplicationContext(), "Executing: " + s, Toast.LENGTH_LONG).show();
                interpretInput(s);
//                msg += s;
                break;
            }
        }
    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }


}
