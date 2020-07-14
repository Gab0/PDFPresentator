package de.hackermuehle.pdfpresenter.viewcontroller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.Action;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.InsetsUIResource;

import com.jidesoft.plaf.basic.BasicJideTabbedPaneUI;
import com.jidesoft.swing.JideTabbedPane;

import de.hackermuehle.pdfpresenter.model.Presentation;
import de.hackermuehle.pdfpresenter.model.State;

/**
 * The navigation pane displays a tabbed interface to the opened presentations
 * and holds a NavigationPanel with thumbnails for each.
 * It allows to change the active/close any presentation.
 */
public class NavigationPane extends JideTabbedPane {
	private static final long serialVersionUID = 1L;
	private State _state;
	private StatePropertyChangeListener _statePropertyChangeListener = new StatePropertyChangeListener();
	private LinkedList<NavigationPanel> _navigationPanels = new LinkedList<NavigationPanel>();

	public NavigationPane(State state) {
		_state = state;
		_state.addPropertyChangeListener(_statePropertyChangeListener);
		
		// Create a tab for each existing presentation (usually none):
		for (Presentation presentation : _state.getPresentations()) {
			NavigationPanel navigationPanel = new NavigationPanel(presentation, state);
			addTab(presentation.getTitle(), null, navigationPanel, presentation.getTitle());
			_navigationPanels.add(navigationPanel);
		}
		
		setTabPlacement(JTabbedPane.RIGHT);
		setShowCloseButtonOnTab(true);
		setBoldActiveTab(true);
		UIManager.put("JideTabbedPane.selectedTabFont", new Font(Font.SANS_SERIF, Font.PLAIN, 18));
		UIManager.put("JideTabbedPane.font", new Font(Font.SANS_SERIF, Font.PLAIN, 18));
		UIManager.put("JideTabbedPane.contentBorderInsets", new InsetsUIResource(0, 0, 0, 0));
		
		setFocusable(false);
		setForeground(Color.DARK_GRAY);
		//setTabShape(JideTabbedPane.SHAPE_WINDOWS);
		//setColorTheme(JideTabbedPane.COLOR_THEME_OFFICE2003);
		addChangeListener(new NavigationPaneChangeListener());
		setUI(new BiggerJideTabbedPaneUI());
		
		// Action on tab close button clicked:
		setCloseAction(new Action() {
			@Override
			public void addPropertyChangeListener(PropertyChangeListener listener) {}

			@Override
			public Object getValue(String key) {return null;}

			@Override
			public boolean isEnabled() {return true;}

			@Override
			public void putValue(String key, Object value) {}

			@Override
			public void removePropertyChangeListener(PropertyChangeListener listener) {}

			@Override
			public void setEnabled(boolean b) {}

			@Override
			public void actionPerformed(ActionEvent e) {
				NavigationPanel navigationPanel = (NavigationPanel) e.getSource();
				remove(navigationPanel);
				
				_navigationPanels.remove(navigationPanel);
				_state.removePresentation(navigationPanel.getPresentation());
				
				// Free possible resources bound by the thumbnails:
				navigationPanel.dispose();
				
				// XXX Otherwise swaps back to default UI - there should be a
				// better approach:
				setUI(new BiggerJideTabbedPaneUI());
			}
		});
	}
	
	@Override
	protected void finalize() throws Throwable {
		_state.removePropertyChangeListener(_statePropertyChangeListener);
		super.finalize();
	}
	
	/**
	 * Listens to state events and reflects changes in the GUI.
	 */
	private class StatePropertyChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			
			// Presentation(s) added or removed, update tabs:
			if (e.getPropertyName().equals("presentations")) {
				
				// Add tabs for each new presentation:
				for (Presentation presentation : _state.getPresentations()) {
					if (getNavigationPanel(presentation) == null) {
						
						// New presentation found, add tab with new NavigationPanel:
						NavigationPanel navigationPanel = new NavigationPanel(presentation, _state);
						addTab(presentation.getTitle(), null, navigationPanel, presentation.getTitle());
						_navigationPanels.add(navigationPanel);
					}
				}
				if (getTabCount() >= 0) setSelectedIndex(getTabCount()-1); // Latest presentation becomes active
				
				// Delete tabs for each removed presentation:
				ListIterator<NavigationPanel> it = _navigationPanels.listIterator();
				while (it.hasNext()) { 
					NavigationPanel navigationPanel = it.next();
					
					if (!_state.getPresentations().contains(navigationPanel.getPresentation())) { 

						// Presentation deleted, remove tab with NavigationPanel:
						remove(navigationPanel); 
						it.remove();
					}
				} 
			}
		}
	}
	
	/**
	 * Listens to tab change events and changes the state accordingly.
	 */
	private class NavigationPaneChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			// Get current tab and set active presentation to the tab's one:
			int tabIndex = getSelectedIndex();
			if (tabIndex < 0) return;

			NavigationPanel navigationPanel = (NavigationPanel) getComponentAt(tabIndex);
			_state.setActivePresentation(navigationPanel.getPresentation());
		}
	}

	public NavigationPanel getNavigationPanel(Presentation presentation) {
		for (NavigationPanel navigationPanel : _navigationPanels) {
			if (navigationPanel.getPresentation().equals(presentation))
				return navigationPanel;
		}
		return null;
	}
}

/**
 * Supports bigger TabbedPaneCloseButtons and blue colored tabs.
 */
class BiggerJideTabbedPaneUI extends BasicJideTabbedPaneUI {
	class BiggerTabCloseButton extends TabCloseButton {
		private static final long serialVersionUID = -276208874408621293L;
		public BiggerTabCloseButton(int type) {
			super(type);
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(24, 24);
		}
	}
	
	@Override
	protected int getColorTheme() {
		return JideTabbedPane.COLOR_THEME_WINXP;
	}
	
	@Override
	protected int getTabShape() {
		return JideTabbedPane.SHAPE_WINDOWS;
	}

	protected TabCloseButton createNoFocusButton(int type) {
		return new BiggerTabCloseButton(type);
	}

	public void ensureCloseButtonCreated() {
		// XXX Ugliness: It's magic.
		_selectColor1 = new Color(0, 128, 255);
		_selectColor2 = new Color(0, 128, 255);

		if (isShowCloseButton() && isShowCloseButtonOnTab()
				&& scrollableTabLayoutEnabled()) {
			if (_closeButtons == null) {
				_closeButtons = new BiggerTabCloseButton[_tabPane.getTabCount()];
			} else if (_closeButtons.length > _tabPane.getTabCount()) {
				TabCloseButton[] temp = new BiggerTabCloseButton[_tabPane
						.getTabCount()];
				System.arraycopy(_closeButtons, 0, temp, 0, temp.length);
				for (int i = temp.length; i < _closeButtons.length; i++) {
            javax.swing.JButton tabCloseButton = _closeButtons[i];
					_tabScroller.tabPanel.remove(tabCloseButton);
				}
				_closeButtons = temp;
			} else if (_closeButtons.length < _tabPane.getTabCount()) {
				TabCloseButton[] temp = new BiggerTabCloseButton[_tabPane
						.getTabCount()];
				System.arraycopy(_closeButtons, 0, temp, 0,
						_closeButtons.length);
				_closeButtons = temp;
			}

			super.ensureCloseButtonCreated();
      } 
	}
}
