package de.hackermuehle.pdfpresenter.model.tools;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.apache.log4j.Logger;

import de.hackermuehle.pdfpresenter.model.Preferences;

public class Laser extends Tool {
	private PropertyChangeSupport _propertyChangeSupport = new PropertyChangeSupport(this);
	private String _color = "red";
  private String DEFAULT_COLOR = "red";
	// XXX Preferences:
	public static final String PREFERENCE_SIZE = "laser.color";

	private Preferences _preferences;

	public Laser(String color) {
		_color = color;
	}

	public void setColor(String color) {
    _color = color;
		_propertyChangeSupport.firePropertyChange("color", null, _color);
	}

    public void setSize(double size) {
    }
	public String getColor() {
		return _color;
	}

    public double getSize() {
        return 10;
    }

	public void addPropertyChangeListener(PropertyChangeListener l) {
		_propertyChangeSupport.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		_propertyChangeSupport.removePropertyChangeListener(l);
	} 

	// XXX Preferences:
	public Laser(Preferences preferences) {
		_preferences = preferences;

		try {
			_color = preferences.getPreference(PREFERENCE_SIZE);
		} catch (IllegalArgumentException e) {
			Logger.getLogger(this.getClass()).warn("Load laser color from preferences failed. " + e.getLocalizedMessage());
			_color = DEFAULT_COLOR;
		} catch (NullPointerException e) {
			Logger.getLogger(this.getClass()).warn("Load laser color from preferences failed. " + e.getLocalizedMessage());
			_color = DEFAULT_COLOR;
		}
	}

	public void saveSizeInPreferences(int size) {
		_preferences.setPreference(PREFERENCE_SIZE, String.valueOf(size));
	}
}
