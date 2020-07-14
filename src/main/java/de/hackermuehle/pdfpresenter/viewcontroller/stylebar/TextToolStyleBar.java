package de.hackermuehle.pdfpresenter.viewcontroller.stylebar;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import de.hackermuehle.pdfpresenter.model.State;
import de.hackermuehle.pdfpresenter.model.tools.TextTool;

public class TextToolStyleBar extends StyleBar {
	
	private static final long serialVersionUID = -737751179118689120L;
	private static final String PREFERENCE_FONT_PREFIX = "stylepalette.text.font";
	private static final String PREFERENCE_SIZE_PREFIX = "stylepalette.text.size";
	private static final String PREFERENCE_COLOR_PREFIX = "stylepalette.text.color";
	
	private static final String[] DEFAULT_FONTS = { Font.SANS_SERIF, Font.SERIF, Font.MONOSPACED };
	private static final int[] DEFAULT_SIZES = 	{ 12, 24, 36 };
	private static final Color[] DEFAULT_COLORS = { Color.RED, Color.BLUE, Color.BLACK }; 

	private FontFamilyGroupedButton _currentFamilyButton;
	private ThicknessGroupedButton _currentSizeButton;
	private ColorGroupedButton _currentColorButton;
	
	private TextTool _textTool;
	private State _controller;
		
	public TextToolStyleBar(TextTool textTool, State controller) {
		super(controller.getPreferences());
		_textTool = textTool;
		
		_controller = controller;
		registerButtons();
		addButtonsToPalette();
	}
	
	
	public void registerButtons() {
		LinkedList<LinkedList<GroupedButton>> buttonGroups = getButtonGroups();
		
		// Font families
		LinkedList<GroupedButton> fontFamilyButtons = new LinkedList<GroupedButton>();

		String[] fontFamilies;
		try {
			String font0 = getPreferences().getPreference(PREFERENCE_FONT_PREFIX + "0");
			String font1 = getPreferences().getPreference(PREFERENCE_FONT_PREFIX + "1");
			String font2 = getPreferences().getPreference(PREFERENCE_FONT_PREFIX + "2");

			if (font0 == null || font1 == null || font2 == null) {
				fontFamilies = DEFAULT_FONTS;
				initFontPreferences();
			}
			else
				fontFamilies = new String[] { font0, font1, font2 };
		} catch (IllegalArgumentException e) {
			Logger.getLogger(this.getClass()).warn("Load TextStylePalette font from preferences failed. "
					+ e.getLocalizedMessage());
			fontFamilies = DEFAULT_FONTS;
			initFontPreferences();
		} catch (NullPointerException e) {
			Logger.getLogger(this.getClass()).warn("Load PenStylePalette color from preferences failed. "
					+ e.getLocalizedMessage());
			fontFamilies = DEFAULT_FONTS;
			initFontPreferences();
		}
		
		FontFamilyActionListener fontFamilyActionListener = new FontFamilyActionListener();
		FontFamilyChangeListener fontFamilyChangeListener = new FontFamilyChangeListener();

		String textFont = _textTool.getFont().getFamily();
		for (int i = 0; i < fontFamilies.length; i++) {
			String font = fontFamilies[i];
			FontFamilyGroupedButton familyButton = new FontFamilyGroupedButton(
					i, font, getButtonHeight(), getAlignment(), _controller);
			familyButton.addActionListener(fontFamilyActionListener);
			familyButton.addPropertyChangeListener(fontFamilyChangeListener);
			// TODO V2: Change to save index of chosen button
			if (_currentFamilyButton == null && font.equals(textFont)) {
				familyButton._isChosen = true;
				_currentFamilyButton = familyButton;
			}
			fontFamilyButtons.add(familyButton);
		}
		
		buttonGroups.add(fontFamilyButtons);
		
		
		// Colors
		LinkedList<GroupedButton> colorButtons = new LinkedList<GroupedButton>();
		// Load colors
		Color[] colors;
		try {
			int rgb1 = Integer.parseInt(getPreferences().getPreference(PREFERENCE_COLOR_PREFIX + "0"));
			int rgb2 = Integer.parseInt(getPreferences().getPreference(PREFERENCE_COLOR_PREFIX + "1"));
			int rgb3 = Integer.parseInt(getPreferences().getPreference(PREFERENCE_COLOR_PREFIX + "2"));
			
			colors = new Color[] { new Color(rgb1), new Color(rgb2), new Color(rgb3) };
		} catch (IllegalArgumentException e) {
			Logger.getLogger(this.getClass()).warn("Load PenStylePalette color from preferences failed. "
					+ e.getLocalizedMessage());
			colors = DEFAULT_COLORS;
			initColorPreferences();
		} catch (NullPointerException e) {
			Logger.getLogger(this.getClass()).warn("Load PenStylePalette color from preferences failed. "
					+ e.getLocalizedMessage());
			colors = DEFAULT_COLORS;
			initColorPreferences();
		}
		
		ColorButtonActionListener colorButtonActionListener = new ColorButtonActionListener();
		ColorChangeListener colorChangeListener = new ColorChangeListener();
		
		Color textColor = _textTool.getColor();
		for (int i = 0; i < colors.length; i++) {
			Color color = colors[i];
			ColorGroupedButton colorButton = new ColorGroupedButton(i, color, 
					getButtonHeight(), getAlignment());
			colorButton.addActionListener(colorButtonActionListener);
			colorButton.addPropertyChangeListener(colorChangeListener);
			if (_currentColorButton == null && color.equals(textColor)) {
				colorButton._isChosen = true;
				_currentColorButton = colorButton;
			}
			colorButtons.add(colorButton);
		}
		buttonGroups.add(colorButtons);
		
		
		// Font size
		LinkedList<GroupedButton> thicknessButtons = new LinkedList<GroupedButton>();

		int[] sizes;
		try {
			int size0 = Integer.parseInt(getPreferences().getPreference(PREFERENCE_SIZE_PREFIX + "0"));
			int size1 = Integer.parseInt(getPreferences().getPreference(PREFERENCE_SIZE_PREFIX + "1"));
			int size2 = Integer.parseInt(getPreferences().getPreference(PREFERENCE_SIZE_PREFIX + "2"));

			sizes = new int[] { size0, size1, size2 };
		} catch (NumberFormatException e) {
			Logger.getLogger(this.getClass()).warn("Load pen size from preferences failed. " 
					+ e.getLocalizedMessage());
			sizes = DEFAULT_SIZES;
			initSizePreferences();
		} catch (NullPointerException e) {
			Logger.getLogger(this.getClass()).warn("Load pen size from preferences failed. " 
					+ e.getLocalizedMessage());
			sizes = DEFAULT_SIZES;
			initSizePreferences();
		}
		
		FontSizeActionListener fontSizeActionListener = new FontSizeActionListener();
		FontSizeChangeListener fontSizeChangeListener = new FontSizeChangeListener();
		
		int textSize = (int) _textTool.getFont().getSize();
		for (int i = 0; i < sizes.length; i++) {
			int size = sizes[i];
			ThicknessGroupedButton sizeButton = new ThicknessGroupedButton(
					i, size, getButtonHeight(), getAlignment());
			sizeButton.addActionListener(fontSizeActionListener);
			sizeButton.addPropertyChangeListener(fontSizeChangeListener);
			if (_currentSizeButton == null && size == textSize) {
				sizeButton._isChosen = true;
				_currentSizeButton = sizeButton;
			}
			thicknessButtons.add(sizeButton);
		}
		
		buttonGroups.add(thicknessButtons);		
		
	}

	
	private void initFontPreferences() {
		for (int i = 0; i < DEFAULT_FONTS.length; i++) {
			getPreferences().setPreference(PREFERENCE_FONT_PREFIX + String.valueOf(i), 
					DEFAULT_FONTS[i]);
		}
	}


