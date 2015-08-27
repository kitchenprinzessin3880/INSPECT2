package org.n52.server.oxf.util.access;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.fzj.ibg.odm.tables.datavalues.DataValue;
import org.fzj.ibg.odm.tables.datavalues.ProcessingStatus;
import org.fzj.ibg.odm.tables.datavalues.QualifierGroup;
import org.fzj.ibg.odm.tables.management.DataDirectory;
import org.fzj.ibg.odm.tables.management.SensorDataStatus;
import org.fzj.ibg.odm.tables.management.Source;
import org.fzj.ibg.odm.tables.sites.SitesLog;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBOperation {
	private static final Logger LOG = LoggerFactory.getLogger(DBOperation.class);
	private static DBOperation instance;
	private static ObservationData dataInstance = null;
	private static String sourceUserCode = null;
	private String email;
	protected static int processingStatusLevel1;
	protected static int processingStatusLevel2a;
	protected static int processingStatusLevel2b;
	protected static int processingStatusLevel2c;
	protected static int unevaluatedId;
	protected static Long processingStatus2cLong;

	public static DBOperation getInstance() {
		if (instance == null) {
			dataInstance = ObservationData.getInstance();
			instance = new DBOperation();
		}
		return instance;
	}

	private DBOperation() {
		getDataClassFromDb();
	}

	public Long getSourcefromUsername(String aidaUserName) {
		// get source id, get sensors can be flagged, sent to client
		Long source = null;
		String sourceCode = null;
		Session session = HibernateUtil.getInstance().getSessionFactory().getCurrentSession();
		Transaction tx = null;

		try {
			tx = session.beginTransaction();
			Query query = session.createQuery("from Source where externalaccountname = :ext ");
			query.setParameter("ext", aidaUserName);
			List<Source> rs = query.list();
			if (rs.size() > 0) {
				Source s = (Source) rs.get(0);
				source = s.getId();
				sourceCode = s.getCode();
				email = s.getEmail();
			}
			tx.commit();
		} catch (RuntimeException e) {
			if (tx != null && tx.isActive()) {
				try {
					tx.rollback();// Second try catch as the rollback could fail
									// as well
				} catch (HibernateException e1) {
					LOG.debug("Error rolling back transaction: getSourcefromUsername()");
				}
				// throw again the first exception
				throw e;
			}
		}
		this.setSourceUserCode(sourceCode);
		return source;
	}

	public Map<String, ArrayList<String>> getAllowedSitesAndPropsByUser(Long id) {
		Session session2 = HibernateUtil.getInstance().getSessionFactory().getCurrentSession();
		Map<String, HashSet<String>> allowedSiteProps = new HashMap<String, HashSet<String>>();
		Transaction trx = null;
		try {
			trx = session2.beginTransaction();
			/*
			String sql = "select datastatus from SensorDataStatus datastatus, SensorInstance sensorinst, ResponsibilityGroup responsibilitygroup, " 
			+ "Responsibility responsibility where " + "responsibility.code = :quality and responsibilitygroup.responsibility.code = responsibility.code " 
					+ "and responsibilitygroup.source.id = :i  "
					+ "and sensorinst.sourceGroup.id = responsibilitygroup.sourceGroup.id " + "and sensorinst.id=datastatus.sensorInstance.id"; */
			String sql = "select datastatus from SensorDataStatus datastatus,SensorInstance sensorinst, ResponsibilityGroup responsibilitygroup, " 
					+ "Responsibility responsibility where " + "responsibility.code = :quality and responsibility.code=responsibilitygroup.responsibility.code " 
							+ "and responsibilitygroup.source.id = :i  "
							+ "and responsibilitygroup.sourceGroup.id=sensorinst.sourceGroup.id " 
							+ "and sensorinst.site.id = datastatus.site.id and sensorinst.id=datastatus.sensorInstance.id";
			Query q = session2.createQuery(sql);
			q.setParameter("i", id);
			q.setParameter("quality", Constants.RESPONSIBILITYQC);
			List<SensorDataStatus> result = q.list();

			if (result.size() > 0) {
				for (Iterator iterator = result.iterator(); iterator.hasNext();) {
					SensorDataStatus r = (SensorDataStatus) iterator.next();
					String site = r.getSite().getCode();
					String prop = r.getVariable().getCode();
					if (allowedSiteProps.get(site) == null) {
						HashSet<String> list = new HashSet<String>();
						list.add(prop);
						allowedSiteProps.put(site, list);
					} else {
						HashSet<String> exisList = (HashSet<String>) allowedSiteProps.get(site);
						exisList.add(prop);
					}
				}
			}
			trx.commit();
		} catch (RuntimeException e) {
			if (trx != null && trx.isActive()) {
				try {
					trx.rollback();
				} catch (HibernateException e1) {
				}
				throw e;
			}
		}

		Map<String, ArrayList<String>> allowedSitePropsUnique = new HashMap<String, ArrayList<String>>();
		// convert HashSet to ArrayList
		for (Entry<String, HashSet<String>> entry : allowedSiteProps.entrySet()) {
			String key = entry.getKey();
			HashSet<String> value = entry.getValue();
			ArrayList<String> list = new ArrayList<String>(value);
			allowedSitePropsUnique.put(key, list);

		}
		return allowedSitePropsUnique;
	}

	public Map<Long, String> requestProcessingStatiFromDb() {
		Map<Long, String> processingLevel = new HashMap<Long, String>();
		Session session = HibernateUtil.getInstance().getSessionFactory().getCurrentSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(ProcessingStatus.class);
			List<ProcessingStatus> result = criteria.list();

			for (Iterator iterator = result.iterator(); iterator.hasNext();) {
				ProcessingStatus ps = (ProcessingStatus) iterator.next();
				String levelName = ps.getAbbreviation();
				if (levelName.equalsIgnoreCase(Constants.QUALIFY_CONTROL_LEVEL_RAW_DATA)) {
					processingStatusLevel1 = Integer.valueOf(Long.toString(ps.getId()));
				}
				if (levelName.equalsIgnoreCase(Constants.PROCESSING_LEVEL_2A)) {
					processingStatusLevel2a = Integer.valueOf(Long.toString(ps.getId()));
				}
				if (levelName.equalsIgnoreCase(Constants.PROCESSING_LEVEL_2B)) {
					processingStatusLevel2b = Integer.valueOf(Long.toString(ps.getId()));
				}
				if (levelName.equalsIgnoreCase(Constants.PROCESSING_LEVEL_2C)) {
					processingStatusLevel2c = Integer.valueOf(Long.toString(ps.getId()));
					// 13.08.2014
					processingStatus2cLong = ps.getId();
				}
				processingLevel.put(ps.getId(), ps.getAbbreviation());
			}
			dataInstance.setProcessingLevel(processingLevel);
			tx.commit();
		} catch (RuntimeException e) {
			if (tx != null && tx.isActive()) {
				try {
					tx.rollback();// Second try catch as the rollback could fail
									// as well
				} catch (HibernateException e1) {
					LOG.debug("Error rolling back transaction: requestProcessingStatiFromDb()");
				}
				// throw again the first exception
				throw e;
			}
		}
		return processingLevel;
	}

	public Map<String, Long> requestQualifiersFromDb() {
		Map<String, Long> qualifiers = new HashMap<String, Long>();
		Session session = HibernateUtil.getInstance().getSessionFactory().getCurrentSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(QualifierGroup.class);
			List<QualifierGroup> result = criteria.list();
			StringBuffer sb = new StringBuffer();
			for (QualifierGroup qg : result) {
				String genericFlag = qg.getGroup().getCode();
				String specificFlag = qg.getQualifier().getCode();
				qualifiers.put(genericFlag + "_" + specificFlag, qg.getId());
			}
			dataInstance.setQualifiers(qualifiers);
			tx.commit();
			String unevaluated = Constants.QUALIFIER_UNEVALUATED + "_" + Constants.QUALIFIER_UNEVALUATED;
			unevaluatedId = qualifiers.get(unevaluated).intValue();
		} catch (RuntimeException e) {
			if (tx != null && tx.isActive()) {
				try {
					tx.rollback();// Second try catch as the rollback could fail
									// as well
				} catch (HibernateException e1) {
					LOG.debug("Error rolling back transaction: requestQualifiersFromDb()");
				}
				// throw again the first exception
				throw e;
			}
		}
		return qualifiers;
	}

	private static void getDataClassFromDb() {
		Map<String, HashMap<String, String>> dataDirectoryMap = new HashMap<String, HashMap<String, String>>();
		Session session = HibernateUtil.getInstance().getSessionFactory().getCurrentSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(DataDirectory.class);
			List<DataDirectory> result = criteria.list();
			for (Iterator iterator = result.iterator(); iterator.hasNext();) {
				DataDirectory d = (DataDirectory) iterator.next();
				String site = d.getSite().getCode();
				String prop = d.getVariable().getCode();
				
				String dclass = d.getDatatableclassname();
				//ASD 17.02.2015
						//d.getDataTableClass().getCode();
				if (dataDirectoryMap.containsKey(site)) {
					dataDirectoryMap.get(site).put(prop, dclass);
				} else {
					HashMap<String, String> map = new HashMap<String, String>();
					map.put(prop, dclass);
					dataDirectoryMap.put(site, map);
				}
			}
			dataInstance.setDataDirectoryMap(dataDirectoryMap);
			tx.commit();
		} catch (RuntimeException e) {
			if (tx != null && tx.isActive()) {
				try {
					tx.rollback();// Second try catch as the rollback could fail
									// as well
				} catch (HibernateException e1) {
					LOG.debug("Error rolling back transaction: getDataClassFromDb()");
				}
				// throw again the first exception
				throw e;
			}
		}
	}

	/*
	public String verifySessionAndGetPloneUsername(String sessionId) {
		LOG.info("[DBOperation] sessionId: " + sessionId);
		String result = null;
		try {
			String url = Constants.getInstance().getUserManagementServiceUrl();
			if (url != null) {
				result = PloneAuthentication.getInstance(url).getUserBySessionCookie(sessionId);
				LOG.info("user: " + result);
			}
		} catch (Exception e) {
			try {
				throw new Exception("Error while authenticating user:", e);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		if (result == null) {
			throw new RuntimeException(new Exception("Error while verifying session: " + sessionId));
		}
		return result;
	} */

	/*
	public List<UserSiteVariablePermission> getUserPermissions(Source source) {
		Session session = HibernateUtil.getInstance().getSessionFactory().openSession();
		Transaction transaction = null;
		List<?> result = null;
		try {
			transaction = session.beginTransaction();
			result = session.createQuery("from UserSiteVariablePermission where source=:source").setParameter("source", source).list();
		} catch (HibernateException e) {
			e.printStackTrace();
		} finally {
			session.close();
		}
		return (List<UserSiteVariablePermission>) result;
	}*/

	public boolean addMultiFlagtoDB(Long sourceId, Map<String, String> sensorPropertyMapGrid, boolean isAddFlag) {
		Map<String, Long> qualityFlagMappingWithId = ObservationData.getInstance().getQualifiers();
		boolean addSuccess = false;
		Map<DataValue, QualifierGroup> data = new HashMap<DataValue, QualifierGroup>();

		for (Map.Entry<String, String> entry : sensorPropertyMapGrid.entrySet()) {
			String keys[] = entry.getKey().toString().split("#");
			String seriesId = keys[0];
			String date = keys[1];
			Date timePeriod = parseTimeStamp(date);
			String[] val = entry.getValue().toString().split("#");
			// val = sensor + "#" + property + "#" + flgArray[0].trim() + "#" +
			// flgArray[1].trim();
			// String date = val[0];
			String sensor = val[0];
			String property = val[1];
			String genericFlg = val[2];
			String specificFlg = val[3];
			String flags = genericFlg + "_" + specificFlg;

			Long flag = qualityFlagMappingWithId.get(flags);
			QualifierGroup qualifierGroup = null;
			try {
				qualifierGroup = (QualifierGroup) HibernateUtil.getInstance().loadObject(new QualifierGroup(), flag);
			} catch (Exception e) {
				LOG.error("error while getting qualifier Group for flag: " + flag, e);
				new RuntimeException(e);
			}

			TreeMap<Date, DataValue> obsData = ObservationData.getInstance().getLocalObservationData().get(seriesId);
			DataValue value = obsData.get(timePeriod);
			data.put(value, qualifierGroup);
		}

		LOG.debug("Total data to be flagged :" + data.size());
		if (data.size() > 0) {
			if (isAddFlag) {
				try {
					addSuccess = updateLevelMultiAddFlagging(data, sourceId);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					addSuccess = updateLevelMultiModifyFlagging(data, sourceId);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			addSuccess = false;
		}
		return addSuccess;

	}

	public boolean addFlagtoDB(Long sourceId, List<String> timeSeries, String[] timeArr, Long flag, boolean isAddFlag) {
		QualifierGroup qualifierGroup = null;
		try {
			qualifierGroup = (QualifierGroup) HibernateUtil.getInstance().loadObject(new QualifierGroup(), flag);
		} catch (Exception e) {
			LOG.error("error while getting qualifier Group for flag: " + flag, e);
			new RuntimeException(e);
		}
		Date beginTimeInterval = parseTimeStamp(timeArr[0]);
		Date endTimeInterval = parseTimeStamp(timeArr[1]);
		boolean addSuccess = false;

		List<DataValue> data = new ArrayList<DataValue>();
		Map<String, TreeMap<Date, DataValue>> localObservationData = ObservationData.getInstance().getLocalObservationData();
		for (int i = 0; i < timeSeries.size(); i++) {
			TreeMap<Date, DataValue> obsData = localObservationData.get(timeSeries.get(i));
			NavigableMap<Date, DataValue> subObsMap = new TreeMap<Date, DataValue>();
			subObsMap = obsData.subMap(beginTimeInterval, true, endTimeInterval, true);
			// data.addAll(subObsMap.values());

			List<DataValue> dataTemp = new ArrayList<DataValue>();
			if (isAddFlag) {
				// filter unevaluated data
				for (DataValue dval : subObsMap.values()) {
					if (!(dval.getProcessingStatus().getAbbreviation().equals(Constants.QUALIFY_CONTROL_LEVEL_RAW_DATA) && dval.getQualifierGroup().getGroup().getCode().equals(Constants.QUALIFIER_UNEVALUATED))) {
						if (dval.getProcessingStatus().getAbbreviation().equals(Constants.PROCESSING_LEVEL_2A) || dval.getProcessingStatus().getAbbreviation().equals(Constants.PROCESSING_LEVEL_2B)) {
							data.add(dval);
						}
					}
				}
			} else {
				data.addAll(subObsMap.values());
			}

			/*
			 * for (Map.Entry<String, DataValue> entry : obsData.entrySet()) {
			 * String key = entry.getKey(); Date existingDate =
			 * parseTimeStamp(key); DataValue val = (DataValue)
			 * entry.getValue(); String raw =
			 * val.getProcessingStatus().getCode(); if
			 * (existingDate.equals(beginTimeInterval) &&
			 * !(raw.equals(Constants.QUALIFY_CONTROL_LEVEL_RAW_DATA))) {
			 * data.add(val); } if (existingDate.before(endTimeInterval) &&
			 * existingDate.after(beginTimeInterval) &&
			 * !(raw.equals(Constants.QUALIFY_CONTROL_LEVEL_RAW_DATA))) {
			 * data.add(val); } if (existingDate.equals(endTimeInterval) &&
			 * !(raw.equals(Constants.QUALIFY_CONTROL_LEVEL_RAW_DATA))) {
			 * data.add(val); } }
			 */
		}

		LOG.debug("Total data to be flagged :" + data.size());
		if (data.size() > 0) {
			if (isAddFlag) {
				try {
					addSuccess = updateLevelAddFlagging(data, qualifierGroup, sourceId);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					addSuccess = updateLevelModifyFlagging(data, qualifierGroup, sourceId);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			addSuccess = false;
		}
		return addSuccess;

	}

	public boolean releaseDataSeries(Long sourceId, List<String> timeSeries, String[] timeArr) {
		Date beginTimeInterval = parseTimeStamp(timeArr[0]);
		Date endTimeInterval = parseTimeStamp(timeArr[1]);
		boolean addSuccess = false;
		List<DataValue> data = new ArrayList<DataValue>();
		Map<String, TreeMap<Date, DataValue>> localObservationData = ObservationData.getInstance().getLocalObservationData();
		for (int i = 0; i < timeSeries.size(); i++) {
			TreeMap<Date, DataValue> obsData = localObservationData.get(timeSeries.get(i));
			NavigableMap<Date, DataValue> subObsMap = new TreeMap<Date, DataValue>();
			subObsMap = obsData.subMap(beginTimeInterval, true, endTimeInterval, true);
			for (DataValue dval : subObsMap.values()) {
				if ((!(dval.getProcessingStatus().getAbbreviation().equals(Constants.QUALIFY_CONTROL_LEVEL_RAW_DATA)) && dval.getQualifierGroup().getGroup().getCode().equalsIgnoreCase(Constants.QUALIFIER_UNEVALUATED))) {
					if (dval.getProcessingStatus().getAbbreviation().equals(Constants.PROCESSING_LEVEL_2A) || dval.getProcessingStatus().getAbbreviation().equals(Constants.PROCESSING_LEVEL_2B)) {
						data.add(dval);
					}
				}
			}
		}
		LOG.debug("Total data to be modified :" + data.size());
		if (data.size() > 0) {
			try {
				addSuccess = updateLevelsOnly(data, sourceId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			addSuccess = false;
		}
		return addSuccess;
	}

	protected boolean updateLevelMultiAddFlagging(Map<DataValue, QualifierGroup> data, Long sourceId) throws Exception {
		int count = 0;
		Session session = HibernateUtil.getInstance().getMainSession();
		Transaction tx = null;
		boolean isSuccessful = false;
		try {
			tx = session.beginTransaction();
			ProcessingStatus processingStatus2c = (ProcessingStatus) HibernateUtil.getInstance().loadObject(new ProcessingStatus(), processingStatus2cLong);
			Source source = (Source) HibernateUtil.getInstance().loadObject(new Source(), sourceId);
			for (Map.Entry<DataValue, QualifierGroup> entry : data.entrySet()) {
				DataValue dataValue = entry.getKey();
				QualifierGroup qualifierGroup = entry.getValue();
				if (!(dataValue.getProcessingStatus().getAbbreviation().equals(Constants.QUALIFY_CONTROL_LEVEL_RAW_DATA))) {
					if (dataValue.getProcessingStatus().getAbbreviation().equals(Constants.PROCESSING_LEVEL_2A) || dataValue.getProcessingStatus().getAbbreviation().equals(Constants.PROCESSING_LEVEL_2B)) {
						// 2b,2A -> 2C
						if (dataValue.getQualifierGroup().getGroup().getCode().equals(Constants.QUALIFIER_UNEVALUATED)) {
							dataValue.setQualifierGroup(qualifierGroup);
							dataValue.setProcessingStatus(processingStatus2c);
							dataValue.setModifiedSource(source);
							dataValue.setModified(new Date());
							session.saveOrUpdate(dataValue);
							count++;
							if (count % 500 == 0 && count > 0) {
								tx.commit();
								tx.begin();
								count = 0;
							}
						}
					}

				}
			}
			tx.commit();
			isSuccessful = true;
			LOG.info("[DBOperation] Update ProcessingStatus done. " + count + " object(s) updated.");
		} catch (HibernateException e) {
			isSuccessful = false;
			if (tx != null)
				tx.rollback();
			e.printStackTrace();

		}
		HibernateUtil.getInstance().closeAllActiveSessions();
		return isSuccessful;
	}

	protected boolean updateLevelsOnly(List<DataValue> data, Long sourceId) throws Exception {
		int count = 0;
		Session session = HibernateUtil.getInstance().getMainSession();
		Transaction tx = null;
		boolean isSuccessful = false;
		try {
			tx = session.beginTransaction();
			Source source = (Source) HibernateUtil.getInstance().loadObject(new Source(), sourceId);
			ProcessingStatus processingStatus2c = (ProcessingStatus) HibernateUtil.getInstance().loadObject(new ProcessingStatus(), processingStatus2cLong);
			for (int i = 0; i < data.size(); i++) {
				DataValue dataValue = data.get(i);
				dataValue.setProcessingStatus(processingStatus2c);
				dataValue.setModifiedSource(source);
				dataValue.setModified(new Date());
				session.saveOrUpdate(dataValue);
				count++;
				if (count % 1000 == 0 && count > 0) {
					tx.commit();
					tx.begin();
					count = 0;
				}
			}
			tx.commit();
			isSuccessful = true;
			LOG.info("[DBOperation] Update ProcessingStatus done. " + count + " object(s) updated.");
		} catch (HibernateException e) {
			isSuccessful = false;
			if (tx != null)
				tx.rollback();
			e.printStackTrace();

		}
		HibernateUtil.getInstance().closeAllActiveSessions();
		return isSuccessful;
	}

	protected boolean updateLevelAddFlagging(List<DataValue> data, QualifierGroup qualifierGroup, Long sourceId) throws Exception {
		int count = 0;
		Session session = HibernateUtil.getInstance().getMainSession();
		Transaction tx = null;
		boolean isSuccessful = false;
		try {
			tx = session.beginTransaction();
			// Long processingStatusId =
			// ObservationData.getInstance().getProcessingLevelIdByName(Constants.PROCESSING_LEVEL_2C);
			LOG.debug("UpdateLevelAddFlagging - QualifierGroup, Processing Status, SourceId:" + qualifierGroup.getGroup().getCode() + ", " + processingStatus2cLong + ", " + sourceId);
			Source source = (Source) HibernateUtil.getInstance().loadObject(new Source(), sourceId);
			ProcessingStatus processingStatus2c = (ProcessingStatus) HibernateUtil.getInstance().loadObject(new ProcessingStatus(), processingStatus2cLong);
			for (int i = 0; i < data.size(); i++) {
				DataValue dataValue = data.get(i);
				dataValue.setQualifierGroup(qualifierGroup);
				dataValue.setProcessingStatus(processingStatus2c);
				dataValue.setModifiedSource(source);
				dataValue.setModified(new Date());
				session.saveOrUpdate(dataValue);
				count++;
				if (count % 1000 == 0 && count > 0) {
					tx.commit();
					tx.begin();
					count = 0;
				}
			}
			tx.commit();
			isSuccessful = true;
			LOG.info("[DBOperation] Update ProcessingStatus done. " + count + " object(s) updated.");
		} catch (HibernateException e) {
			isSuccessful = false;
			if (tx != null)
				tx.rollback();
			e.printStackTrace();

		}
		HibernateUtil.getInstance().closeAllActiveSessions();
		return isSuccessful;
	}

	protected boolean updateLevelMultiModifyFlagging(Map<DataValue, QualifierGroup> data, Long sourceId) throws Exception {
		int count = 0;
		Session session = HibernateUtil.getInstance().getMainSession();
		Transaction tx = null;
		boolean isSuccessful = false;
		try {
			tx = session.beginTransaction();
			// Long processingStatusId =
			// ObservationData.getInstance().getProcessingLevelIdByName(Constants.PROCESSING_LEVEL_2C);
			ProcessingStatus processingStatus2c = (ProcessingStatus) HibernateUtil.getInstance().loadObject(new ProcessingStatus(), processingStatus2cLong);
			Source source = (Source) HibernateUtil.getInstance().loadObject(new Source(), sourceId);
			for (Map.Entry<DataValue, QualifierGroup> entry : data.entrySet()) {
				DataValue dataValue = entry.getKey();
				QualifierGroup qualifierGroup = entry.getValue();
				if (!(dataValue.getProcessingStatus().getAbbreviation().equals(Constants.QUALIFY_CONTROL_LEVEL_RAW_DATA))) {
					if (dataValue.getProcessingStatus().getAbbreviation().equals(Constants.PROCESSING_LEVEL_2A) || dataValue.getProcessingStatus().getAbbreviation().equals(Constants.PROCESSING_LEVEL_2B)) {
						// 2a,2b -> 2c
						dataValue.setQualifierGroup(qualifierGroup);
						dataValue.setProcessingStatus(processingStatus2c);
					} else {
						// dont update data level, just quality flags (2c->2c)
						dataValue.setQualifierGroup(qualifierGroup);
					}
					dataValue.setModifiedSource(source);
					dataValue.setModified(new Date());
					session.saveOrUpdate(dataValue);
					count++;
					if (count % 500 == 0 && count > 0) {
						tx.commit();
						tx.begin();
						count = 0;
					}
				}
			}
			tx.commit();
			isSuccessful = true;
			LOG.info("[DBOperation] Update ProcessingStatus done. " + count + " object(s) updated.");
		} catch (HibernateException e) {
			isSuccessful = false;
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		}
		HibernateUtil.getInstance().closeAllActiveSessions();
		return isSuccessful;
	}

	protected boolean updateLevelModifyFlagging(List<DataValue> data, QualifierGroup qualifierGroup, Long sourceId) throws Exception {
		int count = 0;
		Session session = HibernateUtil.getInstance().getMainSession();
		Transaction tx = null;
		boolean isSuccessful = false;
		try {
			tx = session.beginTransaction();
			// Long processingStatusId =
			// processingStatusId=ObservationData.getInstance().getProcessingLevelIdByName(Constants.PROCESSING_LEVEL_2C);
			LOG.debug("UpdateLevelModifyFlagging - QualifierGroup, Processing Status, SourceId:" + qualifierGroup.getGroup().getCode() + ", " + this.processingStatus2cLong + ", " + sourceId);
			ProcessingStatus processingStatus2c = (ProcessingStatus) HibernateUtil.getInstance().loadObject(new ProcessingStatus(), this.processingStatus2cLong);
			Source source = (Source) HibernateUtil.getInstance().loadObject(new Source(), sourceId);
			for (int i = 0; i < data.size(); i++) {
				DataValue dataValue = data.get(i);
				if (!(dataValue.getProcessingStatus().getAbbreviation().equals(Constants.QUALIFY_CONTROL_LEVEL_RAW_DATA))) {
					if (dataValue.getProcessingStatus().getAbbreviation().equals(Constants.PROCESSING_LEVEL_2A) || dataValue.getProcessingStatus().getAbbreviation().equals(Constants.PROCESSING_LEVEL_2B)) {
						// 2a,2b -> 2c
						dataValue.setQualifierGroup(qualifierGroup);
						dataValue.setProcessingStatus(processingStatus2c);
					} else {
						// dont update data level, just quality flags (2c->2c)
						dataValue.setQualifierGroup(qualifierGroup);
					}
					dataValue.setModifiedSource(source);
					dataValue.setModified(new Date());
					session.saveOrUpdate(dataValue);
					count++;
					if (count % 1000 == 0 && count > 0) {
						tx.commit();
						tx.begin();
						count = 0;
					}
				}
			}
			tx.commit();
			isSuccessful = true;
			LOG.info("[DBOperation] Update ProcessingStatus done. " + count + " object(s) updated.");
		} catch (HibernateException e) {
			isSuccessful = false;
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		}
		HibernateUtil.getInstance().closeAllActiveSessions();
		return isSuccessful;
	}

	private Date parseTimeStamp(String timeStamp) {
		try {
			return Constants.DEFAULT_DATE_FORMAT.parse(timeStamp);
		} catch (ParseException e) {
			throw new RuntimeException("Error while allocating input parameters: timestamp parameter does not match pattern " + Constants.DEFAULT_DATE_FORMAT_PATTERN);
		}
	}

	public static String getSourceUserCode() {
		return sourceUserCode;
	}

	public static void setSourceUserCode(String sourceUserCode) {
		DBOperation.sourceUserCode = sourceUserCode;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean updateDataByRule(String procedure, String phenomenon, Long source, String tsid, String dataClass, String[] timeArr, Long flagId, boolean isAddFlag, double ruleValue, String operator) {
		Transaction tx = null;
		Date beginTimeInterval = parseTimeStamp(timeArr[0]);
		Date endTimeInterval = parseTimeStamp(timeArr[1]);
		Session session = HibernateUtil.getInstance().getSessionFactory().getCurrentSession();
		boolean addFlagStatus = true;
		try {
			tx = session.beginTransaction();
			String ql = "update " +Constants.OBSERVATION_SCHEMA+"." + dataClass + " set qualifierid=:qualifier , processingstatusid =:processing , ";
			ql += "modifiedsourceid=:source , modified =:timevalid ";
			ql += "from cv.variables, observationreferences.sites ";
			ql += "where timestampto between :start and :end and ";
			ql += "variableid=variables.objectid and siteid=sites.objectid and sites.code =:procedure and variables.code = :phenomenons ";
			ql += "and processingstatusid <> :levelOne and ";
			if (isAddFlag) {
				ql += "qualifierid= :unevaluated and (processingstatusid =:level2a or processingstatusid =:level2b) ";
			} else {
				ql += "(processingstatusid =:level2a or processingstatusid =:level2b or processingstatusid =:level2c) ";
			}

			if (operator.equals("==")) {
				ql += "and datavalue = :value";
			}
			if (operator.equals(">")) {
				ql += "and datavalue > :value";
			}
			if (operator.equals("<")) {
				ql += "and datavalue < :value";
			}
			if (operator.equals(">=")) {
				ql += "and datavalue >= :value";
			}
			if (operator.equals("<=")) {
				ql += "and datavalue <= :value";
			}

			Query query = null;
			if (isAddFlag) {
				query = session.createSQLQuery(ql.toString()).setInteger("qualifier", flagId.intValue()).setInteger("processing", processingStatusLevel2c).setInteger("source", source.intValue()).setTimestamp("timevalid", new Date()).setTimestamp("start", beginTimeInterval).setTimestamp("end", endTimeInterval).setString("procedure", procedure)
						.setString("phenomenons", phenomenon).setInteger("levelOne", processingStatusLevel1).setInteger("level2a", processingStatusLevel2a).setInteger("level2b", processingStatusLevel2b).setInteger("unevaluated", unevaluatedId).setDouble("value", ruleValue);
			} else {
				query = session.createSQLQuery(ql.toString()).setInteger("qualifier", flagId.intValue()).setInteger("processing", processingStatusLevel2c).setInteger("source", source.intValue()).setTimestamp("timevalid", new Date()).setTimestamp("start", beginTimeInterval).setTimestamp("end", endTimeInterval).setString("procedure", procedure)
						.setString("phenomenons", phenomenon).setInteger("levelOne", processingStatusLevel1).setInteger("level2a", processingStatusLevel2a).setInteger("level2b", processingStatusLevel2b).setInteger("level2c", processingStatusLevel2c).setDouble("value", ruleValue);

			}

			int result = query.executeUpdate();
			tx.commit();
			LOG.debug("Update Data by Rule done. " + result + " object(s) updated.");
		} catch (HibernateException e) {
			addFlagStatus = false;
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		}
		return addFlagStatus;
	}
	
	public HashMap<String, String> getMaintenanceData(List<String> sensorPropList, long s, long e) {
		Date start = new Date(s);
		Date end = new Date(e);

		Session session = HibernateUtil.getInstance().getSessionFactory().getCurrentSession();
		Transaction tx = null;
		List<SitesLog> results = null;
		HashMap<String, String> result = new HashMap<String, String>();
		try {
			tx = session.beginTransaction();
			for (int i = 0; i < sensorPropList.size(); i++) {
				String[] array = sensorPropList.get(i).split("#"); 
				String sensor = array[0].trim();
				String property = array[1].trim();
				
				/*
				String sql = "select distinct directory.sensorInstance.id from DataDirectory directory where directory.variable.code=:phenomenon and directory.site.code =:procedure";
				List<DataDirectory> res = session.createQuery(sql.toString()).setString("phenomenon", property).setString("procedure", sensor).list();
				LOG.debug(res.toString()); */
				
				String hql = "from SitesLog as log where log.site.code= :procedure ";
				hql += "and log.timestampto <=:end and (log.timestampto is null OR log.timestampto >= :start) and (log.sensorInstance.id is null OR " +
						"log.sensorInstance.id IN " +
						" (select distinct directory.sensorInstance.id from DataDirectory directory where directory.variable.code=:phenomenon and directory.site.code =:procedure)) order by log.timestampfrom";
				results = session.createQuery(hql.toString()).setString("procedure", sensor).setTimestamp("start", start).setTimestamp("end", end).setString("phenomenon", property).list();

				//LOG.debug( sensorPropList.get(i) +" "+ sensor +" "+ property +" "+ results.size());
				String value = "";
				if (results != null && results.size() > 0) {
					for (SitesLog sl : (List<SitesLog>) results) {
						String text = sl.getDiarytext();
						String source = sl.getSource().getCode();
						String flag = sl.getQualifier().getCode();
						// String site = sl.getSite().getCode();
						Date fromDate = sl.getTimestampfrom();
						Date toDate = sl.getTimestampto();
						value += fromDate + "," + toDate + "," + text + "," + flag + "," + source + ";";
					}
					result.put(sensorPropList.get(i), value);
				} else {
					result.put(sensorPropList.get(i), "noData");
				}
			}
			tx.commit(); //17.02.2015 ASD
		} catch (HibernateException ex) {
			if (tx != null)
				tx.rollback();
			ex.printStackTrace();
		}
		return result;
	}

	public HashMap<String, String> getMaintenanceDataOld(List<String> sensors, long s, long e) {
		Date start = new Date(s);
		Date end = new Date(e);

		Session session = HibernateUtil.getInstance().getSessionFactory().getCurrentSession();
		Transaction tx = null;
		HashMap<String, String> result = new HashMap<String, String>();
		try {
			tx = session.beginTransaction();

			for (int i = 0; i < sensors.size(); i++) {
				String hql = "from SitesLog as log where log.site.code= :procedure ";
				hql += "and log.timestampto <=:end and (log.timestampto is null OR log.timestampto >= :start) order by log.timestampfrom";
				List<SitesLog> results = session.createQuery(hql.toString()).setString("procedure", sensors.get(i)).setTimestamp("start", start).setTimestamp("end", end).list();

				String value = "";
				if (results != null && results.size() > 0) {
					for (SitesLog sl : (List<SitesLog>) results) {
						String text = sl.getDiarytext();
						String source = sl.getSource().getCode();
						String flag = sl.getQualifier().getCode();
						// String site = sl.getSite().getCode();
						Date fromDate = sl.getTimestampfrom();
						Date toDate = sl.getTimestampto();
						// System.out.println("values....: " + fromDate + " | "
						// + toDate + " | " + text + " |" + flag + " |" +
						// source);
						value += fromDate + "," + toDate + "," + text + "," + flag + "," + source + ";";
					}
					result.put(sensors.get(i), value);
				} else {
					result.put(sensors.get(i), "noData");
				}
			}
			tx.commit();
		} catch (HibernateException ex) {
			if (tx != null)
				tx.rollback();
			ex.printStackTrace();
		}
		return result;
	}
}
