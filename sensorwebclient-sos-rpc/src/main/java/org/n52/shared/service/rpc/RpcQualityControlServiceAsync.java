package org.n52.shared.service.rpc;

import org.n52.shared.requests.EESDataRequest;
import org.n52.shared.requests.QCDataRequest;
import org.n52.shared.responses.EESDataResponse;
import org.n52.shared.responses.QCDataResponse;

import com.google.gwt.http.client.Request;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface RpcQualityControlServiceAsync {
	
	void getQualityCode(QCDataRequest req, AsyncCallback<QCDataResponse> callback);

	void getQualityCode(java.lang.String userId, java.lang.String sessionId, com.google.gwt.user.client.rpc.AsyncCallback<java.lang.Void> arg3);

	Request getEESQCDiagram(EESDataRequest request, AsyncCallback<EESDataResponse> callback) throws Exception;

	void addQualityFlag(QCDataRequest req, AsyncCallback<QCDataResponse> callback) throws Exception;

	void addQualityFlagWPS(QCDataRequest req, AsyncCallback<QCDataResponse> callback) throws Exception;

	void modifyQualityFlag(QCDataRequest req, AsyncCallback<QCDataResponse> callback) throws Exception;

	void modifyQualityFlagWPS(QCDataRequest req, AsyncCallback<QCDataResponse> callback) throws Exception;
	
	void approveData(QCDataRequest req, AsyncCallback<QCDataResponse> callback) throws Exception;

	void approveDataWPS(QCDataRequest req, AsyncCallback<QCDataResponse> callback) throws Exception;

	void getPropertiesByUser(QCDataRequest request, AsyncCallback<QCDataResponse> callback) throws Exception;

	void modifyFlagShapes(QCDataRequest request, AsyncCallback<QCDataResponse> callback) throws Exception;
}
