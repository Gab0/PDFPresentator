package de.hackermuehle.pdfpresenter.viewcontroller.stylebar;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;

import de.hackermuehle.pdfpresenter.viewcontroller.stylebar.StyleBar.StyleBarAlignment;

/**
 * Button for style palette. Each has an isChosen state in which it is
 * highlighted. It has popover to customize settings of it. Popover
 * can be activated by either sliding on the button, or double clicking it.
 * 
 * @author shuo
 *
 */
public abstract class GroupedButton extends JButton implements MouseListener, MouseMotionListener {

	private static final long serialVersionUID = -7697716898493830091L;

	public static final int SPACE_BETWEEN_BUTTONS = 10;
	static final int DRAG_THRESHOLD = 10;
	
	protected int _id;
	protected int _height;
	protected boolean _isChosen;
	
	private StyleBarAlignment _alignment;
	private Point _mousePoint;
	private boolean _isDragging;
	
	private int _previousX = 0;
	private int _previousY = 0;
	private boolean _isInCustomizeMode = false;
	
	// Guide triangles
	static private Image _triangleDown;
	static private Image _triangleUp;
	static private Image _triangleLeft;
	static private Image _triangleRight;
	
	static {
		
		// Load guiding triangles:
		try {
			_triangleDown = ImageIO.read(ColorGroupedButton.class.getResource("/triangledown.png"));
			_triangleRight = ImageIO.read(ColorGroupedButton.class.getResource("/triangleright.png"));
			_triangleLeft = ImageIO.read(ColorGroupedButton.class.getResource("/triangleleft.png"));
			_triangleUp = ImageIO.read(ColorGroupedButton.class.getResource("/triangleup.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public GroupedButton(int id, int height, StyleBarAlignment alignment) {
		_id = id;
		_height = height;
		_alignment = alignment;
		
		_mousePoint = new Point(_height, _height);
		
		addMouseListener(this);
		addMouseMotionListener(this);
		
		setMaximumSize(new Dimension(_height + 1, _height + 1));
		setPreferredSize(new Dimension(_height + 1, _height + 1));
		
		setBorderPainted(false);
		setContentAreaFilled(false);
		setFocusPainted(false);
		
		setFocusable(false);
	}
	
	public Image getTriangle(StyleBarAlignment forStylePaletteAlignment) {
		if (forStylePaletteAlignment.equals(StyleBarAlignment.TOP)) {
			return _triangleDown;
		} else if (forStylePaletteAlignment.equals(StyleBarAlignment.LEFT)) {
			return _triangleRight;
		} else if (forStylePaletteAlignment.equals(StyleBarAlignment.RIGHT)) {
			return _triangleLeft;
		} else {
			return _triangleUp;
		}
	}
	
	public void setAlignment(StyleBarAlignment alignment) {
		_alignment = alignment;
	}
	
	public StyleBarAlignment getAlignment() {
		return _alignment;
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

	protected void drawGuidingTriangle(Graphics2D g2d) {
		if (getAlignment().equals(StyleBarAlignment.TOP)) {
			Image triangle = getTriangle(getAlignment());
			if (triangle != null) {
				int x = (int) ((double) (getWidth() - triangle.getWidth(null)) / 2);
				int y = (int) (getHeight() -triangle.getHeight(null) - 4);
				g2d.drawImage(triangle, x, y, null);
			}
		} else if (getAlignment().equals(StyleBarAlignment.LEFT)) {
			Image triangle = getTriangle(getAlignment());
			if (triangle != null) {
				int x = getWidth() - triangle.getWidth(null) - 4;
				int y = (int) ((double) (getHeight() - triangle.getHeight(null)) / 2);
				g2d.drawImage(triangle, x, y, null);
			}
		} else if (getAlignment().equals(StyleBarAlignment.RIGHT)) {
			Image triangle = getTriangle(getAlignment());
			if (triangle != null) {
				int x = 4;
				int y = (int) ((double) (getHeight() - triangle.getHeight(null)) / 2);
				g2d.drawImage(triangle, x, y, null);
			}
		} else {
			Image triangle = getTriangle(getAlignment());
			if (triangle != null) {
				int x = (int) ((double) (getWidth() - triangle.getWidth(null)) / 2);
				int y = 4;
				g2d.drawImage(triangle, x, y, null);
			}
		}
	}

	// Mouse listener
	
	@Override
	public void mouseReleased(MouseEvent e) {
		if (isEnabled()) {
			// Reset drag detection variables
			_previousX = 0;
			_previousY = 0;
			_isInCustomizeMode = false;

			// For drag effect listener
			_mousePoint.x = _height;
			_mousePoint.y = _height;
			_isDragging = false;
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		if (isEnabled()) {
			// Drag detection
			_previousX = e.getXOnScreen();
			_previousY = e.getYOnScreen();
		}
	}
	
	@Override
	public void mouseExited(MouseEvent e) {		
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {		
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if (isEnabled()) {
			if (e.getClickCount() == 2) {
				// Double click detected
				slideInCustomizePopover();
			}
		}
	}
	

	// Mouse motion listener
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if (isEnabled()) {
			int currentX = e.getXOnScreen();
			int currentY = e.getYOnScreen();
			int deltaX = currentX - _previousX;
			int deltaY = currentY - _previousY;

			if (shouldSlideIn(deltaX, deltaY)) {
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
	}
	
	/**
	 * Decide whether the delta surpasses the drag threshold
	 * according to the current alignment
	 * 
	 * @param deltaX
	 * @param deltaY
	 * @return
	 */
	private boolean shouldSlideIn(int deltaX, int deltaY) {
		if (_isInCustomizeMode) return false;
		
		if (_alignment.equals(StyleBarAlignment.TOP))
			return deltaY > DRAG_THRESHOLD;
		else if (_alignment.equals(StyleBarAlignment.LEFT))
			return deltaX > DRAG_THRESHOLD;
		else if (_alignment.equals(StyleBarAlignment.RIGHT))
			return -deltaX > DRAG_THRESHOLD;
		else
			return -deltaY > DRAG_THRESHOLD;
	}
	

	@Override
	public void mouseMoved(MouseEvent e) {}
	
	
	protected abstract void slideInCustomizePopover();
}
