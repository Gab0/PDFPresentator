 package de.hackermuehle.pdfpresenter.viewcontroller.stylebar;


import de.hackermuehle.pdfpresenter.model.State;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import de.hackermuehle.pdfpresenter.model.Preferences;

import de.hackermuehle.pdfpresenter.model.tools.WriterTool;

/**
 * Style palette for pen tool
 *
 * @author shuo
 *
 */
public class WriterStyleBar extends StyleBar {

    private static final long serialVersionUID = -3485696631854523018L;
    private static String PREFERENCE_COLOR_PREFIX;
    private static String PREFERENCE_SIZE_PREFIX;
    private static String PREFERENCE_PREFIX;
    private static final Color[] DEFAULT_COLORS = { Color.RED,
                                                    Color.GREEN,
                                                    Color.BLUE,
                                                    Color.BLACK,
                                                    Color.MAGENTA };
    private static final int[] DEFAULT_THICKNESSES = { 2, 5, 10, 40, 50 };

    private ColorGroupedButton _currentColorButton;
    private ThicknessGroupedButton _currentThicknessButton;

    public WriterTool _tool;

    public WriterStyleBar(State state, Preferences preferences) {
        super(preferences);
        updatePreferencePaths("penstylepalette");
        _tool = state.getPen();
        registerButtons();
        addButtonsToPalette();

	}
    private int loadPreferenceInt(String path) {
        return Integer.parseInt(getPreferences().getPreference(PREFERENCE_PREFIX + path));
    }

    public void updatePreferencePaths(String PREFIX) {
        PREFERENCE_PREFIX = PREFIX;
        PREFERENCE_COLOR_PREFIX = PREFIX + ".color";
        PREFERENCE_SIZE_PREFIX = PREFIX + ".size";
    }

	public void registerButtons() {
		LinkedList<LinkedList<GroupedButton>> buttonGroups = getButtonGroups();

		// Most frequently changing first
		LinkedList<GroupedButton> colorButtons = new LinkedList<GroupedButton>();
		// Load colors
		Color[] colors;
    String err = "Load PenStylePalette color from preferences failed. ";
		try {
        int rgb1 = loadPreferenceInt(".color0");
        int rgb2 = loadPreferenceInt(".color1");
        int rgb3 = loadPreferenceInt(".color2");
        int rgb4 = loadPreferenceInt(".color3");
        int rgb5 = loadPreferenceInt(".color4");

        colors = new Color[] { new Color(rgb1), new Color(rgb2), new Color(rgb3),
                               new Color(rgb4), new Color(rgb5) };

		} catch (IllegalArgumentException e) {
			Logger.getLogger(this.getClass()).warn(err + e.getLocalizedMessage());
			colors = DEFAULT_COLORS;
			initColorPreferences();
		} catch (NullPointerException e) {
			Logger.getLogger(this.getClass()).warn(err + e.getLocalizedMessage());
			colors = DEFAULT_COLORS;
			initColorPreferences();
		}

		ColorButtonActionListener colorButtonActionListener = new ColorButtonActionListener();
		ColorChangeListener colorChangeListener = new ColorChangeListener();

		Color penColor = _tool.getColor();
		for (int i = 0; i < colors.length; i++) {
			Color color = colors[i];
			ColorGroupedButton colorButton = new ColorGroupedButton(i, color, 
					getButtonHeight(), getAlignment());
			colorButton.addActionListener(colorButtonActionListener);
			colorButton.addPropertyChangeListener(colorChangeListener);
			if (_currentColorButton == null && color.equals(penColor)) {
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
        int size1 = loadPreferenceInt(".size0");
        int size2 = loadPreferenceInt(".size1");
        int size3 = loadPreferenceInt(".size2");
        int size4 = loadPreferenceInt(".size3");
        int size5 = loadPreferenceInt(".size4");

			thicknesses = new int[] { size1, size2, size3, size4, size5 };
		} catch (NumberFormatException e) {
			Logger.getLogger(this.getClass()).warn("Load pen size from preferences failed. " + e.getLocalizedMessage());
			thicknesses = DEFAULT_THICKNESSES;
			initSizePreferences();
		} catch (NullPointerException e) {
			Logger.getLogger(this.getClass()).warn("Load pen size from preferences failed. " + e.getLocalizedMessage());
			thicknesses = DEFAULT_THICKNESSES;
			initSizePreferences();
		}

		ThicknessButtonActionListener thicknessButtonActionListener = new ThicknessButtonActionListener();
		ThicknessChangeListener thicknessChangeListener = new ThicknessChangeListener();

		int penSize = (int) _tool.getSize();
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
			getPreferences().setPreference("penstylepalette.color" + String.valueOf(i), 
					String.valueOf(DEFAULT_COLORS[i].getRGB()));
		}
	}

	private void initSizePreferences() {
		for (int i = 0; i < DEFAULT_THICKNESSES.length; i++) {
			getPreferences().setPreference("penstylepalette.size" + String.valueOf(i), 
					String.valueOf(DEFAULT_THICKNESSES[i]));
		}
	}

	class ColorButtonActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			Object button = e.getSource();
			if (button instanceof ColorGroupedButton) {
				ColorGroupedButton colorButton = (ColorGroupedButton) button;

				if (_currentColorButton != null)
					_currentColorButton._isChosen = false;
				colorButton._isChosen = true;
				_currentColorButton = colorButton;
        Color color = colorButton.getColor();
				_tool.setColor(color);
				_tool.saveColorInPreferences(color);
				saveButtonColor(colorButton.getId(), color);

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
        Color color = colorButton.getColor();
				_tool.setColor(color);
				_tool.saveColorInPreferences(color);
				saveButtonColor(colorButton.getId(), color);

				repaint();
			}
		}
	}
	
	private void saveButtonColor(int id, Color color) {
		getPreferences().setPreference(PREFERENCE_COLOR_PREFIX + String.valueOf(id), 
				String.valueOf(color.getRGB()));
	}
	
	
	class ThicknessButtonActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			Object button = e.getSource();
			if (button instanceof ThicknessGroupedButton) {
				ThicknessGroupedButton thicknessButton = (ThicknessGroupedButton) button;

				if (_currentThicknessButton != null)
					_currentThicknessButton._isChosen = false;
				thicknessButton._isChosen = true;
				_currentThicknessButton = thicknessButton;
				_tool.setSize(thicknessButton.getThickness());
				_tool.saveSizeInPreferences(thicknessButton.getThickness());
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
				_tool.setSize(thicknessButton.getThickness());
				_tool.saveSizeInPreferences(thicknessButton.getThickness());
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

					if (i == 0) {
						_currentColorButton = colorButton;
						colorButton.setChosen(true);
					}
				}
			}
			if (_currentColorButton != null) {
          Color color = _currentColorButton.getColor();
				_tool.setColor(color);
				_tool.saveColorInPreferences(color);
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
					
					if (i == 0) {
						_currentThicknessButton = thicknessButton;
						thicknessButton.setChosen(true);
					}
				}
			}
			if (_currentThicknessButton != null) {
				_tool.setSize(_currentThicknessButton.getThickness());
				_tool.saveSizeInPreferences(_currentThicknessButton.getThickness());
			}
			
		}
	}
}

