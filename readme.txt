INSPECT is an online quality flagging tool developed based on 52N Sensor Web Client 3.1 (http://52north.org/communities/sensorweb/clients/SensorWebClient/).

I have extended the existing client with a data inspection module that allows users to visually assess and flag the data series retrieved via several SOS.
The observation services access data from the TERENO database.

The flagging tool uses data access control information to allow certain operations (e.g., view series, flagging and data approval)
based on the user roles and the user groups. A Web Processing Service based
on the 52°North WPS (http://52north.org/communities/geoprocessing/wps/) implementation has been developed. The WPS is used to gather more detailed information about the quality descriptors and the
history of a station, to update flagging information, and to approve data release.

Installation/Debugging info: INSPECT2/misc/InstallationInfo.pdf

User Manual : INSPECT2/misc/Manual_INSPECT5.docx

