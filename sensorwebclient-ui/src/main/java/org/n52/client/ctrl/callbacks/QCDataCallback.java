package org.n52.client.ctrl.callbacks;

import org.n52.client.ctrl.RequestManager;
import org.n52.client.ctrl.ServerCallback;
import org.n52.shared.responses.EESDataResponse;


public abstract class QCDataCallback extends ServerCallback<EESDataResponse> {

    public QCDataCallback(RequestManager requestMgr, String errorMsg) {
        super(requestMgr, errorMsg);
    }
}