package de.hackermuehle.pdfpresenter.viewcontroller.stylebar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;

import de.hackermuehle.pdfpresenter.PdfPresenter;
import de.hackermuehle.pdfpresenter.model.Preferences;
import de.hackermuehle.pdfpresenter.viewcontroller.ViewUtilities;

/**
 * Superclass of style palettes.
 * 
 * @author shuo
 *
 * Events:
 *
 */
public abstract class StyleBar extends JToolBar {
	
	private static final long serialVersionUID = -256120027807427451L;
	private static final String PREFERENCE_ALIGNMENT = "stylepalette.alignment";
	private static final int BUTTON_GAP = 10;
	private static final int HEIGHT = 40;
	
	private int _buttonHeight;
	private LinkedList<LinkedList<GroupedButton>> _buttonGroups;
	
	private StyleBarAlignment _alignment;
	private Preferences _preferences;
	private Icon _resetIcon;
	private Icon _resetRolloverIcon;

	public enum StyleBarAlignment {
		TOP, BOTTOM, LEFT, RIGHT;
	}
	
	/**
	 * Construct a new style palette with default height
	 */
	public StyleBar(Preferences preferences) {
		_preferences = preferences;
		
		String alignment = _preferences.getPreference(PREFERENCE_ALIGNMENT);
		if (alignment == null) {
			alignment = "TOP"; // Fail safe
			_preferences.setPreference(PREFERENCE_ALIGNMENT, "TOP");
		}
		try {
			_alignment = StyleBarAlignment.valueOf(alignment);
		} catch (IllegalArgumentException e) {
			Logger.getLogger(getClass()).warn("Load style palette alignment from preferences " +
					"failed. Used default value TOP. " + e.getLocalizedMessage());
			_alignment = StyleBarAlignment.TOP;
			_preferences.setPreference(PREFERENCE_ALIGNMENT, "TOP");
		}
		if (_alignment.equals(StyleBarAlignment.LEFT) || 
				_alignment.equals(StyleBarAlignment.RIGHT))
			setOrientation(VERTICAL);
		
		_buttonHeight = HEIGHT;
		_buttonGroups = new LinkedList<LinkedList<GroupedButton>>();
		
		addPropertyChangeListener(new OrientationChangeListener());
		addComponentListener(new AlignmentChangedComponentListener());
		
		setBackground(Color.LIGHT_GRAY);
		setUI(new PPStyleBarUI());
		
		// TODO V2 get back handle on linux
		
		if (System.getProperty("java.vm.name").startsWith("OpenJDK")) {
			// OpenJDK only, ExpandLayout seems not to work: 
			if (_alignment.equals(StyleBarAlignment.TOP) || _alignment.equals(StyleBarAlignment.BOTTOM))
				setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			else
				setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		} else {
			setLayout(new ExpandLayout(this, getOrientation())); 
		}
		
		if (PdfPresenter.isOnLinux()) setBorder(null);
	}
	
	protected Preferences getPreferences() {
		return _preferences;
	}
	
	
	/**
	 * Load the current aligment from preferences, and realign
	 * the stylepalette if the alignment changed. This includes
	 * both the stored value and the position of each component.
	 */
	public void realign() {
		StyleBarAlignment alignment = StyleBarAlignment.valueOf(
				_preferences.getPreference(PREFERENCE_ALIGNMENT));
		if (!alignment.equals(_alignment)) {
			_alignment = alignment;
			membersUpdateAlignment();
			
			removeAll();
			if (_alignment.equals(StyleBarAlignment.LEFT) || 
					_alignment.equals(StyleBarAlignment.RIGHT)) {
				setOrientation(VERTICAL);
			} else {
				setOrientation(HORIZONTAL);
			}
			// OrientationChangeListener will be triggered and
			// layout buttons
		}
	}
	
	
	/**
	 * Update the saved alignment values (But don't reposition anything).
	 * Returns true if alignment changed
	 * 
	 */
	private void updateAlignment() {
		Rectangle bound = getBounds();
		StyleBarAlignment alignment;
		if (getOrientation() == JToolBar.HORIZONTAL) {
			alignment = bound.y == 0 ? StyleBarAlignment.TOP : StyleBarAlignment.BOTTOM;
		} else {
			alignment = bound.x == 0 ? StyleBarAlignment.LEFT : StyleBarAlignment.RIGHT;
		}
		if (!alignment.equals(_alignment)) {
			_alignment = alignment;
			membersUpdateAlignment();
			_preferences.setPreference(PREFERENCE_ALIGNMENT, _alignment.name());
		}
	}
	
	
	private void membersUpdateAlignment() {
		for (Component component : getComponents()) {
			if (component instanceof GroupedButton) {
				((GroupedButton) component).setAlignment(_alignment);
			}
		}
	}
	
	
	public StyleBarAlignment getAlignment() {
		return _alignment;
	}

	
	@Override
	public void setEnabled(boolean enabled) {
		for (Component c : getComponents()) {
			c.setEnabled(enabled);
		}
		super.setEnabled(enabled);
	}
	
	
	public LinkedList<LinkedList<GroupedButton>> getButtonGroups() {
		return _buttonGroups;
	}
	
	
	/**
	 * Should be called on subclasses in constructor, after registering the buttons.
	 */
	public void addButtonsToPalette() {
		if (getOrientation() == JToolBar.HORIZONTAL) {
			addButtonsHorizontally();
		} else {
			addButtonsVertically();
		}
	}
	

