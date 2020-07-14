package de.hackermuehle.pdfpresenter.viewcontroller.stylebar;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import de.hackermuehle.pdfpresenter.model.Preferences;
import de.hackermuehle.pdfpresenter.model.tools.Marker;


/**
 * Style palette for marker tool
 * 
 * @author shuo
 *
 */
public class MarkerStyleBar extends StyleBar {
	
	private static final long serialVersionUID = 7453775574628277609L;
	private static final String PREFERENCE_COLOR_PREFIX = "markerstylepalette.color";
	private static final String PREFERENCE_SIZE_PREFIX = "markerstylepalette.size";
	
	private static final Color[] DEFAULT_COLORS = { new Color(255, 128, 0), new Color(102, 255, 255), 
		new Color(128, 255, 0), new Color(255, 255, 102), new Color(255, 102, 255) };
	private static final int[] DEFAULT_THICKNESSES = { 10, 20, 30, 40, 50 };
	
	private static final Color[][] _colors = {
		{ new Color(255, 128, 0), new Color(255, 102, 102), new Color(255, 204, 102),  new Color(255, 255, 255) },
		{ new Color(128, 255, 0), new Color(255, 255, 102), new Color(204, 255, 102),  new Color(204, 204, 204) },
		{ new Color(0, 255, 128), new Color(102, 255, 102), new Color(102, 255, 204),  new Color(153, 153, 153) }, 
		{ new Color(0, 128, 255), new Color(102, 255, 255), new Color(102, 204, 255),  new Color(127, 127, 127) }, 
		{ new Color(128, 0, 255), new Color(102, 102, 255), new Color(204, 102, 255),  new Color(102, 102, 102) }, 
		{ new Color(255, 0, 128), new Color(255, 102, 255), new Color(255, 111, 207),  new Color(51, 51, 51) } 
	};
	
	private ColorGroupedButton _currentColorButton;
	private ThicknessGroupedButton _currentThicknessButton;
	
	private Marker _marker;	
	
	public MarkerStyleBar(Marker marker, Preferences preferences) {
		super(preferences);
		_marker = marker;
		
		registerButtons();
		addButtonsToPalette();
	}
	

