package de.hackermuehle.pdfpresenter.model.annotations;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.text.Document;

/**
 * Immutable text annotation. Makes use of the JTextArea paint()-method.
 */
public final class Text implements Annotation {
	private TransparentTextArea _textArea;
	private AffineTransform _transform;
	
	public Text(Document document, Font font, Color color, AffineTransform transform) {
		// Simplification: Use a text area for rendering:
		_textArea = new TransparentTextArea(document, font);	
    	_textArea.setSize(_textArea.getPreferredSize());
    	_textArea.setForeground(color);
		_transform = (AffineTransform) transform.clone();
	}
	
	public AffineTransform getTransform() {
		return (AffineTransform) _transform.clone();
	}
	
	public String getText() {
		return _textArea.getText();
	}
	
	public Color getColor() {
		return _textArea.getForeground();
	}
	
	@Override
	public void paint(Graphics2D g2d) {
		AffineTransform originalTransform = g2d.getTransform();
		Shape originalClip = g2d.getClip();

		g2d.transform(_transform);
		
		g2d.clip(new Rectangle2D.Double(_textArea.getX(), _textArea.getY(), _textArea.getPreferredSize().getWidth(), _textArea.getPreferredSize().getHeight()));
		_textArea.paint(g2d);
		
		g2d.setTransform(originalTransform);
		g2d.setClip(originalClip);
	}

	@Override
	public Rectangle2D getBounds() {
		Rectangle2D bounds = _transform.createTransformedShape(_textArea.getBounds()).getBounds2D();
		return bounds;
	}

	public Font getFont() {
		return _textArea.getFont();
	}

	@Override
	public boolean contains(Point2D point, double size) {
		Rectangle2D bounds = getBounds();
		bounds.setRect(
				bounds.getX() - size/2.0,
				bounds.getY() - size/2.0, 
				bounds.getWidth() + size, 
				bounds.getHeight() + size);
		
		return bounds.contains(point);
	}

	@Override
	public boolean intersects(Line2D line) {
		return getBounds().intersectsLine(line);
	}
	
	@Override
	public Object clone() {
		Object object = null;
		try {
			object = super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace(System.err);
		}
		((Text) object)._textArea = (TransparentTextArea) new TransparentTextArea(_textArea.getDocument(), _textArea.getFont());
		((Text) object)._textArea.setSize(_textArea.getPreferredSize());
		((Text) object)._textArea.setForeground(_textArea.getForeground());
		((Text) object)._transform = (AffineTransform) _transform.clone();
		return object;
	}
}
