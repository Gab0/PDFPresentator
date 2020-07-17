package de.hackermuehle.pdfpresenter.model.annotations;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.RasterFormatException;

import de.hackermuehle.pdfpresenter.PdfPresenter;

/**
 * A line that has a width, color and stroke.
 * Supports multiply blending mode.
 */
public class Line implements Annotation {
    static final int INITIAL_CAPACITY = 100;
	
    // Linux bugfix, see paint(). Max bugfix, incompatible BlenderComposite:
    private static boolean _translucencySupported = !PdfPresenter.isOnMac(); 
	
    private Color _color;
    private BasicStroke _stroke;
    private PolyLine _polyLine = new PolyLine();
    private boolean _translucent;
	
    public Line(Color color, BasicStroke stroke, boolean translucent) {
        _color = color;
        _stroke = stroke;
        _translucent = translucent;
    }
	
    public void setColor(Color color) {
        _color = color;
    }
	
    public void addPoint(Point2D point) {
        _polyLine.addPoint(point);
    }
	
    public Point2D getLastPoint() {
        return (Point2D) _polyLine.getLastPoint().clone();
    }

    public Point2D getFirstPoint() {
        return (Point2D) _polyLine.getLastPoint().clone();
    }

    public void setStroke(BasicStroke stroke) {
        _stroke = stroke;
    }

    public BasicStroke getStroke() {
        return _stroke;
    }

    public void paint(Graphics2D g2d) {
        Stroke originalStroke = g2d.getStroke();
		
        g2d.setColor(_color);
        g2d.setStroke(_stroke);
		
        // Try a better blending if supported:
        if (_translucent && _translucencySupported) {
            Composite originalComposite = g2d.getComposite();
			
            // "Darken" keeps underlying black text readable:
            try {
                g2d.setComposite(BlendComposite.Darken);
                g2d.draw(_polyLine);
				
            } catch (InternalError e) {
                // "Not implemented yet"-Exceptions etc. on Linux.
                _translucencySupported = false;
                g2d.setComposite(originalComposite);
                g2d.draw(_polyLine);
            } catch (RasterFormatException e) {
                // Color model incompatible:
                _translucencySupported = false;
                g2d.setComposite(originalComposite);
                g2d.draw(_polyLine);
            }
            finally {
                g2d.setComposite(originalComposite);
            }
        }
        else g2d.draw(_polyLine);

        g2d.setStroke(originalStroke);
    }

    public Rectangle2D getBounds() {
        Rectangle2D bounds = _polyLine.getBounds2D();

        bounds.setRect(
                       bounds.getX() - _stroke.getLineWidth(),///2.0,
                       bounds.getY() - _stroke.getLineWidth(),///2.0, 
                       bounds.getWidth() + _stroke.getLineWidth()*2, 
                       bounds.getHeight() + _stroke.getLineWidth()*2);
        return bounds;
    }

    public boolean contains(Point2D point, double size) {
        Rectangle2D bounds = _polyLine.getBounds2D();

        bounds.setRect(
                       bounds.getX() - _stroke.getLineWidth()/2.0 - size/2.0,
                       bounds.getY() - _stroke.getLineWidth()/2.0 - size/2.0, 
                       bounds.getWidth() + _stroke.getLineWidth() + size, 
                       bounds.getHeight() + _stroke.getLineWidth() + size);
		
        if (!bounds.contains(point)) return false;
        return _polyLine.getDistance(point) <= (size + _stroke.getLineWidth())/2.0;
    }
	
    public boolean intersects(Line2D line) {
        if (!getBounds().intersectsLine(line)) return false;
        return _polyLine.getDistance(line) < _stroke.getLineWidth();
    }
	
    @Override
    public Object clone() {
        Object object = null;
        try {
            object = super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace(System.err);
        }
        ((Line) object)._polyLine = (PolyLine) _polyLine.clone();
        return object;
    }
}
