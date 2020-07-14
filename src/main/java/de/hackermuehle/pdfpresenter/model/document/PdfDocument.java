package de.hackermuehle.pdfpresenter.model.document;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Vector;

import de.hackermuehle.pdfpresenter.model.Clipping;
import de.intarsys.cwt.awt.environment.CwtAwtGraphicsContext;
import de.intarsys.cwt.environment.IGraphicsContext;
import de.intarsys.pdf.content.CSContent;
import de.intarsys.pdf.parser.COSLoadException;
import de.intarsys.pdf.pd.PDDocument;
import de.intarsys.pdf.pd.PDPage;
import de.intarsys.pdf.pd.PDPageTree;
import de.intarsys.pdf.pd.PDResources;
import de.intarsys.pdf.platform.cwt.rendering.CSPlatformRenderer;
import de.intarsys.tools.locator.FileLocator;

/**
 * A PdfDocument supports the rendering of standard PDF files.
 * The rendering is done using the jPod renderer library.
 */
public class PdfDocument extends Document {

	private PDDocument _pdDocument;
	private PDPageTree _pageTree;
	
	// Cache frequently used data:
	private Vector<CSContent> _pageContent;
	private Vector<Rectangle2D> _pageCropBoxes;
	private Vector<Integer> _pageRotations;
	private Vector<PDResources> _pageResources;
	
	public PdfDocument(String fileName) throws IOException, COSLoadException {
		FileLocator locator = new FileLocator(fileName);
		_pdDocument = PDDocument.createFromLocator(locator);
		_pageTree = _pdDocument.getPageTree();
		
		// These vectors cache return values from calls to the jPodRenderer:
		_pageContent = new Vector<CSContent>(getNumberOfPages());
		_pageContent.setSize(getNumberOfPages());
		_pageCropBoxes = new Vector<Rectangle2D>(getNumberOfPages());
		_pageCropBoxes.setSize(getNumberOfPages());
		_pageRotations = new Vector<Integer>(getNumberOfPages());
		_pageRotations.setSize(getNumberOfPages());
		_pageResources = new Vector<PDResources>(getNumberOfPages());
		_pageResources.setSize(getNumberOfPages());
	}
	
	public String getTitle() {
		return _pdDocument.getName();
	}
	
	public int getNumberOfPages() {
		return _pageTree.getCount();
	}
	
	public double getPageRatio(int pageNr) {
		return getBounds(pageNr).getWidth() / getBounds(pageNr).getHeight();
	}
	
	@Override
	public void paintContent(Graphics2D g2dOutput, int pageNr, Clipping clipping) {
		if (pageNr < 0 || pageNr >= getNumberOfPages())
			throw new IllegalArgumentException("Illegal page number: " + pageNr);
			
		// Load cached page data:
		PDPage page = null;
		Integer rotation = _pageRotations.get(pageNr);
		if (rotation == null) {
			if (page == null) page = _pageTree.getPageAt(pageNr);
			rotation = page.getRotate();
			_pageRotations.set(pageNr, rotation);
		}
		Rectangle2D cropBox = _pageCropBoxes.get(pageNr);
		if (cropBox == null) {
			if (page == null) page = _pageTree.getPageAt(pageNr);
			cropBox = page.getCropBox().toNormalizedRectangle();
			_pageCropBoxes.set(pageNr, cropBox);
		}
		CSContent content = _pageContent.get(pageNr);
		if (content == null) {
			if (page == null) page = _pageTree.getPageAt(pageNr);
			content = page.getContentStream();
			_pageContent.set(pageNr, content);
		}
		PDResources resources = _pageResources.get(pageNr);
		if (resources == null) {
			if (page == null) page = _pageTree.getPageAt(pageNr);
			resources = page.getResources();
			_pageResources.set(pageNr, resources);
		}
		
		AffineTransform originalTransform = g2dOutput.getTransform();
		
		g2dOutput.setPaint(Color.WHITE);
		g2dOutput.fill(clipping.getSource());
		
		IGraphicsContext graphics = new CwtAwtGraphicsContext(g2dOutput);

		// Fancy transformation:
		g2dOutput.translate(getBounds(pageNr).getWidth()/2.0, getBounds(pageNr).getHeight()/2.0);
		g2dOutput.scale(1, -1);
		
		g2dOutput.rotate((-(double)rotation) * Math.PI / 180.0);
		g2dOutput.translate(-cropBox.getWidth()/2.0, -cropBox.getHeight()/2.0);
		g2dOutput.translate(-cropBox.getX(), -cropBox.getY());
		
		if (content != null) {
			CSPlatformRenderer renderer = new CSPlatformRenderer(null, graphics);
//			System.out.println("begin render");
			renderer.process(content, resources);
//			System.out.println("end render");
		}
		
		g2dOutput.setTransform(originalTransform);
	}

	@Override
	public Rectangle2D getBounds(int pageNr) {
		if (pageNr < 0 || pageNr >= getNumberOfPages())
			throw new IllegalArgumentException("Illegal page number: " + pageNr);
		
		// Load cached page data:
		PDPage page = null;
		Integer rotation = _pageRotations.get(pageNr);
		if (rotation == null) {
			if (page == null) page = _pageTree.getPageAt(pageNr);
			rotation = page.getRotate();
			_pageRotations.set(pageNr, rotation);
		}
		Rectangle2D cropBox = _pageCropBoxes.get(pageNr);
		if (cropBox == null) {
			if (page == null) page = _pageTree.getPageAt(pageNr);
			cropBox = page.getCropBox().toNormalizedRectangle();
			_pageCropBoxes.set(pageNr, cropBox);
		}
		
		Rectangle2D bounds = (Rectangle2D) cropBox.clone();
		AffineTransform transform = new AffineTransform();
		transform.rotate(-((double)rotation) * Math.PI / 180.0);
		bounds = transform.createTransformedShape(bounds).getBounds2D();
		bounds.setRect(0, 0, bounds.getWidth(), bounds.getHeight());
			
		return bounds;
	}
	
	public static boolean isAcceptedFileName(String fileName) {
		return fileName.toLowerCase().endsWith("pdf");
	}
	
	public static String[] getAcceptedFileNames() {
		return new String[] {"pdf", "PDF"};
	}
}