package de.hackermuehle.pdfpresenter.viewcontroller;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

import de.hackermuehle.pdfpresenter.model.State;
import de.hackermuehle.pdfpresenter.viewcontroller.stylebar.StyleBar;
import de.hackermuehle.pdfpresenter.viewcontroller.stylebar.StyleBarFactory;
import de.hackermuehle.pdfpresenter.viewcontroller.stylebar.StyleBar.StyleBarAlignment;

/**
 * The MainPanel displays the active page of the active presentation and
 * the style palette of the active tool, if any.
 */
public class MainPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private State _controller;
	private PropertyChangeListener _mainPanelPropertyChangeListener;
	private PresentationPanel _slidePanelMaster;
	private StyleBar _stylePalette;

	public MainPanel(State state) {
		_controller = state;
		_mainPanelPropertyChangeListener = new StatePropertyChangeListener();
		_controller.addPropertyChangeListener(_mainPanelPropertyChangeListener);

		setOpaque(false);
		setFocusable(false);
		setLayout(new BorderLayout());

		_stylePalette = StyleBarFactory.createStyleBar(_controller);
		if (_stylePalette != null) {
			StyleBarAlignment alignment = _stylePalette.getAlignment();
			addStyleBar(alignment);
		}

		_slidePanelMaster = new InnerPresentationPanel(_controller);
		add(_slidePanelMaster, BorderLayout.CENTER);

		requestFocusInWindow();
		
		validate();
		repaint();
	}


	@Override
	protected void finalize() throws Throwable {
		_controller.removePropertyChangeListener(_mainPanelPropertyChangeListener);
		super.finalize();
	}

	/**
	 *  Listens to state events and reflects changes in the GUI.
	 */
	class StatePropertyChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals("activeTool")) {

				StyleBar activeStylePalette = StyleBarFactory.createStyleBar(_controller);
				
				if (_stylePalette != null) {
					if (activeStylePalette != null) {
	
						// When stylePalette is floating, it remains opened.
						// Known Java Bug, see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4782243
						// Workaround has potential flaws, still preferred:
						//Commented out: Only useful when stylePalette is floating.
						/*
						try { 
						if (((BasicToolBarUI) _stylePalette.getUI()).isFloating())
							((BasicToolBarUI) _stylePalette.getUI()).setFloating(false, new Point(0,0));
						} catch(Exception ex) {remove(_stylePalette);};
						*/
						
						remove(_stylePalette);
						
						_stylePalette = null;
					}
					else _stylePalette.setEnabled(false);
				}
				
				if (_controller.getActiveTool() != null && activeStylePalette != null) {
					activeStylePalette.setEnabled(true);
					_stylePalette = activeStylePalette;
					_stylePalette.realign();
					StyleBarAlignment alignment = _stylePalette.getAlignment();
					addStyleBar(alignment);
				}
				
				validate();
				repaint();
			}
		}
	}
	
	private void addStyleBar(StyleBarAlignment alignment) {
		if (alignment.equals(StyleBarAlignment.TOP))
			add(_stylePalette, BorderLayout.NORTH);
		else if (alignment.equals(StyleBarAlignment.LEFT))
			add(_stylePalette, BorderLayout.WEST);
		else if (alignment.equals(StyleBarAlignment.RIGHT))
			add(_stylePalette, BorderLayout.EAST);
		else if (alignment.equals(StyleBarAlignment.BOTTOM))
			add(_stylePalette, BorderLayout.SOUTH);
		else 
			add(_stylePalette, BorderLayout.NORTH);
	}
}
