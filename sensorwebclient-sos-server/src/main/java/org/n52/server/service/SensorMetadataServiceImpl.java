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
package org.n52.server.service;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.n52.server.oxf.util.ConfigurationContext.SERVER_TIMEOUT;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.fzj.ibg.odm.tables.sites.SitesLog;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.n52.client.service.SensorMetadataService;
import org.n52.oxf.adapter.OperationResult;
import org.n52.oxf.adapter.ParameterContainer;
import org.n52.oxf.ows.capabilities.Operation;
import org.n52.oxf.sos.adapter.ISOSRequestBuilder;
import org.n52.oxf.sos.adapter.SOSAdapter;
import org.n52.oxf.sos.util.SosUtil;
import org.n52.oxf.util.JavaHelper;
import org.n52.server.oxf.util.ConfigurationContext;
import org.n52.server.oxf.util.access.AccessorThreadPool;
import org.n52.server.oxf.util.access.Constants;
import org.n52.server.oxf.util.access.DBOperation;
import org.n52.server.oxf.util.access.HibernateUtil;
import org.n52.server.oxf.util.access.OperationAccessor;
import org.n52.server.oxf.util.logging.Statistics;
import org.n52.server.oxf.util.parser.DescribeSensorParser;
import org.n52.server.util.SosAdapterFactory;
import org.n52.shared.responses.GetProcedureDetailsUrlResponse;
import org.n52.shared.responses.SensorMetadataResponse;
import org.n52.shared.serializable.pojos.ReferenceValue;
import org.n52.shared.serializable.pojos.TimeSeriesProperties;
import org.n52.shared.serializable.pojos.sos.Procedure;
import org.n52.shared.serializable.pojos.sos.SOSMetadata;
import org.n52.wps.io.data.IData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensorMetadataServiceImpl implements SensorMetadataService {

	private static final Logger LOG = LoggerFactory.getLogger(SensorMetadataServiceImpl.class);

	@Override
	public SensorMetadataResponse getSensorMetadata(TimeSeriesProperties tsProperties,long start, long end) throws Exception {
		try {
			LOG.debug("Request -> GetSensorMetadata");
			String sosUrl = tsProperties.getSosUrl();
			String procedureId = tsProperties.getProcedure().getId();
			String phenomenonId = tsProperties.getPhenomenon().getId();
			Procedure procedure = ConfigurationContext.getSOSMetadata(sosUrl).getProcedure(procedureId);
			SOSMetadata metadata = ConfigurationContext.getSOSMetadata(sosUrl);

			OperationResult result = requestDescribeSensor(sosUrl, procedureId, metadata);
			DescribeSensorParser parser = new DescribeSensorParser(result.getIncomingResultAsStream(), metadata);
			tsProperties.setMetadataUrl(parser.buildUpSensorMetadataHtmlUrl(procedureId, sosUrl));
			tsProperties.setStationName(parser.buildUpSensorMetadataStationName());
			tsProperties.setUOM(parser.buildUpSensorMetadataUom(phenomenonId));
			tsProperties.setValidTime(parser.parseTimestampOfFirstValue(), parser.parseTimestampOfLastValue());

			HashMap<String, ReferenceValue> refvalues = parser.parseCapsDataFields();

			tsProperties.addAllRefValues(refvalues);
			procedure.addAllRefValues(refvalues);

			ArrayList<String> list = new ArrayList<String>();
			list.add(procedureId+"#"+phenomenonId);
			HashMap<String, String> results = DBOperation.getInstance().getMaintenanceData(list, start, end);
			SensorMetadataResponse response = new SensorMetadataResponse(tsProperties,results);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Parsed SensorMetadata: {}", response.toDebugString());
			}

			JavaHelper.cleanUpDir(ConfigurationContext.XSL_DIR, ConfigurationContext.FILE_KEEPING_TIME, "xml");
			return response;
		} catch (ExecutionException e) {
			LOG.error("Exception occured on server side.", e.getCause());
			throw e; // last chance to log on server side
		} catch (Exception e) {
			LOG.error("Exception occured on server side.", e);
			throw e; // last chance to log on server side
		}
	}

	@Override
	public GetProcedureDetailsUrlResponse getProcedureDetailsUrl(String serviceURL, String procedure) throws Exception {
		try {
			LOG.debug("Request -> getProcedureDetailsUrl");
			SOSMetadata metadata = ConfigurationContext.getSOSMetadata(serviceURL);
			OperationResult result = requestDescribeSensor(serviceURL, procedure, metadata);
			LOG.debug("Request -> getProcedureDetailsUrl " + result.getUsedParameters().toString());
			ByteArrayInputStream resultInputStream = result.getIncomingResultAsStream();
			DescribeSensorParser parser = new DescribeSensorParser(resultInputStream, metadata);
			String url = parser.buildUpSensorMetadataHtmlUrl(procedure, serviceURL);
			return new GetProcedureDetailsUrlResponse(url);
		} catch (ExecutionException e) {
			LOG.error("Exception occured on server side.", e.getCause());
			throw e; // last chance to log on server side
		} catch (Exception e) {
			LOG.error("Exception occured on server side.", e);
			throw e;
		}
	}

	private OperationResult requestDescribeSensor(String sosUrl, String procedureId, SOSMetadata metadata) throws Exception {
		String sosVersion = metadata.getSosVersion();
		String smlVersion = metadata.getSensorMLVersion();
		ParameterContainer parameters = new ParameterContainer();
		parameters.addParameterShell(ISOSRequestBuilder.DESCRIBE_SENSOR_SERVICE_PARAMETER, "SOS");
		parameters.addParameterShell(ISOSRequestBuilder.DESCRIBE_SENSOR_VERSION_PARAMETER, sosVersion);
		parameters.addParameterShell(ISOSRequestBuilder.DESCRIBE_SENSOR_PROCEDURE_PARAMETER, procedureId);
		if (SosUtil.isVersion100(sosVersion)) {
			parameters.addParameterShell(ISOSRequestBuilder.DESCRIBE_SENSOR_OUTPUT_FORMAT, smlVersion);
		} else if (SosUtil.isVersion200(sosVersion)) {
			parameters.addParameterShell(ISOSRequestBuilder.DESCRIBE_SENSOR_PROCEDURE_DESCRIPTION_FORMAT, smlVersion);
		} else {
			throw new IllegalStateException("SOS Version (" + sosVersion + ") is not supported!");
		}

		Operation describeSensor = new Operation(SOSAdapter.DESCRIBE_SENSOR, sosUrl, sosUrl);
		SOSAdapter adapter = SosAdapterFactory.createSosAdapter(metadata);

		OperationAccessor accessor = new OperationAccessor(adapter, describeSensor, parameters);
		FutureTask<OperationResult> task = new FutureTask<OperationResult>(accessor);
		AccessorThreadPool.execute(task);

		// read sensor description
		return task.get(SERVER_TIMEOUT, MILLISECONDS);
	}

	@Override
	public SensorMetadataResponse getMultipleSensorMetadata(List<TimeSeriesProperties> properties,long start, long end) throws Exception {
		List<TimeSeriesProperties> tsPropertiesFinal = new ArrayList<TimeSeriesProperties>();
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < properties.size(); i++) {
			TimeSeriesProperties tsProperties = properties.get(i);
			try {
				LOG.debug("Request -> GetSensorMetadata");
				String sosUrl = tsProperties.getSosUrl();
				String procedureId = tsProperties.getProcedure().getId();

				String phenomenonId = tsProperties.getPhenomenon().getId();
				Procedure procedure = ConfigurationContext.getSOSMetadata(sosUrl).getProcedure(procedureId);
				SOSMetadata metadata = ConfigurationContext.getSOSMetadata(sosUrl);
				//ASD 11.08.2014
				list.add(procedureId+"#"+phenomenonId);

				OperationResult result = requestDescribeSensor(sosUrl, procedureId, metadata);
				DescribeSensorParser parser = new DescribeSensorParser(result.getIncomingResultAsStream(), metadata);
				tsProperties.setMetadataUrl(parser.buildUpSensorMetadataHtmlUrl(procedureId, sosUrl));
				tsProperties.setStationName(parser.buildUpSensorMetadataStationName());
				tsProperties.setUOM(parser.buildUpSensorMetadataUom(phenomenonId));
				tsProperties.setValidTime(parser.parseTimestampOfFirstValue(), parser.parseTimestampOfLastValue());

				HashMap<String, ReferenceValue> refvalues = parser.parseCapsDataFields();
				tsProperties.addAllRefValues(refvalues);
				procedure.addAllRefValues(refvalues);
				tsPropertiesFinal.add(tsProperties);
				JavaHelper.cleanUpDir(ConfigurationContext.XSL_DIR, ConfigurationContext.FILE_KEEPING_TIME, "xml");

			} catch (ExecutionException e) {
				LOG.error("Exception occured on server side.", e.getCause());
				throw e; // last chance to log on server side
			} catch (Exception e) {
				LOG.error("Exception occured on server side.", e);
				throw e; // last chance to log on server side
			}
		}
		//List<String> list = new ArrayList<String>(sensors);
		//ASD
		HashMap<String, String> results = DBOperation.getInstance().getMaintenanceData(list, start, end);
		SensorMetadataResponse response = new SensorMetadataResponse(tsPropertiesFinal,results);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Parsed SensorMetadata: {}", response.toFullDebugString());
		}
		return response;
	}

	public SensorMetadataResponse getMaintenanceDetails(List<String> sensors, long s, long e) throws Exception {
		SensorMetadataResponse response = new SensorMetadataResponse(DBOperation.getInstance().getMaintenanceData(sensors, s, e));
		return response;
	}

}
