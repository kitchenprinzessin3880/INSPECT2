package org.n52.client.ctrl.callbacks;

import org.n52.client.ctrl.RequestManager;
import org.n52.client.ctrl.ServerCallback;
import org.n52.shared.responses.QCDataResponse;


public abstract class GetQualityCodeCallback extends ServerCallback<QCDataResponse> {

	public GetQualityCodeCallback(RequestManager requestMgr, String errorMsg) {
		super(requestMgr, errorMsg);
	}

}