	public int getButtonHeight() {
		return _buttonHeight;
	}
	
	
	public void setButtonHeight(int height) {
		_buttonHeight = height;
	}


	private void addButtonsHorizontally() {
		add(Box.createHorizontalGlue());
		for (int i = _buttonGroups.size() - 1; i >= 0; i--) {
			LinkedList<GroupedButton> buttons = _buttonGroups.get(i);
			for (int j = 0; j < buttons.size(); j++) {
				add(buttons.get(j));
				if (j != buttons.size() - 1)
					add(Box.createRigidArea(new Dimension(BUTTON_GAP, 0)));
			}
			if (i != 0) addSeparator();
		}
		
		add(Box.createHorizontalGlue());
		add(createResetButton());
	}
	
	
	private void addButtonsVertically() {
		for (int i = 0; i < _buttonGroups.size(); i++) {
			LinkedList<GroupedButton> buttons = _buttonGroups.get(i);
			for (int j = 0; j < buttons.size(); j++) {
				add(buttons.get(j));
				if (j != buttons.size() - 1)
					add(Box.createRigidArea(new Dimension(0, BUTTON_GAP)));
			}
			if (i != _buttonGroups.size() - 1) addSeparator();
		}
//		add(Box.createVerticalGlue());
		
		add(Box.createVerticalStrut(25));
		add(createResetButton());
	}
	
	
	private JButton createResetButton() {
		if (_resetIcon == null) 
			_resetIcon = ViewUtilities.createIcon("/resetstylepalette.png", _buttonHeight + 3);
		final JButton button = new JButton(_resetIcon);
		button.setPreferredSize(new Dimension(_buttonHeight, _buttonHeight));
		if (_resetRolloverIcon == null)
			_resetRolloverIcon = ViewUtilities.createIcon("/resetstylepaletterollover.png", 
					_buttonHeight + 3);
		button.setRolloverIcon(_resetRolloverIcon);
		button.setPressedIcon(ViewUtilities.createIcon("/resetstylepalettepressed.png", 
					_buttonHeight + 3));
		button.setBorderPainted(false);
		button.setContentAreaFilled(false);
		button.setFocusPainted(false);
		button.setFocusable(false);
		button.addActionListener(new ResetActionListener());
		
		return button;
	}
		
	
	class OrientationChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			String propName = evt.getPropertyName();
			if ("orientation".equals(propName)) {
				Integer newValue = (Integer) evt.getNewValue();
				if (newValue.intValue() == JToolBar.HORIZONTAL) {
					// OpenJDK only, ExpandLayout seems not to work: 
					if (System.getProperty("java.vm.name").startsWith("OpenJDK")){
						setLayout(new BoxLayout(StyleBar.this, BoxLayout.X_AXIS));
					}

					removeAll();
					addButtonsHorizontally();
					
				} else {
					// OpenJDK only, ExpandLayout seems not to work: 
					if (System.getProperty("java.vm.name").startsWith("OpenJDK")){
						setLayout(new BoxLayout(StyleBar.this, BoxLayout.Y_AXIS));
					}
					
					removeAll();
					addButtonsVertically();
				}
			}	
		}
	}
	
	
	class ResetActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			resetToFactorySettings();
		}
		
	}
	
	
	public abstract void resetToFactorySettings();
	

	private final class AlignmentChangedComponentListener implements
	ComponentListener {
		@Override
		public void componentShown(ComponentEvent e) {}

		@Override
		public void componentResized(ComponentEvent e) {
			updateAlignment();
		}

		@Override
		public void componentMoved(ComponentEvent e) {
			updateAlignment();
		}

		@Override
		public void componentHidden(ComponentEvent e) {}
	}

}
