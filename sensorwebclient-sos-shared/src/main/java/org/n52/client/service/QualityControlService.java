package org.n52.client.service;

import org.n52.shared.requests.EESDataRequest;
import org.n52.shared.requests.QCDataRequest;
import org.n52.shared.responses.EESDataResponse;
import org.n52.shared.responses.QCDataResponse;

public interface QualityControlService {

	EESDataResponse getEESQCDiagram(EESDataRequest request) throws Exception;

	QCDataResponse addQualityFlag(QCDataRequest request) throws Exception;
	
	QCDataResponse modifyQualityFlag(QCDataRequest request) throws Exception;
	
	QCDataResponse approveData(QCDataRequest request) throws Exception;

	QCDataResponse getPropertiesByUser(QCDataRequest request) throws Exception;

	QCDataResponse modifyFlagShapes(QCDataRequest request) throws Exception;

	QCDataResponse getQualityCode(QCDataRequest request) throws Exception;
	
	QCDataResponse addQualityFlagWPS(QCDataRequest request) throws Exception;
	
	QCDataResponse approveDataWPS(QCDataRequest request) throws Exception;
	
	QCDataResponse modifyQualityFlagWPS(QCDataRequest request) throws Exception;

	//QCDataResponse getQualityCodeWPS() throws Exception;

	void getQualityCode(String userId, String sessionId) throws Exception;
}
