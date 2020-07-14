package de.hackermuehle.pdfpresenter.viewcontroller.stylebar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import de.hackermuehle.pdfpresenter.model.Preferences;
import de.hackermuehle.pdfpresenter.model.tools.Eraser;


/**
 * Style palette for eraser tool
 * 
 * @author shuo
 *
 */
public class EraserStyleBar extends StyleBar {
	
	private static final long serialVersionUID = -3485696631854523018L;
	private static final String PREFERENCE_SIZE_PREFIX = "stylepalette.eraser.size";
	
	private static final int[] DEFAULT_THICKNESSES = { 10, 20, 30, 40, 50 };
	
	private ThicknessGroupedButton _currentThicknessButton;
	
	private Eraser _eraser;
	
	public EraserStyleBar(Eraser eraser, Preferences preferences) {
		super(preferences);
		_eraser = eraser;
		
		registerButtons();
		addButtonsToPalette();
	}

	public void registerButtons() {
		LinkedList<LinkedList<GroupedButton>> buttonGroups = getButtonGroups();
		
		LinkedList<GroupedButton> thicknessButtons = new LinkedList<GroupedButton>();
		
		// Load sizes
		int[] thicknesses;
		try {
			int size0 = Integer.parseInt(getPreferences().getPreference(PREFERENCE_SIZE_PREFIX + "0"));
			int size1 = Integer.parseInt(getPreferences().getPreference(PREFERENCE_SIZE_PREFIX + "1"));
			int size2 = Integer.parseInt(getPreferences().getPreference(PREFERENCE_SIZE_PREFIX + "2"));
			int size3 = Integer.parseInt(getPreferences().getPreference(PREFERENCE_SIZE_PREFIX + "3"));
			int size4 = Integer.parseInt(getPreferences().getPreference(PREFERENCE_SIZE_PREFIX + "4"));
			
			thicknesses = new int[] { size0, size1, size2, size3, size4 };
		} catch (IllegalArgumentException e) {
			Logger.getLogger(this.getClass()).warn("Load EraserStylePalette size from preferences failed. " + e.getLocalizedMessage());
			thicknesses = DEFAULT_THICKNESSES;
			initSizePreferences();
		} catch (NullPointerException e) {
			Logger.getLogger(this.getClass()).warn("Load EraserStylePalette size from preferences failed. " + e.getLocalizedMessage());
			thicknesses = DEFAULT_THICKNESSES;
			initSizePreferences();
		}
		
		ThicknessActionListener thicknessActionListener = new ThicknessActionListener();
		ThicknessChangeListener thicknessChangeListener = new ThicknessChangeListener();
				
		int eraserSize = (int) _eraser.getSize();
		for (int i = 0; i < thicknesses.length; i++) {
			int size = thicknesses[i];
			ThicknessGroupedButton thicknessButton = new ThicknessGroupedButton(
					i, size, getButtonHeight(), getAlignment());
			thicknessButton.addActionListener(thicknessActionListener);
			thicknessButton.addPropertyChangeListener(thicknessChangeListener);
			if (_currentThicknessButton == null && size == eraserSize) {
				thicknessButton._isChosen = true;
				_currentThicknessButton = thicknessButton;
			}
			thicknessButtons.add(thicknessButton);
		}
		
		buttonGroups.add(thicknessButtons);
	}
	
	private void initSizePreferences() {
		for (int i = 0; i < DEFAULT_THICKNESSES.length; i++) {
			getPreferences().setPreference(PREFERENCE_SIZE_PREFIX + String.valueOf(i), 
					String.valueOf(DEFAULT_THICKNESSES[i]));
		}
	}
	
	class ThicknessActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			Object button = e.getSource();
			if (button instanceof ThicknessGroupedButton) {
				ThicknessGroupedButton thicknessButton = (ThicknessGroupedButton) button;

				if (_currentThicknessButton != null)
					_currentThicknessButton._isChosen = false;
				thicknessButton._isChosen = true;
				_currentThicknessButton = thicknessButton;
				
				_eraser.setSize(thicknessButton.getThickness());
				_eraser.saveSizeInPreferences(thicknessButton.getThickness());
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
				_eraser.setSize(thicknessButton.getThickness());
				_eraser.saveSizeInPreferences(thicknessButton.getThickness());
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

		if (DEFAULT_THICKNESSES.length == buttons.size()) {
			if (_currentThicknessButton != null)
				_currentThicknessButton.setChosen(false);
			for (int i = 0; i < DEFAULT_THICKNESSES.length; i++) {
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
			if (_currentThicknessButton != null) {
				_eraser.setSize(_currentThicknessButton.getThickness());
				_eraser.saveSizeInPreferences(_currentThicknessButton.getThickness());
			}
		}
	}
}
