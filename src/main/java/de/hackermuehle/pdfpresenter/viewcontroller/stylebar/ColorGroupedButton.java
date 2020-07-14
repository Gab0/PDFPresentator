package de.hackermuehle.pdfpresenter.viewcontroller.stylebar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;

import de.hackermuehle.pdfpresenter.viewcontroller.stylebar.StyleBar.StyleBarAlignment;

public class ColorGroupedButton extends GroupedButton {

	private static final long serialVersionUID = -6870929197524648465L;
	private static final Color[] _pureColors = new Color[] { 
		new Color(255, 0, 0),   new Color(0, 255, 0),   // new Color(255, 255, 0), 
		new Color(0, 255, 255), new Color(0, 0, 255),   new Color(255, 0, 255) };
	private static final Color[] _yellowishColors = new Color[] {
		new Color(255, 255, 0), new Color(255, 204, 102), new Color(255, 255, 102), new Color(204, 255, 102)
	};
	
	private Color _color;
	private Color[][] _colorSwatch;
	
	private boolean _isYellowish;
	private boolean _isPure;
		
	
	public ColorGroupedButton(int id, Color color, int height, 
			StyleBarAlignment alignment) {
		super(id, height, alignment);
						
		setColor(color);
		
		initialize();
	}

	public ColorGroupedButton(int id, Color color, int height, Color[][] colorSwatch, 
			StyleBarAlignment alignment) {
		super(id, height, alignment);
				
		setColor(color);
		_colorSwatch = colorSwatch;
		
		initialize();
	}
	
	private void initialize() {
		_isPure = isPureColor(_color);
		_isYellowish = isYellowishColor(_color);
	}
	

	public Color getColor() {
		return _color;
	}
	
	public void setColor(Color color) {
		_color = color;
		_isPure = isPureColor(color);
		_isYellowish = isYellowishColor(color);
		firePropertyChange("color", null, color);
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		
		RenderingHints renderHints =
			new RenderingHints(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
		renderHints.put(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHints(renderHints);
		
		// Draw color
		// Set fill color
		Paint gradient;
		if (isEnabled()) {
			if (getModel().isPressed()) {
				if (isDragging()) {
					gradient = new RadialGradientPaint(getMousePoint(), _height / 2, 
							new float[] { 0f, 1f }, new Color[] { _color, _color.darker() } );
				} else {
					gradient = new GradientPaint(0f, 0f, _color, 
							_height, _height, _color.darker().darker().darker());
				}
			} else {
				if (_color.equals(Color.BLACK)) {
					// Black
					gradient = new GradientPaint(0f, 0f, Color.GRAY, _height, _height, _color);
				} else if (_isPure) {
					gradient = new GradientPaint(0f, 0f, _color.brighter().brighter().brighter().brighter(), 
							_height, _height, _color.darker().darker());
				} else {
					gradient = new GradientPaint(0f, 0f, _color.brighter(), _height, _height, _color);
				}
			}
		} else {
			gradient = new GradientPaint(0f, 0f, Color.LIGHT_GRAY, _height, _height,Color.GRAY);
		}
		g2d.setPaint(gradient);

		g2d.fillOval(0, 0, _height, _height);
		
		if (isDragging()) {
			drawGuidingTriangle(g2d);
		}
		
		// Draw border
		if (_isChosen) {
			g2d.setStroke(new BasicStroke(3));
			if (isEnabled())
				g2d.setPaint(_isYellowish ? Color.ORANGE : Color.YELLOW);
			else 
				g2d.setPaint(Color.GRAY);
			g2d.drawOval(1, 1, _height - 2, _height - 2);
		}
		g2d.dispose();
	}

	

	private boolean isPureColor(Color color) {
		for (Color pureColor : _pureColors) {
			if (pureColor.equals(color))
				return true;
		}
		return false;
	}
	
	private boolean isYellowishColor(Color color) {
		for (Color yellowishColor : _yellowishColors) {
			if (yellowishColor.equals(color))
				return true;
		}
		return false;
	}


	@Override
	protected void slideInCustomizePopover() {
		if (_colorSwatch == null)
			new ColorPopover(null, this, getAlignment());
		else
			new ColorPopover(null, this, getAlignment(), _colorSwatch);
	}
}
