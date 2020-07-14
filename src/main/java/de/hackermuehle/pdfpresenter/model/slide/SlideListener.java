package de.hackermuehle.pdfpresenter.model.slide;

import java.awt.geom.Rectangle2D;

/**
 * Listens to updates on a slide that require a redraw.
 */
public interface SlideListener {
	
	/**
	 * The slide listened to requires a redraw.
	 * 
	 * @param rectangle2D The region that needs to be redrawn
	 */
	public void update(Rectangle2D rectangle2D);
}
