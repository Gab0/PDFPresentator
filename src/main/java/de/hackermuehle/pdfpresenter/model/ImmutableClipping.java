package de.hackermuehle.pdfpresenter.model;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public interface ImmutableClipping extends Cloneable {
	public Rectangle2D getSource();
	public Rectangle getDestination();
	public AffineTransform getTransform();
	public AffineTransform getInverseTransform();
}