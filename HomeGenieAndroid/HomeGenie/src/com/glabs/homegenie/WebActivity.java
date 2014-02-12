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

package com.glabs.homegenie;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.webkit.HttpAuthHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * Created by Gene on 04/01/14.
 */
public class WebActivity extends Activity {

    private WebView _webbrowser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webbrowser);

        Intent intent = getIntent();
        final String user = intent.getStringExtra("Username");
        final String pass = intent.getStringExtra("Password");
        final String loadurl =  intent.getStringExtra("URL");

        _webbrowser = (WebView) findViewById(R.id.webview);
        _webbrowser.setWebChromeClient(new WebChromeClient());
        _webbrowser.clearCache(true);
        _webbrowser.clearHistory();
        //
        WebSettings webSettings = _webbrowser.getSettings();
        webSettings.setSupportMultipleWindows(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportZoom(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        //
        if (user != null && pass != null && !pass.equals(""))
        {
            final String usernameRandomPassword = user + ":" + pass;
            _webbrowser.setWebViewClient(new WebViewClient(){
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {

                    try {
                        HashMap<String, String> map = new HashMap<String, String>();
                        String authorization = "Basic " + Base64.encodeToString(usernameRandomPassword.getBytes("UTF-8"), Base64.NO_WRAP);
                        map.put("Authorization", authorization);
                        view.loadUrl(url, map);
                    } catch (UnsupportedEncodingException e) {}

                    return true;
                }
            });
            try {
                HashMap<String, String> map = new HashMap<String, String>();
                String authorization = "Basic " + Base64.encodeToString(usernameRandomPassword.getBytes("UTF-8"), Base64.NO_WRAP);
                map.put("Authorization", authorization);
                _webbrowser.loadUrl(loadurl, map);
            } catch (UnsupportedEncodingException e) {}
        }
        else
        {
            _webbrowser.setWebViewClient(new WebViewClient(){
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });
            _webbrowser.loadUrl(loadurl);
        }
    }


}