	public void registerButtons() {
		LinkedList<LinkedList<GroupedButton>> buttonGroups = getButtonGroups();
		
		// Most frequently changing first
		LinkedList<GroupedButton> colorButtons = new LinkedList<GroupedButton>();
		// Load colors
		Color[] colors;
		try {
			int rgb1 = Integer.parseInt(getPreferences().getPreference(PREFERENCE_COLOR_PREFIX + "0"));
			int rgb2 = Integer.parseInt(getPreferences().getPreference(PREFERENCE_COLOR_PREFIX + "1"));
			int rgb3 = Integer.parseInt(getPreferences().getPreference(PREFERENCE_COLOR_PREFIX + "2"));
			int rgb4 = Integer.parseInt(getPreferences().getPreference(PREFERENCE_COLOR_PREFIX + "3"));
			int rgb5 = Integer.parseInt(getPreferences().getPreference(PREFERENCE_COLOR_PREFIX + "4"));
			
			colors = new Color[] { new Color(rgb1), new Color(rgb2), new Color(rgb3), 
					new Color(rgb4), new Color(rgb5) };
		} catch (IllegalArgumentException e) {
			Logger.getLogger(this.getClass()).warn("Load PenStylePalette color from preferences failed. " + e.getLocalizedMessage());
			colors = DEFAULT_COLORS;
			initColorPreferences();
		} catch (NullPointerException e) {
			Logger.getLogger(this.getClass()).warn("Load PenStylePalette color from preferences failed. " + e.getLocalizedMessage());
			colors = DEFAULT_COLORS;
			initColorPreferences();
		}
				
		ColorButtonActionListener colorButtonActionListener = new ColorButtonActionListener();
		ColorChangeListener colorChangeListener = new ColorChangeListener();
				
		Color markerColor = _marker.getColor();
		markerColor = new Color(markerColor.getRed(), markerColor.getGreen(), markerColor.getBlue());
		for (int i = 0; i < colors.length; i++) {
			Color color = colors[i];
			ColorGroupedButton colorButton = new ColorGroupedButton(i, color, getButtonHeight(), 
					_colors, getAlignment());
			colorButton.addActionListener(colorButtonActionListener);
			colorButton.addPropertyChangeListener(colorChangeListener);
			// TODO V2: change to save index of chosen button
			if (_currentColorButton == null && color.equals(markerColor)) {
				colorButton._isChosen = true;
				_currentColorButton = colorButton;
			}
			colorButtons.add(colorButton);
		}
		buttonGroups.add(colorButtons);
		
		// Less frequently changing
		LinkedList<GroupedButton> thicknessButtons = new LinkedList<GroupedButton>();
		int[] thicknesses;
		try {
			int size1 = Integer.parseInt(getPreferences().getPreference("markerstylepalette.size0"));
			int size2 = Integer.parseInt(getPreferences().getPreference("markerstylepalette.size1"));
			int size3 = Integer.parseInt(getPreferences().getPreference("markerstylepalette.size2"));
			int size4 = Integer.parseInt(getPreferences().getPreference("markerstylepalette.size3"));
			int size5 = Integer.parseInt(getPreferences().getPreference("markerstylepalette.size4"));

			thicknesses = new int[] { size1, size2, size3, size4, size5 };
		} catch (NumberFormatException e) {
			Logger.getLogger(this.getClass()).warn("Load pen size from preferences failed. " + 
					e.getLocalizedMessage());
			thicknesses = DEFAULT_THICKNESSES;
			initSizePreferences();
		} catch (NullPointerException e) {
			Logger.getLogger(this.getClass()).warn("Load pen size from preferences failed. " + 
					e.getLocalizedMessage());
			thicknesses = DEFAULT_THICKNESSES;
			initSizePreferences();
		}
		
		ThicknessButtonActionListener thicknessButtonActionListener = new ThicknessButtonActionListener();
		ThicknessChangeListener thicknessChangeListener = new ThicknessChangeListener();
		
		int penSize = (int) _marker.getSize();
		for (int i = 0; i < thicknesses.length; i++) {
			int size = thicknesses[i];
			ThicknessGroupedButton thicknessButton = new ThicknessGroupedButton(i, size, 
					getButtonHeight(), getAlignment());
			thicknessButton.addActionListener(thicknessButtonActionListener);
			thicknessButton.addPropertyChangeListener(thicknessChangeListener);
			if (_currentThicknessButton == null && size == penSize) {
				thicknessButton._isChosen = true;
				_currentThicknessButton = thicknessButton;
			}
			thicknessButtons.add(thicknessButton);
		}
		
		buttonGroups.add(thicknessButtons);
	}
	
	private void initColorPreferences() {
		for (int i = 0; i < DEFAULT_COLORS.length; i++) {
			getPreferences().setPreference("markerstylepalette.color" + String.valueOf(i), 
					String.valueOf(DEFAULT_COLORS[i].getRGB()));
		}
	}


