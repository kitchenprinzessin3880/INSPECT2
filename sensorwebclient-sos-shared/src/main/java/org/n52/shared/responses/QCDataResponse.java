package org.n52.shared.responses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eesgmbh.gimv.shared.util.Bounds;
import org.eesgmbh.gimv.shared.util.ImageEntity;
import org.n52.shared.serializable.pojos.Axis;
import org.n52.shared.serializable.pojos.DesignOptions;
import org.n52.shared.serializable.pojos.TimeSeriesProperties;

public class QCDataResponse extends RepresentationResponse {

	private static final long serialVersionUID = -1036816874508351530L;

	private String imageUrl;

	private Bounds plotArea;

	private ImageEntity[] imageEntities;

	private HashMap<String, Axis> tsAxis;

	private String overviewurl;

	private int width;

	private int height;

	private long begin;

	private long end;

	private ArrayList<TimeSeriesProperties> propertiesList;

	private Map<String, Map<Integer, List<String>>> qualTimeMap = null;

	private ArrayList<String> flagList;

	private Map<String, String> renderCoord;

	private Map<String, ArrayList<String>> qualityFlagList;

	private List allSeriesShapes;

	private Map<String, String> defaultFlagShapesPair;

	private boolean statusCode;
	
	private String username ="";
	
	private Long sourceId;

	private String sourceCode;
	
	private String email;

	private Map<String, ArrayList<String>> allowedSitePropertiesList;


	private QCDataResponse() {
		// for serialization
	}
	
	public QCDataResponse(boolean code) {
		// for serialization
		this.statusCode = code;
	}

	public QCDataResponse(ArrayList<String> flags) {
		// for serialization
		this.flagList = flags;
	}

	public QCDataResponse(String user, Long sourceId2, String eml, String sCode, Map<String, ArrayList<String>> flags, List shapes, Map<String, String> map, Map<String, ArrayList<String>> allowedList) {
		this.username=user;
		this.qualityFlagList = flags;
		this.allSeriesShapes = shapes;
		this.defaultFlagShapesPair = map;
		this.sourceId = sourceId2;
		this.sourceCode= sCode;
		this.allowedSitePropertiesList = allowedList;
		this.email= eml;
	}

	// ASD
	public QCDataResponse(String imageUrl, DesignOptions options, Bounds plotArea, HashMap<String, Axis> tsAxis) {
		this.imageUrl = imageUrl;
		this.begin = options.getBegin();
		this.end = options.getEnd();
		this.width = options.getWidth();
		this.height = options.getHeight();
		this.propertiesList = options.getProperties();
		this.plotArea = plotArea;
		this.tsAxis = tsAxis;
	}

	// ASD
	public QCDataResponse(String imageUrl, HashMap<String, Axis> tsAxis) {
		this.imageUrl = imageUrl;
		this.tsAxis = tsAxis;
	}

	public QCDataResponse(String imageUrl, DesignOptions options, Bounds plotArea, ImageEntity[] imageEntities, HashMap<String, Axis> tsAxis, Map<String, Map<Integer, List<String>>> qualMap) {
		this.imageUrl = imageUrl;
		this.begin = options.getBegin();
		this.end = options.getEnd();
		this.width = options.getWidth();
		this.height = options.getHeight();
		this.propertiesList = options.getProperties();
		this.imageEntities = imageEntities;
		this.plotArea = plotArea;
		this.tsAxis = tsAxis;
		this.qualTimeMap = qualMap;
	}

	public QCDataResponse(String imageUrl, DesignOptions options, Bounds plotArea, ImageEntity[] imageEntities, HashMap<String, Axis> tsAxis, Map<String, Map<Integer, List<String>>> qualMap, Map<String, String> r) {
		this.imageUrl = imageUrl;
		this.begin = options.getBegin();
		this.end = options.getEnd();
		this.width = options.getWidth();
		this.height = options.getHeight();
		this.propertiesList = options.getProperties();
		this.imageEntities = imageEntities;
		this.plotArea = plotArea;
		this.tsAxis = tsAxis;
		this.qualTimeMap = qualMap;
		this.renderCoord = r;
	}

	public QCDataResponse(String imageUrl, DesignOptions options, Bounds plotArea, ImageEntity[] imageEntities, HashMap<String, Axis> tsAxis, Map<String, Map<Integer, List<String>>> qualMap, List<String> timePoint) {
		this.imageUrl = imageUrl;
		this.begin = options.getBegin();
		this.end = options.getEnd();
		this.width = options.getWidth();
		this.height = options.getHeight();
		this.propertiesList = options.getProperties();
		this.imageEntities = imageEntities;
		this.plotArea = plotArea;
		this.tsAxis = tsAxis;
		this.qualTimeMap = qualMap;
	}

	public QCDataResponse(String imageUrl, DesignOptions options, Bounds plotArea, ImageEntity[] imageEntities, HashMap<String, Axis> tsAxis) {
		this.imageUrl = imageUrl;
		this.begin = options.getBegin();
		this.end = options.getEnd();
		this.width = options.getWidth();
		this.height = options.getHeight();
		this.propertiesList = options.getProperties();
		this.imageEntities = imageEntities;
		this.plotArea = plotArea;
		this.tsAxis = tsAxis;
	}

	public void destroy() {
		this.imageEntities = null;
		this.propertiesList.clear();
		this.propertiesList = null;
		this.tsAxis.clear();
		this.tsAxis = null;
	}

	public ArrayList<TimeSeriesProperties> getPropertiesList() {
		return this.propertiesList;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public String getOverviewUrl() {
		return this.overviewurl;
	}

	public HashMap<String, Axis> getAxis() {
		return this.tsAxis;
	}

	public Bounds getPlotArea() {
		return this.plotArea;
	}

	public ImageEntity[] getImageEntities() {
		return this.imageEntities;
	}

	public Map<String, Map<Integer, List<String>>> getQualityTimeMap() {
		return this.qualTimeMap;
	}

	public String getImageUrl() {
		return this.imageUrl;
	}

	public long getBegin() {
		return this.begin;
	}

	public long getEnd() {
		return this.end;
	}

	public ArrayList<String> getFlagsList() {
		return this.flagList;
	}

	public Map<String, ArrayList<String>> getCompleteFlagsList() {
		return this.qualityFlagList;
	}

	public Map<String, String> getResponseCoord() {
		return this.renderCoord;
	}

	public List getAllSeriesShapes() {
		return allSeriesShapes;
	}

	public void setAllSeriesShapes(List allSeriesShapes) {
		this.allSeriesShapes = allSeriesShapes;
	}

	public Map<String, String> getDefaultFlagShapesPair() {
		return defaultFlagShapesPair;
	}

	public void setDefaultFlagShapesPair(Map<String, String> defaultFlagShapesPair) {
		this.defaultFlagShapesPair = defaultFlagShapesPair;
	}
	
	public boolean getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(boolean cd) {
		this.statusCode = cd;
	}


	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Long getSourceId() {
		return sourceId;
	}

	public void setSourceId(Long scId) {
		this.sourceId = scId;
	}


	public String getSourceCode() {
		return sourceCode;
	}

	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}
	
	public Map<String, ArrayList<String>> getAllowedSitePropertiesList() {
		return allowedSitePropertiesList;
	}

	public void setAllowedSitePropertiesList(Map<String, ArrayList<String>> allowedSitePropertiesList) {
		this.allowedSitePropertiesList = allowedSitePropertiesList;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
