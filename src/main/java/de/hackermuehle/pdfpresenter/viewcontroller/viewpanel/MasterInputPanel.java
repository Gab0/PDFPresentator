package de.hackermuehle.pdfpresenter.viewcontroller.viewpanel;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

import de.hackermuehle.pdfpresenter.model.Clipping;
import de.hackermuehle.pdfpresenter.model.ImmutableClipping;
import de.hackermuehle.pdfpresenter.model.Presentation;
import de.hackermuehle.pdfpresenter.model.slide.Slide;

/**
 * A master input panel receives input events (mouse, keyboard, ...) and 
 * transforms them into actions (annotations, zoom, ...). As long as an input
 * is not completed, the input panel is responsible for the visualization of
 * the party finished action (i.e. an unfinished line, a zoom selection
 * rectangle).
 * 
 * In addition, the master input panel allows slave input panels to listen to 
 * changes in its visualization and to copy of these changes (i.e they become
 * a visual clone of the master input panel).
 */
public abstract class MasterInputPanel extends InputViewPanel {

	private static final long serialVersionUID = 1L;
	private LinkedList<SlaveInputPanel> _listeners = new LinkedList<SlaveInputPanel>();
	private Slide _slide;
	private Clipping _clipping;
	private Rectangle2D _source;
	protected Presentation _presentation;

	public MasterInputPanel(Slide slide, Rectangle2D source, Presentation presentation) {
		_slide = slide;
		_source = (Rectangle2D) source.clone();
		_clipping = null;
		_presentation = presentation;
		
		setLayout(null);
		setOpaque(false);
	}
	
	/**
	 * Overwritten since it is necessary to react to size changes before
	 * external listeners receive the size change messages.
	 * This method is used internally by Swing to handle all kind of size
	 * changes. This behavior may change in future, then overwrite setBounds()
	 * instead.
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void reshape(int x, int y, int width, int height) {
		if (width != getWidth() || height != getHeight()) {
			try {
				_clipping = createClipping(_source, width, height);
			} catch (NoninvertibleTransformException e) {
				_clipping = null;
			}
		}
		
		// Places size change messages in the message queue but does not 
		// handle them before this method is terminated:
		super.reshape(x, y, width, height);
	}
	
	public void setSlide(Slide slide) {
		_slide = slide;
	}
	
	public Slide getSlide() {
		return _slide;
	}
	
	public void setSource(Rectangle2D source) {
		_source = source;
	
		try {
			_clipping = createClipping(_source);
		} catch (NoninvertibleTransformException e) {
			_clipping = null;
		}
	}
	
	public ImmutableClipping getClipping() {
		return _clipping;
	}
	
	public void addListener(SlaveInputPanel graphicsListener) {
		_listeners.add(graphicsListener);
	}
	
	public boolean removeListener(SlaveInputPanel graphicsListener) {
		return _listeners.remove(graphicsListener);
	}
	
	protected void updateListeners(Rectangle2D rectangle2D) {
		for (SlaveInputPanel listener : _listeners) {
			listener.update(rectangle2D);
		}
	}
	
	/**
	 * Paints the content of this input panel shared with slave input panels.
	 * 
	 * @see paintContent
	 * @param g2d graphics context in source space.
	 */
	public void paintSlave(Graphics2D g2d, Clipping clipping) {
		AffineTransform originalTransform = g2d.getTransform();
		
		g2d.transform(clipping.getTransform());
		paintContent(g2d);
		
		g2d.setTransform(originalTransform);
	}
	
	/**
	 * Changes the given source rectangle into a source rectangle that has the
	 * same side ratio as the clipping source and lies inside the slide
	 * borders.
	 * 
	 * @param sourceIn The given source rectangle
	 * @return Source rectangle fitted into the clipping source
	 */
	protected Rectangle2D normalizeSource(Rectangle2D sourceIn) {
		Rectangle2D source = (Rectangle2D) sourceIn.clone();
		source = source.createIntersection(_clipping.getSource());
		double sourceRatio = source.getWidth() / source.getHeight();
		
		double slideRatio;
		if (_listeners.isEmpty() || _listeners.getFirst().getHeight() == 0)
			slideRatio = getHeight() != 0 ? getWidth() / (double) getHeight() : 4 / 3;
		else
			slideRatio = _listeners.getFirst().getWidth() / (double) _listeners.getFirst().getHeight();
		
		if (sourceRatio < slideRatio) {
			double width = slideRatio * source.getHeight();
			source.setRect(source.getX()+source.getWidth()/2.0-width/2.0, source.getY(), width, source.getHeight());
		}
		else if (sourceRatio > slideRatio) {
			double height = 1/slideRatio * source.getWidth();
			source.setRect(source.getX(), source.getY()+source.getHeight()/2.0-height/2.0, source.getWidth(), height);
		}
		
		// The rectangle must be included in the source rect:
		source.setRect(Math.max(source.getX(), 0), Math.max(source.getY(), 0), source.getWidth(), source.getHeight());
		
		double dx = source.getX() + source.getWidth() - getSlide().getSize().getX() - getSlide().getSize().getWidth();
		if (dx > 0) {
			source.setRect(source.getX() - dx, source.getY(), source.getWidth(), source.getHeight());
		}
		
		double dy = source.getY() + source.getHeight() - getSlide().getSize().getY() - getSlide().getSize().getHeight();
		if (dy > 0) {
			source.setRect(source.getX(), source.getY() - dy, source.getWidth(), source.getHeight());
		}
		source = source.createIntersection(_clipping.getSource());
		return source;
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (_clipping == null) return;
		
		Graphics2D g2d = (Graphics2D) g;
		//g2d.fill(g.getClip());
		AffineTransform originalTransform = g2d.getTransform();
		g2d.transform(_clipping.getTransform());
		paintContent(g2d);
		g2d.setTransform(originalTransform);
	}
	
	/**
	 * Paints the content of this input panel shared with slave input panels.
	 * If additional content has to be drawn, such as visual hints, that should
	 * not be displayed on slave input panels, the paintSlave()-method has to
	 * be overwritten.
	 * 
	 * @see paintSlave
	 * @param g2d graphics context in source space.
	 */
	protected void paintContent(Graphics2D g2d) {}
}
