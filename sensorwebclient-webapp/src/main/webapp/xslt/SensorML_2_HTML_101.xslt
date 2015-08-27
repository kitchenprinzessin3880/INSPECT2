<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:gml="http://www.opengis.net/gml" xmlns:ism="urn:us:gov:ic:ism:v2"
	xmlns:sch="http://www.ascc.net/xml/schematron" xmlns:smil20="http://www.w3.org/2001/SMIL20/"
	xmlns:smil20lang="http://www.w3.org/2001/SMIL20/Language" xmlns:sml="http://www.opengis.net/sensorML/1.0.1"
	xmlns:swe="http://www.opengis.net/swe/1.0.1" xmlns:xdt="http://www.w3.org/2005/xpath-datatypes"
	xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:altova="http://www.altova.com">
	<xsl:variable name="XML" select="/" />
	<xsl:output method="html" encoding="utf-8" indent="yes" />

	<xsl:param name="SV_OutputFormat" select="'HTML'" />
	<xsl:template match="/">
		<xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html&gt;</xsl:text>
		<html>
			<head>
				<title />
				<link rel="stylesheet" href="../css/layout.min.min.css" />
			</head>
			<body>
				<table class="sensorInfo" border="0" width="100%">
					<xsl:for-each select="$XML">
						<xsl:for-each select="sml:SensorML">
							<xsl:variable name="sml_ident" select="sml:identification" />
							<xsl:choose>
								<xsl:when test="$sml_ident">
									<xsl:for-each select="sml:identification">
										<xsl:for-each select="sml:IdentifierList">
											<xsl:for-each select="sml:identifier">
												<xsl:if test="@name  = &quot;shortName&quot;">
													<tr class="sensorInfo">
														<span>
															<td>
																<b>Station: </b>
															</td>
														</span>
														<td>
															<xsl:for-each select="sml:Term">
																<xsl:for-each select="sml:value">
																	<xsl:apply-templates />
																</xsl:for-each>
															</xsl:for-each>
														</td>
													</tr>
												</xsl:if>
												<xsl:if test="@name  = &quot;uniqueID&quot;">
													<tr class="sensorInfo">
														<span>
															<td>
																<b>ID: </b>
															</td>
														</span>
														<td>
															<xsl:for-each select="sml:Term">
																<xsl:for-each select="sml:value">
																	<xsl:apply-templates />
																</xsl:for-each>
															</xsl:for-each>
														</td>
													</tr>
												</xsl:if>
												<xsl:if test="@name  = &quot;longName&quot;">
													<tr class="sensorInfo">
														<span>
															<td>
																<b>ID: </b>
															</td>
														</span>
														<td>
															<xsl:for-each select="sml:Term">
																<xsl:for-each select="sml:value">
																	<xsl:apply-templates />
																</xsl:for-each>
															</xsl:for-each>
														</td>
													</tr>
												</xsl:if>
											</xsl:for-each>
										</xsl:for-each>
									</xsl:for-each>
									<xsl:for-each select="sml:classification">
										<xsl:for-each select="sml:ClassifierList">
											<xsl:for-each select="sml:classifier">
												<xsl:if test="@name = &quot;SensorType&quot;">
													<tr class="sensorInfo">
														<span>
															<td>
																<b>Sensor: </b>
															</td>
														</span>
														<td>
															<xsl:for-each select="sml:Term">
																<xsl:for-each select="sml:value">
																	<xsl:apply-templates />
																</xsl:for-each>
															</xsl:for-each>
														</td>
													</tr>
												</xsl:if>
												<xsl:if test="@name = &quot;TopicCategory&quot;">
													<tr class="sensorInfo">
														<span>
															<td>
																<b>Parameter: </b>
															</td>
														</span>
														<td>
															<xsl:for-each select="sml:Term">
																<xsl:for-each select="sml:value">
																	<xsl:apply-templates />
																</xsl:for-each>
															</xsl:for-each>
														</td>
													</tr>
												</xsl:if>
											</xsl:for-each>
										</xsl:for-each>
									</xsl:for-each>
									<xsl:for-each select="sml:characteristics">
										<xsl:for-each select="swe:SimpleDataRecord">
											<xsl:for-each select="swe:field">
												<tr class="sensorInfo">
													<td>
														<b>
															<xsl:value-of select="@name" />
															:
														</b>
													</td>
													<td>
														<xsl:for-each select="swe:Text">
															<xsl:for-each select="gml:description">
																<b>
																	<xsl:apply-templates />
																	:
																</b>
															</xsl:for-each>
															<xsl:for-each select="swe:value">
																<xsl:apply-templates />
															</xsl:for-each>
														</xsl:for-each>
													</td>
												</tr>
											</xsl:for-each>
										</xsl:for-each>
									</xsl:for-each>
									<xsl:for-each select="sml:capabilities"> 
										<xsl:for-each select="swe:SimpleDataRecord">
											<xsl:for-each select="swe:field">
												<tr class="sensorInfo">
													<td>
														<b>
															<xsl:value-of select="@name" />
															:
														</b>
													</td>
													<td>
														<xsl:for-each select="swe:Text">
															<xsl:for-each select="gml:description">
																<b>
																	<xsl:apply-templates />
																	:
																</b>
															</xsl:for-each>
															<xsl:for-each select="swe:value">
																<xsl:apply-templates />
															</xsl:for-each>
														</xsl:for-each>
													</td>
												</tr>
											</xsl:for-each>
										</xsl:for-each>
									</xsl:for-each>
									<!-- <xsl:for-each select="sml:positions"> <xsl:for-each select="sml:PositionList"> 
										<xsl:for-each select="sml:position"> <xsl:for-each select="swe:Position"> 
										<tr class="sensorInfo"> <td> <span> <b>Position: </b> </span> </td> <td> 
										<span> ( <xsl:value-of select="substring-after( @referenceFrame, &quot;urn:ogc:crs:&quot;)" 
										/> </span> <span> <xsl:text> )</xsl:text> </span> <xsl:for-each select="swe:location"> 
										<xsl:for-each select="swe:Vector"> <xsl:for-each select="swe:coordinate"> 
										<xsl:for-each select="@name"> <span> <xsl:value-of select="string(.)" /> 
										</span> <span> <xsl:text>: </xsl:text> </span> </xsl:for-each> <xsl:for-each 
										select="swe:Quantity"> <xsl:for-each select="swe:value"> <xsl:apply-templates 
										/> <span> <xsl:text>&#160;</xsl:text> </span> </xsl:for-each> <xsl:for-each 
										select="swe:uom"> <xsl:for-each select="@code"> <span> <xsl:text>&#160;</xsl:text> 
										</span> <span> <xsl:value-of select="string(.)" /> </span> </xsl:for-each> 
										<span> <xsl:text>&#160;</xsl:text> </span> </xsl:for-each> </xsl:for-each> 
										</xsl:for-each> </xsl:for-each> </xsl:for-each> </td> </tr> </xsl:for-each> 
										</xsl:for-each> </xsl:for-each> </xsl:for-each> -->
									<!-- ASD <xsl:for-each select="sml:member"> -->
										<!-- ASD <xsl:for-each select="sml:System"> -->
											<!-- <xsl:for-each select="sml:position"> <xsl:for-each select="swe:Position"> 
												<tr class="sensorInfo"> <td> <span> <b>Position: </b> </span> </td> <td> 
												<span> ( <xsl:value-of select="substring-after(@referenceFrame, &quot;urn:ogc:crs:&quot;)" 
												/> <xsl:value-of select="substring-after(@referenceFrame, &quot;urn:ogc:def:crs:&quot;)" 
												/> </span> <span> <xsl:text>) </xsl:text> </span> <xsl:for-each select="swe:location"> 
												<xsl:for-each select="swe:Vector"> <xsl:for-each select="swe:coordinate"> 
												<xsl:for-each select="@name"> <span> <xsl:value-of select="string(.)" /> 
												</span> <span> <xsl:text>: </xsl:text> </span> </xsl:for-each> <xsl:for-each 
												select="swe:Quantity"> <xsl:for-each select="swe:value"> <xsl:apply-templates 
												/> <span> <xsl:text>&#160;</xsl:text> </span> </xsl:for-each> <xsl:for-each 
												select="swe:uom"> <xsl:for-each select="@code"> <span> <xsl:value-of select="string(.)" 
												/> </span> </xsl:for-each> <span> <xsl:text>&#160;</xsl:text> </span> </xsl:for-each> 
												</xsl:for-each> </xsl:for-each> </xsl:for-each> </xsl:for-each> </td> </tr> 
												</xsl:for-each> </xsl:for-each> -->
											<!-- <xsl:for-each select="sml:positions"> <xsl:for-each select="sml:PositionList"> 
												<xsl:for-each select="sml:position"> <xsl:for-each select="swe:Position"> 
												<tr class="sensorInfo"> <td> <span> <b>Position: </b> </span> </td> <td> 
												<span> ( <xsl:value-of select="substring-after(@referenceFrame, &quot;urn:ogc:crs:&quot;)" 
												/> <xsl:value-of select="substring-after(@referenceFrame, &quot;urn:ogc:def:crs:&quot;)" 
												/> </span> <span> <xsl:text>) </xsl:text> </span> <xsl:for-each select="swe:location"> 
												<xsl:for-each select="swe:Vector"> <xsl:for-each select="swe:coordinate"> 
												<xsl:for-each select="@name"> <span> <xsl:value-of select="string(.)" /> 
												</span> <span> <xsl:text>: </xsl:text> </span> </xsl:for-each> <xsl:for-each 
												select="swe:Quantity"> <xsl:for-each select="swe:value"> <xsl:apply-templates 
												/> <span> <xsl:text>&#160;</xsl:text> </span> </xsl:for-each> <xsl:for-each 
												select="swe:uom"> <xsl:for-each select="@code"> <span> <xsl:value-of select="string(.)" 
												/> </span> </xsl:for-each> <span> <xsl:text>&#160;</xsl:text> </span> </xsl:for-each> 
												</xsl:for-each> </xsl:for-each> </xsl:for-each> </xsl:for-each> </td> </tr> 
												</xsl:for-each> </xsl:for-each> </xsl:for-each> </xsl:for-each> -->
										<!--ASD  </xsl:for-each> -->
									<!--  ASD </xsl:for-each> -->
								</xsl:when>
								<xsl:otherwise>
									<xsl:for-each select="sml:member">
										<xsl:for-each select="sml:System">
											<xsl:for-each select="sml:identification">
												<xsl:for-each select="sml:IdentifierList">
													<xsl:for-each select="sml:identifier">
														<xsl:if test="@name  = &quot;Term&quot;">
															<tr class="sensorInfo">
																<span>
																	<td>
																		<b>Station Id </b>
																	</td>
																</span>
																<td>
																	<xsl:for-each select="sml:Term">
																		<xsl:for-each select="sml:value">
																			<xsl:apply-templates />
																		</xsl:for-each>
																	</xsl:for-each>
																</td>
															</tr>
														</xsl:if>
														<xsl:if test="@name  = &quot;Long Name&quot;">
															<tr class="sensorInfo">
																<span>
																	<td>
																		<b>Full Name </b>
																	</td>
																</span>
																<td>
																	<xsl:for-each select="sml:Term">
																		<xsl:for-each select="sml:value">
																			<xsl:apply-templates />
																		</xsl:for-each>
																	</xsl:for-each>
																</td>
															</tr>
														</xsl:if>
													</xsl:for-each>
												</xsl:for-each>
											</xsl:for-each>
											<xsl:for-each select="sml:classification">
												<xsl:for-each select="sml:ClassifierList">
													<xsl:for-each select="sml:classifier">
														<xsl:if test="@name = &quot;TopicCategory&quot;">
															<tr class="sensorInfo">
																<span>
																	<td>
																		<b>Topic Category </b>
																	</td>
																</span>
																<td>
																	<xsl:for-each select="sml:Term">
																		<xsl:value-of
																			select="substring-after(sml:value,'TopicCategory:')" />
																	</xsl:for-each>
																</td>
															</tr>
														</xsl:if>
														<xsl:if test="@name = &quot;IntendedApplication&quot;">
															<tr class="sensorInfo">
																<span>
																	<td>
																		<b>Intended Application </b>
																	</td>
																</span>
																<td>
																	<xsl:for-each select="sml:Term">
																		<xsl:value-of
																			select="substring-after(sml:value,'IntendedApplication:')" />
																	</xsl:for-each>
																</td>
															</tr>
														</xsl:if>
													</xsl:for-each>
												</xsl:for-each>
											</xsl:for-each>
											<xsl:for-each select="sml:validTime">
												<xsl:for-each select="gml:TimePeriod">
													<tr class="sensorInfo">
														<td>
															<b>
																Valid Time
															</b>
														</td>
														<td>
															<xsl:value-of select="gml:beginPosition" />
															<br />
															<xsl:value-of select="gml:endPosition" />
														</td>
													</tr>
												</xsl:for-each>
											</xsl:for-each>
											<xsl:for-each select="sml:characteristics">
												<xsl:for-each select="swe:SimpleDataRecord">
													<xsl:for-each select="swe:field">
														<tr class="sensorInfo">
															<td>
																<b>
																	<xsl:value-of select="@name" />
																</b>
															</td>
															<td>
																<xsl:for-each select="swe:Text">
																	<xsl:for-each select="gml:description">
																		<b>
																			<xsl:apply-templates />
																			:
																		</b>
																	</xsl:for-each>
																	<xsl:for-each select="swe:value">
																		<xsl:apply-templates />
																	</xsl:for-each>
																</xsl:for-each>
															</td>
														</tr>
													</xsl:for-each>
												</xsl:for-each>
											</xsl:for-each>
											<xsl:for-each select="sml:capabilities">
												<xsl:for-each select="swe:SimpleDataRecord">
													<xsl:for-each select="swe:field">
														<tr class="sensorInfo">
															<td>
																<b>
																	<xsl:value-of select="swe:Text/@definition" />
																	
																</b>
															</td>
															<td>
																<xsl:for-each select="swe:Text/swe:value">
																	<xsl:apply-templates />
																</xsl:for-each>
															</td>
														</tr>
													</xsl:for-each>
												</xsl:for-each>
											</xsl:for-each>
											<xsl:for-each select="sml:contact">
												<xsl:for-each select="sml:ResponsibleParty">
													<tr class="sensorInfo">
														<td>
															<b>
																Contact
											
															</b>
														</td>
														<td>
															<xsl:value-of select="sml:individualName" />
															<br />
															<xsl:value-of select="sml:organizationName" />
															<br />
															<xsl:for-each select="sml:contactInfo/sml:address">
																<xsl:value-of select="sml:city" />,
																<xsl:value-of select="sml:administrativeArea" />,
																<xsl:value-of select="sml:postalCode" />
																<xsl:value-of select="sml:country" />
																<br />
																<xsl:value-of select="sml:electronicMailAddress" />
															</xsl:for-each>
														</td>
													</tr>
												</xsl:for-each>
											</xsl:for-each>

								<xsl:for-each select="sml:position/swe:Position">
										<xsl:for-each select="swe:location">
											<xsl:for-each select="swe:Vector">
												<tr class="sensorInfo">
															<td>
																<b>
																	Position
																</b>
															</td>
															<td>
																<xsl:for-each select="swe:coordinate">
																	<xsl:value-of select="@name" />:
																	<!--  <xsl:value-of select="round(swe:Quantity/swe:value*1000) div 100" />-->
																	<xsl:value-of select="swe:Quantity/swe:value" /><br />
																</xsl:for-each>
															</td>
														</tr>
												
											</xsl:for-each>
										</xsl:for-each>
									</xsl:for-each>
									
									<xsl:for-each select="sml:outputs">
										<xsl:for-each select="sml:OutputList">
												<tr class="sensorInfo">
															<td>
																<b>
																	Observed Properties
																</b>
															</td>
															<td>
																<xsl:for-each select="sml:output" >
																	<xsl:value-of select="@name" />  &#160; 
																</xsl:for-each>
															</td>
														</tr>
					
										</xsl:for-each>
									</xsl:for-each>
									
											<!-- <xsl:for-each select="sml:position"> <xsl:for-each select="swe:Position"> 
												<tr class="sensorInfo"> <td> <span> <b>Position: </b> </span> </td> <td> 
												<span> ( <xsl:value-of select="substring-after(@referenceFrame, &quot;urn:ogc:crs:&quot;)" 
												/> <xsl:value-of select="substring-after(@referenceFrame, &quot;urn:ogc:def:crs:&quot;)" 
												/> </span> <span> <xsl:text>) </xsl:text> </span> <xsl:for-each select="swe:location"> 
												<xsl:for-each select="swe:Vector"> <xsl:for-each select="swe:coordinate"> 
												<xsl:for-each select="@name"> <span> <xsl:value-of select="string(.)" /> 
												</span> <span> <xsl:text>: </xsl:text> </span> </xsl:for-each> <xsl:for-each 
												select="swe:Quantity"> <xsl:for-each select="swe:value"> <xsl:apply-templates 
												/> <span> <xsl:text>&#160;</xsl:text> </span> </xsl:for-each> <xsl:for-each 
												select="swe:uom"> <xsl:for-each select="@code"> <span> <xsl:value-of select="string(.)" 
												/> </span> </xsl:for-each> <span> <xsl:text>&#160;</xsl:text> </span> </xsl:for-each> 
												</xsl:for-each> </xsl:for-each> </xsl:for-each> </xsl:for-each> </td> </tr> 
												</xsl:for-each> </xsl:for-each> -->
											<!-- <xsl:for-each select="sml:positions"> <xsl:for-each select="sml:PositionList"> 
												<xsl:for-each select="sml:position"> <xsl:for-each select="swe:Position"> 
												<tr class="sensorInfo"> <td> <span> <b>Position: </b> </span> </td> <td> 
												<span> ( <xsl:value-of select="substring-after(@referenceFrame, &quot;urn:ogc:crs:&quot;)" 
												/> <xsl:value-of select="substring-after(@referenceFrame, &quot;urn:ogc:def:crs:&quot;)" 
												/> </span> <span> <xsl:text>) </xsl:text> </span> <xsl:for-each select="swe:location"> 
												<xsl:for-each select="swe:Vector"> <xsl:for-each select="swe:coordinate"> 
												<xsl:for-each select="@name"> <span> <xsl:value-of select="string(.)" /> 
												</span> <span> <xsl:text>: </xsl:text> </span> </xsl:for-each> <xsl:for-each 
												select="swe:Quantity"> <xsl:for-each select="swe:value"> <xsl:apply-templates 
												/> <span> <xsl:text>&#160;</xsl:text> </span> </xsl:for-each> <xsl:for-each 
												select="swe:uom"> <xsl:for-each select="@code"> <span> <xsl:value-of select="string(.)" 
												/> </span> </xsl:for-each> <span> <xsl:text>&#160;</xsl:text> </span> </xsl:for-each> 
												</xsl:for-each> </xsl:for-each> </xsl:for-each> </xsl:for-each> </td> </tr> 
												</xsl:for-each> </xsl:for-each> </xsl:for-each> </xsl:for-each> -->

										</xsl:for-each>
										<xsl:for-each select="sml:ProcessModel">
											<xsl:for-each select="gml:description">
												<tr class="sensorInfo">
													<span>
														<td>
															<b>Description: </b>
														</td>
													</span>
													<td>
														<xsl:apply-templates />
													</td>
												</tr>
											</xsl:for-each>
											<xsl:for-each select="gml:identifier">
												<tr class="sensorInfo">
													<span>
														<td>
															<b>Name: </b>
														</td>
													</span>
													<td>
														<xsl:apply-templates />
													</td>
												</tr>
											</xsl:for-each>
											<xsl:for-each select="sml:method">
												<xsl:for-each select="sml:ProcessMethod">
													<xsl:for-each select="gml:description">
														<tr class="sensorInfo">
															<span>
																<td>
																	<b>ProcessMethod Description: </b>
																</td>
															</span>
															<td>
																<xsl:apply-templates />
															</td>
														</tr>
													</xsl:for-each>
												</xsl:for-each>
											</xsl:for-each>
										</xsl:for-each>
									</xsl:for-each>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:for-each>
					</xsl:for-each>
				</table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
											