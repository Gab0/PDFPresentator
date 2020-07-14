package de.hackermuehle.pdfpresenter.viewcontroller.viewpanel;

import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import de.hackermuehle.pdfpresenter.model.Clipping;

/**
 * A {@link ViewPanel} is the base class for all Panels whose visualization
 * depends on a {@link Slide} or a part of it (i.e. they display a slide or
 * take and display input
 * for a slide).
 */
public abstract class ViewPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	public abstract void setSource(Rectangle2D source);

	/**
	 * Creates a clipping that maps the given source to a destination rectangle
	 * that is centered and fits into this panel's border.
	 * 
	 * @param source	The source rectangle.
	 * @return A {@link Clipping} with a destination fitting into the given rectangle.
	 * @throws NoninvertibleTransformException Panel width or height <= 0.
	 */
	protected final Clipping createClipping(Rectangle2D source) throws NoninvertibleTransformException {
		return createClipping(source, getWidth(), getHeight());
	}
	
	/**
	 * Creates a {@link Clipping} that maps the given source to a destination
	 * rectangle that is centered and fits into the rectangle given by width
	 * and height.
	 * 
	 * @param source	The source rectangle.
	 * @param width		The width of the rectangle to fit the destination in.
	 * @param height	The height of the rectangle to fit the destination in.
	 * @return A Clipping with a destination fitting into the given rectangle.
	 * @throws NoninvertibleTransformException Width or height <= 0.
	 */
	protected final Clipping createClipping(Rectangle2D source, int width, int height) throws NoninvertibleTransformException {
		if ((width <= 0) || (height <= 0))
			throw new NoninvertibleTransformException("Width and height cannot be <= 0");
		
		return new Clipping(source, Clipping.createDestination(source, width, height));
	}
}
