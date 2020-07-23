package de.hackermuehle.pdfpresenter.viewcontroller.viewpanel;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

import de.hackermuehle.pdfpresenter.model.Presentation;
import de.hackermuehle.pdfpresenter.model.State;
import de.hackermuehle.pdfpresenter.model.annotations.Line;
import de.hackermuehle.pdfpresenter.model.slide.Slide;
import de.hackermuehle.pdfpresenter.model.tools.Pen;


import org.apache.log4j.Logger;


public class DrawerMasterInputPanel extends MasterInputPanel {

    public Pen _pen;

    public Point2D _direct_line;

    public DrawerMasterInputPanel(Slide slide, Rectangle2D source, Pen pen, State controller) {
        super(slide, source, controller.getActivePresentation());

        _pen = pen;
    }

    public Line drawLine() {
        Logger.getLogger(this.getClass()).warn("PEN: " + _pen);
        return new Line(_pen.getColor(),
                        new BasicStroke(
                                        (float)_pen.getSize() *
                                        (float)getClipping().getInverseTransform().getScaleX(),
                                        _pen.getCap(),
                                        _pen.getJoin()),
                        _pen.isTranslucent());
    }

    public Point2D clickToPoint(MouseEvent event) {
        Point2D point = new Point2D.Double(event.getX(), event.getY());
        getClipping().getInverseTransform().transform(point, point);
        return point;
    }



    @Override
    public void setPresentation(Presentation presentation) {}


 
}
