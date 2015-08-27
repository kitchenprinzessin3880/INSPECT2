package org.n52.server.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.client.service.QualityControlService;
import org.n52.server.oxf.render.sos.CustomRenderingProperties;
import org.n52.server.oxf.util.access.Constants;
import org.n52.server.oxf.util.access.DBOperation;
import org.n52.server.oxf.util.access.ObservationData;
import org.n52.server.oxf.util.generator.EESGenerator;
import org.n52.server.wps.WPSExecuter;
import org.n52.shared.requests.EESDataRequest;
import org.n52.shared.requests.QCDataRequest;
import org.n52.shared.responses.EESDataResponse;
import org.n52.shared.responses.QCDataResponse;
import org.n52.shared.serializable.pojos.QCOptions;

import com.google.gwt.core.shared.GWT;

public class QualityControlServiceImpl implements QualityControlService {

	private static final Logger LOG = LoggerFactory.getLogger(QualityControlServiceImpl.class);
	private static final Map<String, String> processingStatusMap = new HashMap<String, String>();
	private static Map<Long, String> dataLevelMapping = new HashMap<Long, String>();
	private String userId = "";
	private String sessionId = "";
	private String email = "";
	private static DBOperation dbInstance = null;
	private Long sourceId = null;
	private Map<String, ArrayList<String>> allowedSitePropertiesList = null;

	public QualityControlServiceImpl() {
		dbInstance = DBOperation.getInstance();
	}

	/*
	 * @Override public QCDataResponse getQualityCodeWPS() throws Exception {
	 * try { if (LOG.isDebugEnabled()) { String msgTemplate =
	 * "Request -> getQualityFlagWPS(userid: %s)";
	 * LOG.debug(String.format(msgTemplate, userId)); } HashMap<String, Object>
	 * inputs = new HashMap<String, Object>(); inputs.put("sessionId",
	 * sessionId);
	 * 
	 * WPSExecuter wpsExec = new WPSExecuter(sessionId); // get flags
	 * Map<String, ArrayList<String>> flags =
	 * wpsExec.getQualityFlagsSoSData(WPS_PROC_GETFLAGS, inputs);
	 * 
	 * // get data processing levels dataLevelMapping =
	 * wpsExec.getProcessingStatiSosData(WPS_PROC_GETPROCSTATUS, inputs);
	 * 
	 * // set generic flags with symbols, data levels CustomRenderingProperties
	 * customRenderer = CustomRenderingProperties.getInst();
	 * customRenderer.generateDefaultFlagShapePairs(new
	 * ArrayList<String>(flags.keySet()));
	 * customRenderer.setDataProcLevels(dataLevelMapping);
	 * customRenderer.setQualityFlagMappingWithId
	 * (wpsExec.getQualityFlagMapping());
	 * 
	 * //return new QCDataResponse(userId, sessionId, flags,
	 * customRenderer.getAllActiveShapesNames(),
	 * customRenderer.getActiveFlagSymbolPair()); return null; } catch
	 * (Exception e) { LOG.error("Exception occured on server side.", e); throw
	 * e; // last chance to log on server side } }
	 */

	public EESDataResponse getEESQCDiagram(EESDataRequest request) throws Exception {
		try {
			LOG.debug("Performing getEESQCDiagram data request.");
			EESGenerator gen = new EESGenerator(false);
			// ASD
			EESDataResponse dataResponse = (EESDataResponse) gen.producePresentation(request.getOptions());
			return dataResponse;
		} catch (Exception e) {
			LOG.error("Exception occured on server side.", e);
			throw e; // last chance to log on server side
		}
	}

	@Override
	public QCDataResponse getPropertiesByUser(QCDataRequest request) throws Exception {
		if (LOG.isDebugEnabled()) {
			String msgTemplate = "Request -> getPropertiesByUser(sosUrl: %s)";
			LOG.debug(String.format(msgTemplate, request.getOptions().getUser()));
		}
		String sessionId = request.getOptions().getSessionId();
		String sensor = request.getOptions().getSensor();

		// WPSExecuter wpsExec = new WPSExecuter();
		// QCDataResponse resp= wpsExec.getFilteredPropertiesByUser(sessionId,
		// sensor);

		return null;
	}

	@Override
	public QCDataResponse modifyFlagShapes(QCDataRequest request) throws Exception {
		// TODO Auto-generated method stub
		Map<String, String> customFlagShapes = request.getOptions().getFlagSymbolMap();
		CustomRenderingProperties.getInst().setActiveFlagSymbolPair(customFlagShapes);
		QCDataResponse resp = new QCDataResponse(true);
		return resp;
	}

