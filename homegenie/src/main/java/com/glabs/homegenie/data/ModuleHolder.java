package com.glabs.homegenie.data;

import com.glabs.homegenie.client.data.Module;

/**
 * Created by gene on 15/12/15.
 */
public class ModuleHolder {
    public ModuleHolder(Module module) {
        Module = module;
    }
    public Module Module;
    // ViewModel
    public Object Adapter;
    public android.view.View View;
}