	private void initSizePreferences() {
		for (int i = 0; i < DEFAULT_SIZES.length; i++) {
			getPreferences().setPreference(PREFERENCE_SIZE_PREFIX + String.valueOf(i), 
					String.valueOf(DEFAULT_SIZES[i]));
		}
	}
	
	private void initColorPreferences() {
		for (int i = 0; i < DEFAULT_COLORS.length; i++) {
			getPreferences().setPreference(PREFERENCE_COLOR_PREFIX + String.valueOf(i), 
					String.valueOf(DEFAULT_COLORS[i].getRGB()));
		}
	}
	
	class FontFamilyActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			Object button = e.getSource();
			if (button instanceof FontFamilyGroupedButton) {
				FontFamilyGroupedButton familyButton = (FontFamilyGroupedButton) button;

				Font newFont = new Font(familyButton.getFontFamily(), Font.PLAIN, _textTool.getFont().getSize());
				_textTool.setFont(newFont);
				_textTool.saveFamilyInPreferences(newFont.getFamily());
				saveButtonFont(familyButton.getId(), newFont.getFamily());
				
				if (_currentFamilyButton != null)
					_currentFamilyButton._isChosen = false;
				familyButton._isChosen = true;
				_currentFamilyButton = familyButton;
				
				
				repaint();
			}
		}
	}
	
	
	private class FontFamilyChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (!evt.getPropertyName().equals("fontFamily")) return;
			
			Object source = evt.getSource();
			if (source instanceof FontFamilyGroupedButton) {
				FontFamilyGroupedButton fontButton = (FontFamilyGroupedButton) source;
				
				Font oldFont = _textTool.getFont();
				Font newFont = new Font(fontButton.getFontFamily(), Font.PLAIN, oldFont.getSize());
				_textTool.setFont(newFont);
				_textTool.saveFamilyInPreferences(newFont.getFamily());
				saveButtonFont(fontButton.getId(), newFont.getFamily());
				
				repaint();
			} else {
				String errorMessage = "fontFamily property change event received, " +
					"but new value isn't of type Font and thus font family not updated";
				Logger.getLogger(this.getClass()).error(errorMessage);
			}
		}
	}
	
	private void saveButtonFont(int id, String font) {
		getPreferences().setPreference(PREFERENCE_FONT_PREFIX + String.valueOf(id), 
				font);
	}
	
	
	class FontSizeActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			Object button = e.getSource();
			if (button instanceof ThicknessGroupedButton) {
				ThicknessGroupedButton sizeButton = (ThicknessGroupedButton) button;
				_textTool.setFont(new Font(_textTool.getFont().getFamily(), Font.PLAIN, sizeButton.getThickness()));
				
				if (_currentSizeButton != null)
					_currentSizeButton._isChosen = false;
				sizeButton._isChosen = true;
				_currentSizeButton = sizeButton;
				_textTool.setFont(_textTool.getFont().deriveFont((float) sizeButton.getThickness()));
				_textTool.saveSizeInPreferences(sizeButton.getThickness());
				saveButtonSize(sizeButton.getId(), sizeButton.getThickness());
				
				repaint();
			}
		}
	}
	
	
	class FontSizeChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName() != "thickness") return;
			
			Object button = evt.getSource();
			if (button instanceof ThicknessGroupedButton) {
				ThicknessGroupedButton thicknessButton = (ThicknessGroupedButton) button;
				_textTool.setFont(_textTool.getFont().deriveFont((float) thicknessButton.getThickness()));
				_textTool.saveSizeInPreferences(thicknessButton.getThickness());
				saveButtonSize(thicknessButton.getId(), thicknessButton.getThickness());
				
				repaint();
			}
		}
	}
	
	private void saveButtonSize(int id, int size) {
		getPreferences().setPreference(PREFERENCE_SIZE_PREFIX + String.valueOf(id), String.valueOf(size));
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
				_textTool.setColor(colorButton.getColor());
				_textTool.saveColorInPreferences(colorButton.getColor());
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
				_textTool.setColor(colorButton.getColor());
				_textTool.saveColorInPreferences(colorButton.getColor());
				saveButtonColor(colorButton.getId(), colorButton.getColor());
				
				repaint();
			}
		}
	}
	
	private void saveButtonColor(int id, Color color) {
		getPreferences().setPreference(PREFERENCE_COLOR_PREFIX + String.valueOf(id), 
				String.valueOf(color.getRGB()));
	}
	
	
	@Override
	public void resetToFactorySettings() {
		LinkedList<LinkedList<GroupedButton>> buttonGroups = getButtonGroups();
		
		LinkedList<GroupedButton> buttons = buttonGroups.get(0);
		if (DEFAULT_FONTS.length == buttons.size()) {
			if (_currentFamilyButton != null)
				_currentFamilyButton.setChosen(false);
			for (int i = 0; i < DEFAULT_FONTS.length; i++) {
				GroupedButton button = buttons.get(i);
				if (button instanceof FontFamilyGroupedButton) {
					FontFamilyGroupedButton fontButton = (FontFamilyGroupedButton) button;
					fontButton.setFontFamily(DEFAULT_FONTS[i]);
					
					if (i == 0) {
						_currentFamilyButton = fontButton;
						fontButton.setChosen(true);
					}
				}
			}
		}
		
		
		buttons = buttonGroups.get(1);
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
				_textTool.setColor(_currentColorButton.getColor());
				_textTool.saveColorInPreferences(_currentColorButton.getColor());
			}
		}
		
		
		buttons = buttonGroups.get(2);
		if (DEFAULT_SIZES.length == buttons.size()) {
			if (_currentSizeButton != null)
				_currentSizeButton.setChosen(false);
			for (int i = 0; i < DEFAULT_SIZES.length; i++) {
				GroupedButton button = buttons.get(i);
				if (button instanceof ThicknessGroupedButton) {
					ThicknessGroupedButton sizeButton = (ThicknessGroupedButton) button;
					sizeButton.setThickness(DEFAULT_SIZES[i]);
					
					if (i == 1) {
						_currentSizeButton = sizeButton;
						sizeButton.setChosen(true);
					}
				}
			}
		}
		
		
		if (_currentFamilyButton != null) {
			_textTool.setFont(new Font(_currentFamilyButton.getFontFamily(), Font.PLAIN, 
					_currentSizeButton.getThickness()));
			_textTool.saveFamilyInPreferences(_currentFamilyButton.getFontFamily());
			_textTool.saveSizeInPreferences(_currentSizeButton.getThickness());
		}
	}	
	
}
