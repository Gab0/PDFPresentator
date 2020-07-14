package de.hackermuehle.pdfpresenter.viewcontroller.stylebar;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.Icon;
import javax.swing.JButton;

/**
 * Maybe in V2
 * 
 * @author shuo
 *
 */
public abstract class PopoverableToggleButton extends JButton implements MouseListener, MouseMotionListener {

	private static final long serialVersionUID = -7697716898493830091L;

	public static final int SPACE_BETWEEN_BUTTONS = 10;
	static final int DRAG_THRESHOLD = 10;
	
	protected int _id;
	protected int _height;
	protected boolean _isChosen;
	
	private Point _mousePoint;
	private boolean _isDragging;
	
	private int _previousY = 0;
	private boolean _isInCustomizeMode = false;
	
	public PopoverableToggleButton(Icon icon) {
		super(icon);
		
		_mousePoint = new Point(_height, _height);
		
		addMouseListener(this);
		addMouseMotionListener(this);
		
//		setMaximumSize(new Dimension(_height + 1, _height + 1));
//		setPreferredSize(new Dimension(_height + 1, _height + 1));
	}
	
	public PopoverableToggleButton(int id, int height) {
		_id = id;
		_height = height;
		
		_mousePoint = new Point(_height, _height);
		
		addMouseListener(this);
		addMouseMotionListener(this);
		
		setMaximumSize(new Dimension(_height + 1, _height + 1));
		setPreferredSize(new Dimension(_height + 1, _height + 1));
		
		setBorderPainted(false);
		setContentAreaFilled(false);
		setFocusPainted(false);
	}
	
	public Point getMousePoint() {
		return _mousePoint;
	}
	
	public boolean isDragging() {
		return _isDragging;
	}
	
	
	public int getId() {
		return _id;
	}
	
	
	public void setId(int id) {
		_id = id;
	}
	
	
	public boolean isChosen() {
		return _isChosen;
	}
	
	
	public void setChosen(boolean isChosen) {
		_isChosen = isChosen;
	}



	// Mouse listener
	
	@Override
	public void mouseReleased(MouseEvent e) {
		// Reset drag detection variables
		_previousY = 0;
		_isInCustomizeMode = false;
		
		// For drag effect listener
		_mousePoint.x = _height;
		_mousePoint.y = _height;
		_isDragging = false;
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		// Drag detection
		_previousY = e.getYOnScreen();
	}
	
	@Override
	public void mouseExited(MouseEvent e) {		
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {		
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			// Double click detected
			slideInCustomizePopover();
		}
	}
	

	// Mouse motion listener
	
	@Override
	public void mouseDragged(MouseEvent e) {
		int currentY = e.getYOnScreen();
		int delta = Math.abs(currentY - _previousY);
		
		if (!_isInCustomizeMode && delta > DRAG_THRESHOLD) {
			// Drag detected
			_isInCustomizeMode = true;
			// Fire action so that action listener will react and
			// set this button as chosen
			fireActionPerformed(new ActionEvent(this, 0, "chosen"));
			slideInCustomizePopover();
		}
		
		// For drag effect listener
		_isDragging = true;
		_mousePoint.x = e.getX();
		_mousePoint.y = e.getY();
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {		
	}
	
	
	protected abstract void slideInCustomizePopover();
}
