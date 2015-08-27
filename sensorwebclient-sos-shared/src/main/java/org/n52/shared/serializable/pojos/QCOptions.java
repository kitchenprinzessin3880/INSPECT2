package org.n52.shared.serializable.pojos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QCOptions implements Serializable {

	private static final long serialVersionUID = -3922742599500705640L;
	private ArrayList<TimeSeriesProperties> properties; 
	private String begin = "";

	private String end = "";

	private int height;

	private int width;

	private String timePoint = "";

	private String sensor = "";

	private String property = "";

	private ArrayList<String> sensorList;

	private ArrayList<String> propertyList;

	private String sessionId = "";

	private String flaggingType = "";

	private Map<String, String> sensorPropertyList;

	private Long sourceId;

	private String session;

	private String genericFlag = "";

	private String specificFlag = "";

	private Map<String, String> flagSymbolMap = new HashMap<String, String>();

	private String timeSeriesId = "";

	Map<String, String> multiPointsMap = null;
	
	private String email ="";
	
	private double ruleValue;


	private String operator;

	//ASD 17.11 2014
	private String wpsUrl;
	private String wpsPwd;
    private String wpsUser;
    private String authenthicateUrl;
    
	private QCOptions() {
		// do nothin
	}

	public QCOptions(HashMap<String, String> flgSymlMap) {
		this.flagSymbolMap = flgSymlMap;
	}

	//ASD 17.11.2014
	public QCOptions(String url, String usr, String pwd, String authen) {
		this.wpsUrl = url;
		this.wpsUser = usr;
		this.wpsPwd = pwd;
		this.authenthicateUrl =authen;
	}
	
	public QCOptions(String type, Long usr, String time, String prop, String sen, String gFlg, String sFlg) {
		this.timePoint = time;
		this.sourceId = usr;
		this.property = prop;
		this.sensor = sen;
		this.flaggingType = type;
		this.genericFlag = gFlg;
		this.specificFlag = sFlg;
	}

	public QCOptions(String type, String id, Long usr, String time, String prop, String sen, String gFlg, String sFlg) {
		this.timePoint = time;
		this.sourceId = usr;
		this.property = prop;
		this.sensor = sen;
		this.flaggingType = type;
		this.genericFlag = gFlg;
		this.specificFlag = sFlg;
		this.timeSeriesId = id;
	}

	// TV and TR
	public QCOptions(String type, Long usr,String eml, String s, String e, HashMap<String, String> sensorPropertyMap, String gFlg, String sFlg) {
		this.sourceId = usr;
		this.begin = s;
		this.end = e;
		this.sensorPropertyList = sensorPropertyMap;
		this.flaggingType = type;
		this.genericFlag = gFlg;
		this.specificFlag = sFlg;
		this.email= eml;
	}
	
	// Approve series
		public QCOptions(String type, Long usr,String eml, String s, String e, HashMap<String, String> sensorPropertyMap) {
			this.sourceId = usr;
			this.begin = s;
			this.end = e;
			this.sensorPropertyList = sensorPropertyMap;
			this.flaggingType = type;
			this.email= eml;
		}

	//TV-Rules
	public QCOptions(String type, Long usr,String eml, String tsid,String sense, String prop, String st, String ed, String gFlg, String sFlg, double val, String ope) {
		this.sourceId = usr;
		this.begin = st;
		this.end = ed;
		this.timeSeriesId = tsid;
		this.sensor= sense;
		this.property= prop;
		this.flaggingType = type;
		this.genericFlag = gFlg;
		this.specificFlag = sFlg;
		this.email= eml;
		this.ruleValue = val;
		this.operator=ope;
	}

	public QCOptions(String type, Long usr, String s, String e, HashMap<String, String> sensorPropertyMap) {
		this.sourceId = usr;
		this.begin = s;
		this.end = e;
		this.sensorPropertyList = sensorPropertyMap;
		this.flaggingType = type;
	}

	// multipoint
	public QCOptions(String type, Long usr, String eml,HashMap<String, String> gridMap) {
		this.sourceId = usr;
		this.multiPointsMap = gridMap;
		this.flaggingType = type;
		this.email= eml;
	}

	public QCOptions(String session, String sensor) {
		this.sessionId = session;
		this.sensor = sensor;
	}

	public QCOptions(ArrayList<String> stations, ArrayList<String> properties) {
		this.sensorList = stations;
		this.propertyList = properties;
	}

	public String getFlaggingType() {
		return flaggingType;
	}

	public void setFlaggingType(String flaggingType) {
		this.flaggingType = flaggingType;
	}

	public String getTimePoint() {
		return this.timePoint;
	}

	public String getStartRange() {
		return this.begin;
	}

	public String getEndRange() {
		return this.end;
	}

	public Long getUser() {
		return this.sourceId;
	}

	public String getProperty() {
		return this.property;
	}

	public String getSensor() {
		return this.sensor;
	}

	public ArrayList<String> getSensorList() {
		return sensorList;
	}

	public void setSensorList(ArrayList<String> sensorList) {
		this.sensorList = sensorList;
	}

	public ArrayList<String> getPropertyList() {
		return propertyList;
	}

	public void setPropertyList(ArrayList<String> propertyList) {
		this.propertyList = propertyList;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String printRangeRequest() {
		StringBuilder sb = new StringBuilder("QCOptions [");
		sb.append("user: ").append(sourceId).append(", ");
		sb.append("begin: ").append(begin).append(", ");
		sb.append("end: ").append(end).append(", ");
		sb.append("sensor-property: ").append(sensorPropertyList.toString()).append(", ");
		sb.append("flag: ").append(genericFlag).append(",").append(specificFlag).append("]");
		return sb.toString();
	}

	public String prinMultiPointsRequest() {
		StringBuilder sb = new StringBuilder("QCOptions [");
		sb.append("user: ").append(sourceId).append(", ");
		sb.append("sensor-property-all: ").append(multiPointsMap.toString()).append("]");
		return sb.toString();
	}

	public String printViewRequest() {
		StringBuilder sb = new StringBuilder("QCOptions [");
		sb.append("user: ").append(sourceId).append(", ");
		sb.append("begin: ").append(begin).append(", ");
		sb.append("end: ").append(end).append(", ");
		sb.append("sensor-property: ").append(sensorPropertyList.toString()).append(", ");
		sb.append("flag: ").append(genericFlag).append(",").append(specificFlag).append("]");
		return sb.toString();
	}
	
	public String printApproveRequest() {
		StringBuilder sb = new StringBuilder("QCOptions [");
		sb.append("user: ").append(sourceId).append(", ");
		sb.append("begin: ").append(begin).append(", ");
		sb.append("end: ").append(end).append(", ");
		sb.append("sensor-property: ").append(sensorPropertyList.toString()).append("] ");
		return sb.toString();
	}
	
	public String printRuleRequest() {
		StringBuilder sb = new StringBuilder("QCOptions [");
		sb.append("user: ").append(sourceId).append(", ");
		sb.append("begin: ").append(begin).append(", ");
		sb.append("end: ").append(end).append(", ");
		sb.append("property: ").append(property).append(", ");
		sb.append("sensor: ").append(sensor).append(", ");
		sb.append("operator: ").append(operator).append(", ");
		sb.append("value: ").append(ruleValue).append(", ");
		sb.append("flag: ").append(genericFlag).append(",").append(specificFlag).append("]");
		return sb.toString();
	}

	public String printPointRequest() {
		StringBuilder sb = new StringBuilder("QCOptions [");
		sb.append("user: ").append(sourceId).append(", ");
		sb.append("time: ").append(timePoint).append(", ");
		sb.append("property: ").append(property).append(", ");
		sb.append("sensor: ").append(sensor).append(", ");
		sb.append("id: ").append(timeSeriesId).append(", ");
		sb.append("flag: ").append(genericFlag).append(",").append(specificFlag).append("]");
		return sb.toString();
	}

	public String printAll() {
		StringBuilder sb = new StringBuilder("QCOptions [");
		sb.append("user: ").append(sourceId).append(", ");
		sb.append("begin: ").append(begin).append(", ");
		sb.append("end: ").append(end).append(", ");
		sb.append("timeinstance: ").append(timePoint).append(", ");
		sb.append("property: ").append(property).append(", ");
		sb.append("sensor: ").append(sensor).append(", ");
		sb.append("flag: ").append(genericFlag).append(",").append(specificFlag).append("]");
		return sb.toString();
	}

	public String getGenericFlag() {
		return genericFlag;
	}

	public void setGenericFlag(String genericFlag) {
		this.genericFlag = genericFlag;
	}

	public String getSpecificFlag() {
		return specificFlag;
	}

	public void setSpecificFlag(String specificFlag) {
		this.specificFlag = specificFlag;
	}

	public Map<String, String> getSensorPropertyList() {
		return sensorPropertyList;
	}

	public void setSensorPropertyList(Map<String, String> sensorPropertyList) {
		this.sensorPropertyList = sensorPropertyList;
	}

	public Map<String, String> getFlagSymbolMap() {
		return flagSymbolMap;
	}

	public void setFlagSymbolMap(Map<String, String> activeFlagSymbolMap) {
		this.flagSymbolMap = activeFlagSymbolMap;
	}

	public String getTimeSeriesId() {
		return timeSeriesId;
	}

	public Map<String, String> getMultiPointsMap() {
		return multiPointsMap;
	}

	public void setMultiPointsMap(Map<String, String> multiPointsMap) {
		this.multiPointsMap = multiPointsMap;
	}

	public void setHeight(int h) {
		this.height = h;
	}

	public void setWidth(int w) {
		this.width = w;
	}

	public int getHeight() {
		return this.height;
	}

	public int getWidth() {
		return this.width;
	}
	
    public ArrayList<TimeSeriesProperties> getProperties() {
        return this.properties;
    }

    public void setProperties(ArrayList<TimeSeriesProperties> properties) {
		this.properties = properties;
	}


	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public double getRuleValue() {
		return ruleValue;
	}

	public void setRuleValue(double ruleValue) {
		this.ruleValue = ruleValue;
	}
	

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}
	
	public String getWpsUrl() {
		return wpsUrl;
	}

	public void setWpsUrl(String wpsUrl) {
		this.wpsUrl = wpsUrl;
	}

	public String getWpsPwd() {
		return wpsPwd;
	}

	public void setWpsPwd(String wpsPwd) {
		this.wpsPwd = wpsPwd;
	}

	public String getWpsUser() {
		return wpsUser;
	}

	public void setWpsUser(String wpsUser) {
		this.wpsUser = wpsUser;
	}

	public String getAuthenthicateUrl() {
		return authenthicateUrl;
	}

	public void setAuthenthicateUrl(String authenthicateUrl) {
		this.authenthicateUrl = authenthicateUrl;
	}


}
