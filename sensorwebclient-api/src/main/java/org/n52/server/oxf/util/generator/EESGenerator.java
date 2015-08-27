/**
 * ﻿Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.server.oxf.util.generator;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.eesgmbh.gimv.shared.util.Bounds;
import org.eesgmbh.gimv.shared.util.ImageEntity;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.XYPlot;
import org.n52.oxf.OXFException;
import org.n52.oxf.feature.OXFFeatureCollection;
import org.n52.oxf.ows.capabilities.ITime;
import org.n52.server.oxf.render.sos.CustomRenderingProperties;
import org.n52.server.oxf.render.sos.DiagramRenderer;
import org.n52.server.oxf.util.ConfigurationContext;
import org.n52.server.oxf.util.access.oxfExtensions.TimePosition_OXFExtension;
import org.n52.shared.responses.EESDataResponse;
import org.n52.shared.responses.RepresentationResponse;
import org.n52.shared.serializable.pojos.Axis;
import org.n52.shared.serializable.pojos.DesignOptions;
import org.n52.shared.serializable.pojos.TimeSeriesProperties;
import org.n52.shared.serializable.pojos.sos.Offering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EESGenerator extends Generator {

	private static final Logger LOGGER = LoggerFactory.getLogger(EESGenerator.class);

	public static final String FORMAT = "jpg";

	private boolean isOverview = false;

	private boolean isQualityControl = false;

	private DiagramRenderer renderer;

	private Map<String, String> responseTextField;
	// ASD
	// private Map<String, Map<Integer, List<String>>> qualTimeMap = new
	// HashMap<String, Map<Integer, List<String>>>();
	private Map<String, Map<Double, List<String>>> qualTimeMap = new HashMap<String, Map<Double, List<String>>>();
	private ArrayList<String> timeSeriesList;
	private JFreeChart chart;
	private ChartRenderingInfo renderingInfo;

	private static String RAWDATALEVEL = "1";
	private static String UNFLAGGED = "unevaluated,unevaluated";

	public EESGenerator(boolean isOverview) {
		this.isOverview = isOverview;
		this.renderer = new DiagramRenderer(isOverview, this);

	}

	/*
	 * public EESGenerator() { this.renderer = new DiagramRenderer(isOverview,
	 * this); }
	 */

	public RepresentationResponse producePresentation(DesignOptions options) throws GeneratorException {
		
		timeSeriesList = new ArrayList<String>();
		renderingInfo = new ChartRenderingInfo(new StandardEntityCollection());
		for (int i = 0; i < options.getProperties().size(); i++) {
			TimeSeriesProperties prop = options.getProperties().get(i);
			timeSeriesList.add(prop.getTsID());
		}

		this.isQualityControl = options.isQCActivated();
		String chartUrl = createChart(options, renderingInfo);
		
		Rectangle2D plotArea = renderingInfo.getPlotInfo().getDataArea();
		for (Axis axis : renderer.getAxisMapping().values()) {
			axis.setMaxY(plotArea.getMaxY());
			axis.setMinY(plotArea.getMinY());
		}

		// ASD
		Bounds chartArea = new Bounds(plotArea.getMinX(), plotArea.getMaxX(), plotArea.getMinY(), plotArea.getMaxY());

		if (!this.isOverview) {
			
			ImageEntity[] entities = {};
			entities = createImageEntities(renderingInfo.getEntityCollection());
			LOGGER.debug("Produced EES diagram: " + chartUrl);
			// LOGGER.debug("ImageEntities: " + entities.toString());

			RepresentationResponse dataResponse = null;
			if (isQualityControl) {
				LOGGER.debug("EESGenerator - Quality Control? Selected coordinates: " + isQualityControl + " " + options.getSelectedCoordinates());
				dataResponse = new EESDataResponse(chartUrl, options, chartArea, entities, renderer.getAxisMapping(), qualTimeMap, responseTextField);
			} else {
				dataResponse = new EESDataResponse(chartUrl, options, chartArea, entities, renderer.getAxisMapping(), qualTimeMap);
			}
			
			return dataResponse;
		} else {
			LOGGER.debug("Produced EES Overview diagram " + chartUrl);
			return new EESDataResponse(chartUrl, options, chartArea, renderer.getAxisMapping());
		}

	}

	public RepresentationResponse produceTSPresentation(DesignOptions options) throws GeneratorException {
		timeSeriesList = new ArrayList<String>();
		renderingInfo = new ChartRenderingInfo(new StandardEntityCollection());
		for (int i = 0; i < options.getProperties().size(); i++) {
			TimeSeriesProperties prop = options.getProperties().get(i);
			timeSeriesList.add(prop.getTsID());
		}

		this.isQualityControl = options.isQCActivated();
		String chartUrl = createTSChart(options, renderingInfo);

		Rectangle2D plotArea = renderingInfo.getPlotInfo().getDataArea();

		for (Axis axis : renderer.getAxisMapping().values()) {
			axis.setMaxY(plotArea.getMaxY());
			axis.setMinY(plotArea.getMinY());
		}

		// ASD
		Bounds chartArea = new Bounds(plotArea.getMinX(), plotArea.getMaxX(), plotArea.getMinY(), plotArea.getMaxY());

		if (!this.isOverview) {
			ImageEntity[] entities = {};
			entities = createImageEntities(renderingInfo.getEntityCollection());
			LOGGER.debug("Produced EES diagram: " + chartUrl);
			// LOGGER.debug("ImageEntities: " + entities.toString());

			RepresentationResponse dataResponse = null;
			if (isQualityControl) {
				LOGGER.debug("EESGenerator - Quality Control? Selected coordinates: " + isQualityControl + " " + options.getSelectedCoordinates());
				dataResponse = new EESDataResponse(chartUrl, options, chartArea, entities, renderer.getAxisMapping(), qualTimeMap, responseTextField);
			} else {
				dataResponse = new EESDataResponse(chartUrl, options, chartArea, entities, renderer.getAxisMapping(), qualTimeMap);
			}
			return dataResponse;
		} else {
			LOGGER.debug("Produced EES Overview diagram " + chartUrl);
			return new EESDataResponse(chartUrl, options, chartArea, renderer.getAxisMapping());
		}

	}

	private ArrayList<String> getTimeList(Map<String, String> map) {
		return null;
	}

	/**
	 * Creates the image entities.
	 * 
	 * @param entities
	 *            the entities
	 * @return the array list
	 */
	private ImageEntity[] createImageEntities(EntityCollection entities) {
		ArrayList<ImageEntity> imageEntities = new ArrayList<ImageEntity>();
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		if (!this.isOverview) {
			// reducer
			int xyItemCount = 0;
			for (Iterator<?> iter = entities.iterator(); iter.hasNext();) {
				Object o = iter.next();
				if (o instanceof XYItemEntity) {
					xyItemCount++;
				}
			}
			/*
			 * int reducer = 1; if (xyItemCount >
			 * ConfigurationContext.TOOLTIP_MIN_COUNT) { reducer = xyItemCount /
			 * ConfigurationContext.TOOLTIP_MIN_COUNT; } LOGGER.debug("Reduce "
			 * + xyItemCount + " Entities to " + (xyItemCount / reducer) +
			 * " Tooltips");
			 */

			// int counter = 0;
			for (Iterator<?> iter = entities.iterator(); iter.hasNext();) {
				// counter++;
				Object o = iter.next();
				// if ((counter % reducer) == 0) {
				if (o instanceof XYItemEntity) {
					XYItemEntity e = (XYItemEntity) o;

					ImageEntity imageEntity = new ImageEntity(new Bounds(e.getArea().getBounds2D().getMinX(), e.getArea().getBounds2D().getMaxX(), e.getArea().getBounds2D().getMinY(), e.getArea().getBounds2D().getMaxY()), e.getDataset().getGroup().getID());
					double time = e.getDataset().getXValue(e.getSeriesIndex(), e.getItem());
					double value = e.getDataset().getYValue(e.getSeriesIndex(), e.getItem());
					String datasetGroupId = e.getDataset().getGroup().getID();
					String flag = this.renderer.getQualifierList(datasetGroupId, f.format(new Date((long) time)));

					String dtLevelValue = "";
					String flgValue = "";
					if (flag.equalsIgnoreCase("noData") || flag == null || flag.equals("") || flag.equals("N/A")) {
						dtLevelValue = RAWDATALEVEL;
						flgValue = UNFLAGGED;
					} else {
						// convert flag ids into actual values
						String[] parts = flag.split("_"); // 2_2

						String dataLevelId = parts[0];
						String flgId = parts[1];
						dtLevelValue = CustomRenderingProperties.getInst().getDataProcLevels().get(dataLevelId).toString();
						Map<Long, String> flgMap = CustomRenderingProperties.getInst().getQualityFlagMappingWithId();
						flgValue = flgMap.get(flgId);
					}

					// ASD
					String uom;
					String color;
					if (e.getURLText() != null) {
						uom = e.getURLText().split(";")[0];
						color = e.getURLText().split(";")[1];
					} else {
						uom = "[n/a]";
						color = "white";
					}
					imageEntity.putHoverHtmlFragment(createHoverHtmlString(color, time, value, uom, dtLevelValue, flgValue));
					imageEntities.add(imageEntity);
				}
				// }
			}
		}
		return imageEntities.toArray(new ImageEntity[imageEntities.size()]);
	}

	// ASD
	public String createTSChart(DesignOptions options, ChartRenderingInfo renderInfo) throws GeneratorException {
		try {

			// query database
			//Map<String, OXFFeatureCollection> entireCollMap = getFeatureCollectionFor(options, true);

			ITime time = null;
			if (options.getTimeParam() == null) {
				time = readOutTime(options);
			} else {
				time = new TimePosition_OXFExtension(options.getTimeParam());
			}
			//System.out.println ("TIME.............." + time.toISO8601Format()); //2014-04-08T19:23:19+0200/2014-04-10T19:23:19+0200
			
			for (TimeSeriesProperties property : options.getProperties()) {
				List<String> fois = new ArrayList<String>();
				List<String> procedures = new ArrayList<String>();
				List<String> observedProperties = new ArrayList<String>();

				// extract request parameters from offering
				Offering offering = property.getOffering();
				observedProperties.add(property.getPhenomenon().getId());
				procedures.add(property.getProcedure().getId());
				fois.add(property.getFoi().getId());

				String sosUrl = property.getSosUrl();
				String offeringId = offering.getId();
				//System.out.println ("OTHER.............." + sosUrl +" "+ offeringId +" " + property.getFoi().getId() +" "+ property.getPhenomenon().getId() +" " +property.getProcedure().getId());
			}

			// ASD
			//this.chart = producePresentation(entireCollMap, options);
			//this.chart.removeLegend();
			// ASD
			//String chartFileName = chartFileName = createAndSaveImage(options, this.chart, renderInfo, this.isOverview);

			return ConfigurationContext.IMAGE_SERVICE + "";
		} catch (Exception e) {
			throw new GeneratorException(e.getMessage(), e);
		}
	}

	public String createChart(DesignOptions options, ChartRenderingInfo renderInfo) throws GeneratorException {
		try {
			Map<String, OXFFeatureCollection> entireCollMap = getFeatureCollectionFor(options, true);

			// ASD
			this.chart = producePresentation(entireCollMap, options);
			this.chart.removeLegend();
			// ASD
			String chartFileName = chartFileName = createAndSaveImage(options, this.chart, renderInfo, this.isOverview);

			return ConfigurationContext.IMAGE_SERVICE + chartFileName;
		} catch (Exception e) {
			throw new GeneratorException(e.getMessage(), e);
		}
	}

	public String createTimeSeriesChart(DesignOptions options, ChartRenderingInfo renderInfo) throws GeneratorException {
		try {
			Map<String, OXFFeatureCollection> entireCollMap = getFeatureCollectionFor(options, true);

			// ASD
			this.chart = producePresentation(entireCollMap, options);
			this.chart.removeLegend();
			// ASD
			String chartFileName = chartFileName = createAndSaveImage(options, this.chart, renderInfo, this.isOverview);

			return ConfigurationContext.IMAGE_SERVICE + chartFileName;
		} catch (Exception e) {
			throw new GeneratorException(e.getMessage(), e);
		}
	}

	public void generateTimeSeriesCoordinates() {
		// ASD
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		MultiMap multiTempMap = new MultiHashMap();
		EntityCollection ent = renderingInfo.getEntityCollection();

		Rectangle2D plotArea = renderingInfo.getPlotInfo().getDataArea();
		XYPlot plot = (XYPlot) chart.getXYPlot();

		if (!this.isOverview && ent.getEntities().size() > 0) {
			// reducer
			/*
			 * ASD int xyItemCount = 0; for (Iterator<?> iter = ent.iterator();
			 * iter.hasNext();) { Object o = iter.next(); if (o instanceof
			 * XYItemEntity) { xyItemCount++; } }
			 * 
			 * int reducer = 1; if (xyItemCount >
			 * ConfigurationContext.TOOLTIP_MIN_COUNT) { reducer = xyItemCount /
			 * ConfigurationContext.TOOLTIP_MIN_COUNT; }
			 * 
			 * int counter = 0;
			 */
			String timeSeriesId = "";

			for (Iterator<?> iter = ent.iterator(); iter.hasNext();) {
				// counter++;
				Object o = iter.next();
				// ASD
				// if ((counter % reducer) == 0) {
				if (o instanceof XYItemEntity) {
					XYItemEntity e = (XYItemEntity) o;

					timeSeriesId = e.getDataset().getGroup().getID();
					double time = e.getDataset().getXValue(e.getSeriesIndex(), e.getItem());
					double value = e.getDataset().getYValue(e.getSeriesIndex(), e.getItem());

					double x1 = plot.getDomainAxis().valueToJava2D(time, plotArea, plot.getDomainAxisEdge());
					double y1 = plot.getRangeAxis().valueToJava2D(value, plotArea, plot.getRangeAxisEdge());
					// int x1 = (int) Math.round(x);
					// int y1 = (int) Math.round(y);
					String timeString = sdf.format(new Date((long) time));
					ArrayList<String> myArr = new ArrayList<String>();
					myArr.add(Double.toString(x1));
					myArr.add(Double.toString(y1));
					myArr.add(timeString);
					myArr.add(Double.toString(value));
					// this.qualTimeMap.put(sdf.format(new Date((long)
					// time)), myArr);
					multiTempMap.put(timeSeriesId, x1 + "#" + y1 + "#" + timeString + "#" + value);
					myArr.clear();
				}
				// }
			}
		}

		for (int i = 0; i < timeSeriesList.size(); i++) {
			Map<Double, List<String>> wrapper = new HashMap<Double, List<String>>();
			String id = timeSeriesList.get(i);
			Collection coll = (Collection) multiTempMap.get(id);
			if (coll != null) {
				Iterator iterator = coll.iterator();
				while (iterator.hasNext()) {
					List<String> myArr = new ArrayList<String>();

					String totalStr = iterator.next().toString();
					String[] splits = totalStr.split("#");
					String x = splits[0]; // xCoord
					String y = splits[1]; // yCoord
					String time = splits[2]; // time
					String value = splits[3]; // value

					myArr.add(y);
					myArr.add(time);
					myArr.add(value);
					wrapper.put(Double.parseDouble(x), myArr);
				}

			}
			// sort hashmap
			// Map<Integer, List<String>> treeMap = new TreeMap<Integer,
			// List<String>>(wrapper);
			Map<Double, List<String>> treeMap = new TreeMap<Double, List<String>>(wrapper);
			qualTimeMap.put(id, treeMap);
		}
	}

	private String createHoverValues(String color, double time, double value, String uom, double x, double y) {
		SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss z");
		StringBuilder html = new StringBuilder();
		html.append("<div style='background-color: #F6F3F2;border: 2px solid ");
		html.append("#").append(color).append(";");
		html.append("'>").append("<span class='n52_sensorweb_client_tooltip'>");
		html.append(f.format(new Date((long) time)));
		html.append(":&nbsp;").append(value).append(" ").append(uom);
		html.append(" ").append(x).append(" ").append(y);
		html.append("</span></div>");
		return html.toString();
	}

	// ASD
	private String createHoverHtmlString(String color, double time, double value, String uom, String level, String qualifer) {
		SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss z");
		StringBuilder html = new StringBuilder();
		html.append("<div style='background-color: #F6F3F2;border: 2px solid ");
		html.append("#").append(color).append(";");
		html.append("'>").append("<span class='n52_sensorweb_client_tooltip'>");
		html.append("Date").append(":&nbsp;").append(f.format(new Date((long) time)));
		html.append("<br>").append("Value").append(":&nbsp;").append(value).append(" ").append(uom);
		html.append("<br>").append("Data Level & Flags").append(":&nbsp;").append(level).append("&nbsp;[").append(qualifer).append("]");
		html.append("</span></div>");
		return html.toString();
	}

	private JFreeChart producePresentation(Map<String, OXFFeatureCollection> entireCollMap, DesignOptions options) throws OXFException, IOException {

		Calendar begin = Calendar.getInstance();
		Calendar end = Calendar.getInstance();
		begin.setTimeInMillis(options.getBegin());
		end.setTimeInMillis(options.getEnd());
		return renderer.renderChart(entireCollMap, options, begin, end, ConfigurationContext.FACADE_COMPRESSION);
	}

	public void setResponseCoord(Map<String, String> r) {
		this.responseTextField = new HashMap<String, String>();
		this.responseTextField = r;
	}

	public ChartRenderingInfo getRenderingInfo() {
		return renderingInfo;
	}

	public void setRenderingInfo(ChartRenderingInfo renderingInfo) {
		this.renderingInfo = renderingInfo;
	}

}
