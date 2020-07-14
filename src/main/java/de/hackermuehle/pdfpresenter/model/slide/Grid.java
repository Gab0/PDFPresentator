package de.hackermuehle.pdfpresenter.model.slide;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org.apache.log4j.Logger;

import de.hackermuehle.pdfpresenter.model.Clipping;
import de.hackermuehle.pdfpresenter.model.Preferences;

/**
 * A Grid, either horizontal or horizontal and vertical, that can be drawn for
 * any purpose (i.e. pen input on a slide is easier with a grid displayed
 * underneath).
 * 
 * Immutable.
 */
public class Grid implements Cloneable {
	
	private double _distance;
	private Type _type;

	// XXX Preferences:
	private static final String PREFERENCE_TYPE = "general.grid.type";
	private static final String PREFERENCE_DENSITY = "general.grid.density";
	private Preferences _preferences;
	public static final double DEFAULT_DENSITY = 30.0;
	public static final Type DEFAULT_TYPE = Type.BOTH;
	
	/**
	 * Creates a grid either horizontal or horizontal and vertical with a given 
	 * line distance.
	 * 
	 * @param type		Type, p.e. {@link Grid.Type#VERTICAL}
	 * @param distance	Line distance
	 */
	public Grid(Type type, double distance) {
		setType(type);
		setDistance(distance); 
	}
	
	/**
	 * Construct and load saved settings from preferences
	 */
	public Grid(Preferences preferences) {
		_preferences = preferences;
		loadPreferences();
	}
	
	public enum Type {
		NONE,
		VERTICAL,
		HORIZONTAL,
		BOTH
	}
	
	public Type getType() {
		return _type;
	}

	public double getDistance() {
		return _distance;
	}
	
	@Override
	public Object clone() {
		Object object = null;
		try {
			object = super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace(System.err);
		}
		return object;
	}

	public void paint(Graphics2D g2d, Clipping clipping, Rectangle2D anchor) {
		Rectangle2D dest = clipping.getSource();
		if (g2d.getClipBounds() != null)
			dest.add(g2d.getClipBounds());
		//dest.add(anchor); // not used in current setup.
		
		g2d.setStroke(new BasicStroke(0));
		g2d.setColor(new Color(130, 130, 130));
		
		double distance = _distance;// * clipping.getInverseTransform().getScaleX();
		
		if ((_type == Type.HORIZONTAL) || (_type == Type.BOTH)) {
			double y = 0;//dest.getY();
			while (y < dest.getY() + dest.getHeight()) {
				g2d.draw(new Line2D.Double(dest.getX(), y, (dest.getX() + dest.getWidth()), y));
				y += distance;
			}
		}
		
		if ((_type == Type.VERTICAL) || (_type == Type.BOTH)) {
			double x = 0;//dest.getX();
			while (x < dest.getX() + dest.getWidth()) {
				g2d.draw(new Line2D.Double(x, dest.getY(), x, (dest.getY() + dest.getHeight())));
				x += distance;
			}
		}
	}	
	
	
	// XXX: Remove preferences from grid:
	/**
	 * Construct and use the given settings, do <i>not</i> 
	 * set them to preferences upon creation
	 */
	public Grid(Type type, double distance, Preferences preferences) {
		_preferences = preferences;
		_type = type;
		
		if (distance <= 0) _distance = 10;
		else _distance = distance;
	}
	
	private void loadPreferences() {
		String typeName = _preferences.getPreference(PREFERENCE_TYPE);
		try {
			setType(Type.valueOf(typeName));
		} catch (IllegalArgumentException e) {
			Logger.getLogger(getClass()).warn("Load grid type from preferences " +
					"failed. Used default value BOTH (Checkered). " + e.getLocalizedMessage());
			setType(DEFAULT_TYPE);
		} catch (NullPointerException e) {
			Logger.getLogger(getClass()).warn("Load grid type from preferences " +
					"failed. Used default value BOTH (Checkered). " + e.getLocalizedMessage());
			setType(DEFAULT_TYPE);
		}
		
		try {
			String densityString = _preferences.getPreference(PREFERENCE_DENSITY);
		
			double density = Double.valueOf(densityString);
			setDistance(density);
		} catch (NumberFormatException e) {
			Logger.getLogger(getClass()).warn("Load grid density from preferences " +
					"failed. Used default value 30.0 (Checkered). " + e.getLocalizedMessage());
			// TODO dummy. change to proper value
			setDistance(DEFAULT_DENSITY);
		} catch (NullPointerException e) {
			Logger.getLogger(getClass()).warn("Load grid density from preferences " +
					"failed. Used default value 30.0 (Checkered). " + e.getLocalizedMessage());
			setDistance(DEFAULT_DENSITY);
		}
	}
	
	// XXX:
	private void setType(Type type) {
		_type = type;
		
		// XXX: Please remove the preferences from the getters / setters!
		if (_preferences != null) _preferences.setPreference(PREFERENCE_TYPE, type.name());
	}
	
	// XXX:
	private void setDistance(double distance) {
		_distance = distance;
		
		// XXX: Please remove the preferences from the getters / setters!
		if (_preferences != null) _preferences.setPreference(PREFERENCE_DENSITY, String.valueOf(distance));
	}
	
	// FIXME: Please remove the preferences from any object!
	// Otherwise equals() will not always return expected results.
	// Possibly other classes are concerned, too.
	@Override
	public boolean equals(Object obj) {
		if (this == obj ) return true;
		if (!(obj instanceof Grid)) return false;
		Grid grid = (Grid)obj;
		return (_type == grid._type) && (_distance == grid._distance);
	}
}
