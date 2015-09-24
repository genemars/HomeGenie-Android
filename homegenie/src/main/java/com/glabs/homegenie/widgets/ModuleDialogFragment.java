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

import android.support.v4.app.DialogFragment;

import com.glabs.homegenie.client.data.Module;

/**
 * Created by Gene on 03/01/14.
 */
public class ModuleDialogFragment extends DialogFragment {

    protected Module _module = null;

    public void setDataModule(Module m) {
        _module = m;
    }

    public void refreshView() {
//        getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        if (_module != null && getDialog() != null) {
            getDialog().setTitle(_module.getDisplayName());
        }
    }

}
