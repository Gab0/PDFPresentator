package de.hackermuehle.pdfpresenter.model.tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.apache.log4j.Logger;

import de.hackermuehle.pdfpresenter.model.Preferences;

public class Pen extends Tool {
	private PropertyChangeSupport _propertyChangeSupport = new PropertyChangeSupport(this);
	private Color _color;
	private double _size;
	
	// XXX: Preferences
	public static final String PREFERENCE_COLOR = "pen.color";
	public static final String PREFERENCE_SIZE = "pen.size";
	private static final Color DEFAULT_COLOR = Color.RED;
	private static final float DEFAULT_SIZE = 10f; 
	private Preferences _preferences;
	
	public Pen() {
		_color = DEFAULT_COLOR;
		_size = DEFAULT_SIZE;
	}
	
	public Pen(Color color, double size) {
		_color = color;
		_size = size;
	}
	
	public boolean isTranslucent() {
		return false;
	}
	
	public void setColor(Color color) {
		_color = color;
		_propertyChangeSupport.firePropertyChange("color", null, _color); 		
	}
	
	public Color getColor() {
		return _color;
	}
	
	public int getCap() {
		return BasicStroke.CAP_ROUND;
	}

	public int getJoin() {
		return BasicStroke.JOIN_ROUND;
	}
	
	public void setSize(double size) {
		_size = size;
		_propertyChangeSupport.firePropertyChange("size", null, _size); 
	}
	
	public double getSize() {
		return _size;
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		_propertyChangeSupport.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		_propertyChangeSupport.removePropertyChangeListener(l);
	} 
	
	// XXX Preferences:
	protected Preferences getPreferences() {
		return _preferences;
	}
	
	protected void setPreferences(Preferences preferences) {
		_preferences = preferences;
	}
	
	public void saveColorInPreferences(Color color) {
		_preferences.setPreference(PREFERENCE_COLOR, String.valueOf(color.getRGB()));
	}

	public void saveSizeInPreferences(double size) {
		_preferences.setPreference(PREFERENCE_SIZE, String.valueOf(size));
	}
	
	public Pen(Preferences preferences) {
		_preferences = preferences;
		
		try {
			int rgb = Integer.parseInt(preferences.getPreference(PREFERENCE_COLOR));
			_color = new Color(rgb);
		} catch (IllegalArgumentException e) {
			Logger.getLogger(this.getClass()).warn("Load pen color from preferences failed. " + e.getLocalizedMessage());
			_color = DEFAULT_COLOR;
		} catch (NullPointerException e) {
			Logger.getLogger(this.getClass()).warn("Load pen color from preferences failed. " + e.getLocalizedMessage());
			_color = DEFAULT_COLOR;
		}
		
		try {
			_size = Double.parseDouble(preferences.getPreference(PREFERENCE_SIZE));
		} catch (NumberFormatException e) {
			Logger.getLogger(this.getClass()).warn("Load pen size from preferences failed. " + e.getLocalizedMessage());
			_size = DEFAULT_SIZE;
		} catch (NullPointerException e) {
			Logger.getLogger(this.getClass()).warn("Load pen size from preferences failed. " + e.getLocalizedMessage());
			_size = DEFAULT_SIZE;
		}
	}
}
