package de.hackermuehle.pdfpresenter.model;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;

/**
 * In PdfPresenter, there are two important spaces: source space and destination
 * space. The source space is defined in slide-size units, destination space 
 * is defined in pixels.
 * 
 * A Clipper is used to define a part of a slide (source space) that should be
 * displayed on screen (destination space) and he calculates the necessary
 * transformations.
 */
public class Clipping implements ImmutableClipping {

	protected Rectangle2D _source;
	protected Rectangle _destination;
	protected AffineTransform _transform;
	protected AffineTransform _inverseTransform;
	
	public Clipping(Rectangle2D source, Rectangle destination) throws NoninvertibleTransformException {
		_source = (Rectangle2D) source.clone();
		_destination = (Rectangle) destination.clone();
		_transform = createTransform(_source, _destination);
		_inverseTransform = _transform.createInverse();
	}
	
	public Rectangle2D getSource() {
		return (Rectangle2D) _source.clone();
	}
	
	public Rectangle getDestination() {
		return (Rectangle) _destination.clone();
	}
	
	public AffineTransform getTransform() {
		return (AffineTransform) _transform.clone();
	}
	
	public AffineTransform getInverseTransform() {
		return (AffineTransform) _inverseTransform.clone();
	}
	
	public void setSource(Rectangle2D source) throws NoninvertibleTransformException {
		_source = (Rectangle2D) source.clone();
		_transform = createTransform(_source, _destination);
		_inverseTransform = _transform.createInverse();
	}
	
	public void setDestination(Rectangle destination) throws NoninvertibleTransformException {
		_destination = (Rectangle) destination.clone();
		_transform = createTransform(_source, _destination);
		_inverseTransform = _transform.createInverse();
	}
	
	@Override
	public Object clone() {
		Object object = null;
		try {
			object = super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		((Clipping) object)._transform = (AffineTransform) _transform.clone();
		((Clipping) object)._inverseTransform = (AffineTransform) _inverseTransform.clone();
		return object;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Clipping)) return false;
		Clipping clipping = (Clipping)obj;
		return (_destination.equals(clipping._destination)) &&
			   (_source.equals(clipping._source));
	}
	
	/**
	 * Calculates a destination rectangle that lies in the center of the given
	 * rectangle defined by width and height and has maximal size, but has
	 * the same side ratio as the source rectangle.
	 * 
	 * @param source
	 * @param width
	 * @param height
	 * @return Destination rectangle with maximal size under the given constraints
	 */
	public static Rectangle createDestination(Rectangle2D source, int width, int height) {
		double scale = Math.min(width, height);
		double viewportRatio = width / (double) height;
		double sourceRatio = source.getWidth() / source.getHeight();
		double ratio = 1;
		
		if (sourceRatio >= 1.0) {
			if (viewportRatio >= sourceRatio)
				ratio = sourceRatio;
			else if (viewportRatio >= 1.0) {
				scale = Math.max(width, height);
			}
		}
		else {
			if (viewportRatio <= sourceRatio)
				ratio = 1.0/sourceRatio;
			else if (viewportRatio <= 1.0)
				scale = Math.max(width, height);
		}
		scale *= ratio;
		
		int destinationWidth = (int) (scale * sourceRatio);
		int destinationHeight = (int) (scale * 1.0 / sourceRatio);
		 
		if (destinationWidth > destinationHeight) destinationWidth = (int) scale;
		else destinationHeight = (int) scale;		
		Rectangle destination = new Rectangle((width - destinationWidth) / 2, (height - destinationHeight) / 2, destinationWidth, destinationHeight);
		
		return destination;
	}
	
	/**
	 * Creates an AffineTransform that transforms from source space into
	 * destination space, i.e. the corners of the source rectangles are mapped 
	 * onto the corners of the destination rectangle.
	 * 
	 * @param source
	 * @param destination
	 * @return An AffineTransform mapping source to destination
	 */
	public static AffineTransform createTransform(Rectangle2D source, Rectangle destination) {
		AffineTransform transform = new AffineTransform();
		double scaleX = destination.getWidth() / source.getWidth();
		double scaleY = destination.getHeight() / source.getHeight();
		transform.translate(destination.getX(), destination.getY());
		transform.scale(scaleX, scaleY);
		transform.translate(-source.getX(), -source.getY());
		return transform;
	}
}
