package org.n52.shared.serializable.pojos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.Serializable;
import org.eesgmbh.gimv.shared.util.Bounds;
import org.eesgmbh.gimv.shared.util.ImageEntity;
import org.n52.shared.serializable.pojos.Axis;
import org.n52.shared.serializable.pojos.DesignOptions;
import org.n52.shared.serializable.pojos.TimeSeriesProperties;

public class ResponseOptions implements Serializable {
	private static final long serialVersionUID = -3922742599500705640L;
	private String imageUrl;

	private Bounds plotArea;

	private List<ImageEntity> imageEntities;

	private HashMap<String, Axis> tsAxis;

	private String overviewurl;

	private int width;

	private int height;

	private long begin;

	private long end;

	private ArrayList<TimeSeriesProperties> propertiesList;

	private Map<String, Map<Double, List<String>>> qualTimeMap = null;

	private ArrayList<String> flagList;

	private Map<String, String> renderCoord;

	private ResponseOptions() {
		// do nothin
	}

	// ASD - overview diagram
	public ResponseOptions(String imageUrl, DesignOptions options, Bounds plotArea, HashMap<String, Axis> tsAxis) {
		this.imageUrl = imageUrl;
		this.begin = options.getBegin();
		this.end = options.getEnd();
		this.width = options.getWidth();
		this.height = options.getHeight();
		this.propertiesList = options.getProperties();
		this.plotArea = plotArea;
		this.tsAxis = tsAxis;
	}

	// main diagram w/o quality control
	public ResponseOptions(String imageUrl, DesignOptions options, Bounds plotArea, List<ImageEntity> imageEntities, HashMap<String, Axis> tsAxis, Map<String, Map<Double, List<String>>> qualMap) {
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

	// Quality control - main diagram
	public ResponseOptions(String imageUrl, DesignOptions options, Bounds plotArea, List<ImageEntity> imageEntities, HashMap<String, Axis> tsAxis, Map<String, Map<Double, List<String>>> qualMap, Map<String, String> r) {
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

	public List<ImageEntity> getImageEntities() {
		return this.imageEntities;
	}

	public Map<String, Map<Double, List<String>>> getQualityTimeMap() {
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

	public Map<String, String> getResponseCoord() {
		return this.renderCoord;
	}

}
