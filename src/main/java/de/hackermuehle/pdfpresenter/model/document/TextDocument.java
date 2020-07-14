package de.hackermuehle.pdfpresenter.model.document;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import de.hackermuehle.pdfpresenter.model.CacheObserver;
import de.hackermuehle.pdfpresenter.model.Clipping;
import de.hackermuehle.pdfpresenter.model.annotations.TransparentTextArea;

/**
 * A TextDocument supports the rendering of standard text files.
 */
public class TextDocument extends Document {
	TransparentTextArea _editorPane = new TransparentTextArea();
	
	public TextDocument(String fileName) throws IOException {
		File file = new File(fileName);
		FileInputStream fileReader = new FileInputStream(file);
		InputStreamReader inputStreamReader = new InputStreamReader(fileReader);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			_editorPane.append(line);
			_editorPane.append("\n");
		}
		fileReader.close();
		bufferedReader.close();
		_editorPane.setSize(_editorPane.getPreferredSize());
		_editorPane.validate();
		_editorPane.repaint();
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

		return _editorPane.getPreferredSize().getWidth() / ((double) _editorPane.getPreferredSize().getHeight());
	}
	
	/**
	 * ImageDocument is not cached, since the rendering process is very fast.
	 * 
	 * @return Always null
	 */
	@Override
	public DocumentCacheEntry cache(int pageNr, Clipping clipping,
			int priority, CacheObserver observer) {
		return null;
	}
	
	@Override
	public void paintContent(Graphics2D g2d, int pageNr, Clipping clipping) {
		if (pageNr < 0 || pageNr >= getNumberOfPages())
			throw new IllegalArgumentException("Illegal page number: " + pageNr);

		// Text:
		g2d.translate(_editorPane.getX(), _editorPane.getY());
		_editorPane.paint(g2d);
		
		g2d.translate(-_editorPane.getX(), -_editorPane.getY());
	}

	@Override
	public Rectangle2D getBounds(int pageNr) {
		if (pageNr < 0 || pageNr >= getNumberOfPages())
			throw new IllegalArgumentException("Illegal page number: " + pageNr);
		
		Rectangle2D pageRect = new Rectangle2D.Double(0, 0, _editorPane.getPreferredSize().getWidth(), _editorPane.getPreferredSize().getHeight());
		return pageRect;
	}
	
	public static boolean isAcceptedFileName(String fileName) {
		for (String format : getAcceptedFileNames()) {
			if (fileName.endsWith(format)) return true; 
		}
		return false;
	}
	
	public static String[] getAcceptedFileNames() {
		return new String[] {"txt", "TXT"};
	}
}
