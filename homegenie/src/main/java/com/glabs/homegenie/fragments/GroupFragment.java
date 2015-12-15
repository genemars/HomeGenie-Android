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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.glabs.homegenie.R;
import com.glabs.homegenie.adapters.GenericWidgetAdapter;
import com.glabs.homegenie.adapters.ModulesAdapter;
import com.glabs.homegenie.client.data.Group;
import com.glabs.homegenie.client.data.Module;
import com.glabs.homegenie.client.data.ModuleParameter;
import com.glabs.homegenie.data.ModuleHolder;
import com.glabs.homegenie.widgets.ModuleDialogFragment;

import java.util.ArrayList;

public final class GroupFragment extends Fragment {

    public static GroupFragment newInstance(Group g) {
        GroupFragment fragment = new GroupFragment();
        fragment.setGroup(g);
        return fragment;
    }

    private View myView;
    private Group group;

    public void setGroup(Group g) {
        group = g;
        if (myView != null) {
            ListView lv = (ListView) myView.findViewById(R.id.listView);
            updateListAdapter(lv);
        }
    }

    private void updateListAdapter(ListView lv) {
        ArrayList<Module> controlModules = new ArrayList<>();
        if (group!= null && group.Modules != null) {
            for (Module m : group.Modules) {
                if (m == null) continue;
                ModuleParameter widgetParam = m.getParameter("Widget.DisplayModule");
                String widget = "";
                if (widgetParam != null && widgetParam.Value != null) widget = widgetParam.Value;
                if (!((widget.equals("") && m.DeviceType != null && m.DeviceType.equals("Program")) || widget.equals("homegenie/generic/program") || m.Domain.equals("HomeGenie.UI.Separator") || m.Domain.equals("Favourites.Link")))
                    controlModules.add(m);
            }
        }

        ModulesAdapter adapter = new ModulesAdapter(myView.getContext(), R.layout.widget_item_generic, controlModules);
        lv.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.fragment_groupmodules, container, false);
        ListView lv = (ListView) myView.findViewById(R.id.listView);

        updateListAdapter(lv);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ModuleHolder moduleHolder = (ModuleHolder) view.getTag();
                if (moduleHolder != null && moduleHolder.Adapter != null) {
                    ModuleDialogFragment fmWidget = ((GenericWidgetAdapter) moduleHolder.Adapter).getControlFragment();
                    if (fmWidget != null) {
                        fmWidget.setDataModule(moduleHolder.Module);
                        FragmentManager fm = getActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fm.beginTransaction();
                        fragmentTransaction.add(fmWidget, "WIDGET");
                        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        fragmentTransaction.commit();
                    } else {
                        Intent acWidgetIntent = ((GenericWidgetAdapter) moduleHolder.Adapter).getControlActivityIntent(moduleHolder.Module);
                        if (acWidgetIntent != null) {
                            startActivity(acWidgetIntent);
                            getActivity().overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
                        }
                    }
                }

            }
        });

        return myView;
    }

}
