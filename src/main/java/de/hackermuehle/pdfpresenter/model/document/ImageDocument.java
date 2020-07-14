package de.hackermuehle.pdfpresenter.model.document;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import de.hackermuehle.pdfpresenter.model.CacheObserver;
import de.hackermuehle.pdfpresenter.model.Clipping;
import de.intarsys.pdf.parser.COSLoadException;

/**
 * An ImageDocument supports the rendering of image files supported by the
 * Java ImageIO.
 */
public class ImageDocument extends Document {
	private BufferedImage _image;
	
	public ImageDocument(String fileName) throws IOException, COSLoadException {
		_image = (BufferedImage) ImageIO.read(new File(fileName));
	}
	
	public String getTitle() {
		return "";
	}
	
	public int getNumberOfPages() {
		return 1;
	}
	
	public double getPageRatio(int pageNr) {
		if (pageNr < 0 || pageNr >= getNumberOfPages())
			throw new IllegalArgumentException("Illegal page number: " + pageNr);

		return _image.getWidth() / ((double) _image.getHeight());
	}
	
	/**
	 * ImageDocument is not cached, since the rendering process is very fast.
	 * 
	 * @return Always null
	 */
	@Override
	public DocumentCacheEntry cache(int pageNr, Clipping clipping, int priority, CacheObserver observer) {
		return null;
	}
	
	@Override
	public void paintContent(Graphics2D g2d, int pageNr, Clipping clipping) {
		if (pageNr < 0 || pageNr >= getNumberOfPages())
			throw new IllegalArgumentException("Illegal page number: " + pageNr);
		
		g2d.drawImage(_image, 0, 0, null);
	}

	@Override
	public Rectangle2D getBounds(int pageNr) {
		if (pageNr < 0 || pageNr >= getNumberOfPages())
			throw new IllegalArgumentException("Illegal page number: " + pageNr);
		
		Rectangle2D pageRect = new Rectangle2D.Double(0, 0, _image.getWidth(), _image.getHeight());
		return pageRect;
	}
	
	public static boolean isAcceptedFileName(String fileName) {
		for (String format : ImageIO.getReaderFormatNames()) {
			if (fileName.endsWith(format)) return true; 
		}
		return false;
	}
	
	public static String[] getAcceptedFileNames() {
		return ImageIO.getReaderFormatNames();
	}
}
