package de.hackermuehle.pdfpresenter.viewcontroller.viewpanel;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

import de.hackermuehle.pdfpresenter.model.Presentation;
import de.hackermuehle.pdfpresenter.model.State;
import de.hackermuehle.pdfpresenter.model.annotations.Line;
import de.hackermuehle.pdfpresenter.model.slide.Slide;
import de.hackermuehle.pdfpresenter.model.tools.Pen;

import org.apache.log4j.Logger;


public class PenMasterInputPanel extends LaserPointerMasterInputPanel {
    private static final long serialVersionUID = 1L;
    private Line _line;
    private Pen _pen;
    private Point2D  _direct_line = null;

    public PenMasterInputPanel(Slide slide, Rectangle2D source, Pen pen, State controller) {
        super(slide, source, controller.getLaser(), controller.getActivePresentation());

        // Configure underlying eraser panel:
        LinkedList<Integer> laserButtons = new LinkedList<Integer>();
        laserButtons.add(MouseEvent.BUTTON3);
        setLaserButtons(laserButtons);

        _pen = pen;

        addMouseListener(new InputPanelMouseListener());
        addMouseMotionListener(new InputPanelMouseMotionListener());
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

                // Start painting a new line at the current position:
                Point2D point = new Point2D.Double(event.getX(), event.getY());
                getClipping().getInverseTransform().transform(point, point);

                // If CTRL is pressed we'll store the initial point to make a straight line;
                if ((event.getModifiers() & MouseEvent.CTRL_MASK) == MouseEvent.CTRL_MASK)
                    _direct_line = (Point2D) point.clone();
                    else _direct_line = null;


                _line = new Line(
                                 _pen.getColor(),
                                 new BasicStroke(
                                                 (float)_pen.getSize() * (float)getClipping().getInverseTransform().getScaleX(),
                                                 _pen.getCap(),
                                                 _pen.getJoin()),
                                 _pen.isTranslucent());
                _line.addPoint(point);

                if (getClipping() != null) {

                    // Update the newly painted point:
                    int width = (int) Math.ceil(_line.getStroke().getLineWidth() * (float)getClipping().getTransform().getScaleX());

                    repaint(event.getX() - (int)Math.ceil(width/2), event.getY() - (int)Math.ceil(width/2), (int)width, (int)width);
                    updateListeners(new Rectangle2D.Double(point.getX() - _line.getStroke().getLineWidth()/2,
                                                           point.getY() - _line.getStroke().getLineWidth()/2,
                                                           _line.getStroke().getLineWidth(),
                                                           _line.getStroke().getLineWidth()));
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent event) {
            if (event.getButton() == MouseEvent.BUTTON1) {
                if (_line != null) {

                    // If the user is drawing a line, insert it into the slide:
                    getSlide().insert(_line);
                    getSlide().concludeAction();

                    Rectangle2D bounds = _line.getBounds();
                    _line = null;

                    if (getClipping() != null) {

                        // Update the whole - now empty - line:
                        repaint(getClipping().getTransform().createTransformedShape(bounds).getBounds());
                        updateListeners(bounds);
                    }
                    _direct_line = null;
                }
            }
        }
    }

    class InputPanelMouseMotionListener implements MouseMotionListener {

        @Override
        public void mouseDragged(MouseEvent event) {
            if (_line != null && getClipping() != null) {


                // If the user is drawing a line, add the current point:
                Point2D point = new Point2D.Double(event.getX(), event.getY());
                getClipping().getInverseTransform().transform(point, point);

                Point2D lastPoint = null;
                if (_direct_line != null) {
                    _line = new Line(_pen.getColor(),
                                     new BasicStroke((float)_pen.getSize() * (float)getClipping().getInverseTransform().getScaleX(), _pen.getCap(), _pen.getJoin()),
                                     _pen.isTranslucent());

                    _line.addPoint(_direct_line);
                    _line.addPoint(point);

                }
                else {
                    lastPoint = _line.getLastPoint();

                    _line.addPoint(point);
                }
                    // Update the newly painted line segment:
                    double size = Math.sqrt(_line.getStroke().getLineWidth()*_line.getStroke().getLineWidth()*2);
                    Rectangle2D bounds2D = new Rectangle2D.Double(lastPoint.getX(), lastPoint.getY(), 0, 0);
                    bounds2D.add(point);
                    bounds2D.setRect(bounds2D.getX() - size/2, bounds2D.getY() - size/2, bounds2D.getWidth() + size, bounds2D.getHeight() + size);

                    repaint(getClipping().getTransform().createTransformedShape(bounds2D).getBounds());

                    updateListeners(bounds2D);
                
            }
        }

        @Override
        public void mouseMoved(MouseEvent event) {}
    }

    @Override
    protected void paintContent(Graphics2D g2d) {
        super.paintContent(g2d);
        if (_line != null) _line.paint(g2d);
    }

    @Override
    public void setPresentation(Presentation presentation) {}
}
