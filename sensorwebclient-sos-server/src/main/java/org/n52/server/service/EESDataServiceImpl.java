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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fzj.ibg.odm.tables.datavalues.DataValue;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.util.Log;
import org.n52.client.service.EESDataService;
import org.n52.server.oxf.util.access.Constants;
import org.n52.server.oxf.util.access.HibernateUtil;
import org.n52.server.oxf.util.access.ObservationData;
import org.n52.server.oxf.util.access.ObservationRequester;
import org.n52.server.oxf.util.generator.EESGenerator;
import org.n52.server.oxf.util.generator.GeneratorException;
import org.n52.server.oxf.util.generator.TimeSeriesGenerator;
import org.n52.shared.requests.EESDataRequest;
import org.n52.shared.responses.EESDataResponse;
import org.n52.shared.responses.RepresentationResponse;
import org.n52.shared.serializable.pojos.DesignOptions;
import org.n52.shared.serializable.pojos.ResponseOptions;
import org.n52.shared.serializable.pojos.TimeSeriesProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EESDataServiceImpl implements EESDataService {

	private static final Logger LOG = LoggerFactory.getLogger(EESDataServiceImpl.class);

	@Override
	// ASD
	public EESDataResponse getEESDiagram(EESDataRequest request) throws IOException, Exception {
		try {
			LOG.debug("Performing EES diagram data request.");
			// ASD
			EESGenerator gen = new EESGenerator(false);
			EESDataResponse dataResponse = (EESDataResponse) gen.producePresentation(request.getOptions());
			return dataResponse;
		} catch (Exception e) {
			LOG.error("[EESDataServiceImpl] Exception occured on server side.", e);
			throw e; // last chance to log on server side
		}
	}

	@Override
	public EESDataResponse getEESOverview(EESDataRequest request) throws IOException, Exception {
		try {
			LOG.debug("Performing EES overview data request");
			EESGenerator gen = new EESGenerator(true);
			return (EESDataResponse) gen.producePresentation(request.getOptions());
		} catch (Exception e) {
			LOG.error("[EESDataServiceImpl] Exception occured on server side.", e);
			throw e; // last chance to log on server side
		}
	}

	@Override
	public EESDataResponse getDiagrams(EESDataRequest request) throws IOException, Exception {

		//LOG.debug("PERFORMING MULTIPLE DIAGRAMS DATA REQUESTS.......................");
		HashMap<String, DesignOptions> optionsMap = request.getOptionsMap();
		DesignOptions mainTSOptions = optionsMap.get("main");
		DesignOptions overviewTSOptions = optionsMap.get("overview");
		LOG.debug("START DATA REQUESTS : " + overviewTSOptions.toString());
		ObservationRequester obsRequester = new ObservationRequester(request);
		Map<String, TimeSeriesCollection> ovDatasetMap = obsRequester.requestData();
		//LOG.debug("END DATA REQUESTS.......................");
		LOG.debug("START DATA RENDERING.......................");
		TimeSeriesGenerator genOv = new TimeSeriesGenerator(true,ovDatasetMap);
		ResponseOptions ovResponse = genOv.generatePresentation(overviewTSOptions);
		//LOG.debug("END DATA RENDERING OVERVIEW.......................");
		//LOG.debug("START DATA RENDERING MAIN.......................");
		MainSeriesThread b = new MainSeriesThread(obsRequester.getMainDatasetMap(), mainTSOptions);
		b.start();
		synchronized (b) {
			try {
				//LOG.debug("Waiting for main series to complete.........................................");
				b.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			LOG.debug("END DATA RENDERING.......................");
			HashMap<String, ResponseOptions> resOptions = new HashMap<String, ResponseOptions>();
			resOptions.put("main", b.getMainResponse());
			resOptions.put("overview", ovResponse);
			//LOG.debug("END DATA RENDERING OVERALL.......................");
			return new EESDataResponse(resOptions);
		}
	}
	
	class MainSeriesThread extends Thread {
		Map<String, TimeSeriesCollection> dataset = null;
		DesignOptions mainTSOptions = null;
		ResponseOptions mainResponse = null;

		public MainSeriesThread(Map<String, TimeSeriesCollection> mainDatasetMap, DesignOptions options) {
			this.dataset = mainDatasetMap;
			this.mainTSOptions = options;
		}

		@Override
		public void run() {
			synchronized (this) {
				try {
					TimeSeriesGenerator genMain = new TimeSeriesGenerator(false, dataset);
					this.mainResponse = genMain.generatePresentation(mainTSOptions);
				} catch (GeneratorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				notify();
			}
		}

		public ResponseOptions getMainResponse() {
			return mainResponse;
		}
	}

}
