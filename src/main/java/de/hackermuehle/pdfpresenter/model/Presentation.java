package de.hackermuehle.pdfpresenter.model;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.hackermuehle.pdfpresenter.model.slide.Slide;

/**
 * A presentation is a collection of slides (of possibly different types).
 * Slides can be added/removed dynamically.
 */
public class Presentation {
	private LinkedList<Slide> _slides = new LinkedList<Slide>();
	private Slide _activeSlide = null;
	private String _title;
	private PropertyChangeSupport _propertyChangeSupport = new PropertyChangeSupport(this); 
	private Rectangle2D _source = new Rectangle(0, 0);
	private boolean _gridVisibility = false;;
	
	/**
	 * Creates and initializes an empty presentation with the given title.
	 * 
	 * @param title Title of the presentation
	 */
	public Presentation(String title) {
		_title = title;
	}
	
	@Override
	protected void finalize() throws Throwable {
		dispose();
		super.finalize();
	}
	
	/**
	 * Sets whether a grid should be drawn above the slides of this
	 * presentation.
	 * 
	 * @param gridVisibility True for a grid to be drawn
	 */
	public void setGridVisibility(boolean gridVisibility) {
		_gridVisibility = gridVisibility;
		_propertyChangeSupport.firePropertyChange("gridVisibility", null, _gridVisibility); 
	}
	
	public boolean getGridVisibility() {
		return _gridVisibility;
	}
	
	/**
	 * Sets the part of the slide that should be shown to the user. The given
	 * rectangle doesn't have to be inside the slide boundaries.
	 * 
	 * @param source The part of the slide to be shown
	 */
	public void setSource(Rectangle2D source) {
		_source = source;
		_propertyChangeSupport.firePropertyChange("source", null, _source); 
	}

	public Rectangle2D getSource() {
		return (Rectangle2D) _source.clone();
	}
	
	public void setTitle(String title) {
		_title = title;
	}
	
	public String getTitle() {
		return _title;
	}
	
	/**
	 * Sets the slide that should be active, i.e. that should be shown to the 
	 * user.
	 * 
	 * @param slide The slide to become active
	 * @return True if the active slide has changed
	 */
	public boolean setActiveSlide(Slide slide) {
		
		if (!_slides.contains(slide)) return false;
		if (slide.equals(_activeSlide)) return true;
		
		Slide oldValue = _activeSlide;
		_activeSlide = slide;
		
		_source = _activeSlide.getSize();
		
		_propertyChangeSupport.firePropertyChange("activeSlide", oldValue, _activeSlide);
		_propertyChangeSupport.firePropertyChange("source", oldValue, _source);
		return true;
	}
	
	/**
	 * Sets the slide next to the active slide in the list of the slides as 
	 * active.
	 * 
	 * @see {@link #setActiveSlide(Slide)}
	 * @return True whether the active slide changed
	 */
	public boolean nextSlide() {
		int nextIndex = _slides.indexOf(_activeSlide) + 1;
		if (nextIndex >= _slides.size()) return false;
		
		setActiveSlide(_slides.get(nextIndex));
		return true;
	}
	
	/**
	 * Sets the slide previous to the active slide in the list of the slides as 
	 * active.
	 * 
	 * @see {@link #setActiveSlide(Slide)}
	 * @return True whether the active slide changed
	 */
	public boolean previousSlide() {
		int previousIndex = _slides.indexOf(_activeSlide) - 1;
		if (previousIndex < 0) return false;
		
		setActiveSlide(_slides.get(previousIndex));
		return true;
	}
	
	/**
	 * Sets the fist in the list of the slides as active.
	 * 
	 * @see {@link #setActiveSlide(Slide)}
	 * @return True whether the active slide changed
	 */
	public boolean firstSlide() {
		if (_slides.size() > 0) {
			setActiveSlide(_slides.getFirst());
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Sets the last in the list of the slides as active.
	 * 
	 * @see {@link #setActiveSlide(Slide)}
	 * @return True whether the active slide changed
	 */
	public boolean lastSlide() {
		if (_slides.size() > 0) {
			setActiveSlide(_slides.getLast());
			return true;
		} else {
			return false;
		}
	}
	
	public Slide getActiveSlide() {
		return _activeSlide;
	}
	
	public List<Slide> getSlides() {
		return Collections.unmodifiableList(_slides);
	}
	
	/**
	 * Adds a slide directly next to the active slide into the list of slides
	 * and makes it the active slide.
	 * 
	 * @see {@link #setActiveSlide(Slide)}
	 * @param slide Slide to add
	 */
	public void addSlide(Slide slide) {
		
		if (_activeSlide == null) _slides.add(slide);
		else _slides.add(_slides.indexOf(_activeSlide) + 1, slide);
		
		Slide oldValue = _activeSlide;
		_activeSlide = slide;
		
		// Reset source to entire slide:
		_source = _activeSlide.getSize();
		
		_propertyChangeSupport.firePropertyChange("slides", null, _slides); 
		_propertyChangeSupport.firePropertyChange("activeSlide", oldValue, _activeSlide);
		_propertyChangeSupport.firePropertyChange("source", null, _source);
	}
	
	/**
	 * @return The slide next to the active slide or null, if none exists
	 */
	public Slide getNextSlide() {
		if (_slides.size() <= 0) {
			return null;
		} else {
			int nextIndex = _slides.indexOf(_activeSlide) + 1;
			if (nextIndex < _slides.size()) {
				return _slides.get(nextIndex);
			} else {
				return null;
			}
		}
	}
	
	/**
	 * Removes the active slide from the list of slides and makes its successor
	 * the active slide.
	 * 
	 * @see {@link #setActiveSlide(Slide)}
	 * @return True if removal was successful
	 */
	public boolean removeSlide() {
		if (_activeSlide == null) return false;
		Slide oldValue = _activeSlide;
		
		int index = _slides.indexOf(_activeSlide);
		_slides.remove(_activeSlide);
		
		if (_slides.isEmpty()) {
			_activeSlide = null;
		} else {
			if (index == 0) {
				_activeSlide = _slides.getFirst();
			} else {
				_activeSlide = _slides.get(index - 1);
			}
			
			// Reset source to entire slide:
			_source = _activeSlide.getSize();
		}
		
		_propertyChangeSupport.firePropertyChange("slides", null, _slides); 
		_propertyChangeSupport.firePropertyChange("activeSlide", oldValue, _activeSlide);
		_propertyChangeSupport.firePropertyChange("source", null, _source);
		
		return true;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		_propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		_propertyChangeSupport.removePropertyChangeListener(listener);
	}
	
	protected void dispose() {
		_slides.clear();
	}
}
