package de.hackermuehle.pdfpresenter.model.tools;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.apache.log4j.Logger;

import de.hackermuehle.pdfpresenter.model.Preferences;
import de.hackermuehle.pdfpresenter.model.State;

public class TextTool extends Tool {
	private PropertyChangeSupport _propertyChangeSupport = new PropertyChangeSupport(this); 
	private Font _font;
	private Color _color;
	
	// XXX Preferences
	public static final String PREFERENCE_FONT = "text.font";
	public static final String PREFERENCE_SIZE = "text.size";
	public static final String PREFERENCE_COLOR = "text.color";
	private Preferences _preferences;
	private static final String DEFAULT_FONT = Font.SANS_SERIF;
	private static final int DEFAULT_SIZE = 24;
	private static final Color DEFAULT_COLOR = Color.BLACK;
	
	public TextTool(Color color, Font font) {
		_color = color;
		_font = font;
	}
	
	public void setFont(Font font) {
		_font = font;
		_propertyChangeSupport.firePropertyChange("font", null, _font); 
	}

	public Font getFont() {
		return _font;
	}
	
	public Color getColor() {
		return _color;
	}

	public void setColor(Color color) {
		_color = color;
		_propertyChangeSupport.firePropertyChange("color", null, _color); 
	}
	
	public void addPropertyChangeListener(PropertyChangeListener l) {
		_propertyChangeSupport.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		_propertyChangeSupport.removePropertyChangeListener(l);
	} 
	
	// XXX Preferences:
	public TextTool(State state) {
		_preferences = state.getPreferences();
		
		try {
			String font = state.getPreference(PREFERENCE_FONT);
			int size = Integer.parseInt(state.getPreference(PREFERENCE_SIZE));
			_font = new Font(font, Font.PLAIN, size);
		} catch (NumberFormatException e) {
			Logger.getLogger(this.getClass()).warn("Load text font and size from preferences failed. " + e.getLocalizedMessage());
			_font = new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_SIZE);
		} catch (NullPointerException e) {
			Logger.getLogger(this.getClass()).warn("Load text font and size from preferences failed. " + e.getLocalizedMessage());
			_font = new Font(DEFAULT_FONT, Font.PLAIN, DEFAULT_SIZE);
		}
		
		try {
			int rgb = Integer.parseInt(state.getPreference(PREFERENCE_COLOR));
			_color = new Color(rgb);
		} catch (IllegalArgumentException e) {
			Logger.getLogger(this.getClass()).warn("Load text color from preferences failed. " + e.getLocalizedMessage());
			_color = DEFAULT_COLOR;
		} catch (NullPointerException e) {
			Logger.getLogger(this.getClass()).warn("Load text color from preferences failed. " + e.getLocalizedMessage());
			_color = DEFAULT_COLOR;
		}
	}
	
	public void saveFamilyInPreferences(String family) {
		_preferences.setPreference(PREFERENCE_FONT, family);
	}
	
	public void saveSizeInPreferences(int size) {
		_preferences.setPreference(PREFERENCE_SIZE, String.valueOf(size));
	}
	
	public void saveColorInPreferences(Color color) {
		_preferences.setPreference(PREFERENCE_COLOR, String.valueOf(color.getRGB()));
	}
}
