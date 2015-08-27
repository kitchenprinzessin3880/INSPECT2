

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.util.EventHandler;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.MouseMoveEvent;
import com.smartgwt.client.widgets.events.MouseMoveHandler;
import com.smartgwt.client.widgets.events.MouseOutEvent;
import com.smartgwt.client.widgets.events.MouseOutHandler;
import com.smartgwt.client.widgets.events.MouseStillDownEvent;
import com.smartgwt.client.widgets.events.MouseStillDownHandler;
import com.smartgwt.client.widgets.events.MouseUpEvent;
import com.smartgwt.client.widgets.events.MouseUpHandler;
import com.smartgwt.client.widgets.events.MouseWheelEvent;
import com.smartgwt.client.widgets.events.MouseWheelHandler;

public class Showcase implements EntryPoint{

    public void onModuleLoad() {
       RootPanel.get().add(getViewPanel());
    }


    public Canvas getViewPanel() {

      Canvas canvas = new Canvas();

      final TrackerLabel eventTrackerLabel = new TrackerLabel();
      eventTrackerLabel.setContents("<nobr>Last event: (mouse over the canvas below...)</nobr>");
      eventTrackerLabel.setHeight(20);

      final MouserLabel label = new MouserLabel();
      label.setContents("<b>Mouse Me</b>");
      label.setAlign(Alignment.CENTER);
      label.setOverflow(Overflow.HIDDEN);
      label.setShowEdges(true);
      label.setBackgroundColor("lightblue");
      label.setWidth(200);
      label.setHeight(200);
      label.setTop(40);
      label.addMouseWheelHandler(new MouseWheelHandler() {
          public void onMouseWheel(MouseWheelEvent event) {
              int wheelDelta = EventHandler.getWheelDelta();

              int newSize = label.getWidth() + wheelDelta * label.getZoomMultiplier();
              if (newSize < label.getMinSize()) {
                  newSize = label.getMinSize();
              } else if (newSize > label.getMaxSize()) {
                  newSize = label.getMaxSize();
              }
              label.setWidth(newSize);
              label.setHeight(newSize);
              eventTrackerLabel.setLastEvent("mouseWheel", label);
          }
      });

      label.addMouseStillDownHandler(new MouseStillDownHandler() {
          public void onMouseStillDown(MouseStillDownEvent event) {
              Integer opacity = label.getOpacity();
              if (opacity == null) opacity = 100;
              label.setOpacity(Math.max(0, opacity - 5));
              eventTrackerLabel.setLastEvent("mouseStillDown", label);
          }
      });

      label.addMouseUpHandler(new MouseUpHandler() {
          public void onMouseUp(MouseUpEvent event) {
              label.setOpacity(100);
              eventTrackerLabel.setLastEvent("mouseUp", label);
          }
      });

      label.addMouseMoveHandler(new MouseMoveHandler() {
          public void onMouseMove(MouseMoveEvent event) {
              //scale to 1
              float xScale = (label.getOffsetX() * 1f) / label.getWidth();
              float yScale = (label.getOffsetY() * 1f) / label.getHeight();

              // increasing red intensity on the x axis, green on the y axis.  Blue stays at zero.
              label.setBackgroundColor("rgb(0," + Math.round(255 * xScale) + "," + Math.round(255 * yScale) + ")");
              eventTrackerLabel.setLastEvent("mouseMove", label);
          }
      });

      label.addMouseOutHandler(new MouseOutHandler() {
          public void onMouseOut(MouseOutEvent event) {
              //restore settings
              label.setBackgroundColor("lightblue");
              label.setOpacity(100);
              eventTrackerLabel.setLastEvent("mouseOut", label);
          }
      });

      canvas.addChild(eventTrackerLabel);
      canvas.addChild(label);
      return canvas;
  }
  class TrackerLabel extends Label {

      public void setLastEvent(String eventName, MouserLabel label) {
          int localX = label.getOffsetX();
          int localY = label.getOffsetY();
          setContents("<nobr>Last event: <b>" + eventName + "</b> (" + localX + ", " + localY + ")</nobr>");
      }
  }


  class MouserLabel extends Label {
      private int minSize = 40;
      private int maxSize = 400;
      private int zoomMultiplier = 15;

      public int getMinSize() {
          return minSize;
      }

      public void setMinSize(int minSize) {
          this.minSize = minSize;
      }

      public int getMaxSize() {
          return maxSize;
      }

      public void setMaxSize(int maxSize) {
          this.maxSize = maxSize;
      }

      public int getZoomMultiplier() {
          return zoomMultiplier;
      }

      public void setZoomMultiplier(int zoomMultiplier) {
          this.zoomMultiplier = zoomMultiplier;
      }
  }
}

   