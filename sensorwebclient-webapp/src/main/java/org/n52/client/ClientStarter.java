/**
 * ﻿Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */

package org.n52.client;


import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;

public class ClientStarter implements EntryPoint {
	private static String REDIRECTLOGINURL = "http://icg4aida.icg.kfa-juelich.de/login_form?came_from=";
    public void onModuleLoad() {
        GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            public void onUncaughtException(Throwable e) {
                GWT.log("Uncaught Exception", e);
            }
        });
		//ASD - dev mode - enable this!
        com.google.gwt.user.client.Cookies.setCookie("__ac","rLK7kZnV1B8QUUOTb0fi/DrASzMgYS5kZXZhcmFqdQ==");

    	String cookie = Cookies.getCookie("__ac");
    
    	if(cookie==null)
    	{
    		Window.alert("Access Denied. Please login.");
    		String loc = com.google.gwt.user.client.Window.Location.getPath();
    		Window.Location.replace(REDIRECTLOGINURL+loc);
    	}
    	else
    	{
    		// TODO refactor startup to be more explicit
    		Application.start();
    	}
    }

}