	public boolean addUpdateFlags(QCDataRequest request, boolean isAddFlag) {
		// LOG.debug("START DATABASE ADD : " + new Date().toGMTString());
		String addFlagType = request.getOptions().getFlaggingType();
		// String usr = this.userId;
		Long source = this.sourceId;

		Map<String, Long> qualityFlagMappingWithId = ObservationData.getInstance().getQualifiers();
		boolean status = false;

		/*
		 * if (addFlagType.equalsIgnoreCase("TIMEPOINT")) { LOG.debug(
		 * "QualityControlServiceImpl - Adding quality flags by timepoint : " +
		 * request.getOptions().printPointRequest()); String time =
		 * request.getOptions().getTimePoint(); String id =
		 * request.getOptions().getTimeSeriesId(); String[] timeArr = {time};
		 * timeSeriesList.add(id); isInterval = false; status =
		 * dbInstance.addFlagtoDB(source, timeSeriesList,timeArr, flagId,
		 * isAddFlag,isInterval); }
		 */

		if (addFlagType.equalsIgnoreCase("RULE")) {
			String genericFlag = request.getOptions().getGenericFlag();
			String specificFlag = request.getOptions().getSpecificFlag();
			Long flagId = getFlagMappingId(qualityFlagMappingWithId, genericFlag, specificFlag);
			String begin = request.getOptions().getStartRange();
			String end = request.getOptions().getEndRange();
			String tsid = request.getOptions().getTimeSeriesId();
			String[] timeArr = { begin, end };
			String dataClass = ObservationData.getInstance().getDataDirectoryMap().get(request.getOptions().getSensor()).get(request.getOptions().getProperty());
			LOG.debug("QualityControlServiceImpl - Adding quality flags by rule : " + request.getOptions().printRuleRequest());
			status = dbInstance.updateDataByRule(request.getOptions().getSensor(), request.getOptions().getProperty(), source, tsid, dataClass, timeArr, flagId, isAddFlag, request.getOptions().getRuleValue(), request.getOptions().getOperator());
		}
		if (addFlagType.equalsIgnoreCase("TIMERANGE") || addFlagType.equalsIgnoreCase("VIEW") || addFlagType.equalsIgnoreCase("APPROVE")) {
			ArrayList<String> timeSeriesList = new ArrayList<String>();
			Map<String, String> sensorPropertyMap = request.getOptions().getSensorPropertyList();
			Iterator it = sensorPropertyMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry) it.next();
				String key = (String) pairs.getKey();
				timeSeriesList.add(key);
			}
			String begin = request.getOptions().getStartRange();
			String end = request.getOptions().getEndRange();
			String[] timeArr = { begin, end };

