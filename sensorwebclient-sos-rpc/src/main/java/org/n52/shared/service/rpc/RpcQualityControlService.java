package org.n52.shared.service.rpc;

import org.n52.client.service.QualityControlService;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("QualityControlService")
public interface RpcQualityControlService extends RemoteService, QualityControlService {

}