	private void initSizePreferences() {
		for (int i = 0; i < DEFAULT_THICKNESSES.length; i++) {
			getPreferences().setPreference("markerstylepalette.size" + String.valueOf(i), 
					String.valueOf(DEFAULT_THICKNESSES[i]));
		}
	}
	
	
	class ColorButtonActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			Object button = e.getSource();
			if (button instanceof ColorGroupedButton) {
				ColorGroupedButton colorButton = (ColorGroupedButton) button;
				
				Color color = colorButton.getColor();
				
				if (_currentColorButton != null)
					_currentColorButton._isChosen = false;
				colorButton._isChosen = true;
				_currentColorButton = colorButton;
				_marker.setColor(color);
				_marker.saveColorInPreferences(color);
				saveButtonColor(colorButton.getId(), colorButton.getColor());

				repaint();
			}
		}
	}
	
	
	class ColorChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName() != "color") return;
			
			Object button = evt.getSource();
			if (button instanceof ColorGroupedButton) {
				ColorGroupedButton colorButton = (ColorGroupedButton) button;
				_marker.setColor(colorButton.getColor());
				_marker.saveColorInPreferences(colorButton.getColor());
				saveButtonColor(colorButton.getId(), colorButton.getColor());

				repaint();
			}
		}
	}
	
	
	private void saveButtonColor(int id, Color color) {
		getPreferences().setPreference(PREFERENCE_COLOR_PREFIX + String.valueOf(id), String.valueOf(color.getRGB()));
	}
	
	
	
	class ThicknessButtonActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			Object button = e.getSource();
			if (button instanceof ThicknessGroupedButton) {
				ThicknessGroupedButton thicknessButton = (ThicknessGroupedButton) button;
				_marker.setSize(thicknessButton.getThickness());
				
				if (_currentThicknessButton != null)
					_currentThicknessButton._isChosen = false;
				thicknessButton._isChosen = true;
				_currentThicknessButton = thicknessButton;
				_marker.setSize(thicknessButton.getThickness());
				_marker.saveSizeInPreferences(thicknessButton.getThickness());
				saveButtonSize(thicknessButton.getId(), thicknessButton.getThickness());
				
				repaint();
			}
		}
	}
	
	
	class ThicknessChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName() != "thickness") return;
			
			Object button = evt.getSource();
			if (button instanceof ThicknessGroupedButton) {
				ThicknessGroupedButton thicknessButton = (ThicknessGroupedButton) button;
				_marker.setSize(thicknessButton.getThickness());
				_marker.saveSizeInPreferences(thicknessButton.getThickness());
				saveButtonSize(thicknessButton.getId(), thicknessButton.getThickness());
				
				repaint();
			}
		}
	}
	
	private void saveButtonSize(int id, int size) {
		getPreferences().setPreference(PREFERENCE_SIZE_PREFIX + String.valueOf(id), String.valueOf(size));
	}
	
	@Override
	public void resetToFactorySettings() {
		LinkedList<LinkedList<GroupedButton>> buttonGroups = getButtonGroups();
		
		LinkedList<GroupedButton> buttons = buttonGroups.get(0);
		if (DEFAULT_COLORS.length == buttons.size()) {
			if (_currentColorButton != null)
				_currentColorButton.setChosen(false);
			for (int i = 0; i < DEFAULT_COLORS.length; i++) {
				GroupedButton button = buttons.get(i);
				if (button instanceof ColorGroupedButton) {
					ColorGroupedButton colorButton = (ColorGroupedButton) button;
					colorButton.setColor(DEFAULT_COLORS[i]);
					
					if (i == 3) {
						_currentColorButton = colorButton;
						colorButton.setChosen(true);
					}
				}
			}
			if (_currentColorButton != null) {
				_marker.setColor(_currentColorButton.getColor());
				_marker.saveColorInPreferences(_currentColorButton.getColor());
			}
		}
		
		
		buttons = buttonGroups.get(1);
		if (DEFAULT_THICKNESSES.length == buttons.size()) {
			if (_currentThicknessButton != null)
				_currentThicknessButton.setChosen(false);
			for (int i = 0; i < DEFAULT_COLORS.length; i++) {
				GroupedButton button = buttons.get(i);
				if (button instanceof ThicknessGroupedButton) {
					ThicknessGroupedButton thicknessButton = (ThicknessGroupedButton) button;
					thicknessButton.setThickness(DEFAULT_THICKNESSES[i]);
					
					if (i == 1) {
						_currentThicknessButton = thicknessButton;
						thicknessButton.setChosen(true);
					}
				}
			}
			if (_currentColorButton != null) {
				_marker.setColor(_currentColorButton.getColor());
				_marker.saveColorInPreferences(_currentColorButton.getColor());
			}
		}
	}


}
