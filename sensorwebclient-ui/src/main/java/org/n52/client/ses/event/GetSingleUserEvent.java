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
package org.n52.client.ses.event;

import org.eesgmbh.gimv.client.event.FilteredDispatchGwtEvent;
import org.n52.client.ses.event.handler.GetSingleUserEventHandler;

/**
 * The Class GetSingleUserEvent.
 */
public class GetSingleUserEvent extends FilteredDispatchGwtEvent<GetSingleUserEventHandler> {

    /** The TYPE. */
    public static Type<GetSingleUserEventHandler> TYPE = new Type<GetSingleUserEventHandler>();

    /** The parameterId. */
    private String id;

    /**
     * Instantiates a new gets the single user event.
     * 
     * @param parameterId
     *            the parameterId
     * @param blockedHandlers
     *            the blocked handlers
     */
    public GetSingleUserEvent(String id, GetSingleUserEventHandler... blockedHandlers) {
        super(blockedHandlers);
        this.id = id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eesgmbh.gimv.client.event.FilteredDispatchGwtEvent#onDispatch(com
     * .google.gwt.event.shared.EventHandler)
     */
    @Override
    protected void onDispatch(GetSingleUserEventHandler handler) {
        handler.onGetSingleUser(this);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.gwt.event.shared.GwtEvent#getAssociatedType()
     */
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<GetSingleUserEventHandler> getAssociatedType() {
        return TYPE;
    }

    /**
     * Gets the parameterId.
     * 
     * @return the parameterId
     */
    public String getId() {
        return this.id;
    }
}
