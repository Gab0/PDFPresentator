package de.hackermuehle.pdfpresenter.viewcontroller.stylebar;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.Timer;

import de.hackermuehle.pdfpresenter.PdfPresenter;
import de.hackermuehle.pdfpresenter.viewcontroller.stylebar.StyleBar.StyleBarAlignment;


// TODO BUG program cannot quit while this Dialog is present
public abstract class Popover extends JDialog {
	
//	//-------CONSTANTS---------//
	// Reserved for triangle shape on top of the popover
//	public static final int MENU_WIDTH = 400;
//	
//	public static final int MENU_HEIGHT = 250;
//	// Menu arrow is the arrow on top of or below the menu. It points to the device.
//	public static final int MENU_ARROW_WIDTH = 20;
//	
//	protected static final int MENU_ARROW_BASE_LENGTH = (int) 1.0 * MENU_ARROW_WIDTH / 2;
//	
//	public static final int MENU_ARROW_HEIGHT = (int)((1.0 * MENU_ARROW_WIDTH / 2) * 1.2);
//	
//	public static final int MIN_DISTANCE_TO_BORDER = 3;
//	
//	public static final int BOUND_FOR_ARROW = 20;
//	
//	public static final int MIN_ARROW_DISTANCE_FROM_BORDER = 20;
//	// The distance between the menu edge and inner components
//	public static final int MENU_INSET = 20;
	
	//-------------------------//

	private static final long serialVersionUID = 1446956705190542264L;
	private static final int SLIDE_DISTANCE = 10;
	
	private CloseAndDismissPopoverActionListener _closeAndDismissPopoverActionListener;
	
	private final Timer _timer;
	private JButton _button;
	private StyleBarAlignment _alignment;
	private boolean _isPositioned = false;
	
	/**
	 * 
	 * @param frame
	 * @param button
	 * @param alignment Component.TOP_ALIGNMENT, BOTTOM_ALIGNMENT, LEFT_ALIGNMENT or RIGHT_ALIGNMENT
	 */
	public Popover(JFrame frame, JButton button, StyleBarAlignment alignment) {
		_button = button;
		_alignment = alignment;
				
		setResizable(false);
		setUndecorated(true);
		
		// Dismiss popover when it loses focus
		setModal(false);
		addWindowFocusListener(new PopoverWindowFocusListener());
		
		// Slide in with animation
		_timer = new Timer(5, new SlideInActionListener());
		_timer.start();
		
		
		// TODO V2: beautify
		// Shaped window will be officially supported in Java 7.
		// For Java 6 AWTUtilities provides the same functionalities.
		// http://www.pushing-pixels.org/?p=1209
		// http://download.oracle.com/javase/tutorial/uiswing/misc/trans_shaped_windows.html
				
//		if (AWTUtilities.isTranslucencySupported(Translucency.TRANSLUCENT.PERPIXEL_TRANSPARENT)) {
//			Shape shape = new Ellipse2D.Float(0, 0, panel.getWidth(), panel.getHeight());
//			AWTUtilities.setWindowShape(this, shape);
//		} 
	}
	
	@Override
	public void validate() {
		// TODO Auto-generated method stub
		super.validate();
		
		if (!_isPositioned) {
			int x = 0;
			int y = 0;
			if (_alignment.equals(StyleBarAlignment.TOP)) {
				x = _button.getLocationOnScreen().x + _button.getWidth() / 2 - getWidth() / 2;
				y = _button.getLocationOnScreen().y + _button.getHeight() + 2 - SLIDE_DISTANCE; 
			} else if (_alignment.equals(StyleBarAlignment.LEFT)) {
				x = _button.getLocationOnScreen().x + _button.getWidth() + 2 - SLIDE_DISTANCE;
				if (PdfPresenter.isOnMac()) x += 2;
				y = _button.getLocationOnScreen().y + _button.getHeight() / 2 - getHeight() / 2; 
			} else if (_alignment.equals(StyleBarAlignment.RIGHT)) {
				x = _button.getLocationOnScreen().x - getWidth() - 2 + SLIDE_DISTANCE;
				if (PdfPresenter.isOnMac()) x -= 2;
				y = _button.getLocationOnScreen().y + _button.getHeight() / 2 - getHeight() / 2; 
			} else {
				x = _button.getLocationOnScreen().x + _button.getWidth() / 2 - getWidth() / 2;
				y = _button.getLocationOnScreen().y - getHeight() - 2 + SLIDE_DISTANCE; 
			}
			
			setLocation(x, y);
			_isPositioned = true;
		}
	}



	protected JButton createDoneButton() {
		ImageIcon icon = new ImageIcon(Popover.class.getResource("/saveAndClose.png"));
		JButton doneButton = new JButton(icon);
		doneButton.addActionListener(getCloseAndDismissPopoverActionListener());
		doneButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		ImageIcon pressedIcon = new ImageIcon(Popover.class.getResource("/saveAndClosePressed.png"));
		doneButton.setPressedIcon(pressedIcon);
		doneButton.setBorderPainted(false); 		// For Mac
		doneButton.setContentAreaFilled(false); 	// For Windows

		return doneButton;
	}
	

	private final class SlideInActionListener implements ActionListener {
		private int count = 5;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if (count > 0) {
				doNextStep();
				count--;
			} else {
				_timer.stop();
			}
		}

		private void doNextStep() {
			if (_alignment.equals(StyleBarAlignment.TOP))
				setLocation(getX(), getY() + 2);
			else if (_alignment.equals(StyleBarAlignment.LEFT))
				setLocation(getX() + 2, getY());
			else if (_alignment.equals(StyleBarAlignment.RIGHT))
				setLocation(getX() - 2, getY());
			else // Bottom
				setLocation(getX(), getY() - 2);
		}
	}

	private final class PopoverWindowFocusListener implements
			WindowFocusListener {
		@Override
		public void windowLostFocus(WindowEvent e) {
			dismissPopover();
		}

		@Override
		public void windowGainedFocus(WindowEvent e) {}
	}

	protected class CloseAndDismissPopoverActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Close the popover
			dismissPopover();
		}
	}
	
	
	private void dismissPopover() {
		// Dismiss the popover
		setVisible(false);
		dispose();
	}
	
	protected CloseAndDismissPopoverActionListener getCloseAndDismissPopoverActionListener() {
		if (_closeAndDismissPopoverActionListener == null)
			_closeAndDismissPopoverActionListener = new CloseAndDismissPopoverActionListener();
		
		return _closeAndDismissPopoverActionListener;
	}
	

}