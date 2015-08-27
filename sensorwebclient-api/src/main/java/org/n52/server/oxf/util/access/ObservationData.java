package org.n52.server.oxf.util.access;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.fzj.ibg.odm.tables.datavalues.DataValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObservationData {

	private static ObservationData instance;
	private static Map<String, TreeMap<Date, DataValue>> localObservationData = null;
	private static Map<String, HashMap<String, String>> dataDirectoryMap = null;
	private static Map<Long, String> processingLevel = null;
	private static Map<String, Long> qualifiers = null;
	// private Map<String, HashMap<String, ArrayList<String>>>
	// dataWithQualifierMap =null;
	private static final Logger LOG = LoggerFactory.getLogger(ObservationData.class);

	public static ObservationData getInstance() {
		if (instance == null) {
			instance = new ObservationData();
		}
		return instance;
	}

	private ObservationData() {
		dataDirectoryMap = new HashMap<String, HashMap<String, String>>();
		localObservationData = new HashMap<String, TreeMap<Date, DataValue>>();
		processingLevel = new HashMap<Long, String>();
		qualifiers = new HashMap<String, Long>();
	}

	private void clearLocalObservationData() {
		this.localObservationData.clear();
	}

	public static Map<String, HashMap<String, String>> getDataDirectoryMap() {
		return dataDirectoryMap;
	}

	public static void setDataDirectoryMap(Map<String, HashMap<String, String>> dataDirectoryMap) {
		ObservationData.dataDirectoryMap = dataDirectoryMap;
	}

	public static Map<Long, String> getProcessingLevel() {
		return processingLevel;
	}

	/*
	public Long getProcessingLevelIdByName(String name) {
		Iterator it = processingLevel.entrySet().iterator();
		Long pId = null;
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			if (pairs.getValue().equals(name)) {
				pId = (Long) pairs.getKey();
				break;
			}
		}
		return pId;
	}*/

	public static void setProcessingLevel(Map<Long, String> processingLevel) {
		ObservationData.processingLevel = processingLevel;
	}

	public static Map<String, Long> getQualifiers() {
		return qualifiers;
	}

	public static void setQualifiers(Map<String, Long> qualifiers2) {
		ObservationData.qualifiers = qualifiers2;
	}
	public static Map<String, TreeMap<Date, DataValue>> getLocalObservationData() {
		return localObservationData;
	}

	public static void setLocalObservationData(Map<String, TreeMap<Date, DataValue>> localObservationData) {
		ObservationData.localObservationData = localObservationData;
	}

}
