package de.hackermuehle.pdfpresenter.viewcontroller.stylebar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;

import de.hackermuehle.pdfpresenter.model.State;
import de.hackermuehle.pdfpresenter.viewcontroller.stylebar.StyleBar.StyleBarAlignment;

public class FontFamilyGroupedButton extends GroupedButton {

	private static final long serialVersionUID = 3284491094572395341L;

	public static final int FONT_SIZE = 35;
	private static final int BUTTON_WIDTH = 40;
	
	private Font _font;
	
	private State _controller;
	
	public FontFamilyGroupedButton(int id, String fontFamilyName, int height, StyleBarAlignment alignment, State controller) {
		super(id, height, alignment);
		
		_controller = controller;

		_font = new Font(fontFamilyName, Font.PLAIN, FONT_SIZE);
		setPreferredSize(new Dimension(BUTTON_WIDTH , BUTTON_WIDTH));
	}
	
	
	public String getFontFamily() {
		return _font.getFamily();
	}
	
	
	public void setFontFamily(String fontFamily) {
		_font = new Font(fontFamily, Font.PLAIN, FONT_SIZE);
		firePropertyChange("fontFamily", null, _font);
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
		
		// Draw background
		// Set fill color
		Paint gradient;
		if (isEnabled()) {
			if (getModel().isPressed()) {
				if (isDragging()) {
					gradient = new RadialGradientPaint(getMousePoint(), BUTTON_WIDTH / 2, 
							new float[] { 0f, 1f }, new Color[] { Color.WHITE.darker(), Color.LIGHT_GRAY.darker() } );
				} else {
					gradient = new GradientPaint(0f, 0f, Color.WHITE.darker(), 
							_height, _height, Color.LIGHT_GRAY);
				}
			} else {
				gradient = new GradientPaint(0f, 0f, Color.WHITE, 
						_height, _height, Color.LIGHT_GRAY);
			}
		} else {
			gradient = new GradientPaint(0f, 0f, Color.LIGHT_GRAY, _height, _height, Color.GRAY);
		}
		g2d.setPaint(gradient);

		g2d.fillRoundRect(0, 0, BUTTON_WIDTH, _height, 5, 5);
		
		String text = "Aa";
		g2d.setFont(_font.deriveFont(25f));
		g2d.setPaint(isEnabled() ? Color.BLACK : Color.GRAY);
		
		// Draw text
		// Measure the font and the message
		FontRenderContext renderContext = g2d.getFontRenderContext();
		Rectangle2D bounds = _font.getStringBounds(text, renderContext);
		LineMetrics metrics = _font.getLineMetrics(text, renderContext);
		
		float textWidth = (float) (bounds.getWidth() + bounds.getY() / 2);
		g2d.drawString(text, (BUTTON_WIDTH - textWidth) / 2f, 
				(_height + metrics.getHeight()) / 2f - metrics.getDescent() - 1f);

		if (isDragging()) {
			drawGuidingTriangle(g2d);
		}
		
		// Draw border
		if (_isChosen) {
			g2d.setStroke(new BasicStroke(3));
			g2d.setColor(isEnabled() ? Color.YELLOW : Color.GRAY);
			g2d.drawRoundRect(1, 1, BUTTON_WIDTH - 3, _height - 3, 5, 5);
		}
		
				
		g2d.dispose();
	}


	@Override
	protected void slideInCustomizePopover() {
		new FontFamilyPopover(null, this, getAlignment(), _controller);	
	}

}
