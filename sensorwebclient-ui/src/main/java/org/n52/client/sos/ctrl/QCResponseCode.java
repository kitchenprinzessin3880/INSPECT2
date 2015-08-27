package org.n52.client.sos.ctrl;

public enum QCResponseCode {
	TIMEZONEERROR ("Invalid time zone, e.g., +0100", "red"),
	CLICKAGAIN ("Please click on the point again!", "red"),
	FLAGADDED ("Flagging is completed! ","blue"),
	LEVELUPDATED ("Data series have been approved!","blue"),
	LEVELUPDATEDOFFLINE ("Offline data approval has been initiated. You will be notified via email once the selected datasets are approved.","blue"),
	LEVELFAILED ("Error occurred! Data approval is not successful!","red"),
	LEVELFAILEDOFFLINE ("Error occurred! Data approval is not successful.","red"),
	FLAGADDEDOFFLINE ("Offline Flagging has been initiated. You will be notified via email once the selected datasets are flagged. ","blue"),
	FLAGGINGFAILEDOFFLINE ("Error occurred! Offline glagging is not successful. ","red"),
	FLAGGINGFAILED ("Error occurred! Flagging is not successful. ","red"),
	TIMERANGEINVALID ("Start Date must be before the End Date. ","red"),
	INVALIDDATEFORMAT ("Please enter a valid DateTime (YYYY-MM-dd HH:mm:ssZZZ), e.g., 2014-01-26 22:15:00+0100 ","red"),
	INVALIDSTARTDATEFORMAT ("Please enter a valid Start Date (YYYY-MM-dd HH:mm:ssZZZ),e.g., 2014-01-26 22:15:00+0100 ","red"),
	INVALIDENDDATEFORMAT ("Please enter a valid End Date (YYYY-MM-dd HH:mm:ssZZZ),e.g., 2014-01-26 22:15:00+0100 ","red"),
	SELECTALL ("Please select all fields (service, offering, sensor, property)! ", "red"),
	REDUNDANTSHAPES ("Series Shape must be unique! ", "red"),
	SHAPESUPDATED ("Series Shape have been updated. Loading timeseries....... ", "blue"),
	STORAGEEMPTY ("No query template exists. ", "red"),
	SHAPESNOTUPDATED ("Series Shapes are NOT updated. Click *Reset All* to set default flag shapes. ", "red"),
	ONESTATIONAVAILABLE("Sensor modification is not possible. Only one station with the selected properties exists.","blue"),
	FLAGGINGNOTAUTHORIZED("You are not authorized to perform data flagging. Please contact your data administrator!","red"),
	FLAGGINGNOTALLOWED("You are not allowed to flag the selected series. ","red"),
	COORDINATENOTDETECTED("Clicked point is not detected. Please click on the exact location of the time point.","blue"),
	FLAGGINGNOTALLOWEDPARTIAL("Partial flagging - not allowed to flag the following series: ","red"),
	TIMESERIESNULL("Time series cannot be null.","red");
	
	private String msg;
	private String color;

	QCResponseCode(String m, String c)
	{
		this.msg=m;
		this.color = c;
	}
	
	public void setColor(String c)
	{
		this.color = c;
	}
	
	public String getColor()
	{
		return this.color;
	}
	public String getResponseMessage() { return msg; }
	

}
