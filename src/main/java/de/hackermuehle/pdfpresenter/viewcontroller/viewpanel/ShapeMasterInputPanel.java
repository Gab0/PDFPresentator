package de.hackermuehle.pdfpresenter.viewcontroller.viewpanel;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Line2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

import de.hackermuehle.pdfpresenter.model.Presentation;
import de.hackermuehle.pdfpresenter.model.State;
import de.hackermuehle.pdfpresenter.model.annotations.Line;
import de.hackermuehle.pdfpresenter.model.slide.Slide;
import de.hackermuehle.pdfpresenter.model.tools.Pen;

import org.apache.log4j.Logger;


public class ShapeMasterInputPanel extends DrawerMasterInputPanel {
    Line _line;

    public ShapeMasterInputPanel(Slide slide, Rectangle2D source, Pen pen, State controller) {
        super(slide, source, pen, controller);



        addMouseListener(new InputPanelMouseListener());

    }


    class InputPanelMouseListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent event) {}

        @Override
        public void mouseEntered(MouseEvent event) {}

        @Override
        public void mouseExited(MouseEvent event) {}

        @Override
        public void mousePressed(MouseEvent event) {
            if (event.getButton() == MouseEvent.BUTTON1) {
                if (getClipping() == null) return;

                _direct_line = (Point2D) clickToPoint(event).clone();

                Logger.getLogger(ShapeMasterInputPanel.class).warn("CLICK " + _direct_line);
            }


        }

        @Override
        public void mouseReleased(MouseEvent event) {

            if (event.getButton() == MouseEvent.BUTTON1) {
            Point2D point = clickToPoint(event);
            Logger.getLogger(ShapeMasterInputPanel.class).warn("B" + _direct_line);
            if (_direct_line == null) return;
            if (getClipping() == null) return;

            _line = drawLine();

            _line.addPoint((Point2D) _direct_line.clone());
            _line.addPoint((Point2D) point.clone());

            double lineAngle = getAngle(point, _direct_line);
            double rads      = Math.toRadians(lineAngle);
            double distance  = point.distance(_direct_line);

            Logger.getLogger(ShapeMasterInputPanel.class).warn("A " + lineAngle);


            AffineTransform a1t =
                AffineTransform.getRotateInstance(Math.toRadians(23),
                                                  point.getX(), point.getY());

            AffineTransform a2t =
                AffineTransform.getRotateInstance(Math.toRadians(-23),
                                                  point.getX(), point.getY());

            Point2D a1p = new Point2D.Double(0, 0);
            Point2D a2p = new Point2D.Double(0, 0);

            Point2D target = rescaleDistance(point, _direct_line, 0.2);
            a1t.transform(target, a1p);
            a2t.transform(target, a2p);



            Line a1 = drawLine();
            a1.addPoint((Point2D) point.clone());
            a1.addPoint(a1p);

            Line a2 = drawLine();
            a2.addPoint((Point2D) point.clone());
            a2.addPoint(a2p);

            Logger.getLogger(ShapeMasterInputPanel.class).warn(a2);

            getSlide().insert(_line);
            getSlide().insert(a1);
            getSlide().insert(a2);
            getSlide().concludeAction();

            _direct_line = null;
            }
        }

        public Point2D rescaleDistance(Point2D origin, Point2D prev_target, double factor) {

            double X = origin.getX();
            double Y = origin.getY();

            double vX = prev_target.getX() - X;
            double vY = prev_target.getY() - Y;

            return new Point2D.Double(X + vX * factor, Y + vY * factor);
        }

        public Point2D makeAngle(Point2D origin, double rads, double size) {

            double endX   = origin.getX() + size * Math.sin(rads);
            double endY   = origin.getY() + size * Math.cos(rads);
        return new Point2D.Double(endX, endY);


        }

        public double getAngle(Point2D origin, Point2D target) {
            double angle = (double)
                Math.toDegrees(Math.atan2(target.getY() - origin.getY(),
                                          target.getX() - origin.getX()));

            if(angle < 0){
                angle += 360;
            }

            return angle;

        }

    }

    class InputPanelMouseMotionListener implements MouseMotionListener {

        @Override
        public void mouseDragged(MouseEvent event) {}

        @Override
        public void mouseMoved(MouseEvent event) {}


    }

    @Override
    public void setPresentation(Presentation presentation) {}

}
