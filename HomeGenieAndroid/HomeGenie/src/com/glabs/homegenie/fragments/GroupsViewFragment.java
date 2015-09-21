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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.glabs.homegenie.R;
import com.glabs.homegenie.StartActivity;
import com.glabs.homegenie.adapters.GenericWidgetAdapter;
import com.glabs.homegenie.adapters.GroupsFragmentAdapter;
import com.glabs.homegenie.adapters.ModulesAdapter;
import com.glabs.homegenie.client.Control;
import com.glabs.homegenie.client.data.Group;
import com.glabs.homegenie.client.data.Module;
import com.glabs.homegenie.client.data.ModuleParameter;
import com.glabs.homegenie.widgets.ModuleDialogFragment;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

/**
 * Created by Gene on 01/01/14.
 */
public class GroupsViewFragment extends Fragment {

    //
    // Compatibility API 8
    // these MenuItem constants were not available
    // so we reproduce them using the same values of API 11
    final int SHOW_AS_ACTION_IF_ROOM = 1;
    final int SHOW_AS_ACTION_WITH_TEXT = 4;
    final int SHOW_AS_ACTION_NEVER = 0;
    protected Hashtable<Integer, ModulesAdapter> mAdapterList = new Hashtable<Integer, ModulesAdapter>();
    private ViewPager mPager;
    private PageIndicator mIndicator;
    private GroupsFragmentAdapter mAdapter;
    private int mCurrentGroup = 0;
    private ArrayList<Module> mGroupPrograms = new ArrayList<Module>();


    public void setGroups(ArrayList<Group> groups) {
        mCurrentGroup = 0;
        mAdapter.setGroups(groups);
        mIndicator.setCurrentItem(0);
        UpdateJumpToGroupMenu(groups);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /** Inflating the layout for this fragment **/
        View v = inflater.inflate(R.layout.fragment_groups, null);

        mAdapter = new GroupsFragmentAdapter(getActivity().getSupportFragmentManager());

        mPager = (ViewPager) v.findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (TitlePageIndicator) v.findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        mIndicator.setCurrentItem(mCurrentGroup);
        //
        mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int pindex) {
                _setCurrentGroup(pindex);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                // TODO Auto-generated method stub
            }
        });
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void UpdateJumpToGroupMenu(ArrayList<Group> groups) {
        StartActivity rootactivity = (StartActivity) getActivity();
        if (rootactivity != null) {
            Menu menu = rootactivity.getActionMenu();
            if (menu != null) {
                MenuItem jumpto = menu.findItem(R.id.action_jumpto);
                Menu submenu = jumpto.getSubMenu();
                if (submenu == null) return;
                //
                submenu.removeGroup(Menu.NONE);
                if (groups.size() > 0) {
                    int gid = 0;
                    for (Group group : groups) {
                        MenuItem grp = submenu.add(Menu.NONE, gid, gid, group.Name);
                        //                    prg.setIcon(R.drawable.ic_action_flash_on);
                        MenuCompat.setShowAsAction(grp, SHOW_AS_ACTION_IF_ROOM | SHOW_AS_ACTION_WITH_TEXT);
                        grp.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                mIndicator.setCurrentItem(menuItem.getItemId());
                                return true;
                            }
                        });
                        gid++;
                    }
                }
            }
        }
    }

    public void UpdateCurrentGroupMenu() {
        StartActivity rootactivity = (StartActivity) getActivity();
        Menu menu = rootactivity.getActionMenu();
        if (menu != null) {
            MenuItem automation = menu.findItem(R.id.menu_automation);
            if (automation != null) {
                automation.setEnabled(false);
                Menu submenu = automation.getSubMenu();
                if (submenu == null) return;
                //
                submenu.removeGroup(Menu.NONE);
                if (mGroupPrograms.size() > 0) {
                    for (Module program : mGroupPrograms) {
                        MenuItem prg = submenu.add(Menu.NONE, Menu.NONE, Menu.NONE, program.getDisplayName());
                        prg.setIcon(R.drawable.ic_action_flash_on);
                        MenuCompat.setShowAsAction(prg, SHOW_AS_ACTION_IF_ROOM | SHOW_AS_ACTION_WITH_TEXT);
                        final String paddress = program.Address;
                        String groupname = "";
                        try {
                            groupname = Uri.encode(mAdapter.getGroup(mCurrentGroup).Name, "UTF-8");
                        } catch (Exception e) {
                        }
                        final String gname = groupname;
                        prg.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                String apicall = "HomeAutomation.HomeGenie/Automation/Programs.Run/" +
                                        paddress + "/" +
                                        gname + "/" + new Date().getTime();
                                Control.callServiceApi(apicall, null);
                                return true;
                            }
                        });
                    }
                    automation.setEnabled(true);
                }
            }
            //
