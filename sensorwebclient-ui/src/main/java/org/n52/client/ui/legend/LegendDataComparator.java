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
package org.n52.client.ui.legend;

import java.util.Comparator;

import com.google.gwt.core.client.GWT;


public class LegendDataComparator implements Comparator<Object> {

    public int compare(Object arg0, Object arg1) {
        LegendData ts1 = (LegendData) arg0;
        LegendData ts2 = (LegendData) arg1;
        //ASD commented out 28.07.2014
        //return new Integer(ts1.getOrdering()).compareTo(new Integer(ts2.getOrdering()));
        return new String(ts1.getId()).compareTo(new String(ts2.getId()));
    }

}