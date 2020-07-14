package de.hackermuehle.pdfpresenter.model.slide;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import de.hackermuehle.pdfpresenter.model.CacheObserver;
import de.hackermuehle.pdfpresenter.model.Clipping;
import de.hackermuehle.pdfpresenter.model.annotations.Annotation;
import de.hackermuehle.pdfpresenter.model.annotations.ActionList;

/** 
 * A slide represents a transparent surface on which one can draw.
 * The slide itself can be rendered to a g2d context.
 * 
 * Drawing on a slide means to perform actions on a slide:
 * Subsequent calls to insert / remove annotations should be concluded with
 * a call to "concludeAction". Those concluded bundles of actions can be 
 * undone / redone in one single step.
 */
public class Slide {
	private static Rectangle2D _defaultSize = new Rectangle2D.Double(0, 0, 400, 300);
	private ActionList _actions = new ActionList();
	private LinkedList<SlideListener> _listeners = new LinkedList<SlideListener>();
	private Rectangle2D _size;
	
	/**
	 * Constructs and initializes an empty slide with default size (ratio 4/3).
	 */
	public Slide() {
		_size = _defaultSize;
	}
	
	public void addListener(SlideListener slideListener) {
		_listeners.add(slideListener);
	}
	
	public void removeListener(SlideListener slidesListener) {
		_listeners.remove(slidesListener);
	}
	
	/**
	 * Inserts a new annotation.
	 * After execution, previously undone actions can no longer be redone.
	 * 
	 * @param annotation
	 */
	public void insert(Annotation annotation) {
		_actions.insert((Annotation) annotation.clone());
		
		for (SlideListener listener : _listeners) {
			listener.update(annotation.getBounds());
		}
	}
	
	/**
	 * Removes the given annotation, if it was previously inserted into this
	 * slide.
	 * After successful execution, previously undone actions can no longer be
	 * redone.
	 * 
	 * @param annotation
	 * @throws NoSuchElementException
	 */
	public void remove(Annotation annotation) {
		try {
			_actions.remove(annotation);
			
			for (SlideListener listener : _listeners) {
				listener.update(annotation.getBounds());
			}
		} catch (NoSuchElementException e) {
			throw new NoSuchElementException("annotation not in list");
		}
	}
	
	/**
	 * Concludes the action consisting of all added and removed annotations
	 * since the last call to this method. A concluded action can be undone /
	 * redone.
	 * 
	 * @see {@link undo}, {@link redo}
	 * @return false if action is empty, otherwise true
	 */
	public boolean concludeAction() {
		return _actions.conclude();
	}
	
	public boolean canUndo() {
		return _actions.canUndo();
	}
	
	/**
	 * Resets the annotations on this slide to the state they have been in
	 * at the last call to {@link concludeAction}.
	 */
	public void undo() {
		try {
			Rectangle2D bounds = _actions.undo();
			for (SlideListener listener : _listeners) {
				listener.update(bounds);
			}
		} catch(NoSuchElementException e) {
			throw new NoSuchElementException("no action to undo");
		}
	}
	
	public boolean canRedo() {
		return _actions.canRedo();
	}
	
	/**
	 * Resets the annotations on this slide to the state they have been in
	 * before the last call to {@link undo}.
	 */
	public void redo() {
		try {
			Rectangle2D bounds = _actions.redo();
			for (SlideListener listener : _listeners) {
				listener.update(bounds);
			}
		} catch(NoSuchElementException e) {
			throw new NoSuchElementException("no action to undo");
		}
	}
	
	/**
	 * Removes all annotations.
	 * 
	 * @see #remove(Annotation)
	 */
	public void reset() {
		_actions.reset();
		
		for (SlideListener listener : _listeners) {
			listener.update(getSize());
		}
	}
	
