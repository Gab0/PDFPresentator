package de.hackermuehle.pdfpresenter.model.annotations;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JTextArea;
import javax.swing.text.Document;

/**
 * A JTextArea with transparent background (background is not drawn).
 */
public class TransparentTextArea extends JTextArea implements Cloneable {
	private static final long serialVersionUID = 1L;
	
	public TransparentTextArea() {
		super();
        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));
	}
	
	public TransparentTextArea(Document document, Font font) { 
		super(document);
		setFont(font);
        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));
	}
}
