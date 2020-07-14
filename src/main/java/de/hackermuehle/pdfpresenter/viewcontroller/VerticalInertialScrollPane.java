package de.hackermuehle.pdfpresenter.viewcontroller;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;

import de.hackermuehle.pdfpresenter.model.State;

/**
 * Touch optimized JScrollPane. Provides iPhone-esque accelerated scrolling
 * 
 * @author shuo
 *
 */
public class VerticalInertialScrollPane extends JScrollPane {

	private static final long serialVersionUID = 5279475603041788217L;
	
	private JPanel _innerPanel;
	
	// Inertial Scroll
	private int _mouseY = 0;
	private Timer _timer;
	private ScrollTimerActionListener _timerActionListener;
	private boolean _doesOptimizeForPen;
	
	public VerticalInertialScrollMouseListener _verticalInertialScrollMouseListener;
	public VerticalInertialScrollMouseMotionListener _verticalInertialScrollMouseMotionListener;
	
	
	public VerticalInertialScrollPane(JPanel view, final State state) {
		super(view, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		_innerPanel = view;
		_doesOptimizeForPen = state.getOptimizeForPen();
		
		state.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("optimizeForPen"))
					_doesOptimizeForPen = state.getOptimizeForPen();
				
			}
		});
		
		// TODO V2: custom Scrollbar
		// getVerticalScrollBar().setEnabled(false);
		
		_verticalInertialScrollMouseListener = new VerticalInertialScrollMouseListener();
		_verticalInertialScrollMouseMotionListener = new VerticalInertialScrollMouseMotionListener();
		
		addMouseListener(_verticalInertialScrollMouseListener);
		addMouseMotionListener(_verticalInertialScrollMouseMotionListener);
	}
	
	
	public void handleMouseEvent(MouseEvent e) {
		processMouseEvent(e);
	}
	
	public void handleMouseMotionEvent(MouseEvent e) {
		processMouseMotionEvent(e);
	}
	
	public boolean isScrolling() {
		return _timer != null && _timer.isRunning();
	}
	
	class VerticalInertialScrollMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {}

		@Override
		public void mousePressed(MouseEvent e) {
			// Stop scrolling on mouse press
			if (isScrolling()) {
				_timer.stop();
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// Reset y
			_mouseY = 0;
		}
	}
	
	
	private class VerticalInertialScrollMouseMotionListener implements MouseMotionListener {
		@Override
		public void mouseMoved(MouseEvent e) {}

		@Override
		public void mouseDragged(MouseEvent e) {
			// iPhone-style scrolling
			if (_mouseY == 0) {
				_mouseY = e.getYOnScreen();
			} else {
				int verticalDelta = e.getYOnScreen() - _mouseY;
				_mouseY = e.getYOnScreen();
				
				if (_timer == null) {
					_timerActionListener = new ScrollTimerActionListener(verticalDelta);
					_timer = new Timer(18, _timerActionListener);
					_timer.setInitialDelay(0);
					_timer.start();
				} else {
					_timerActionListener.init(verticalDelta);
					_timer.restart();
				}
			}
		}
	}
	
	
	private class ScrollTimerActionListener implements ActionListener {
		double speed = 0;
		boolean doesScrollDownwards;		
		
		private ScrollTimerActionListener(double initialSpeed) {
			init(initialSpeed);
		}
		
		private void init(double initialSpeed) {
			if (_doesOptimizeForPen)
				speed = Math.abs(initialSpeed) < 20 ? initialSpeed : initialSpeed * 2;
			else
				speed = initialSpeed;
			doesScrollDownwards = (initialSpeed <= 0);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			double acceleration = _doesOptimizeForPen ? 1 : 1.5;
			
			if (!doesScrollDownwards) {
				acceleration = -acceleration;
			}

			if (isScrolling()) {
				Rectangle visibleRect = _innerPanel.getVisibleRect();
				visibleRect.y -= speed;
				
				_innerPanel.scrollRectToVisible(visibleRect);
				
				if (visibleRect.y > 0 && visibleRect.y + visibleRect.height 
						< _innerPanel.getHeight())
					speed = speed + acceleration;
				else
					speed = 0;
			} else {
				_timer.stop();
			}
		}
		
		private boolean isScrolling() {
			if (doesScrollDownwards) return speed < 0;
			else return speed > 0;
		}
	};
	
}
