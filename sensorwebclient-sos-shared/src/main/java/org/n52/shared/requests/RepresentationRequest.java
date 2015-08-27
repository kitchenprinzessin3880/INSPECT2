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
package org.n52.shared.requests;

import java.io.Serializable;
import java.util.HashMap;

import org.n52.shared.serializable.pojos.DesignOptions;
import org.n52.shared.serializable.pojos.QCOptions;


public abstract class RepresentationRequest implements Serializable {

    private static final long serialVersionUID = -5047908941386315508L;
    
    protected DesignOptions options;
    
    //ASD
    protected HashMap<String,DesignOptions> optionsMap;
    
    protected RepresentationRequest() {
        // for serialization
    }

    public RepresentationRequest(DesignOptions options) {
        this.options = options;
    }
    
    public RepresentationRequest(HashMap<String,DesignOptions> options) {
        this.optionsMap = options;
    }
  
    public DesignOptions getOptions() {
        return this.options;
    }
    
    public HashMap<String,DesignOptions> getOptionsMap() {
        return this.optionsMap;
    }

}
