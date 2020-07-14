package de.hackermuehle.pdfpresenter.viewcontroller;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Factory for view elements
 * 
 * @author shuo
 *
 */
public class ViewUtilities {
	
	public static ImageIcon zoom(ImageIcon imI, double width, double height) {
		Image source = imI.getImage();

		BufferedImage dest = new BufferedImage(
				(int) (imI.getIconWidth() * width),
				(int) (imI.getIconHeight() * height),
				BufferedImage.TYPE_INT_RGB);

		AffineTransform ta = new AffineTransform();

		ta.scale(width, height);

		Graphics2D g2d = dest.createGraphics();
		g2d.drawImage(source, ta, null);
		g2d.dispose();

		return new ImageIcon(dest);
	}
	
	
	public static BufferedImage loadImageAndZoom(String path, double width, double height) {
		// Read image from path
		BufferedImage image = null;
	    try {
			image = ImageIO.read(new File(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		if (image != null) {

		
		BufferedImage dest = new BufferedImage(
				(int) width,
				(int) height,
				BufferedImage.TYPE_INT_RGB);

		AffineTransform ta = new AffineTransform();

		ta.scale(width, height);

		Graphics2D g2d = dest.createGraphics();
		g2d.drawImage(image, ta, null);
		g2d.dispose();

		return dest;
	}
	
	/**
	 * Create an ImageIcon object from the given location. <p>
	 * To resize provide a maximum height, the icon will be resized
	 * if the height of the read in image is not equal to the given height.<p>
	 * To have the icon in its original size, set height to smaller than 
	 * or equal to 0.
	 * 
	 * @param location
	 * @param height
	 * @return
	 */
	public static ImageIcon createIcon(String location, int height) {	
		ImageIcon icon = new ImageIcon(ViewUtilities.class.getResource(location));
		
		Image source = icon.getImage();
		if (height > 0 && source.getHeight(null) != height) {
			Image image = source.getScaledInstance(-1, height, Image.SCALE_SMOOTH);
			icon.setImage(image);
		}

		return icon;
	}
	
	
	// Temporary. Before our official solution comes :) 
	public static Image createImage(String location, int maxWidth) {
		Image sourceImage = loadImage(location);
		return zoomImage(sourceImage, maxWidth);
	}
	
	
	public static Image loadImage(String location) {
		// Read image from path
		ImageIcon icon = new ImageIcon(ViewUtilities.class.getResource(location));
		return icon.getImage();
	}
	
	
	public static Image zoomImage(BufferedImage sourceImage, int maxWidth) {
		// Read image from path
		return sourceImage.getScaledInstance(maxWidth, -1, Image.SCALE_SMOOTH);
	}
	
	
	public static Image zoomImage(Image sourceImage, int maxWidth) {
		// Read image from path
		return sourceImage.getScaledInstance(maxWidth, -1, Image.SCALE_SMOOTH);
	}
	
}

