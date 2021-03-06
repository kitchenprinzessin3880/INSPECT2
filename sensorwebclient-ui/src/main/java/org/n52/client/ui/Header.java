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
package org.n52.client.ui;

import static com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat.ISO_8601;
import static org.n52.client.sos.i18n.SosStringsAccessor.i18n;

import java.util.Date;

import org.n52.client.ctrl.TimeManager;
import org.n52.client.ses.util.SesUtil;
import org.n52.client.sos.ctrl.DataManagerSosImpl;
import org.n52.client.sos.ctrl.SOSRequestManager;
import org.n52.client.sos.data.DataStoreTimeSeriesImpl;
import org.n52.client.sos.legend.TimeSeries;
import org.n52.ext.ExternalToolsException;
import org.n52.ext.link.AccessLinkFactory;
import org.n52.ext.link.sos.TimeRange;
import org.n52.ext.link.sos.TimeSeriesParameters;
import org.n52.ext.link.sos.TimeSeriesPermalinkBuilder;
import org.n52.shared.serializable.pojos.sos.SOSMetadata;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.BkgndRepeat;
import com.smartgwt.client.types.Cursor;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.HTMLFlow;
import com.smartgwt.client.widgets.HTMLPane;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.Layout;
import com.smartgwt.client.widgets.layout.VLayout;

public class Header extends HLayout {

	private String elemID;

	private String url;

	private Label userNameTxt = null;

	public static com.google.gwt.user.client.ui.Label requestCounter;

	public Header(String id) {
		this.elemID = id;
		generateHeader();
	}

	private void generateHeader() {

		setStyleName("n52_sensorweb_client_headerContainer");
		setBackgroundImage("../img/BannerINSPECT.png");
		setHeight(48);
		Label space = new Label();
		
		HLayout linkLayout = new HLayout();
		linkLayout.setStyleName("n52_sensorweb_client_linkBlock");
		linkLayout.setAlign(Alignment.RIGHT);
		linkLayout.setWidth100();
		linkLayout.setMargin(5);

		// ASD
		// linkLayout.addMember(getVersionInfo());
		userNameTxt = new Label();
		userNameTxt.setStyleName("n52_sensorweb_client_header");
		userNameTxt.setAutoWidth();
		userNameTxt.setVisible(false);
		userNameTxt.setWrap(false);

		linkLayout.addMember(userNameTxt);
		// linkLayout.addMember(getSeparator());
		// linkLayout.addMember(getKillProcessLink());

		// linkLayout.addMember(getRestartLink());
		linkLayout.addMember(getSeparator());
		linkLayout.addMember(getHelpLink());
		// linkLayout.addMember(getSeparator());
		// linkLayout.addMember(getAddBookmarkLink());
		linkLayout.addMember(getSeparator());
		if (SesUtil.isSesActiv()) {
			linkLayout.addMember(getAdminLink());
			linkLayout.addMember(getSeparator());
		}
		linkLayout.addMember(getImprintLink());
		linkLayout.addMember(getSeparator());
		linkLayout.addMember(getToaster());
		linkLayout.addMember(getSeparator());
		// ASD
		// linkLayout.addMember(getCopyrightLink());

		addMember(linkLayout);
	}