			if (addFlagType.equalsIgnoreCase("APPROVE")) {
				LOG.debug("QualityControlServiceImpl - Approve data series : " + request.getOptions().printApproveRequest());
				status = dbInstance.releaseDataSeries(source, timeSeriesList, timeArr);
			} else {
				String genericFlag = request.getOptions().getGenericFlag();
				String specificFlag = request.getOptions().getSpecificFlag();
				Long flagId = getFlagMappingId(qualityFlagMappingWithId, genericFlag, specificFlag);
				LOG.debug("QualityControlServiceImpl - Adding quality flags by range/view : " + request.getOptions().printViewRequest());
				status = dbInstance.addFlagtoDB(source, timeSeriesList, timeArr, flagId, isAddFlag);
			}
		}
		if (addFlagType.equalsIgnoreCase("MULTIPOINT")) {
			Map<String, String> sensorPropertyMapGrid = request.getOptions().getMultiPointsMap();
			status = dbInstance.addMultiFlagtoDB(source, sensorPropertyMapGrid, isAddFlag);
		}
		// LOG.debug("END DATABASE ADD : " + new Date().toGMTString());
		return status;
	}

	@Override
	public void getQualityCode(String uId, String sId) throws Exception {
		// TODO Auto-generated method stub
		this.userId = uId;
		this.sessionId = sId;
		this.sourceId = dbInstance.getSourcefromUsername(userId);
		this.email = dbInstance.getEmail();
		// get all sites and props applicable to the given user
		this.allowedSitePropertiesList = dbInstance.getAllowedSitesAndPropsByUser(sourceId);
	}

	@Override
	public QCDataResponse addQualityFlag(QCDataRequest request) throws Exception {
		// TODO Auto-generated method stub
		boolean addStatus = addUpdateFlags(request, true);
		QCDataResponse dataResponse = new QCDataResponse(addStatus);
		return dataResponse;
	}

	@Override
	public QCDataResponse addQualityFlagWPS(QCDataRequest request) throws Exception {
		// TODO Auto-generated method stub
		LOG.debug("QualityControlServiceImpl - Adding quality flags via WPS : " + request.getOptions().printAll());
		Runnable r = new WPSExecuter(this.sessionId, request, Constants.WPS_PROC_ADDFLAG, false, request.getOptions().getEmail());
		new Thread(r).start();

		QCDataResponse dataResponse = new QCDataResponse(true);
		return dataResponse;
	}

	@Override
	public QCDataResponse modifyQualityFlagWPS(QCDataRequest request) throws Exception {
		// TODO Auto-generated method stub
		LOG.debug("QualityControlServiceImpl - Overwrite quality flags : " + request.getOptions().printAll());
		Runnable r = new WPSExecuter(this.sessionId, request, Constants.WPS_PROC_UPDATEFLAG, true, request.getOptions().getEmail());
		new Thread(r).start();
		QCDataResponse dataResponse = new QCDataResponse(true);
		return dataResponse;
	}

	@Override
	public QCDataResponse modifyQualityFlag(QCDataRequest request) throws Exception {
		// TODO Auto-generated method stub
		LOG.debug("QualityControlServiceImpl - Modify quality flags : " + request.getOptions().printAll());
		boolean modifyStatus = addUpdateFlags(request, false);
		QCDataResponse dataResponse = new QCDataResponse(modifyStatus);
		return dataResponse;
	}

	public QCDataResponse getQualityCode(QCDataRequest request) throws Exception {
		try {
			if (LOG.isDebugEnabled()) {
				String msgTemplate = "Request -> getQualityFlag(userid: %s)";
				LOG.debug(String.format(msgTemplate, userId));
			}
			dataLevelMapping = dbInstance.requestProcessingStatiFromDb();
			Map<String, Long> qualityFlagMapping = dbInstance.requestQualifiersFromDb();
			
			Map<String, ArrayList<String>> flags = new HashMap<String, ArrayList<String>>();

			Iterator it = qualityFlagMapping.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry) it.next();
				String[] temp = pairs.getKey().toString().split("_");
				String genericFlag = temp[0];
				String specificFlag = temp[1];

				ArrayList<String> value = flags.get(genericFlag);
				if (value != null && !(genericFlag.equals(specificFlag))) {
					value.add(specificFlag);
				} else {
					ArrayList<String> specFlagList = new ArrayList<String>();
					specFlagList.add(specificFlag);
					flags.put(genericFlag, specFlagList);
				}
			}
			// set generic flags with symbols, data levels
			String sourceCode = dbInstance.getSourceUserCode();
			CustomRenderingProperties customRenderer = CustomRenderingProperties.getInst();
			customRenderer.generateDefaultFlagShapePairs(new ArrayList<String>(flags.keySet()));
			return new QCDataResponse(userId, sourceId, email, sourceCode, flags, customRenderer.getAllActiveShapesNames(), customRenderer.getActiveFlagSymbolPair(), allowedSitePropertiesList);

		} catch (Exception e) {
			LOG.error("Exception occured on server side.", e);
			throw e; // last chance to log on server side
		}
	}

	private Long getFlagMappingId(Map<String, Long> qualityFlagMappingWithId, String gFlag, String sFlag) {
		String key = gFlag + "_" + sFlag;
		Long id = qualityFlagMappingWithId.get(key);
		return id;
	}

	@Override
	public QCDataResponse approveData(QCDataRequest request) throws Exception {
		boolean approveStatus = addUpdateFlags(request, false);
		QCDataResponse dataResponse = new QCDataResponse(approveStatus);
		return dataResponse;
	}

	@Override
	public QCDataResponse approveDataWPS(QCDataRequest request) throws Exception {
		Runnable r = new WPSExecuter(this.sessionId, request, Constants.WPS_PROC_APPROVEDATA, true, request.getOptions().getEmail());
		new Thread(r).start();
		QCDataResponse dataResponse = new QCDataResponse(true);
		return dataResponse;
	}

}