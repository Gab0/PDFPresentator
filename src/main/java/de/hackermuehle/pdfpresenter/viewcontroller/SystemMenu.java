package de.hackermuehle.pdfpresenter.viewcontroller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.JPopupMenu.Separator;

import de.hackermuehle.pdfpresenter.PdfPresenter;
import de.hackermuehle.pdfpresenter.model.State;
import de.hackermuehle.pdfpresenter.viewcontroller.stylebar.DropDownButton;

public class SystemMenu extends DropDownButton {
	private static final long serialVersionUID = 1L;
	private State _state;
	private int _buttonLength;
	private JFrame _frame;
	private int _indexRecentsBegin;
	private HashSet<JComponent> _recentMenuItems;
	private StatePropertyChangeListener _statePropertyChangeListener = new StatePropertyChangeListener();
	
	public SystemMenu(int buttonLength, JFrame frame, State controller) {
		_state = controller;
		_state.addPropertyChangeListener(_statePropertyChangeListener);
		
		_buttonLength = buttonLength;
		_frame = frame;
		_recentMenuItems = new HashSet<JComponent>();
		
		setFocusable(false);
		addActionListener(new OnClickActionListener());

		setBorderPainted(false);
		setOpaque(false);
		setToolTipText(PdfPresenter.getLocalizedString("smOpenMenu"));
		setIcon(ViewUtilities.createIcon("/mainMenu.png", _buttonLength));
		
		JMenuItem menuItem = new JMenuItem(PdfPresenter.getLocalizedString("smNewWhiteboard"));
		menuItem.addActionListener(new NewWhiteboardActionListener());
		getMenu().add(menuItem);

		menuItem = new JMenuItem(PdfPresenter.getLocalizedString("smOpen"));
		menuItem.addActionListener(new OpenFileActionListener());
		getMenu().add(menuItem);
		
		// Recents:
		_indexRecentsBegin = getMenu().getComponentCount();
		updateRecents();
		
		menuItem = new JMenuItem(PdfPresenter.getLocalizedString("smPreferences"));
		menuItem.addActionListener(new PreferencesActionListener());
		getMenu().addSeparator();
		getMenu().add(menuItem);
		
		menuItem = new JMenuItem(PdfPresenter.getLocalizedString("smAbout"));
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						new AboutDialog(_frame);
					}
				});
			}
		});
		getMenu().addSeparator();
		getMenu().add(menuItem);

		menuItem = new JMenuItem(PdfPresenter.getLocalizedString("smExit"));
		menuItem.addActionListener(new ExitActionListener());
		getMenu().addSeparator();
		getMenu().add(menuItem);
	}

	@Override
	protected void finalize() throws Throwable {
		_state.removePropertyChangeListener(_statePropertyChangeListener);
		super.finalize();
	}
	
	private void updateRecents() {
		if (!_recentMenuItems.isEmpty()) {
			for (JComponent recent : _recentMenuItems) {
				getMenu().remove(recent);
			}
			_recentMenuItems.clear();
		}
		
		if (_state.getShowRecents()) {
			List<String> recentFiles = _state.getRecentFilenames();
			Separator seperator = new JPopupMenu.Separator();
			getMenu().insert(seperator, _indexRecentsBegin);
			_recentMenuItems.add(seperator);
			if (recentFiles.size() > 0) {
				int i = 1;
				for (String file : recentFiles) {
					JMenuItem fileItem = new JMenuItem(i + " " + State.extractFileName(file));
					fileItem.addActionListener(new RecentButtonActionListener(file));
					getMenu().add(fileItem, _indexRecentsBegin + i++);
					_recentMenuItems.add(fileItem);
				}
			} else {
				JMenuItem placeHolderItem = new JMenuItem(PdfPresenter.getLocalizedString("smRecents"));
				placeHolderItem.setEnabled(false);
				getMenu().add(placeHolderItem, _indexRecentsBegin + 1);
				_recentMenuItems.add(placeHolderItem);
			}
		}
	}

	private final class OnClickActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// FIXME: _controller.openPdfPresentation();
		}
	}
	
	private class StatePropertyChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals("recentFilenames") ||
				e.getPropertyName().equals("showRecents")) {
				updateRecents();
			}
		}
	}

	private final class RecentButtonActionListener implements ActionListener {
		private final String path;

		private RecentButtonActionListener(String path) {
			this.path = path;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			FileChooser.openPresentation(path, _state);
		}
	}

	class PreferencesActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					new PreferencesDialog(null, _state);
				}
			});;
		}
	}
	
	class NewWhiteboardActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			_state.openWhiteboardPresentation();
		}
	}

	class OpenFileActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			FileChooser.openPresentation(_frame, _state);
		}
	}
	
	class ExitActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
    		_state.savePreferencesToDisk();
			System.exit(0);
		}
	}
}
