package de.hackermuehle.pdfpresenter.viewcontroller.viewpanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Stroke;

import javax.swing.border.AbstractBorder;

/**
 * A dashed border of 1 pixel width.
 */
public class DashedBorder extends AbstractBorder {
	private static final long serialVersionUID = 1L;

	@Override
	public Insets getBorderInsets(Component c) {
		return new Insets(1, 1, 1, 1);
	}
	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		super.paintBorder(c, g, x, y, width, height);
		
		Graphics2D g2d = (Graphics2D) g;
		
		Stroke stroke = new BasicStroke(0, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 10, 7 }, 0);
		g2d.setStroke(stroke);
		
		//GradientPaint gp = new GradientPaint(10, 10, Color.WHITE, 15, 15, Color.BLACK, true);  
	    //g2d.setPaint(gp);
		g2d.setPaint(new Color(0, 0, 0, 100));
	    g2d.draw(new Rectangle(x, y, width-1, height-1));
	}
}
