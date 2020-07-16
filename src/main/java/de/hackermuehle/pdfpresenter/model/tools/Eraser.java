package de.hackermuehle.pdfpresenter.model.tools;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.apache.log4j.Logger;

import de.hackermuehle.pdfpresenter.model.Preferences;

public class Eraser extends Tool {
	private PropertyChangeSupport _propertyChangeSupport = new PropertyChangeSupport(this);
	private double _size;

	// XXX Preferences:
	public static final String PREFERENCE_SIZE = "eraser.size";
	private static final int DEFAULT_SIZE = 20;
	private Preferences _preferences;

	public Eraser(double size) {
		_size = size;
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
	public Eraser(Preferences preferences) {
		_preferences = preferences;

		try {
			_size = Integer.parseInt(preferences.getPreference(PREFERENCE_SIZE));
		} catch (IllegalArgumentException e) {
			Logger.getLogger(this.getClass()).warn("Load eraser size from preferences failed. " + e.getLocalizedMessage());
			_size = DEFAULT_SIZE;
		} catch (NullPointerException e) {
			Logger.getLogger(this.getClass()).warn("Load eraser size from preferences failed. " + e.getLocalizedMessage());
			_size = DEFAULT_SIZE;
		}
	}
	
	public void saveSizeInPreferences(int size) {
		_preferences.setPreference(PREFERENCE_SIZE, String.valueOf(size));
	}
}
