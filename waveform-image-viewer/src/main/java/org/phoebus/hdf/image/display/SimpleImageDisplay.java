package org.phoebus.hdf.image.display;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.csstudio.javafx.rtplot.*;
import org.csstudio.javafx.rtplot.data.ArrayPlotDataProvider;
import org.csstudio.javafx.rtplot.data.SimpleDataItem;
import org.csstudio.javafx.rtplot.internal.ImagePlot;
import org.csstudio.javafx.rtplot.internal.Plot;
import org.csstudio.javafx.rtplot.internal.TraceImpl;
import org.epics.util.array.ArrayLong;

import java.awt.*;


public class SimpleImageDisplay {

    @FXML
    AnchorPane upper;
    @FXML
    Pane imagePane;

    @FXML
    AnchorPane lower;

    @FXML
    Button b1;
    @FXML
    Button b2;

    @FXML
    final ImagePlot imagePlot = new ImagePlot(true);
    @FXML
    final RTTimePlot rtTimePlot = new RTTimePlot(true);
    @FXML
    final Plot<Double> plot2 = new Plot<>(Double.class, true);

    public final static ColorMappingFunction RAINBOW = value -> new Color(Color.HSBtoRGB((float)value, 1.0f, 1.0f)).getRGB();

    @FXML
    public void initialize() throws Exception {

        b1 = new Button("b1");
        b2 = new Button("update");

        b2.setOnAction(actionEvent ->  {
            imagePlot.setValue(200, 200, computeData(200, 200), false);
        });

        lower.getChildren().add(b2);

        plot2.setTitle("Plot Demo");
        plot2.getXAxis().setName("The horizontal quantities on 'X'");
        plot2.addYAxis("Another Axis");
        plot2.getYAxes().get(1).setOnRight(true);

        final ArrayPlotDataProvider<Double> data1 = new ArrayPlotDataProvider<>();
        final ArrayPlotDataProvider<Double> data2 = new ArrayPlotDataProvider<>();
        final ArrayPlotDataProvider<Double> data3 = new ArrayPlotDataProvider<>();
        for (double x = -10.0; x <= 10.0; x += 1.0)
            if (x == 2.0)
            {
                data1.add(new SimpleDataItem<>(x, Double.NaN));
                data2.add(new SimpleDataItem<>(x, Double.NaN));
                data3.add(new SimpleDataItem<>(x, Double.NaN));
            }
            else
            {
                data1.add(new SimpleDataItem<>(x, x*x - 5.0));
                data2.add(new SimpleDataItem<>(x, 2*x));
                data3.add(new SimpleDataItem<>(x, x*x + 5.0));
            }
        plot2.addTrace(new TraceImpl<>("Demo Data", "socks", data1, javafx.scene.paint.Color.BLUE, TraceType.BARS, 0, LineStyle.SOLID, PointType.NONE, 15, 0));
        plot2.addTrace(new TraceImpl<>("Demo Data", "socks", data1, javafx.scene.paint.Color.VIOLET, TraceType.BARS, 10, LineStyle.SOLID, PointType.NONE, 15, 0));
        plot2.addTrace(new TraceImpl<>("More Data", "pants", data2, javafx.scene.paint.Color.RED, TraceType.AREA, 3, LineStyle.SOLID, PointType.SQUARES, 15, 1));
        plot2.addTrace(new TraceImpl<>("More Data", "pants", data3, javafx.scene.paint.Color.GREEN, TraceType.LINES_DIRECT, 1, LineStyle.DASHDOT, PointType.XMARKS, 5, 0));
        plot2.getXAxis().setValueRange(-12.0, 12.0);

        //imagePane.getChildren().add(plot);
        imagePane.getChildren().add(plot2);

        rtTimePlot.getXAxis().setGridVisible(true);
        rtTimePlot.getXAxis().setAutoscale(true);

        //imagePane.getChildren().add(rtTimePlot);
        populate();
    }

    private void populate() {

        //plot.setUpdateThrottle(200, TimeUnit.MILLISECONDS);

        imagePlot.getXAxis().setGridVisible(true);
        imagePlot.getXAxis().setAutoscale(true);

        imagePlot.setColorMapping(RAINBOW);

        upper.getChildren().add(imagePlot);

        imagePlot.setValue(200, 200, computeData(200, 200), false);
    }

    private static final long start = System.currentTimeMillis();
    ArrayLong computeData(int WIDTH, int HEIGHT) {
        final long now = System.currentTimeMillis();

        final long phase = (long) ((now - start)/1000.0);

        final long[] data = new long[WIDTH * HEIGHT];
        int i = 0;
        for (int y=0; y<HEIGHT; ++y)
        {
            final double dy = y - HEIGHT/2;
            final double dy2 = dy*dy;
            for (int x=0; x<WIDTH; ++x)
            {
                final double dx = x - WIDTH/2;
                final double r = Math.sqrt(dx*dx + dy2);
                data[i++] = (long) (Math.exp(-r/(WIDTH/2)) * (1.0 + Math.cos(2*Math.PI*(r/(WIDTH/6) - phase))));
            }
        }
        return ArrayLong.of(data);
    }
}
