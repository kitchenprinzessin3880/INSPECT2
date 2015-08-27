/**
 * This class is modified from EESGenerator.java - ASD.
 */
package org.n52.server.oxf.util.generator;

import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eesgmbh.gimv.shared.util.Bounds;
import org.eesgmbh.gimv.shared.util.ImageEntity;
import org.fzj.ibg.odm.tables.datavalues.DataValue;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.time.TimeSeriesCollection;
import org.n52.server.oxf.render.sos.TimeSeriesRenderer;
import org.n52.server.oxf.util.ConfigurationContext;
import org.n52.server.oxf.util.access.ObservationData;
import org.n52.shared.responses.RepresentationResponse;
import org.n52.shared.serializable.pojos.Axis;
import org.n52.shared.serializable.pojos.DesignOptions;
import org.n52.shared.serializable.pojos.ResponseOptions;
import org.n52.shared.serializable.pojos.TimeSeriesProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeSeriesGenerator extends Generator {

	private static final Logger LOGGER = LoggerFactory.getLogger(TimeSeriesGenerator.class);

	public static final String FORMAT = "jpg";

	private boolean isOverview = false;

	private boolean isQualityControl = false;

	private TimeSeriesRenderer renderer;

	private Map<String, String> responseTextField;
	private Map<String, Map<Double, List<String>>> coordTimeMap = new HashMap<String, Map<Double, List<String>>>();

	private Map<String,String> timeSeriesList;
	private JFreeChart chart;
	private ChartRenderingInfo renderingInfo;
	List<ImageEntity> imageEntities = new ArrayList<ImageEntity>();
	SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss z");
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

	public TimeSeriesGenerator(boolean isOverview, Map<String, TimeSeriesCollection> dataset) {
		// LOGGER.debug("Rendering Diagram (start)...............:"+
		// "isOverview:"+isOverview +" " +new Date().toGMTString());
		this.isOverview = isOverview;
		this.renderer = new TimeSeriesRenderer(isOverview, this, dataset);
	}

	public RepresentationResponse producePresentation(DesignOptions options) throws GeneratorException {
		// do nothing
		return null;
	}

	public ResponseOptions generatePresentation(DesignOptions options) throws GeneratorException {
		timeSeriesList = new HashMap<String,String>();
		renderingInfo = new ChartRenderingInfo(new StandardEntityCollection());
		for (int i = 0; i < options.getProperties().size(); i++) {
			TimeSeriesProperties prop = options.getProperties().get(i);
			timeSeriesList.put(prop.getTsID(), prop.getPhenomenon().getId());
		}

		this.isQualityControl = options.isQCActivated();
		String chartUrl = createChart(options, renderingInfo);
		Rectangle2D plotArea = renderingInfo.getPlotInfo().getDataArea();

		for (Axis axis : renderer.getAxisMapping().values()) {
			axis.setMaxY(plotArea.getMaxY());
			axis.setMinY(plotArea.getMinY());
		}
		Bounds chartArea = new Bounds(plotArea.getMinX(), plotArea.getMaxX(), plotArea.getMinY(), plotArea.getMaxY());
		ResponseOptions responseOptions = null;

		if (!this.isOverview) {
			//ImageEntity[] entities = {};
			// entities =
			// createImageEntities(renderingInfo.getEntityCollection());
			//entities = imageEntities.toArray(new ImageEntity[imageEntities.size()]);

			//LOGGER.debug("Main diagram: " + chartUrl);
			if (isQualityControl) {
				// LOGGER.debug("EESGenerator - Quality Control? Selected coordinates: "
				// + isQualityControl + " " + options.getSelectedCoordinates());
				responseOptions = new ResponseOptions(chartUrl, options, chartArea, imageEntities, renderer.getAxisMapping(), coordTimeMap, responseTextField);
			} else {
				responseOptions = new ResponseOptions(chartUrl, options, chartArea, imageEntities, renderer.getAxisMapping(), coordTimeMap);
			}
		} else {
			//LOGGER.debug("Overview diagram: " + chartUrl);
			responseOptions = new ResponseOptions(chartUrl, options, chartArea, renderer.getAxisMapping());
		}
		return responseOptions;
	}

	/*
	 * private ImageEntity[] createImageEntities(EntityCollection entities) {
	 * ArrayList<ImageEntity> imageEntities = new ArrayList<ImageEntity>();
	 * SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ"); if
	 * (!this.isOverview) { // reducer for (Iterator<?> iter =
	 * entities.iterator(); iter.hasNext();) { Object o = iter.next(); if (o
	 * instanceof XYItemEntity) { XYItemEntity e = (XYItemEntity) o;
	 * 
	 * ImageEntity imageEntity = new ImageEntity(new
	 * Bounds(e.getArea().getBounds2D().getMinX(),
	 * e.getArea().getBounds2D().getMaxX(), e.getArea().getBounds2D().getMinY(),
	 * e.getArea().getBounds2D().getMaxY()), e.getDataset().getGroup().getID());
	 * double time = e.getDataset().getXValue(e.getSeriesIndex(), e.getItem());
	 * double value = e.getDataset().getYValue(e.getSeriesIndex(), e.getItem());
	 * String datasetGroupId = e.getDataset().getGroup().getID();
	 * 
	 * //ArrayList<String> flags =
	 * ObservationData.getInstance().getDataWithQualifierMap
	 * ().get(datasetGroupId).get(f.format(new Date((long) time))); DataValue
	 * dataValue =
	 * ObservationData.getInstance().getLocalObservationData().get(datasetGroupId
	 * ).get(f.format(new Date((long) time))); String flgValue = ""; String
	 * dtLevelValue = ""; if (dataValue == null) { dtLevelValue = "N/A";
	 * flgValue = "N/A"; } else { dtLevelValue =
	 * dataValue.getProcessingStatus().getCode(); flgValue =
	 * dataValue.getQualifierGroup().getGroup().getCode() + "_" +
	 * dataValue.getQualifierGroup().getQualifier().getCode(); }
	 * 
	 * // ASD String uom; String color; if (e.getURLText() != null) { uom =
	 * e.getURLText().split(";")[0]; color = e.getURLText().split(";")[1]; }
	 * else { uom = "[n/a]"; color = "white"; }
	 * imageEntity.putHoverHtmlFragment(createHoverHtmlString(color, time,
	 * value, uom, dtLevelValue, flgValue)); imageEntities.add(imageEntity); } }
	 * } return imageEntities.toArray(new ImageEntity[imageEntities.size()]); }
	 */

	public String createChart(DesignOptions options, ChartRenderingInfo renderInfo) throws GeneratorException {
		try {
			Calendar begin = Calendar.getInstance();
			Calendar end = Calendar.getInstance();
			begin.setTimeInMillis(options.getBegin());
			end.setTimeInMillis(options.getEnd());
			this.chart = renderer.renderChart(options, begin, end, ConfigurationContext.FACADE_COMPRESSION);
			this.chart.removeLegend();
			String chartFileName = createAndSaveImage(options, this.chart, renderInfo, this.isOverview);
			return ConfigurationContext.IMAGE_SERVICE + chartFileName;
		} catch (Exception e) {
			throw new GeneratorException(e.getMessage(), e);
		}
	}

	// data point to screen coordinate conversion
	public void generateTimeSeriesCoordinates(HashMap<String, Integer> axes) {
		// ASD
		EntityCollection ent = renderingInfo.getEntityCollection();

		Rectangle2D plotArea = renderingInfo.getPlotInfo().getDataArea();
		XYPlot plot = (XYPlot) chart.getXYPlot();

		if (ent.getEntities().size() > 0) {
			for (Object o : ent.getEntities()) {
				if (o instanceof XYItemEntity) {
					XYItemEntity e = (XYItemEntity) o;
					ImageEntity imageEntity = new ImageEntity(new Bounds(e.getArea().getBounds2D().getMinX(), e.getArea().getBounds2D().getMaxX(), e.getArea().getBounds2D().getMinY(), e.getArea().getBounds2D().getMaxY()), e.getDataset().getGroup().getID());

					String uom = "";
					String color = "";
					if (e.getURLText() != null) {
						uom = e.getURLText().split(";")[0];
						color = e.getURLText().split(";")[1];
					} else {
						uom = "[n/a]";
						color = "white";
					}
					String propertyType="";
					String timeSeriesId = e.getDataset().getGroup().getID();
					String phenomenon = timeSeriesList.get(timeSeriesId);
					if (phenomenon.contains("_")) {
						 propertyType = phenomenon.substring(0, phenomenon.indexOf("_"));
					} else {
						propertyType = phenomenon;
					}
					
					double time = e.getDataset().getXValue(e.getSeriesIndex(), e.getItem());
					double value = e.getDataset().getYValue(e.getSeriesIndex(), e.getItem());
					double x1 = plot.getDomainAxis().valueToJava2D(time, plotArea, plot.getDomainAxisEdge());
					//double y1 = plot.getRangeAxis().valueToJava2D(value,plotArea, plot.getRangeAxisEdge());
					double y1 = plot.getRangeAxis(axes.get(propertyType)).valueToJava2D(value,plotArea, plot.getRangeAxisEdge(axes.get(propertyType)));
					
					String timeString = sdf.format(new Date((long) time)); 

					DataValue dataValue = ObservationData.getInstance().getLocalObservationData().get(timeSeriesId).get(new Date((long) time));
					String flgValue = "";
					String dtLevelValue = "";
					if (dataValue == null) {
						dtLevelValue = "N/A";
						flgValue = "N/A";
					} else {
						dtLevelValue = dataValue.getProcessingStatus().getCode();
						flgValue = dataValue.getQualifierGroup().getGroup().getCode() + "_" + dataValue.getQualifierGroup().getQualifier().getCode();
					}

					imageEntity.putHoverHtmlFragment(createHoverHtmlString(color, timeString, value, uom, dtLevelValue, flgValue));
					imageEntities.add(imageEntity);

					List<String> myArr = new ArrayList<String>();
					myArr.add(timeString);
					myArr.add(Double.toString(value));
					myArr.add(Double.toString(y1));

					if (coordTimeMap.get(timeSeriesId) != null) {
						coordTimeMap.get(timeSeriesId).put(x1, myArr);
					} else {
						Map<Double, List<String>> wrapper = new TreeMap<Double, List<String>>();
						wrapper.put(x1, myArr);
						coordTimeMap.put(timeSeriesId, wrapper);
					}
				}
			}

			Map<String, Map<Double, List<String>>> coordTimeMapSorted = new HashMap<String, Map<Double, List<String>>>();
			
			for (String tId : timeSeriesList.keySet()) {
				if (coordTimeMap.get(tId) != null) {
					Map<Double, List<String>> treeMap = new TreeMap<Double, List<String>>(coordTimeMap.get(tId));
					coordTimeMapSorted.put(tId, treeMap);
				} 
			}
			coordTimeMap = coordTimeMapSorted;
		}
		//LOGGER.debug("GenerateTimeSeriesCoordinates-------------------END");
	}

	// ASD
	private String createHoverHtmlString(String color, String time, double value, String uom, String level, String qualifer) {
		StringBuilder html = new StringBuilder();
		html.append("<div align='left' style='background-color: #F6F3F2;border: 2px solid ");
		html.append("#").append(color).append(";");
		html.append("'>").append("<span class='n52_sensorweb_client_tooltip'>");
		//html.append("Date").append(":&nbsp;").append(time);
		html.append(time);
		//html.append("<br>").append("Value").append(":&nbsp;").append(value).append(" ").append(uom);
		//html.append(" | ").append(String.format("%.5g%n", value)).append(" ").append(uom);
		html.append(" , ").append(value).append(uom);
		//html.append("<br>").append("Level & Flag").append(":&nbsp;").append(level).append("&nbsp;[").append(qualifer).append("]");
		html.append(" (").append(level).append("_").append(qualifer).append(")");
		html.append("</span></div>");
		return html.toString();
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

	public Map<String, Map<Double, List<String>>> getCoordTimeMap() {
		return coordTimeMap;
	}

	public void setCoordTimeMap(Map<String, Map<Double, List<String>>> coordTimeMap) {
		this.coordTimeMap = coordTimeMap;
	}

}
