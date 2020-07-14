package de.hackermuehle.pdfpresenter.viewcontroller.stylebar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;

/**
 * A graident color block
 * 
 * @author shuo
 *
 */
public class GradientColorComponent extends JComponent {

	private static final long serialVersionUID = 5818655437939929819L;
	private static final Color[] _yellowishColors = new Color[] {
		new Color(255, 255, 0), new Color(255, 204, 102), new Color(255, 255, 102), new Color(204, 255, 102)
	};
	
	private Color _color;
	private Color _originalColor;
	private Color _pressedColor;
	private boolean _isChosen;
	private boolean _isYellowish;
	
	public GradientColorComponent(Color color) {
		_color = color;
		_originalColor = new Color(color.getRed(), color.getGreen(), color.getBlue());
		_pressedColor = color.darker();
		_isYellowish = isYellowishColor(_color);
		
		addMouseListener(new GradientColorMouseListener());
	}
	
	public void setChosen(boolean isChosen) {
		_isChosen = isChosen;
		repaint();
	}
	
	public boolean isChosen() {
		return _isChosen;
	}
	
	
	public void setColor(Color color) {
		_color = color;
		_originalColor = new Color(color.getRed(), color.getGreen(), color.getBlue());;
		repaint();
	}
	
	
	public Color getColor() {
		return _originalColor;
	}
	
	private void setPressed(boolean isPressed) {		
		// Create "pressed" effect
		_color = isPressed ? _pressedColor : _originalColor;
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		
		// Draw gradient
		GradientPaint gradient;
		if (_isChosen) {
			gradient = new GradientPaint(0f, 0f, 
					_color, getWidth(), getHeight(), _color.brighter());
		} else {
			gradient = new GradientPaint(0f, 0f, 
					_color.brighter(), getWidth(), getHeight(), _color);
		}
		
		g2d.setPaint(gradient);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		
		if (_isChosen) {
			// Draw border if chosen
			g2d.setPaint(_isYellowish ? Color.ORANGE : Color.YELLOW);
			g2d.setStroke(new BasicStroke(5));
			// XXX hack, don't know why it clips a pixel at 
			// right and bottom side
			g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
		}
	}
	
	private boolean isYellowishColor(Color color) {
		for (Color yellowishColor : _yellowishColors) {
			if (yellowishColor.equals(color))
				return true;
		}
		return false;
	}
	
	private final class GradientColorMouseListener implements MouseListener {
		@Override
		public void mouseReleased(MouseEvent e) {
			setPressed(false);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			setPressed(true);
		}

		@Override
		public void mouseExited(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseClicked(MouseEvent e) {}
	}

}
