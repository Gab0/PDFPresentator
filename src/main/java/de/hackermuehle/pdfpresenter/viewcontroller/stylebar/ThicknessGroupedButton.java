package de.hackermuehle.pdfpresenter.viewcontroller.stylebar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;

import de.hackermuehle.pdfpresenter.viewcontroller.stylebar.StyleBar.StyleBarAlignment;

public class ThicknessGroupedButton extends GroupedButton {
	
	private static final long serialVersionUID = -7363947621645395043L;
	private int _thickness;

	private boolean _doesPaintDragEffect = _thickness > DRAG_THRESHOLD * 2;
	
	public ThicknessGroupedButton(int id, int thickness, int height, StyleBarAlignment alignment) {
		super(id, height, alignment);
				
		setThickness(thickness);
	}
	
	
	public void setThickness(int thickness) {
		_thickness = thickness;
		_doesPaintDragEffect = _thickness > DRAG_THRESHOLD * 2;
		firePropertyChange("thickness", null, _thickness);
		repaint();
	}
	
	
	public int getThickness() {
		return _thickness;
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
		int startPointCoordinate = (_height - _thickness) / 2 + 1;
		
		// Set fill color
		Color color = Color.WHITE;
		Paint gradient;
		if (isEnabled()) {
			if (getModel().isPressed()) {
				if (_doesPaintDragEffect && isDragging()) {
					gradient = new RadialGradientPaint(getMousePoint(), _height / 2, 
							new float[] { 0f, 1f }, new Color[] { color.brighter(), color.darker() } );
				} else {
					gradient = new GradientPaint(0f, 0f, color.brighter(), 
							_height, _height, color.darker().darker());
				}
			} else {
				gradient = new GradientPaint(0f, 0f, color.brighter().brighter(), 
						_height, _height, color.darker());
			}
		} else {
			gradient = new GradientPaint(0f, 0f, Color.LIGHT_GRAY, _height, _height, Color.GRAY);
		}
		g2d.setPaint(gradient);
				
		if (startPointCoordinate > 0) {
			g2d.fillOval(startPointCoordinate, startPointCoordinate, _thickness, _thickness);
		} else {
			g2d.fillRect(0, 0, _height, _height);
			g2d.setPaint(isEnabled() ? Color.BLACK : Color.GRAY);
			g2d.setFont(new Font("Arial", Font.PLAIN, 25));
			g2d.drawString(String.valueOf(_thickness), 6, 29);
		}
		
		if (isDragging()) {
			drawGuidingTriangle(g2d);
		}
		
		// Draw border
		if (_isChosen) {
			g2d.setStroke(new BasicStroke(3));
			g2d.setPaint(isEnabled() ? Color.YELLOW : Color.GRAY);
			g2d.drawOval(1, 1, _height - 2, _height - 2);
		} else if (_thickness < 40) {
			g2d.setStroke(new BasicStroke(2));
			g2d.setPaint(isEnabled() ? new Color(204, 204, 204) : Color.LIGHT_GRAY);
			g2d.drawOval(1, 1, _height - 2, _height - 2);
		}

		g2d.dispose();
	}
	
	
	@Override
	protected void slideInCustomizePopover() {
		new SliderPopover(null, this, getAlignment());	
	}
	
}
