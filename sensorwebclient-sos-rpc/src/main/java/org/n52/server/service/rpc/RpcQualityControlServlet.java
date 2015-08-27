package org.n52.server.service.rpc;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.n52.server.oxf.util.access.Constants;
import org.n52.client.service.QualityControlService;
import org.n52.server.oxf.util.access.PloneAuthentication;
import org.n52.server.oxf.util.logging.Statistics;
import org.n52.server.service.QualityControlServiceImpl;
import org.n52.shared.requests.EESDataRequest;
import org.n52.shared.requests.QCDataRequest;
import org.n52.shared.responses.EESDataResponse;
import org.n52.shared.responses.QCDataResponse;
import org.n52.shared.service.rpc.RpcQualityControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class RpcQualityControlServlet extends RemoteServiceServlet implements RpcQualityControlService {

	private static final long serialVersionUID = 4883356620013749048L;
	private static final Logger LOG = LoggerFactory.getLogger(RpcQualityControlServlet.class);

	private QualityControlService service;

	@Override
	public void init() throws ServletException {
		LOG.debug("Initialize " + getClass().getName() + " Servlet for SOS Client");
		service = new QualityControlServiceImpl();
	}

	@Override
	public EESDataResponse getEESQCDiagram(EESDataRequest request) throws Exception {
		// TODO Auto-generated method stub
		Statistics.saveHostRequest(this.getThreadLocalRequest().getRemoteHost());
		return service.getEESQCDiagram(request);

		// return null;
	}

	@Override
	public QCDataResponse addQualityFlagWPS(QCDataRequest request) throws Exception {
		// TODO Auto-generated method stub
		Statistics.saveHostRequest(this.getThreadLocalRequest().getRemoteHost());
		return service.addQualityFlagWPS(request);
	}

	@Override
	public QCDataResponse getPropertiesByUser(QCDataRequest request) throws Exception {
		// TODO Auto-generated method stub
		Statistics.saveHostRequest(this.getThreadLocalRequest().getRemoteHost());
		return service.getPropertiesByUser(request);
	}

	@Override
	public QCDataResponse modifyFlagShapes(QCDataRequest request) throws Exception {
		// TODO Auto-generated method stub
		Statistics.saveHostRequest(this.getThreadLocalRequest().getRemoteHost());
		return service.modifyFlagShapes(request);
	}

	@Override
	public void getQualityCode(String userId, String sessionId) throws Exception {
		// TODO Auto-generated method stub
		//do nothing
	}

	@Override
	public QCDataResponse modifyQualityFlagWPS(QCDataRequest request) throws Exception {
		// TODO Auto-generated method stub
		Statistics.saveHostRequest(this.getThreadLocalRequest().getRemoteHost());
		return service.modifyQualityFlagWPS(request);
	}

	@Override
	public QCDataResponse addQualityFlag(QCDataRequest request) throws Exception {
		// TODO Auto-generated method stub
		Statistics.saveHostRequest(this.getThreadLocalRequest().getRemoteHost());
		return service.addQualityFlag(request);
	}

	@Override
	public QCDataResponse modifyQualityFlag(QCDataRequest request) throws Exception {
		// TODO Auto-generated method stub
		Statistics.saveHostRequest(this.getThreadLocalRequest().getRemoteHost());
		return service.modifyQualityFlag(request);
	}

	@Override
	public QCDataResponse getQualityCode(QCDataRequest request) throws Exception{
		//ASD 17.11. 2014
		Constants.setWPS_URL(request.getOptions().getWpsUrl());
		Constants.setEMAIL_FROM(request.getOptions().getWpsUser());
		Constants.setEMAIL_PWD(request.getOptions().getWpsPwd());
		Constants.setURL_USER_AUTHENTICATE(request.getOptions().getAuthenthicateUrl());
		
		HttpServletRequest req = this.getThreadLocalRequest();
		String sessionId="";
		String userId="";
		if (req != null) {
			sessionId = this.getPloneSessionIdentifier(req.getCookies());
			//LOG.debug("Session Id from getPloneSessionIdentifier() :" + sessionId);
			if(sessionId.equals("")) {
				LOG.debug("Session Id NOT Found - DEFAULT ID is used :" + Constants.DEFAULT_SESSION_ID);
				sessionId = Constants.DEFAULT_SESSION_ID;
			}
		} 
		if (!sessionId.equals("")) {
			userId = PloneAuthentication.getInstance(Constants.URL_USER_AUTHENTICATE).getUserBySessionCookie(sessionId);
		}
		
		LOG.debug("User Authentication Details: " + sessionId + " " + userId);
		service.getQualityCode(userId,sessionId);
		
		return service.getQualityCode(request);
	}


	private String getPloneSessionIdentifier(Cookie[] cookies) {
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("__ac")) {
					String value = cookie.getValue();
					//LOG.debug("RpcQualityControlServlet - getPloneSessionIdentifier(): " + value);
					return value;
				}
			}
		} else {
			LOG.debug("RpcQualityControlServlet - Cookies are null");
		}
		return "";
	}

	@Override
	public QCDataResponse approveData(QCDataRequest request) throws Exception {
		// TODO Auto-generated method stub
		return service.approveData(request);
	}

	@Override
	public QCDataResponse approveDataWPS(QCDataRequest request) throws Exception {
		// TODO Auto-generated method stub
		return service.approveDataWPS(request);
	}
}