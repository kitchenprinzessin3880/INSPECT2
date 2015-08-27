<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=9" />
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<meta name="keywords" lang="en"
	content="Sensor Web, SWE, OGC, SOS, Time Series, Data, Data Flagging, TEODOOR">
<meta name="gwt:property" content="locale=<%=request.getLocale()%>">
<meta name="author" content="TERENO FZJ - http://teodoor.icg.kfa-juelich.de/">
<meta name="DC.title" content="INSPECT Data Flagging Tool">
<meta name="DC.creator" content="TERENO FZJ - http://teodoor.icg.kfa-juelich.de/">
<meta name="DC.subject"
	content="Sensor Web Client including SOS and data flagging support">
<title>INSPECT Online Data Flagging Tool</title>

<!--link rel="shortcut icon" href="img/fav.png" type="image/x-icon"-->
<!--link rel="icon" href="img/fav.png" type="image/x-icon"-->
<link rel="schema.DC" href="http://purl.org/dc/elements/1.1/">
<!--link rel="icon" href="img/fav.png" type="image/x-icon"-->
<link href="css/layout.css" rel="stylesheet">
</head>

<body>
	<div id="loadingWrapper">
		<div id="spacer"></div>
		<div class="loadingIndicator">
			<img src="img/loader.gif" width="45" height="45"
				style="margin-right: 8px; float: left; vertical-align: center;" />
			<div id="operator"></div>
			<!--span id="loadingMsg">Loading ${application.title}</span-->
			<span id="loadingMsg">Loading INSPECT</span>
		</div>
	</div>
	<script type="text/javascript" src="js/bookmark.js"></script>
	<script type="text/javascript" src="js/proj4js-compressed.js"></script>
	<script type="text/javascript" src="js/OpenLayers/OpenLayers.js"></script>
	<script type="text/javascript" src="js/OpenStreetMap.js"></script>
	<script type="text/javascript" src="client/client.nocache.js"></script>
</body>
</html>
