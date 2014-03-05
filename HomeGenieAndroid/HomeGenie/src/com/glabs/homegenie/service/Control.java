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

package com.glabs.homegenie.service;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;

import com.glabs.homegenie.StartActivity;
import com.glabs.homegenie.service.data.Group;
import com.glabs.homegenie.service.data.Module;
import com.glabs.homegenie.service.data.ModuleParameter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Control {

    private static String _hg_address;
    private static String _hg_user;
    private static String _hg_pass;

    private static StartActivity _context;

    public static void setContext(StartActivity context) {
        _context = context;
    }


    private static final ResponseHandler<String> _response_handler = new ResponseHandler<String>() {
        public String handleResponse(final HttpResponse response)
                throws HttpResponseException, IOException {
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() >= 300) {
                throw new HttpResponseException(statusLine.getStatusCode(),
                        statusLine.getReasonPhrase());
            }

            HttpEntity entity = response.getEntity();
            return entity == null ? null : EntityUtils.toString(entity, "UTF-8");
        }
    };

    public static void setHgServer(String ip, String user, String pass) {
        _hg_address = ip;
        _hg_user = user;
        _hg_pass = pass;
    }

    public static void getGroups(GetGroupsCallback callback) {
        new GetGroupsRequest(callback).execute();

    }

    public static void getGroupModules(String group, GetGroupModulesCallback callback) {
        new GetGroupModulesRequest(group, callback).execute();

    }

    public static void callServiceApi(String servicecall, ServiceCallCallback callback) {
        new ServiceCallRequest(servicecall, callback).execute();
    }

    public static HttpGet getHttpGetRequest(String url) {
        HttpGet getRequest = new HttpGet(url);
        //getRequest.setHeader("Accept", "text/json");
        if (!_hg_user.equals("") && !_hg_pass.equals("")) {
            getRequest.addHeader("Authorization", "Basic " + Base64.encodeToString((_hg_user + ":" + _hg_pass).getBytes(), Base64.NO_WRAP));
        }
        return getRequest;
    }

    public static String getHgBaseHttpAddress() {
        return "http://" + _hg_address + "/";
    }


    public static String getUpnpDisplayName(Module m) {
        String desc = m.getDisplayAddress();
        if (m.getParameter("UPnP.ModelDescription") != null && !m.getParameter("UPnP.ModelDescription").Value.trim().equals("")) {
            desc = m.getParameter("UPnP.ModelDescription").Value;
        } else if (m.getParameter("UPnP.ModelName") != null && !m.getParameter("UPnP.ModelName").Value.trim().equals("")) {
            desc = m.getParameter("UPnP.ModelName").Value;
        }
        return desc;
    }


    public interface ServiceCallCallback {
        void serviceCallCompleted(String response);
    }

    public interface GetGroupsCallback {
        void groupsUpdated(boolean success, ArrayList<Group> groups);
    }


    public static class ServiceCallRequest extends AsyncTask<String, Boolean, String> {

        private String serviceUrl;
        //
        private ServiceCallCallback callback;

        public ServiceCallRequest(String servicecall, ServiceCallCallback callback) {
            this.serviceUrl = "http://" + _hg_address + "/api/" + servicecall;
            this.callback = callback;
        }

        @Override
        protected String doInBackground(String... params) {
            //execute the post
            try {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet getRequest = getHttpGetRequest(serviceUrl);
                return client.execute(getRequest, _response_handler);
            } catch (Exception e) {
                //Log.e("AsyncOperationFailed", e.getMessage());
                e.printStackTrace();
            }

            return "";
        }


        protected void onPostExecute(String response) {
            if (callback != null) {
                if (_context != null) {
                    _context.updateGroupModules();
                }
                callback.serviceCallCompleted(response);
            }
        }
    }

    public static class GetGroupsRequest extends AsyncTask<String, Boolean, String> {

        private String serviceUrl;
        //
        private GetGroupsCallback callback;

        public GetGroupsRequest(GetGroupsCallback callback) {
            this.serviceUrl = "http://" + _hg_address + "/api/HomeAutomation.HomeGenie/Config/Groups.List/";
            this.callback = callback;
        }

        @Override
        protected String doInBackground(String... params) {
            //execute the post
            try {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpParams httpParameters = new BasicHttpParams();
                // Set the timeout in milliseconds until a connection is established.
                // The default value is zero, that means the timeout is not used.
                int timeoutConnection = 10000;
                HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
                // Set the default socket timeout (SO_TIMEOUT)
                // in milliseconds which is the timeout for waiting for data.
                int timeoutSocket = 10000;
                HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);        //getRequest.setHeader("Content-type", "text/json");
                client.setParams(httpParameters);
                //
                HttpGet getRequest = getHttpGetRequest(serviceUrl);
                return client.execute(getRequest, _response_handler);
            } catch (Exception e) {
                if (callback != null) callback.groupsUpdated(false, new ArrayList<Group>());
//                Log.e("AsyncOperationFailed", e.getMessage());
                e.printStackTrace();
            }

            return "";
        }

        protected void onPostExecute(String jsonString) {

            if (jsonString == null || jsonString.equals("")) return;
            //
            ArrayList<Group> groups = new ArrayList<Group>();
            try {
                JSONArray jgroups = new JSONArray(jsonString);
                for (int g = 0; g < jgroups.length(); g++) {
                    JSONObject jg = (JSONObject) jgroups.get(g);
                    Group group = new Group();
                    group.Name = jg.getString("Name");
                    JSONArray jgmodules = jg.getJSONArray("Modules");
                    for (int m = 0; m < jgmodules.length(); m++) {
                        JSONObject jmp = (JSONObject) jgmodules.get(m);
                        Module mod = new Module();
                        mod.Domain = jmp.getString("Domain");
                        mod.Address = jmp.getString("Address");
                        group.Modules.add(mod);
                    }
                    groups.add(group);
                }
                if (callback != null) callback.groupsUpdated(true, groups);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                if (callback != null) callback.groupsUpdated(false, groups);
            }

        }
    }


    public interface GetGroupModulesCallback {
        void groupModulesUpdated(ArrayList<Module> modules);
    }

    public static class GetGroupModulesRequest extends AsyncTask<String, Boolean, String> {

        private String serviceUrl;
        //
        private GetGroupModulesCallback callback;

        public GetGroupModulesRequest(String groupName, GetGroupModulesCallback callback) {
            if (groupName.equals("")) {
                this.serviceUrl = "http://" + _hg_address + "/api/HomeAutomation.HomeGenie/Config/Modules.List/";
            } else {
                this.serviceUrl = "http://" + _hg_address + "/api/HomeAutomation.HomeGenie/Config/Groups.ModulesList/" + Uri.encode(groupName);
            }
            this.callback = callback;
        }

        @Override
        protected String doInBackground(String... params) {
            //execute the post
            try {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet getRequest = getHttpGetRequest(serviceUrl);
                return client.execute(getRequest, _response_handler);
            } catch (Exception e) {
//                Log.e("AsyncOperationFailed", e.getMessage());
                e.printStackTrace();
            }

            return "";
        }

        protected void onPostExecute(String jsonString) {

            if (jsonString == null || jsonString.equals("")) return;

            ArrayList<Module> modlist = new ArrayList<Module>();
            try {
                JSONArray groupmodules = new JSONArray(jsonString);
                for (int m = 0; m < groupmodules.length(); m++) {
                    JSONObject jm = (JSONObject) groupmodules.get(m);
                    Module module = new Module();
                    module.Domain = jm.getString("Domain");
                    module.Address = jm.getString("Address");
                    module.DeviceType = jm.getString("DeviceType");
                    module.Name = jm.getString("Name");
                    module.Description = jm.getString("Description");
                    module.RoutingNode = jm.getString("RoutingNode");
                    //
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    //
                    JSONArray jmproperties = jm.getJSONArray("Properties");
                    for (int p = 0; p < jmproperties.length(); p++) {
                        JSONObject jmp = (JSONObject) jmproperties.get(p);
                        ModuleParameter param = new ModuleParameter(jmp.getString("Name"), jmp.getString("Value"));
                        param.Description = jmp.getString("Description");
                        try {
                            param.UpdateTime = dateFormat.parse(jmp.getString("UpdateTime"));
                        } catch (Exception e) {
                        }
                        param.LastValue = jmp.getString("LastValue");
                        try {
                            param.LastUpdateTime = dateFormat.parse(jmp.getString("LastUpdateTime"));
                        } catch (Exception e) {
                        }
                        module.Properties.add(param);
                    }
                    modlist.add(module);
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (callback != null) callback.groupModulesUpdated(modlist);

        }
    }


}

