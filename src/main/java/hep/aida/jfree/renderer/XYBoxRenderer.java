package hep.aida.jfree.renderer;

import hep.aida.jfree.dataset.XYZRangedDataset;
import hep.aida.jfree.dataset.XYZRangedDataset.ZRange;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;

/**
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: $
 */
public class XYBoxRenderer extends AbstractXYItemRenderer {

    double boxWidth;
    double boxHeight;

    /*
     * double boxHypotenuse; double theta; // upper left angle of triangle
     * double phi; // lower right angle of triangle
     */

    public XYBoxRenderer(double boxWidth, double boxHeight) {
        this.boxWidth = boxWidth;
        this.boxHeight = boxHeight;
        /*
         * this.boxHypotenuse = sqrt(boxWidth * boxWidth + boxHeight *
         * boxHeight); this.theta = asin(this.boxHeight / this.boxHypotenuse);
         * this.phi = Math.PI - (Math.PI/2) - theta;
         * 
         * System.out.println("boxHypotenuse = " + this.boxHypotenuse);
         * System.out.println("theta = " + this.theta);
         * System.out.println("phi = " + this.phi);
         * System.out.println("angles sum = " + (this.theta + this.phi +
         * Math.PI/2));
         */
    }

    private double getHeightScaled(double z, ZRange range) {
        return (z / range.getZMax()) * boxHeight;
    }

    private double getWidthScaled(double z, ZRange range) {
        return (z / range.getZMax()) * boxWidth;
    }

    /*
     * private double getHypotenuseScaled(double z, ZRange range) { double scale
     * = z / range.zmax; return boxHypotenuse * scale; }
     * 
     * private double getHeight(double hypotenuse) { return hypotenuse *
     * sin(theta); }
     * 
     * private double getWidth(double hypotenuse) { return hypotenuse *
     * cos(theta); }
     */

    public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item, CrosshairState crosshairState, int pass) {

        if (!this.isSeriesVisible(series))
            return;

        double x = dataset.getXValue(series, item);
        double y = dataset.getYValue(series, item);
        double z = 0.0;
        if (dataset instanceof XYZDataset) {
            z = ((XYZDataset) dataset).getZValue(series, item);
        }

        if (z == 0)
            return;

        ZRange zrange = null;
        if (dataset instanceof XYZRangedDataset) {
            zrange = ((XYZRangedDataset) dataset).getZRange(series);
        } else {
            throw new IllegalArgumentException("Dataset is wrong type: " + dataset.getClass().getCanonicalName());
        }

        double heightScaled = this.getHeightScaled(z, zrange);
        double widthScaled = this.getWidthScaled(z, zrange);

        /*
         * double hypotenuseLengthScaled = this.getHypotenuseScaled(z, zrange);
         * double heightNew = this.getHeight(hypotenuseLengthScaled); double
         * widthNew = this.getWidth(hypotenuseLengthScaled);
         * System.out.println("z = " + z); System.out.println("heightScaled = "
         * + heightScaled); System.out.println("widthScaled = " + widthScaled);
         * System.out.println("heightNew = " + heightNew);
         * System.out.println("widthNew = " + widthNew); System.out.println();
         */

        double xx0 = domainAxis.valueToJava2D(x, dataArea, plot.getDomainAxisEdge());
        double yy0 = rangeAxis.valueToJava2D(y, dataArea, plot.getRangeAxisEdge());
        double xx1 = domainAxis.valueToJava2D(x + widthScaled, dataArea, plot.getDomainAxisEdge());
        double yy1 = rangeAxis.valueToJava2D(y + heightScaled, dataArea, plot.getRangeAxisEdge());

        Rectangle2D box;
        PlotOrientation orientation = plot.getOrientation();

        double widthDraw = Math.abs(xx1 - xx0);
        double heightDraw = Math.abs(yy1 - yy0);

        if (orientation.equals(PlotOrientation.HORIZONTAL)) {
            // FIXME: Have not checked this.
            box = new Rectangle2D.Double(Math.min(yy0, yy1) + heightDraw / 2, Math.min(xx0, xx1) - widthDraw / 2, Math.abs(yy1 - yy0), Math.abs(xx0 - xx1));
        } else {
            // Don't know if this adjustment to XY works in all generality
            // but seems okay for test case.
            box = new Rectangle2D.Double(Math.min(xx0, xx1) - widthDraw / 2, Math.min(yy0, yy1) + heightDraw / 2, Math.abs(xx1 - xx0), Math.abs(yy1 - yy0));

        }
        g2.setPaint(this.getSeriesOutlinePaint(series));
        Stroke stroke = this.getSeriesStroke(series);
        if (stroke != null) {
            g2.setStroke(stroke);
        } else {
            g2.setStroke(new BasicStroke(1.0f));
        }
        g2.draw(box);
        Paint paint = this.getSeriesFillPaint(series);
        if (paint != null) {
            g2.fill(box);
        }
        EntityCollection entities = state.getEntityCollection();
        if (entities != null) {
            addEntity(entities, box, dataset, series, item, 0.0, 0.0);
        }
    }
}