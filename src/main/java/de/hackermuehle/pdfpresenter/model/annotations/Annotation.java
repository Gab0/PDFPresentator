package de.hackermuehle.pdfpresenter.model.annotations;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Immutable Interface for graphical annotations.
 * A graphical annotation is a representation of a graphical object that can
 * be displayed on a slide, e.g. a line or a text.
 */
public interface Annotation extends Cloneable {
	public Rectangle2D getBounds();
	
	/**
	 * Checks whether this annotation, when drawn, would contain the given
	 * point with the given radius.
	 * 
	 * @param point The point possibly contained
	 * @param size The radius
	 * @return True whether the point is contained
	 */
	public boolean contains(Point2D point, double size);
	
	/**
	 * Checks whether this annotation, when drawn, would be intersected by the
	 * given line.
	 * 
	 * @param line The line possibly intersecting
	 * @return True whether the line intersects
	 */
	public boolean intersects(Line2D line);
	
	/**
	 * Paints this annotation on a graphics context in source space.
	 * 
	 * @param g2d Graphics context
	 */
	public void paint(Graphics2D g2d);
	
	public Object clone();
}
