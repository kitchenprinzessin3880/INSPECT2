package org.n52.server.oxf.util.access;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {

	private static Constants _this;

	private static final Logger LOG = LoggerFactory.getLogger(Constants.class);
	public static String DEFAULT_SESSION_ID = "rLK7kZnV1B8QUUOTb0fi/DrASzMgYS5kZXZhcmFqdQ==";
	
	//ASD 17.11.2014
	public static String URL_USER_AUTHENTICATE = "";
	public static String EMAIL_FROM = "";
	public static String EMAIL_PWD = "";
	public static String WPS_URL = "";

	public static final String WPS_PROC_APPROVEDATA = "fzj.ibg.sos.wps.ApproveDataService";
	public static final String WPS_PROC_GETFLAGS = "fzj.ibg.sos.wps.GetQualityFlagsSosDataService";
	public static final String WPS_PROC_GETPROCSTATUS = "fzj.ibg.sos.wps.GetProcessingStatiSosDataService";
	public static final String WPS_PROC_ADDFLAG = "fzj.ibg.sos.wps.AddFlagSosDataService";
	public static final String WPS_PROC_UPDATEFLAG = "fzj.ibg.sos.wps.UpdateFlagSosDataService";
	public static final String WPS_PROC_GETSENPROPBYUSR = "fzj.ibg.sos.wps.GetPermittedObservedProperties";
	public static final String USER_NAME = "username";
	public static final String SESSION_ID = "sessionId";
	public static final String FLAG = "flag";
	public static final String STRING_RESULT_PARAMETER = "stringResult";
	public static final String RESPONSIBILITYQC = "quality";
	public static final String SOS_URL_PARAMETER_NAME = "sos_url";
	public static final String NEW_DATA_VALUE_PARAMETER = "datavalue";
	public static final String PHENOMENON_PARAMETER_NAME = "phenomenon";
	public static final String PROCEDURE_PARAMETER_NAME = "procedure";
	public static final String TIMESTAMP_PARAMETER_NAME = "timestamp";
	public static final String TIME_INTERVAL_START_PARAMETER_NAME = "begin";
	public static final String TIME_INTERVAL_END_PARAMETER_NAME = "end";
	public static final String BOOLEAN_RESULT_PARAMETER = "booleanResult";
	public static final String QUALIFY_CONTROL_LEVEL_RAW_DATA = "1";
	public static final String PROCESSING_LEVEL_2A = "2a";
	public static final String PROCESSING_LEVEL_2B = "2b";
	public static final String PROCESSING_LEVEL_2C = "2c";
	//public static final String OBSERVATION_SCHEMA ="terenodata"; //odm 2.1 only
	public static final String OBSERVATION_SCHEMA ="observationdata"; //odm 2.2
	
	public static final String DEFAULT_DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ssZ";
	public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat(
			DEFAULT_DATE_FORMAT_PATTERN);

	public static final String QUALIFIER_UNEVALUATED = "unevaluated";

	public static Constants getInstance() throws FileNotFoundException,
			IOException {
		if (_this == null) {
			_this = new Constants();
		}
		return _this;
	}

	private Constants() throws FileNotFoundException, IOException {
		// do nothing
	}
	
	public static void setEMAIL_FROM(String eMAIL_FROM) {
		EMAIL_FROM = eMAIL_FROM;
	}

	public static void setEMAIL_PWD(String eMAIL_PWD) {
		EMAIL_PWD = eMAIL_PWD;
	}
	
	public static void setWPS_URL(String wPS_URL) {
		WPS_URL = wPS_URL;
	}

	public static void setURL_USER_AUTHENTICATE(String uRL_USER_AUTHENTICATE) {
		URL_USER_AUTHENTICATE = uRL_USER_AUTHENTICATE;
	}

}
