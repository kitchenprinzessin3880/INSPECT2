package org.n52.client.ctrl.callbacks;

import org.n52.client.ctrl.RequestManager;
import org.n52.client.ctrl.ServerCallback;
import org.n52.shared.responses.QCDataResponse;

public abstract class AddQualityFlagCallback extends ServerCallback<QCDataResponse> {

	public AddQualityFlagCallback(RequestManager requestMgr, String errorMsg) {
		super(requestMgr, errorMsg);
	}

}
