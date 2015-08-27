package org.n52.server.wps;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.n52.server.oxf.util.access.Constants;
import net.opengis.wps.x100.CapabilitiesDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessBriefType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.geotools.feature.FeatureCollection;
import org.n52.server.oxf.util.access.ObservationData;
import org.n52.shared.requests.QCDataRequest;
import org.n52.wps.client.ExecuteRequestBuilder;
import org.n52.wps.client.ExecuteResponseAnalyser;
import org.n52.wps.client.WPSClientException;
import org.n52.wps.client.WPSClientSession;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WPSExecuter implements Runnable {

	private static final long serialVersionUID = -3516243815348233569L;
	private static final Logger LOG = LoggerFactory.getLogger(WPSExecuter.class);
	private String email = "";
	private String sessionId = "";
	private QCDataRequest request = null;
	private boolean isOverwrite = false;
	private String wpsProcessId = "";
	private String subject = "OFFLINE FLAGGING STATUS - ";
	private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	String addFlagType = "";

	@Override
	public void run() {
		boolean status = addFlagSosData();
		if(wpsProcessId.equalsIgnoreCase(Constants.WPS_PROC_APPROVEDATA)) {
			subject = "OFFLINE DATA APPROVAL STATUS - ";
		}
		StringBuffer message = new StringBuffer();
		Date date = new Date();
		StringBuilder info = new StringBuilder();
		info.append("<html><table border=\"1\">");
		info.append("<tr><th>Sensor</th><th>Property</th></tr>");
		if (addFlagType.equalsIgnoreCase("RULE")) {
			String sensor = request.getOptions().getSensor();
			String property = request.getOptions().getProperty();
			info.append("<tr><td>" + sensor + "</td><td>" + property + "</td></tr>");
		}
		else {
			Iterator it = request.getOptions().getSensorPropertyList().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry) it.next();
				String val = pairs.getValue().toString();
				String[] valuePair = val.split("#");
				String sensor = valuePair[0].toString();
				String property = valuePair[1].toString();
				info.append("<tr><td>" + sensor + "</td><td>" + property + "</td></tr>");
			}
		}
		info.append("</table></html>");

		StringBuilder dtInfo = new StringBuilder();
		dtInfo.append("<p><b>Start</b> : " + request.getOptions().getStartRange()).append("\n");
		dtInfo.append("<b>End</b> : " + request.getOptions().getEndRange()).append("\n");
		if (status) {
			LOG.debug("OFFLINE WPS is COMPLETED.........................................................");
			message.append("The offline flagging/approval of the following data is SUCCESSFUL: ").append("<p>");
			message.append(info.toString()).append("\n");
			message.append(dtInfo.toString());
		} else {
			LOG.debug("OFFLINE WPS is NOT COMPLETED......................................................");
			message.append("The offline flagging/approval of the following data is FAILED: ").append("<p>");
			message.append(info.toString()).append("\n");
			message.append(dtInfo.toString());
		}

		String attachFile = null; // to-do include pdf url
		SendMail.send(this.email, subject + dateFormat.format(date), message.toString(), Constants.EMAIL_FROM, Constants.EMAIL_PWD, attachFile);
	}

	public WPSExecuter(String session, QCDataRequest req, String pid, boolean isOverwriteFlg, String eml) {
		this.sessionId = session;
		this.email = eml;
		this.request = req;
		this.wpsProcessId = pid;
		this.isOverwrite = isOverwriteFlg;
		this.addFlagType = request.getOptions().getFlaggingType();
	}

	public boolean addFlagSosData() {
		boolean addStatus = false;
		String usr = this.sessionId;
		String genericFlag = "";
		String specificFlag = "";
		Long flagId = null;
		String begin = null;
		String end = null;
		begin = request.getOptions().getStartRange();
		end = request.getOptions().getEndRange();
		
		if (!addFlagType.equalsIgnoreCase("APPROVE")) {
			Map<String, Long> qualityFlagMappingWithId = ObservationData.getInstance().getQualifiers();
			genericFlag = request.getOptions().getGenericFlag();
			specificFlag = request.getOptions().getSpecificFlag();
			flagId = getFlagMappingId(qualityFlagMappingWithId, genericFlag, specificFlag);
		}

		if (addFlagType.equalsIgnoreCase("RULE")) {
			String sensor = request.getOptions().getSensor();
			String property = request.getOptions().getProperty();
			Map<String, Object> inputView = new HashMap<String, Object>();
			inputView.put("sessionId", usr);
			inputView.put("flag", String.valueOf(flagId));
			inputView.put("phenomenon", property);
			inputView.put("procedure", sensor);
			inputView.put("begin", begin);
			inputView.put("end", end);
			LOG.debug(wpsProcessId +": "+ inputView.toString() + " isOverwrite=" + isOverwrite);
			String status = executeAddFlagProcess(wpsProcessId, inputView, isOverwrite);
			addStatus = Boolean.parseBoolean(status);
		} else if (addFlagType.equalsIgnoreCase("TIMERANGE") || addFlagType.equalsIgnoreCase("VIEW")) {
			Map<String, String> sensorPropertyMap = request.getOptions().getSensorPropertyList();
			Iterator it = sensorPropertyMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry) it.next();
				// String key = pairs.getKey().toString();
				String val = pairs.getValue().toString();
				String[] valuePair = val.split("#");
				String sensorVal = valuePair[0].toString();
				String propertyVal = valuePair[1].toString();
				Map<String, Object> inputView = new HashMap<String, Object>();
				inputView.put("sessionId", usr);
				inputView.put("flag", String.valueOf(flagId));
				inputView.put("phenomenon", propertyVal);
				inputView.put("procedure", sensorVal);
				inputView.put("begin", begin);
				inputView.put("end", end);
				LOG.debug(wpsProcessId +": "+ inputView.toString() + " isOverwrite=" + isOverwrite);
				String status = executeAddFlagProcess(wpsProcessId, inputView, isOverwrite);
				addStatus = Boolean.parseBoolean(status);
			}
		} else if (addFlagType.equalsIgnoreCase("APPROVE")) {
			Map<String, String> sensorPropertyMap = request.getOptions().getSensorPropertyList();
			Iterator it = sensorPropertyMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry) it.next();
				// String key = pairs.getKey().toString();
				String val = pairs.getValue().toString();
				String[] valuePair = val.split("#");
				String sensorVal = valuePair[0].toString();
				String propertyVal = valuePair[1].toString();
				Map<String, Object> inputView = new HashMap<String, Object>();
				inputView.put("sessionId", usr);
				inputView.put("phenomenon", propertyVal);
				inputView.put("procedure", sensorVal);
				inputView.put("begin", begin);
				inputView.put("end", end);
				LOG.debug(wpsProcessId +": "+ inputView.toString());
				String status = executeAddFlagProcess(wpsProcessId, inputView, isOverwrite);
				addStatus = Boolean.parseBoolean(status);
			}
		}
		return addStatus;
	}

	private String executeAddFlagProcess(String pid, Map<String, Object> inputs, boolean isOverwrite) {
		ProcessDescriptionType describeProcessDocument = null;
		IData data = null;
		try {
			describeProcessDocument = requestDescribeProcess(pid);

		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			data = executeProcess(describeProcessDocument, inputs, "booleanResult");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Object procOutput = data.getPayload();
		LOG.debug("WPSExecuter - Adding flag response: " + String.valueOf(procOutput) + " isOverwrite=" + isOverwrite);
		return String.valueOf(procOutput);
	}

	private Long getFlagMappingId(Map<String, Long> qualityFlagMappingWithId, String gFlag, String sFlag) {
		String key = gFlag + "_" + sFlag;
		Long id = qualityFlagMappingWithId.get(key);
		return id;
	}

	/*
	 * public Map<Long, String> getProcessingStatiSosData(String pid,
	 * HashMap<String, Object> inputs) { Map<Long, String> dataLevelMapping =
	 * new HashMap<Long, String>();
	 * 
	 * ProcessDescriptionType describeProcessDocument = null; IData data = null;
	 * try { describeProcessDocument = requestDescribeProcess(pid);
	 * 
	 * } catch (IOException e) { e.printStackTrace(); } try { data =
	 * executeProcess(describeProcessDocument, inputs, "stringResult"); } catch
	 * (Exception e) { // TODO Auto-generated catch block e.printStackTrace(); }
	 * String procOutput = (String) data.getPayload();
	 * 
	 * // split data level, store in the hashmap // 1,1;5,3;4,2c;2,2a;3,2b
	 * String[] dataLevelPair = procOutput.split(";"); // convert pairs into
	 * hashmap for (int i = 0; i < dataLevelPair.length; i++) { String[] elem =
	 * dataLevelPair[i].split(","); String key = elem[0]; String value =
	 * elem[1]; dataLevelMapping.put(Long.valueOf(key), value);
	 * 
	 * }
	 * 
	 * return dataLevelMapping; }
	 * 
	 * public Map<String, ArrayList<String>> getQualityFlagsSoSData(String pid,
	 * HashMap<String, Object> inputs) throws Exception { ProcessDescriptionType
	 * describeProcessDocument = null; IData data = null; try {
	 * describeProcessDocument = requestDescribeProcess(pid);
	 * 
	 * } catch (IOException e) { e.printStackTrace(); } data =
	 * executeProcess(describeProcessDocument, inputs, "stringResult"); String
	 * procOutput = (String) data.getPayload(); storeQualityFlags(procOutput);
	 * 
	 * Map<String, ArrayList<String>> qMap = new HashMap<String,
	 * ArrayList<String>>(); Iterator it =
	 * qualityFlagMapping.entrySet().iterator(); while (it.hasNext()) {
	 * Map.Entry pairs = (Map.Entry) it.next(); String qualifiers[] =
	 * pairs.getValue().toString().split(","); String genericFlag =
	 * qualifiers[0]; String specificFlag = qualifiers[1]; ArrayList<String>
	 * value = qMap.get(genericFlag); if (value != null &&
	 * !(genericFlag.equals(specificFlag))) { value.add(specificFlag); } else {
	 * ArrayList<String> specFlagList = new ArrayList<String>();
	 * specFlagList.add(specificFlag); qMap.put(genericFlag, specFlagList); } }
	 * 
	 * return qMap; }
	 * 
	 * 
	 * private void storeQualityFlags(String getQualProcOuput) { String[]
	 * flagPair = getQualProcOuput.split(";");
	 * 
	 * // convert pairs into hashmap for (int i = 0; i < flagPair.length; i++) {
	 * String[] elem = flagPair[i].split(","); String key = elem[0]; String
	 * value = elem[1].trim() + "," + elem[2].trim(); //
	 * genericFlagsWithIds.put(key, elem[1].toString()); // create hashmap with
	 * id,generic flags qualityFlagMapping.put(Long.valueOf(key), value);
	 * 
	 * } }
	 */

	private static ProcessDescriptionType requestDescribeProcess(String processId) throws IOException {

		WPSClientSession wpsClient = WPSClientSession.getInstance();

		ProcessDescriptionType processDescription = wpsClient.getProcessDescription(Constants.WPS_URL, processId);

		//OutputDescriptionType[] complexDesc = processDescription.getProcessOutputs().getOutputArray();

		/*
		 * for (OutputDescriptionType input : complexDesc) {
		 * LOG.debug("WPSExecuter - Output:" +
		 * input.getIdentifier().getStringValue()); }
		 * 
		 * InputDescriptionType[] inputList =
		 * processDescription.getDataInputs().getInputArray();
		 * 
		 * for (InputDescriptionType input : inputList) {
		 * LOG.debug("WPSExecuter - Input:" + processId + ":" +
		 * input.getIdentifier().getStringValue()); }
		 */
		return processDescription;
	}

	private IData executeProcess(ProcessDescriptionType processDescription, Map<String, Object> inputs, String outputName) throws Exception {
		ExecuteRequestBuilder executeBuilder = new ExecuteRequestBuilder(processDescription);

		for (InputDescriptionType input : processDescription.getDataInputs().getInputArray()) {
			String inputName = input.getIdentifier().getStringValue();
			Object inputValue = inputs.get(inputName);
			if (input.getLiteralData() != null) {
				if (inputValue instanceof String) {
					LOG.debug("WPSExecuter - Executing Process: " + inputName + "   " + (String) inputValue);
					executeBuilder.addLiteralData(inputName, (String) inputValue);
				}
			} else if (input.getComplexData() != null) {
				// Complexdata by value
				if (inputValue instanceof FeatureCollection) {
					IData data = new GTVectorDataBinding((FeatureCollection) inputValue);
					executeBuilder.addComplexData(inputName, data, "http://schemas.opengis.net/gml/3.1.1/base/feature.xsd", null, "text/xml");
				}
				// Complexdata Reference
				if (inputValue instanceof String) {
					executeBuilder.addComplexDataReference(inputName, (String) inputValue, "http://schemas.opengis.net/gml/3.1.1/base/feature.xsd", null, "text/xml");
				}
				if (inputValue == null && input.getMinOccurs().intValue() > 0) {
					throw new IOException("Property not set, but mandatory: " + inputName);
				}
			}
		}

		executeBuilder.getExecute().getExecute().addNewResponseForm().addNewResponseDocument().addNewOutput().addNewIdentifier().setStringValue(outputName);
		executeBuilder.getExecute().getExecute().setService("WPS");
		// System.out.println("WPSExecuter -executeProcess Request: " +
		// executeBuilder.getExecute().xmlText());

		WPSClientSession wpsClient = WPSClientSession.getInstance();
		Object responseObject = wpsClient.execute(Constants.WPS_URL, executeBuilder.getExecute());

		if (responseObject instanceof ExecuteResponseDocument) {
			ExecuteResponseDocument response = (ExecuteResponseDocument) responseObject;
			ExecuteResponseAnalyser analyser = new ExecuteResponseAnalyser(executeBuilder.getExecute(), response, processDescription);
			String output = response.getExecuteResponse().getProcessOutputs().getOutputArray(0).getData().getLiteralData().getStringValue();

			IData data = new LiteralStringBinding(output);
			return data;
		}
		throw new Exception("Exception: " + responseObject.toString());
	}

	private static CapabilitiesDocument requestGetCapabilities() throws WPSClientException {

		WPSClientSession wpsClient = WPSClientSession.getInstance();
		wpsClient.connect(Constants.WPS_URL);
		CapabilitiesDocument capabilities = wpsClient.getWPSCaps(Constants.WPS_URL);
		ProcessBriefType[] processList = capabilities.getCapabilities().getProcessOfferings().getProcessArray();

		/*
		 * for (ProcessBriefType process : processList) {
		 * System.out.println("WPSExecuter-requestGetCapabilities():" +
		 * process.getIdentifier().getStringValue()); }
		 */
		return capabilities;
	}

}
