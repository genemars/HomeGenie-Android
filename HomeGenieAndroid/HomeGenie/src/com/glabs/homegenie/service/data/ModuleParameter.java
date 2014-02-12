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

package com.glabs.homegenie.service.data;

import java.util.Date;

public class ModuleParameter {
 
    public ModuleParameter(String name, String value) {
    	this.Name = name;
    	this.Value = value;
	}
	public String Name = "";
    public String Value = "";
    public String Description = "";
    public Date UpdateTime = new Date();
    //
    public String LastValue = "";
    public Date LastUpdateTime = new Date();
    
}