	private Layout getHomeLabel() {
		Layout layout = new HLayout();
		layout.setStylePrimaryName("n52_sensorweb_client_logoBlock");
		Img homeLabel = new Img("../img/client-logo.png", 289, 55);
		homeLabel.setStyleName("n52_sensorweb_client_logo");
		homeLabel.setCursor(Cursor.POINTER);
		homeLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String url = "http://52north.org/communities/sensorweb/";
				Window.open(url, "_blank", "");
			}
		});
		layout.addMember(homeLabel);
		return layout;
	}

	// ASD
	private Layout getTERENOLabel() {
		Layout layout = new HLayout();
		layout.setAlign(VerticalAlignment.CENTER);
		layout.setStylePrimaryName("n52_sensorweb_client_logoBlock");
		Img homeLabel = new Img("../img/tereno-logo.png", 200, 55);
		homeLabel.setStyleName("n52_sensorweb_client_logo");
		homeLabel.setMargin(9);
		homeLabel.setCursor(Cursor.POINTER);
		homeLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String url = "http://teodoor.icg.kfa-juelich.de/overview-de";
				Window.open(url, "_blank", "");
			}
		});
		
		Img homeLabel2 = new Img("../img/logo-fzj.png",180,55);
		homeLabel2.setStyleName("n52_sensorweb_client_logo");
		homeLabel2.setMargin(9);
		homeLabel2.setCursor(Cursor.POINTER);
		homeLabel2.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String url = "http://www.fz-juelich.de/ibg/ibg-3/EN/Home/home_node.html";
				Window.open(url, "_blank", "");
			}
		});
		layout.addMember(homeLabel);
		layout.addMember(homeLabel2);
		return layout;
	}
	
	
	private Label getVersionInfo() {
		String version = "foobar";
		Label versionLabel = getHeaderLinkLabel("Version: " + version);
		versionLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.open("version-info.txt", "_blank", "");
			}
		});
		return versionLabel;

	}

	private Label getCopyrightLink() {

		DateTimeFormat dateFormatter = DateTimeFormat.getFormat("yyyy");
		String year = dateFormatter.format(new Date());

		Label copyright = getHeaderLinkLabel("&copy; 52&#176;North, GmbH " + year);
		copyright.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				String url = "http://www.52north.org";
				Window.open(url, "_blank", "");
			}
		});
		return copyright;
	}

	private Label getAddBookmarkLink() {
		Label addToFavorites = getHeaderLinkLabel(i18n.addToBookmarks());
		addToFavorites.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent evt) {
				addToFavorites();
			}
		});
		return addToFavorites;
	}

	private Label getRestartLink() {
		Label restart = getHeaderLinkLabel(i18n.restart());
		restart.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent evt) {
				String localeParameter = Window.Location.getParameter("locale");
				String href = Window.Location.getHref();
				String permaLink = getPermaLink(href);
				if (isEnglishLocale(localeParameter)) {
					url = href.substring(0, href.indexOf("?"));
					url += "?locale=en" + permaLink;
				} else if (isGermanLocale(localeParameter)) {
					url = href.substring(0, href.indexOf("?"));
					url += "?locale=de" + permaLink;
				} else {
					if (GWT.isProdMode()) {
						url = href + "?" + permaLink.substring(1);
					} else {
						url = href + permaLink;
					}
				}
				Window.Location.assign(url);
			}
		});
		return restart;
	}

	private Label getAdminLink() {
		Label admin = getHeaderLinkLabel(i18n.admin());
		admin.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DataPanel dataPanel = View.getInstance().getDataPanel();
				DataPanelTab newTab;
				if (dataPanel.getCurrentTab().equals(View.getInstance().getSesTab())) {
					newTab = View.getInstance().getEesTab();
				} else {
					newTab = View.getInstance().getSesTab();
				}
				dataPanel.getPanel().selectTab(newTab);
				dataPanel.setCurrentTab(newTab);
				dataPanel.update();
			}
		});
		return admin;
	}

	private boolean isEnglishLocale(String value) {
		return value != null && value.equals("en");
	}

	private boolean isGermanLocale(String value) {
		return value != null && value.equals("de");
	}

	private Label getHelpLink() {
		Label help = getHeaderLinkLabel(i18n.help());
		help.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
			public void onClick(com.smartgwt.client.widgets.events.ClickEvent event) {
				//String helpUrl = GWT.getHostPageBaseURL() + i18n.helpPath();
				String helpUrl = GWT.getHostPageBaseURL() + "Manual.pdf";
				Window.open(helpUrl, "", "");
			}
		});
		return help;
	}

	// ASD
	private Label getKillProcessLink() {
		Label kill = getHeaderLinkLabel("Stop Request");
		kill.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
			public void onClick(com.smartgwt.client.widgets.events.ClickEvent event) {
				SC.confirm("Proceed with stopping ongoing data request?", new BooleanCallback() {
					public void execute(Boolean value) {
						if (value != null && value) {
							SOSRequestManager.getInstance().abruptEnding();
						} else {
							// do nothing
						}
					}
				});
			}
		});
		return kill;
	}

	private Label getToaster() {
		Label msg = getHeaderLinkLabel("Messages");
		msg.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
			public void onClick(com.smartgwt.client.widgets.events.ClickEvent event) {
					Toaster.getInstance().show();
			}
		});
		return msg;
	}

	private Label getImprintLink() {
		Label imprint = getHeaderLinkLabel(i18n.Impressum());
		imprint.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				com.smartgwt.client.widgets.Window w = new com.smartgwt.client.widgets.Window();
				w.setTitle(i18n.Impressum());
				w.setWidth(450);
				w.setHeight(460);
				w.centerInPage();
				w.setIsModal(true);

				VLayout layout = new VLayout();
				HTMLPane pane = new HTMLPane();
				pane.setContentsURL(i18n.imprintPath());
				layout.setStyleName("n52_sensorweb_client_imprint_content");
				layout.addMember(pane);
				w.addItem(layout);
				w.show();
			}
		});
		return imprint;
	}

	private Label getHeaderLinkLabel(String labelText) {
		Label label = new Label(labelText);
		label.setStyleName("n52_sensorweb_client_headerlink");
		label.setAutoWidth();
		label.setWrap(false);
		return label;
	}

	private Label getSeparator() {
		Label pipe = new Label("|");
		pipe.setStyleName("n52_sensorweb_client_pipe");
		pipe.setAutoWidth();
		return pipe;
	}

	String getPermaLink(String baseUrl) {
		TimeSeries[] ts = DataStoreTimeSeriesImpl.getInst().getTimeSeriesSorted();
		TimeSeriesPermalinkBuilder builder = new TimeSeriesPermalinkBuilder();
		for (TimeSeries timeSeries : ts) {
			TimeSeriesParameters parameters = createTimeSeriesParameters(timeSeries);
			parameters.setTimeRange(createTimeRange());
			builder.addParameters(parameters);
		}
		try {
			AccessLinkFactory factory = builder.build();
			return factory.createAccessURL(baseUrl).toString();
		} catch (ExternalToolsException e) {
			Toaster.getInstance().addErrorMessage("Malformed base URL: " + baseUrl);
			return baseUrl;
		}

		// String permalink = "";
		// if (ts.length > 0) {
		// // get SERVICES
		// permalink += "&sos=";
		// for (int i = 0; i < ts.length; i++) {
		// permalink += ts[i].getSosUrl();
		// if (i < ts.length && i != ts.length - 1) {
		// permalink += ",";
		// }
		// }
		// // get OFF
		// permalink += "&offering=";
		// for (int i = 0; i < ts.length; i++) {
		// permalink += ts[i].getOfferingId();
		// if (i < ts.length && i != ts.length - 1) {
		// permalink += ",";
		// }
		// }
		// // get FOI
		// permalink += "&stations=";
		// for (int i = 0; i < ts.length; i++) {
		// permalink += ts[i].getFeatureId();
		// if (i < ts.length && i != ts.length - 1) {
		// permalink += ",";
		// }
		// }
		// // get PROC
		// permalink += "&procedures=";
		// for (int i = 0; i < ts.length; i++) {
		// permalink += ts[i].getProcedureId();
		// if (i < ts.length && i != ts.length - 1) {
		// permalink += ",";
		// }
		// }
		// // get PHEN
		// permalink += "&phenomenons=";
		// for (int i = 0; i < ts.length; i++) {
		// permalink += ts[i].getPhenomenonId();
		// if (i < ts.length && i != ts.length - 1) {
		// permalink += ",";
		// }
		// }
		// }
		// permalink += "&begin="
		// + .format(new Date(TimeManager.getInst().getBegin()));
		// permalink += "&end="
		// + DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ss").format(new
		// Date(TimeManager.getInst().getEnd()));
		//
		// URL encoding
		// permalink = permalink.replace("#", "%23");
		//
		// return permalink;
	}

	private TimeRange createTimeRange() {
		Date start = new Date(TimeManager.getInst().getBegin());
		Date end = new Date(TimeManager.getInst().getEnd());
		DateTimeFormat format = DateTimeFormat.getFormat(ISO_8601);
		return TimeRange.createTimeRange(format.format(start), format.format(end));
	}

	private TimeSeriesParameters createTimeSeriesParameters(TimeSeries timeSeries) {
		String sos = timeSeries.getSosUrl();
		String offering = timeSeries.getOfferingId();
		String procedure = timeSeries.getProcedureId();
		String phenomenon = timeSeries.getPhenomenonId();
		String feature = timeSeries.getFeatureId();
		SOSMetadata metadata = DataManagerSosImpl.getInst().getServiceMetadata(sos);
		String sosVersion = metadata.getSosVersion();
		return new TimeSeriesParameters(sos, sosVersion, offering, procedure, phenomenon, feature);
	}

	// Calls native javascript function addbookmark listed in Client.html
	private native void addToFavorites() /*-{
											$wnd.addBookmark();
											}-*/;

	public void updateUserName(String usr) {
		this.userNameTxt.setVisible(true);
		this.userNameTxt.setContents("<span style='color:blue;'>" + usr + "</span>");
	}

}
