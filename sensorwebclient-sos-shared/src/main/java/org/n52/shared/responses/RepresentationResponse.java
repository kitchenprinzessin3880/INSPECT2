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
package org.n52.shared.responses;

import java.io.Serializable;
import java.util.HashMap;

import org.n52.shared.serializable.pojos.DesignOptions;
import org.n52.shared.serializable.pojos.ResponseOptions;

public abstract class RepresentationResponse implements Serializable {
	private static final long serialVersionUID = 1345134780017053553L;
    protected HashMap<String,ResponseOptions> optionsMap;
    
    protected RepresentationResponse() {
        // for serialization
    }
    
    public RepresentationResponse(HashMap<String,ResponseOptions> options) {
        this.optionsMap = options;
    }
   
	public HashMap<String,ResponseOptions> getResponseOptionsMap() {
        return this.optionsMap;
    }

}