	public Annotation getAnnotation(Point2D point, double size, Class<? extends Annotation> annotationClass) {
		
		// Iterate in reverse, top-to-bottom order to get the topmost first:
		ListIterator<Annotation> it = _actions.getAnnotations().listIterator(_actions.getAnnotations().size());
		while (it.hasPrevious()) {
			Annotation annotation = it.previous();
			if (!annotationClass.isInstance(annotation)) continue;
			if (annotation.contains(point, size))
				return annotation;
		}
		return null;
	}
	
	public Annotation getAnnotation(Line2D line, Class<? extends Annotation> annotationClass) {

		// Iterate in reverse, top-to-bottom order to get the topmost first:
		ListIterator<Annotation> it = _actions.getAnnotations().listIterator(_actions.getAnnotations().size());
		while (it.hasPrevious()) {
			Annotation annotation = it.previous();
			if (!annotationClass.isInstance(annotation)) continue;
			if (annotation.intersects(line))
				return annotation;
		}
		return null;
	}
	
	public List<Annotation> getAnnotations() {
		return _actions.getAnnotations();
	}
	
	/**
	 * Creates a {@link SlideCacheEntry} if caching abilities are available.
	 * 
	 * @param clipping
	 * @param priority
	 * @param observer CacheObserver or null
	 * @return SlideCacheEntry or null
	 */
	public SlideCacheEntry cache(final Clipping clipping, final Integer priority, final CacheObserver observer) {
		return null;
	}
	
	/**
	 * @return The default slide size with a page ratio 4 / 3.
	 */
	public Rectangle2D getDefaultSize() {
		return (Rectangle2D) _defaultSize.clone();
	}
	
	/**
	 * @see {@link #setSize(Rectangle2D)}
	 * @return The current size of the slide
	 */
	public Rectangle2D getSize() {
		return (Rectangle2D) _size.clone();
	}
	
	/**
	 * Modifies the size of the slide. Defaults to the default slide size.
	 * 
	 * @see {@link #getSize(Rectangle2D)}, {@link #getDefaultSize()}
	 */
	public void setSize(Rectangle2D size) {
		_size = size;
		// TODO: Inform about size change (currently manual repaint)
	}
	
	/**
	 * The background color of the slide. Overwrite this method to define your
	 * own background color.
	 * 
	 * @return Background color
	 */
	public Color getBackground() {
		return Color.WHITE;
	}
	
	/**
	 * Paint the slide to a graphic context in destination space.
	 * 
	 * @param g2d		Graphic context in destination space
	 * @param clipping	{@link Clipping} used
	 * @param Grid		A grid or null
	 * @param revision	Unused
	 */
	public void paint(Graphics2D g2d, Clipping clipping, Grid grid, int revision) {
		Shape originalClip = g2d.getClip();
		AffineTransform originalTransform = g2d.getTransform();
		
		 // Transforms g2d into source space:
		g2d.transform(clipping.getTransform());
		g2d.clip(getSize());
		
		// 0. Fill background parts outside slide:
		g2d.setPaint(getBackground());
		g2d.fill(clipping.getSource());
		
		// 1. Paint complete slide:
		paintBackground(g2d, clipping, revision);
		if (grid != null) grid.paint(g2d, clipping, getSize());
		paintAnnotations(g2d, clipping, revision);
		
		g2d.setTransform(originalTransform);
		g2d.setClip(originalClip);
	}
	
	/**
	 * Paint the background of the slide to a graphic context in user space.
	 * 
	 * @param g2d		Graphic context
	 * @param clipping	{@link Clipping} used
	 * @param revision	Unused
	 */
	protected void paintBackground(Graphics2D g2d, Clipping clipping, int revision) {}
	
	/**
	 * Paint the annotations of the slide to a graphic context in user space.
	 * 
	 * @param g2d		Graphic context
	 * @param clipping	{@link Clipping} used
	 * @param revision	Unused
	 */
	protected void paintAnnotations(Graphics2D g2d, Clipping clipping, int revision) {
		Object originalHintValue = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		Object originalTextHintValue = g2d.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		for (Annotation annotation : _actions.getAnnotations()) {
			annotation.paint(g2d);
		}
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, originalHintValue);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, originalTextHintValue);
	}
}
