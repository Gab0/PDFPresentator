package de.hackermuehle.pdfpresenter.viewcontroller.stylebar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.hackermuehle.pdfpresenter.model.State;
import de.hackermuehle.pdfpresenter.viewcontroller.VerticalInertialScrollPane;
import de.hackermuehle.pdfpresenter.viewcontroller.stylebar.StyleBar.StyleBarAlignment;

public class FontFamilyPopover extends Popover {
	
	private static final long serialVersionUID = 3176443258479841784L;
	private static final int FONT_POPOVER_SCROLLPANE_WIDTH = 155;
	private static final int FONT_POPOVER_SCROLLPANE_HEIGHT = 250; // 180
	private static final int FONT_LABEL_HEIGHT = 30;
	private static final int SHAKE_THRESHOLD = 10;
	
	private FontFamilyGroupedButton _button;
	private VerticalInertialScrollPane _fontsScrollPane;
	private JPanel _fontInnerPanel;
	
	private JLabel _currentFontFamilyLabel;
	private String _currentFontFamilyName;
	
	private JLabel _tentativeFontLabel;
	private Point _tentativePoint;
	private Point _tentativeHelpPoint;
	private boolean _isScrolling;
	
	private State _controller;
	
	public FontFamilyPopover(JFrame frame, FontFamilyGroupedButton button, 
			StyleBarAlignment alignment, State controller) {
		super(frame, button, alignment);
		
		_button = button;
		_currentFontFamilyName = _button.getFontFamily(); 
		_tentativePoint = new Point();
		_tentativeHelpPoint = new Point();
		_controller = controller;
		
		getContentPane().add(createPanel());
		
		pack();
		setVisible(true);
		
		if (_currentFontFamilyLabel != null) {
			scrollToCurrentFontFamilyLabel();
		}
	}
	
	private void scrollToCurrentFontFamilyLabel() {
		Point location = _currentFontFamilyLabel.getLocation();
		location.y += (_fontsScrollPane.getHeight() + _currentFontFamilyLabel.getHeight()) / 2;
		System.out.println(location);
		_fontInnerPanel.scrollRectToVisible(new Rectangle(location));
	}
	
	private JPanel createPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(Color.BLACK);
		
		_fontInnerPanel = new JPanel();
		_fontInnerPanel.setLayout(new GridLayout(0, 1));
		_fontInnerPanel.setBackground(Color.BLACK);
		
		_fontsScrollPane = new VerticalInertialScrollPane(_fontInnerPanel, _controller);
		_fontsScrollPane.setPreferredSize(new Dimension(FONT_POPOVER_SCROLLPANE_WIDTH, 
				FONT_POPOVER_SCROLLPANE_HEIGHT));
		_fontsScrollPane.setBorder(BorderFactory.createEmptyBorder());
		
		// Add font buttons
		String[] availableFontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().
			getAvailableFontFamilyNames();
		
		FontPopoverMouseListener fontPopoverMouseListener = new FontPopoverMouseListener();
		FontPopoverMouseMotionListener fontPopoverMouseMotionListener = new FontPopoverMouseMotionListener();
		
		for (String fontName : availableFontNames) {
			Font font = new Font(fontName, Font.PLAIN, 20);
			JLabel fontLabel = new JLabel(fontName, JLabel.CENTER);

			// Set label look
			fontLabel.setPreferredSize(new Dimension(_fontsScrollPane.getWidth(), FONT_LABEL_HEIGHT));
			fontLabel.setFont(font);
			fontLabel.setOpaque(true);
			fontLabel.setForeground(Color.WHITE);
			
			if (fontName.equals(_currentFontFamilyName)) {
				fontLabel.setBackground(Color.GRAY);
				_currentFontFamilyLabel = fontLabel;
			} else {
				fontLabel.setBackground(Color.BLACK);
			}

			fontLabel.addMouseListener(fontPopoverMouseListener);
			fontLabel.addMouseMotionListener(fontPopoverMouseMotionListener);
			
			_fontInnerPanel.add(fontLabel);
		}
		
		panel.add(_fontsScrollPane);		
		panel.add(createDoneButton());	
		
		return panel;
	}
	
	
	private final class FontPopoverMouseMotionListener implements
			MouseMotionListener {
		@Override
		public void mouseMoved(MouseEvent e) {}

		@Override
		public void mouseDragged(MouseEvent e) {
			_fontsScrollPane.handleMouseMotionEvent(e);
		}
	}


	private final class FontPopoverMouseListener implements MouseListener {

		@Override
		public void mouseReleased(MouseEvent e) {
			_fontsScrollPane.handleMouseEvent(e);

			if (!_isScrolling) {
				Object source = e.getSource();
				if (_tentativeFontLabel.equals(source)) {
					_tentativeHelpPoint.x = e.getXOnScreen();
					_tentativeHelpPoint.y = e.getYOnScreen();
					if (_tentativePoint.distance(_tentativeHelpPoint) < SHAKE_THRESHOLD)
						chooseFontAndScrollIntoView(_tentativeFontLabel);
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// Choose slide
			if (_fontsScrollPane.isScrolling()) {
				_isScrolling = true;
			} else {
				_isScrolling = false;
				_tentativeFontLabel = (JLabel) e.getSource();
				_tentativePoint.x = e.getXOnScreen();
				_tentativePoint.y = e.getYOnScreen();
			}
			
			// Handle inertial scroll event
			_fontsScrollPane.handleMouseEvent(e);	
		}

		@Override
		public void mouseExited(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseClicked(MouseEvent e) {			
//			JLabel fontLabel = (JLabel) e.getSource();
//			chooseFontAndScrollIntoView(fontLabel);
		}
	}
	
	
	private void chooseFontAndScrollIntoView(JLabel fontLabel) {
		_currentFontFamilyLabel.setBackground(Color.BLACK);

		fontLabel.setBackground(Color.GRAY);
		_currentFontFamilyLabel = fontLabel;
		_currentFontFamilyName = fontLabel.getText();
		_button.setFontFamily(_currentFontFamilyName);
		
		// Scroll into view if (partially) not
		Rectangle labelVisibleRect = _currentFontFamilyLabel.getVisibleRect();
		if (labelVisibleRect.height < FONT_LABEL_HEIGHT) {
			Rectangle panelVisibleRect = _fontInnerPanel.getVisibleRect();
			if (labelVisibleRect.y > 0) {
				// Scroll down
				panelVisibleRect.y -= labelVisibleRect.y;
			} else {
				// Scroll up
				panelVisibleRect.y += FONT_LABEL_HEIGHT - labelVisibleRect.height;
			}
			_fontInnerPanel.scrollRectToVisible(panelVisibleRect);
		}
	}

	
}
