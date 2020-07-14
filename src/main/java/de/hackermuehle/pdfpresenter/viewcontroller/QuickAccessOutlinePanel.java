package de.hackermuehle.pdfpresenter.viewcontroller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.hackermuehle.pdfpresenter.PdfPresenter;
import de.hackermuehle.pdfpresenter.model.State;
import de.intarsys.pdf.parser.COSLoadException;

public class QuickAccessOutlinePanel extends JPanel {
	private static final Color BACKGROUND_COLOR = new Color(235,235,235);
	
	// TODO V2 delete some/all recents
	
	private static final long serialVersionUID = -8468542947890006725L;
	
	private QuickAccessPanel _quickAccessPanel;

	public QuickAccessOutlinePanel(JFrame frame, State state) {
		
		setLayout(null);
		_quickAccessPanel = new QuickAccessPanel(frame, state);
		add(_quickAccessPanel);
		setBackground(BACKGROUND_COLOR);
		
		addComponentListener(new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent e) {}
			
			@Override
			public void componentResized(ComponentEvent e) {				
				int x = (getWidth() - _quickAccessPanel.getWidth()) / 2;
				int y = (getHeight() - _quickAccessPanel.getHeight()) / 2;
				_quickAccessPanel.setLocation(x, y);
				_quickAccessPanel.setVisible(true);
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {}
			
			@Override
			public void componentHidden(ComponentEvent e) {}
		});
	}
	
	
	class QuickAccessPanel extends JPanel {

		private static final long serialVersionUID = -8862810694617786279L;

		private boolean _listRecentFiles;

		private Image _background;
		private State _state;
		
		private JButton _whiteboardButton;
		private JButton _browseButton;
		private JButton _recent1Button;
		private JButton _recent2Button;
		private JButton _recent3Button;
		private JButton _preferencesButton;
		private JLabel _preferencesLabel;
		
		private JPanel _buttonBar;
		private JFrame _frame;
		
		public QuickAccessPanel(JFrame frame, State controller) {
			_state = controller;
			_listRecentFiles = controller.getShowRecents();
			ImageIcon icon = new ImageIcon(QuickAccessPanel.class.getResource("/quickaccessbackground.png"));
			_background = icon.getImage();
			_frame = frame;
			
			setOpaque(false);
			setLayout(new BorderLayout());
			setBounds(0, 0, 666, 374);
			setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
			setVisible(false);
			
			_state.addPropertyChangeListener(new QuickAccessPropertyChangeListener());
			
			createUi();
			updateRecentFileButtons();
		}

		private void createUi() {
			// Title
			JLabel welcomeLabel = new JLabel(PdfPresenter.getLocalizedString("qaTitle"), JLabel.CENTER);
			welcomeLabel.setFont(getFont().deriveFont(48f));
			welcomeLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 10, 0));
			
			add(welcomeLabel, BorderLayout.PAGE_START);
			
			_buttonBar = new JPanel();
			_buttonBar.setLayout(new BoxLayout(_buttonBar, BoxLayout.X_AXIS));
			_buttonBar.setOpaque(false);
	
			// Central buttons
			Font font = new Font(getFont().getFamily(), Font.PLAIN, 18);
			
			_whiteboardButton = new JButton(PdfPresenter.getLocalizedString("qaWhiteboard"), 
					new ImageIcon(QuickAccessPanel.class.getResource("/whiteboard.png")));
			_whiteboardButton.setVerticalTextPosition(JButton.BOTTOM);
			_whiteboardButton.setHorizontalTextPosition(JButton.CENTER);
			_whiteboardButton.setFont(font);
			_whiteboardButton.setBorderPainted(false);
			_whiteboardButton.setOpaque(false);
			_whiteboardButton.setContentAreaFilled(false);
			_whiteboardButton.addMouseListener(new ButtonMouseListener(_whiteboardButton));
			_whiteboardButton.addActionListener(new NewWhiteboardActionListener());
			
			_browseButton = new JButton(PdfPresenter.getLocalizedString("qaBrowse"), 
					new ImageIcon(QuickAccessPanel.class.getResource("/browse.png")));
			_browseButton.setVerticalTextPosition(JButton.BOTTOM);
			_browseButton.setHorizontalTextPosition(JButton.CENTER);
			_browseButton.setFont(font);
			_browseButton.setBorderPainted(false);
			_browseButton.setOpaque(false);
			_browseButton.setContentAreaFilled(false);
			_browseButton.addMouseListener(new ButtonMouseListener(_browseButton));
			_browseButton.addActionListener(new OpenFileActionListener());
			
			_recent1Button = new JButton(PdfPresenter.getLocalizedString("qaRecent"), 
					new ImageIcon(QuickAccessPanel.class.getResource("/slide.png")));
			_recent1Button.setVerticalTextPosition(JButton.BOTTOM);
			_recent1Button.setHorizontalTextPosition(JButton.CENTER);
			_recent1Button.setFont(font);
			_recent1Button.setBorderPainted(false);
			_recent1Button.setOpaque(false);
			_recent1Button.setContentAreaFilled(false);
			_recent1Button.setEnabled(false);
			_recent1Button.addMouseListener(new ButtonMouseListener(_recent1Button));
			
			_recent2Button = new JButton(PdfPresenter.getLocalizedString("qaRecent"), 
					new ImageIcon(QuickAccessPanel.class.getResource("/slide.png")));
			_recent2Button.setVerticalTextPosition(JButton.BOTTOM);
			_recent2Button.setHorizontalTextPosition(JButton.CENTER);
			_recent2Button.setFont(font);
			_recent2Button.setBorderPainted(false);
			_recent2Button.setOpaque(false);
			_recent2Button.setContentAreaFilled(false);
			_recent2Button.setEnabled(false);
			_recent2Button.addMouseListener(new ButtonMouseListener(_recent2Button));
			
			_recent3Button = new JButton(PdfPresenter.getLocalizedString("qaRecent"), 
					new ImageIcon(QuickAccessPanel.class.getResource("/slide.png")));
			_recent3Button.setVerticalTextPosition(JButton.BOTTOM);
			_recent3Button.setHorizontalTextPosition(JButton.CENTER);
			_recent3Button.setFont(font);
			_recent3Button.setBorderPainted(false);
			_recent3Button.setOpaque(false);
			_recent3Button.setContentAreaFilled(false);
			_recent3Button.setEnabled(false);
			_recent3Button.addMouseListener(new ButtonMouseListener(_recent3Button));
						
			_buttonBar.add(Box.createHorizontalGlue());
			_buttonBar.add(_whiteboardButton);
			_buttonBar.add(_browseButton);
			if (_listRecentFiles) {
				_buttonBar.add(_recent1Button);
				_buttonBar.add(_recent2Button);
				_buttonBar.add(_recent3Button);
			}
			_buttonBar.add(Box.createHorizontalGlue());

			add(_buttonBar, BorderLayout.CENTER);
			
			// Bottom
			JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			bottomPanel.setOpaque(false);
			
			_preferencesLabel = new JLabel(PdfPresenter.getLocalizedString(
					_listRecentFiles ? "qaTurnOffRecentFilesIn" : "qaTurnOnRecentFilesIn"));
			_preferencesLabel.setFont(font);
			bottomPanel.add(_preferencesLabel);
			
			_preferencesButton = new JButton(PdfPresenter.getLocalizedString("qaPreferences"), 
					new ImageIcon(QuickAccessPanel.class.getResource("/preferences.png")));
			_preferencesButton.setVerticalTextPosition(JButton.CENTER);
			_preferencesButton.setHorizontalTextPosition(JButton.RIGHT);
			_preferencesButton.setFont(font);
			_preferencesButton.setBorderPainted(false);
			_preferencesButton.setOpaque(false);
			_preferencesButton.setContentAreaFilled(false);
			_preferencesButton.addMouseListener(new ButtonMouseListener(_preferencesButton));
			_preferencesButton.addActionListener(new PreferencesActionListener());
			
			bottomPanel.add(_preferencesButton);
			
			add(bottomPanel, BorderLayout.PAGE_END);
		}
		

		private String getDisplayFileName(String name) {
			int indexExtensionBegins = name.lastIndexOf('.');
			name = name.substring(0, indexExtensionBegins);
			if (name.length() > 11)
				name = name.substring(0, 4) + "..." + 
					name.substring(name.length() - 4, name.length());
			
			return name;
		}
		
		public void updateShowRecents() {
			if (_listRecentFiles == _state.getShowRecents()) return;
			if (_state.getShowRecents()) {
				_preferencesLabel.setText(PdfPresenter.getLocalizedString("qaTurnOffRecentFilesIn"));
				
				// Remove glue
				_buttonBar.remove(getComponentCount());
				_buttonBar.add(_recent1Button);
				_buttonBar.add(_recent2Button);
				_buttonBar.add(_recent3Button);
				_buttonBar.add(Box.createHorizontalGlue());
			} else {
				_preferencesLabel.setText(PdfPresenter.getLocalizedString("qaTurnOnRecentFilesIn"));
				
				_buttonBar.remove(_recent1Button);
				_buttonBar.remove(_recent2Button);
				_buttonBar.remove(_recent3Button);
			}
		
			revalidate();
			repaint();
		}

		private void updateRecentFileButtons() {
			if (_recent1Button == null || _recent2Button == null || _recent3Button == null)
				return;
						
			List<String> recents = _state.getRecentFilenames();
			int size = recents.size();
			if (size > 0) {
				String file = recents.get(0);
				String name = getDisplayFileName(State.extractFileName(file));
				_recent1Button.setText(name);
				_recent1Button.setEnabled(true);
				if (_recent1Button.getActionListeners().length > 0) {
					for (ActionListener listener : _recent1Button.getActionListeners())
						_recent1Button.removeActionListener(listener);
				}
				_recent1Button.addActionListener(new RecentButtonActionListener(file));
			} else {
				_recent1Button.setText(PdfPresenter.getLocalizedString("qaRecent"));
				_recent1Button.setEnabled(false);
			}
			if (size > 1) {
				String file = recents.get(1);
				String name = getDisplayFileName(State.extractFileName(file));
				_recent2Button.setText(name);
				_recent2Button.setEnabled(true);
				if (_recent2Button.getActionListeners().length > 0) {
					for (ActionListener listener : _recent2Button.getActionListeners())
						_recent2Button.removeActionListener(listener);
				}
				_recent2Button.addActionListener(new RecentButtonActionListener(file));
			} else {
				_recent2Button.setText(PdfPresenter.getLocalizedString("qaRecent"));
				_recent2Button.setEnabled(false);
			}
			if (size > 2) {
				String file = recents.get(2);
				String name = getDisplayFileName(State.extractFileName(file));
				_recent3Button.setText(name);
				_recent3Button.setEnabled(true);
				if (_recent3Button.getActionListeners().length > 0) {
					for (ActionListener listener : _recent3Button.getActionListeners())
						_recent3Button.removeActionListener(listener);
				}
				_recent3Button.addActionListener(new RecentButtonActionListener(
						file));
			} else {
				_recent3Button.setText(PdfPresenter.getLocalizedString("qaRecent"));
				_recent3Button.setEnabled(false);
			}
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			g.drawImage(_background, 0, 0, null);
		}
		
		
		private final class RecentButtonActionListener implements
				ActionListener {
			private final String path;

			private RecentButtonActionListener(String path) {
				this.path = path;
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					_state.openPresentation(path);
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (COSLoadException e1) {
					e1.printStackTrace();
				}
				
				((JButton) e.getSource()).setContentAreaFilled(false);
			}
		}


		private final class ButtonMouseListener implements MouseListener {
			private final JButton button;

			private ButtonMouseListener(JButton button) {
				this.button = button;
			}

			@Override
			public void mouseReleased(MouseEvent e) {}

			@Override
			public void mousePressed(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {
				if (button.isEnabled())
					button.setContentAreaFilled(false);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				if (button.isEnabled())
					button.setContentAreaFilled(true);
			}

			@Override
			public void mouseClicked(MouseEvent e) {}
		}
		
		
		class NewWhiteboardActionListener implements ActionListener {

			@Override
			public void actionPerformed(ActionEvent e) {
				_state.openWhiteboardPresentation();
				_whiteboardButton.setContentAreaFilled(false);
			}
		}

		class OpenFileActionListener implements ActionListener {

			@Override
			public void actionPerformed(ActionEvent event) {
				FileChooser.openPresentation(_frame, _state);
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
				});
				_preferencesButton.setContentAreaFilled(false);
			}
		}

		
		private class QuickAccessPropertyChangeListener implements PropertyChangeListener {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				if (e.getPropertyName().equals("showRecents")) {
					updateShowRecents();
					_listRecentFiles = _state.getShowRecents();
				} else if (e.getPropertyName().equals("recentFilenames")) {
					updateRecentFileButtons();
				}
			}
		}
	}
	
}
