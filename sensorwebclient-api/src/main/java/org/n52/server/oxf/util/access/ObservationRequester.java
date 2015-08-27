package org.n52.server.oxf.util.access;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.fzj.ibg.odm.tables.datavalues.DataValue;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.n52.shared.requests.EESDataRequest;
import org.n52.shared.serializable.pojos.DesignOptions;
import org.n52.shared.serializable.pojos.TimeSeriesProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObservationRequester {
	private static final Logger LOGGER = LoggerFactory.getLogger(ObservationRequester.class);

	private Map<String, TimeSeriesCollection> mainDatasetMap = null;
	private Map<String, TimeSeriesCollection> ovDatasetMap = null;
	private HashMap<String, DesignOptions> optionsMap = null;

	public ObservationRequester(EESDataRequest request) {
		this.mainDatasetMap = new HashMap<String, TimeSeriesCollection>();
		this.ovDatasetMap = new HashMap<String, TimeSeriesCollection>();
		this.optionsMap = request.getOptionsMap();
	}

	public Map<String, TimeSeriesCollection> requestData() {
		// reuse the same main session
		Session session = HibernateUtil.getInstance().getSessionFactory().openSession();

		DesignOptions mainTSOptions = optionsMap.get("main");
		DesignOptions overviewTSOptions = optionsMap.get("overview");

		// time
		Date beginMainInterval = new Date(mainTSOptions.getBegin());
		Date endMainInterval = new Date(mainTSOptions.getEnd());
		Date beginOvInterval = new Date(overviewTSOptions.getBegin());
		Date endOvInterval = new Date(overviewTSOptions.getEnd());

		Map<String, TreeMap<Date, DataValue>> overallObservationData = new HashMap<String, TreeMap<Date, DataValue>>();
		Transaction transaction = null;

		for (int i = 0; i < mainTSOptions.getProperties().size(); i++) {
			TimeSeriesCollection mainDataSeriesColl = new TimeSeriesCollection();
			TimeSeriesCollection ovDataSeriesColl = new TimeSeriesCollection();
			TimeSeriesProperties prop = mainTSOptions.getProperties().get(i);

			String timSeriesId = prop.getTsID();
			String procedure = prop.getProcedure().getId();
			String foi = prop.getFoi().getId();
			String property = prop.getPhenomenon().getId();
			String senseProp = foi + "___" + property + "___" + procedure;
			List<DataValue> ovDataValue = null;
			try {
				String dataClass = ObservationData.getInstance().getDataDirectoryMap().get(procedure).get(property);

				String hql = "from " + dataClass + " as datavalue where (datavalue.timestampto between :start and :end) and datavalue.site.code= :procedure";
				TimeSeries ovTimeSeries = new TimeSeries(senseProp);
				TimeSeries mainTimeSeries = new TimeSeries(senseProp);
				try {
					//LOGGER.debug("Initiate SQL request..............................: "+ foi +" " + property);
					transaction = session.beginTransaction();
					hql += " and datavalue.variable.code = :phenomenons";
					ovDataValue = session.createQuery(hql.toString()).setString("phenomenons", property).setTimestamp("start", beginOvInterval).setTimestamp("end", endOvInterval).setString("procedure", procedure).list();

					//LOGGER.debug("SQL request is completed.............................: " + foi + " " + property);
					if (ovDataValue.size() > 0) {
						prop.setHasData(true);
					} else {
						prop.setHasData(false);
					}

					TreeMap<Date, DataValue> dataValueObj = new TreeMap<Date, DataValue>();

					for (DataValue dataValue : ovDataValue) {
						Date timeStmp = dataValue.getTimestampto();
						Double resultVal = dataValue.getDatavalue();
						// String timeStmpStr =
						// Constants.DEFAULT_DATE_FORMAT.format(timeStmp);
						// Second s = new
						// Second(timeStmp,TimeZone.getTimeZone("Etc/UTC"),Locale.ENGLISH);

						FixedMillisecond s = new FixedMillisecond(timeStmp);
						ovTimeSeries.add(s, resultVal);
						if (timeStmp.before(endMainInterval) && timeStmp.after(beginMainInterval)) {
							mainTimeSeries.add(s, resultVal);
							dataValueObj.put(timeStmp, dataValue);
						} else if (timeStmp.equals(beginMainInterval)) {
							mainTimeSeries.add(s, resultVal);
							dataValueObj.put(timeStmp, dataValue);
						} else if (timeStmp.equals(endMainInterval)) {
							mainTimeSeries.add(s, resultVal);
							dataValueObj.put(timeStmp, dataValue);
						}
					}
					overallObservationData.put(timSeriesId, dataValueObj);
					ovDataSeriesColl.addSeries(ovTimeSeries);
					mainDataSeriesColl.addSeries(mainTimeSeries);
					transaction.commit();

				} catch (HibernateException ex) {
					ex.printStackTrace();
					// do not close the session!
				}
			} catch (Exception e) {
				LOGGER.error("[ObservationRequester] Exception occured on server side.", e);
				e.printStackTrace(); // last chance to log on server side
			}
			mainDatasetMap.put(timSeriesId, mainDataSeriesColl);
			ovDatasetMap.put(timSeriesId, ovDataSeriesColl);
		}

		// update main session 13.08.2014
		// close previously opened session
		HibernateUtil.getInstance().getMainSession().close();
		HibernateUtil.getInstance().setMainSession(session);
		ObservationData.getInstance().setLocalObservationData(overallObservationData);
		return ovDatasetMap;
	}

	public Map<String, TimeSeriesCollection> getMainDatasetMap() {
		return mainDatasetMap;
	}

	public void setMainDatasetMap(Map<String, TimeSeriesCollection> mainDatasetMap) {
		this.mainDatasetMap = mainDatasetMap;
	}

	public Map<String, TimeSeriesCollection> getOvDatasetMap() {
		return ovDatasetMap;
	}

	public void setOvDatasetMap(Map<String, TimeSeriesCollection> ovDatasetMap) {
		this.ovDatasetMap = ovDatasetMap;
	}

}
