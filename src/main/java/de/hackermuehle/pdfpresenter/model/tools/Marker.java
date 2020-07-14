package de.hackermuehle.pdfpresenter.model.tools;

import java.awt.BasicStroke;
import java.awt.Color;

import org.apache.log4j.Logger;

import de.hackermuehle.pdfpresenter.model.Preferences;

public class Marker extends Pen {
	
	// XXX Preferences:
	public static final String PREFERENCE_COLOR = "marker.color";
	public static final String PREFERENCE_SIZE = "marker.size";
	private static final Color DEFAULT_COLOR = new Color(255, 255, 102);
	private static final float DEFAULT_SIZE = 30f; 
	
	public Marker(Color color, double size) {
		super(color, size);
		setColor(color);
	}
	
	@Override
	public void setColor(Color color) {
		// Add transparency
		super.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
	}
	
	public int getCap() {
		return BasicStroke.CAP_SQUARE;
	}

	public int getJoin() {
		return BasicStroke.JOIN_ROUND;
	}
	
	public boolean isTranslucent() {
		return true;
	}
	
	// XXX Preferences:
	public Marker(Preferences preferences) {
		setPreferences(preferences);
		
		try {
			int rgb = Integer.parseInt(preferences.getPreference(PREFERENCE_COLOR));
			Color color = new Color(rgb);
			setColor(color);
			saveColorInPreferences(color);
		} catch (IllegalArgumentException e) {
			Logger.getLogger(this.getClass()).warn("Load marker color from preferences failed. " + e.getLocalizedMessage());
			setColor(DEFAULT_COLOR);
			saveColorInPreferences(DEFAULT_COLOR);
		} catch (NullPointerException e) {
			Logger.getLogger(this.getClass()).warn("Load marker color from preferences failed. " + e.getLocalizedMessage());
			setColor(DEFAULT_COLOR);
			saveColorInPreferences(DEFAULT_COLOR);
		}
		
		try {
			double size = Double.parseDouble(preferences.getPreference(PREFERENCE_SIZE));
			setSize(size);
			saveSizeInPreferences(size);
		} catch (NumberFormatException e) {
			Logger.getLogger(this.getClass()).warn("Load marker size from preferences failed. " + e.getLocalizedMessage());
			setSize(DEFAULT_SIZE);
			saveSizeInPreferences(DEFAULT_SIZE);
		} catch (NullPointerException e) {
			Logger.getLogger(this.getClass()).warn("Load marker size from preferences failed. " + e.getLocalizedMessage());
			setSize(DEFAULT_SIZE);
			saveSizeInPreferences(DEFAULT_SIZE);
		}	
	}

	public void saveColorInPreferences(Color color) {
		getPreferences().setPreference(PREFERENCE_COLOR, String.valueOf(color.getRGB()));
	}
	
	public void saveSizeInPreferences(double size) {
		getPreferences().setPreference(PREFERENCE_SIZE, String.valueOf(size));
	}
}