//            MenuItem recordMacro = submenu.add(1, Menu.NONE, Menu.NONE, "Record macro");
//            recordMacro.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//                @Override
//                public boolean onMenuItemClick(MenuItem menuItem) {
//                    StartActivity sa = (StartActivity)getActivity();
//                    sa.openMacroRecordMenu();
//                    return true;
//                }
//            });
//            rootactivity.supportInvalidateOptionsMenu();
        }
    }

    public void RefreshView() {
        GroupFragment f = mAdapter.getItem(mCurrentGroup);
        View v = f.getView();
        ListView lv = (ListView) v.findViewById(R.id.listView);
        ModulesAdapter adapter = (ModulesAdapter)lv.getAdapter();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void UpdateCurrentGroupModules() {
        ArrayList<Module> modules = null;
        try
        {
            modules = mAdapter.getGroup(mCurrentGroup).Modules;
        } catch (Exception e) {}
        //
        if (modules == null) return;
        //
        ArrayList<Module> controlModules = new ArrayList<Module>();
        mGroupPrograms.clear();
        for (Module m : modules) {
            if (m == null) continue;
            ModuleParameter widgetParam = m.getParameter("Widget.DisplayModule");
            String widget = "";
            if (widgetParam != null) widget = widgetParam.Value;
            if (widget.equals("homegenie/generic/program")) {
                mGroupPrograms.add(m);
            } else {
                controlModules.add(m);
            }
        }
        //
        GroupFragment f = mAdapter.getItem(mCurrentGroup);
        View v = f.getView();
        if (v != null) {
            ListView lv = (ListView) v.findViewById(R.id.listView);
            if (lv.getAdapter() == null) {
                ModulesAdapter adapter;
                adapter = new ModulesAdapter(v.getContext(), R.layout.widget_item_generic, controlModules);
                mAdapterList.put(mCurrentGroup, adapter);
                lv.setAdapter(adapter);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Module module = (Module) view.getTag();
                        if (module != null && module.Adapter != null) {
                            ModuleDialogFragment fmWidget = ((GenericWidgetAdapter) module.Adapter).getControlFragment();
                            if (fmWidget != null) {
                                fmWidget.setDataModule(module);
                                //
                                FragmentManager fm = getActivity().getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                                //
                                fragmentTransaction.add(fmWidget, "WIDGET");
                                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                                fragmentTransaction.commit();
                            } else {
                                Intent acWidgetIntent = ((GenericWidgetAdapter) module.Adapter).getControlActivityIntent(module);
                                if (acWidgetIntent != null) {
                                    startActivity(acWidgetIntent);
                                    getActivity().overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
                                }
                            }
                        }

                    }
                });
            } else {
                ((ModulesAdapter) lv.getAdapter()).notifyDataSetChanged();
            }
        }
    }

    private void _setCurrentGroup(int pindex) {
        final Fragment f = mAdapter.getItem(mCurrentGroup);
        if (f != null)
        {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.detach(f);
                    ft.attach(f);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    ft.commit();
                }
            });
        }
        mCurrentGroup = pindex;
        UpdateCurrentGroupModules();
        UpdateCurrentGroupMenu();
    }

}
