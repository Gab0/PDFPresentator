package de.hackermuehle.pdfpresenter.viewcontroller.viewpanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;

import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.hackermuehle.pdfpresenter.PdfPresenter;
import de.hackermuehle.pdfpresenter.model.Clipping;
import de.hackermuehle.pdfpresenter.model.ImmutableClipping;
import de.hackermuehle.pdfpresenter.model.Presentation;
import de.hackermuehle.pdfpresenter.model.State;
import de.hackermuehle.pdfpresenter.model.annotations.Text;
import de.hackermuehle.pdfpresenter.model.annotations.TransparentTextArea;
import de.hackermuehle.pdfpresenter.model.slide.Slide;
import de.hackermuehle.pdfpresenter.model.tools.TextTool;

public class TextToolMasterInputPanel extends EraserMasterInputPanel{
	private static final long serialVersionUID = 1L;
	private static final int TEXTAREA_MIN_WIDTH = 5;
	private TransparentTextArea _textArea = null;
	private Text _text = null;
	private TextTool _textTool;
	private Point _draggingOrigin = new Point();
	private PropertyChangeListener _textToolChangeListener;
	
	public TextToolMasterInputPanel(Slide slide, Rectangle2D source, TextTool textTool, State controller) {
		super(slide, source, controller.getEraser(), controller.getActivePresentation());
		
		// Configure underlying eraser panel:
		LinkedList<Integer> eraserButtons = new LinkedList<Integer>();
		eraserButtons.add(MouseEvent.BUTTON3);
		setEraserButtons(eraserButtons);
		
		_textTool = textTool;
		setLayout(null);
		setOpaque(false);
		addMouseListener(new InputPanelMouseListener());
		addMouseMotionListener(new InputPanelMouseMotionListener());
		
		_textToolChangeListener = new TextToolChangeListener();
		_textTool.addPropertyChangeListener(_textToolChangeListener);
	}
	
	@Override
	public void removeNotify() {
		
		// Conclude text input as annotation before removal:
		if (_textArea != null)
			insertTextAnnotation(_textArea);
		_textArea = null;
		super.removeNotify();
	}

	@Override
	public void reshape(int x, int y, int width, int height) {
		
		// Conclude text input as annotation before resize:
		if (width != getWidth() || height != getHeight()) {
			if (_textArea != null)
				insertTextAnnotation(_textArea);
			_textArea = null;
		}
		
		super.reshape(x, y, width, height);
	}
	
    private class TextAreaKeyListener implements KeyListener {
        public void keyPressed(KeyEvent e) {
        	
        	// Conclude text input on escape:
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
        		if (_textArea != null)
        			insertTextAnnotation(_textArea);
        		_textArea = null;
            }
        }
        
        public void keyReleased(KeyEvent k) {}

        public void keyTyped(KeyEvent k) {}
    }
    
	class InputPanelMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {}

		/**
		 * Create a new text or edit an existing text:
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			if (getClipping() == null) return;
			
			Point2D.Double point = new Point2D.Double(e.getX(), e.getY());
			getClipping().getInverseTransform().transform(point, point);
			_text = (Text) getSlide().getAnnotation(point, 0, Text.class);
			
			// Insert existing text area as text-annotation:
			if (_textArea != null) {
				
				insertTextAnnotation(_textArea);
				_textArea = null;
				
				if (_text == null) return;
			}
			
			if (e.getButton() != MouseEvent.BUTTON1) return;
			
			if (_text == null) {
				// Create new empty text area for text-input
				// (only if there is no underlying text annotation):
				
				_textArea = insertTextArea("", _textTool.getFont(), _textTool.getColor(), e.getPoint(), true);
				_textArea.requestFocus();
				updateListeners(getTextareaSourceRect(_textArea, getClipping()));
				
				_draggingOrigin = new Point((int) (e.getPoint().getX() - _textArea.getBounds().getX()),(int) (e.getPoint().getY() - _textArea.getBounds().getY())); // Set dragging origin for possible subsequent dragging events.
			}
			else {
				// Create a new textArea to edit an existing annotation:

				float size =(float)( _text.getFont().getSize() * _text.getTransform().getScaleX() * getClipping().getTransform().getScaleX());
				Rectangle2D bounds = getClipping().getTransform().createTransformedShape(_text.getBounds()).getBounds2D();
				Point2D position = new Point2D.Double(bounds.getX(), bounds.getY());

				_textArea = insertTextArea(_text.getText(), _text.getFont().deriveFont(size), _text.getColor(), position, false);
				getSlide().remove(_text);
				updateListeners(getTextareaSourceRect(_textArea, getClipping()));
				//repaint(_textArea.getBounds());
				_textArea.requestFocus();
				_text = null;
				_draggingOrigin = new Point((int) (e.getPoint().getX() - _textArea.getBounds().getX()),(int) (e.getPoint().getY() - _textArea.getBounds().getY())); // Set dragging origin for possible subsequent dragging events.
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {}
	}
	
	class InputPanelMouseMotionListener implements MouseMotionListener {

		@Override
		public void mouseDragged(MouseEvent e) {
			
			// Only drag at left click:
			if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != MouseEvent.BUTTON1_DOWN_MASK)
				return;

			if (_textArea != null) {
				Rectangle2D bounds = getTextareaSourceRect(_textArea, getClipping());
				
				Point point = new Point();
				point.x = (int) (e.getX() - _draggingOrigin.getX());
				point.y = (int) (e.getY() - _draggingOrigin.getY());

				_textArea.setLocation(point);
				
				bounds.add(getTextareaSourceRect(_textArea, getClipping()));
				updateListeners(bounds);
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {}
	}
	
	class TextAreaCaretListener implements CaretListener {
		@Override
		public void caretUpdate(CaretEvent e) {
			
			// Bug ID: 4705104 (causes deadlock on Mac OS X):
			fitTextAreaSizeToText();
		}
	}
	
	// Workaround for Bug ID: 4705104 (see TextAreaCaretListener):
	class WORKAROUND_4705104_TextAreaCaretListener implements CaretListener {

		@Override
		public void caretUpdate(CaretEvent e) {
			if (getClipping() == null) return;
			
			Rectangle2D bounds = getTextareaSourceRect(_textArea, getClipping());

			bounds.add(getTextareaSourceRect(_textArea, getClipping()));
			updateListeners(bounds);
		}
	}
	
	// Workaround for Bug ID: 4705104 (see TextAreaCaretListener):
	class WORKAROUND_4705104_TextAreaDocumentListener implements DocumentListener {

		@Override
		public void changedUpdate(DocumentEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					fitTextAreaSizeToText();
				}});
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					fitTextAreaSizeToText();
				}});
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					fitTextAreaSizeToText();
				}});
		}
	}
	
	class TextToolChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals("font")) {
				if (_textArea != null) {
					Rectangle2D bounds = getTextareaSourceRect(_textArea, getClipping());
					
					_textArea.setFont(_textTool.getFont());
					Dimension size = _textArea.getPreferredSize();
					if (size.width < TEXTAREA_MIN_WIDTH) size.width = TEXTAREA_MIN_WIDTH;
					else size.width += 1;
					_textArea.setSize(size);
					
					bounds.add(getTextareaSourceRect(_textArea, getClipping()));
					updateListeners(bounds);
				}
			}
			else if (e.getPropertyName().equals("color")) {
				if (_textArea != null) {
					_textArea.setForeground(_textTool.getColor());
					
					updateListeners(getTextareaSourceRect(_textArea, getClipping()));
				}	
			}
		}
	}
	
	@Override
	public void paintSlave(Graphics2D g2d, Clipping clipping) {
		super.paintSlave(g2d, clipping);
		
		if (_textArea != null && getClipping() != null) {
			AffineTransform originalTransform = g2d.getTransform();
			Shape originalClip = g2d.getClip();
			
			g2d.transform(clipping.getTransform());
			g2d.transform(getClipping().getInverseTransform());
			g2d.translate(_textArea.getX(), _textArea.getY());
			
			g2d.clip(new Rectangle2D.Double(0, 0, _textArea.getSize().getWidth() + 1, _textArea.getSize().getHeight() + 1));
			_textArea.paint(g2d);
			
			g2d.setTransform(originalTransform);
			g2d.setClip(originalClip);
		}
	}
	
	private void fitTextAreaSizeToText() {
		if (getClipping() == null) return;
		
		Rectangle2D bounds = getTextareaSourceRect(_textArea, getClipping());

		// Known Java Bug ID: 4705104. Causes deadlock on Mac OS X when called
		// from within a caret listener.
		// See http://bugs.sun.com/view_bug.do?bug_id=4705104 for details.
		Dimension size = _textArea.getPreferredSize(); 
		
		if (size.width < TEXTAREA_MIN_WIDTH) size.width = TEXTAREA_MIN_WIDTH;
		else size.width += 1;
		
		if (!size.equals(_textArea.getPreferredSize()))
			_textArea.setSize(size);
		
		bounds.add(getTextareaSourceRect(_textArea, getClipping()));
		updateListeners(bounds);
	}
	
	private Rectangle2D getTextareaSourceRect(TransparentTextArea textArea, ImmutableClipping clipping) {
		if (clipping != null) return clipping.getInverseTransform().createTransformedShape(textArea.getBounds()).getBounds2D();
		else return new Rectangle(0, 0);
	}
	
	private void insertTextAnnotation(TransparentTextArea textArea) {
		Rectangle2D bounds = getTextareaSourceRect(textArea, getClipping());
		
		// Remove textArea:
		remove(textArea);	
		
		// Insert into slide if not empty:	
		if (!textArea.getText().equals("")) {
			
			AffineTransform transform = getClipping().getInverseTransform();
			transform.translate(_textArea.getX(), _textArea.getY());
			if (_textArea.getBorder() != null) 
				transform.translate(-_textArea.getBorder().getBorderInsets(_textArea).left, -_textArea.getBorder().getBorderInsets(_textArea).top);
			getSlide().insert(new Text(textArea.getDocument(), textArea.getFont(), _textArea.getForeground(), transform));
			
			getSlide().concludeAction();
		}
		repaint(textArea.getBounds());
		
		textArea = null;
		updateListeners(bounds);
		return;
	}
	
	private TransparentTextArea insertTextArea(String text, Font font, Color color, Point2D point, boolean center) {
		TransparentTextArea textArea = new TransparentTextArea();
		textArea.addKeyListener(new TextAreaKeyListener());
		
		textArea.setFont(font);
		textArea.setForeground(color);
		
		// Add a small border:
		textArea.setBorder(new DashedBorder());
		point.setLocation(point.getX() + textArea.getBorder().getBorderInsets(textArea).left, point.getY() + textArea.getBorder().getBorderInsets(textArea).top);
		
		// Higher precision calculation needed: 
		textArea.getDocument().putProperty("i18n", Boolean.TRUE);
		textArea.setText(text);
		
		// Set position and size:
		Dimension size = textArea.getPreferredSize();
		if (size.width < TEXTAREA_MIN_WIDTH) size.width = TEXTAREA_MIN_WIDTH;
		else size.width += 1;
		textArea.setSize(size);
		
		if (!center)
			textArea.setLocation((int)point.getX(), (int)point.getY());
		else
			textArea.setLocation((int)point.getX(), (int)(point.getY() - size.getHeight()/2));
		
		// Add listeners and inform slave panels about changes:
		if (PdfPresenter.isOnMac()) {

			// Workaround for Bug ID: 4705104 (see TextAreaCaretListener):
			textArea.getDocument().addDocumentListener(new WORKAROUND_4705104_TextAreaDocumentListener());
			textArea.addCaretListener(new WORKAROUND_4705104_TextAreaCaretListener());
		}
		else {
			textArea.addCaretListener(new TextAreaCaretListener());
		}
		
		add(textArea);
		return textArea;
	}
	
	@Override
	public void setSlide(Slide slide) {
		// Conclude text input as annotation before removal:
		if (_textArea != null)
			insertTextAnnotation(_textArea);
		_textArea = null;
		
		super.setSlide(slide);
	}
	
	@Override
	public void setSource(Rectangle2D source) {
		// Conclude text input as annotation before scaling:
		if (_textArea != null)
			insertTextAnnotation(_textArea);
		_textArea = null;
		
		super.setSource(source);
	}
	
	@Override
	public void setPresentation(Presentation presentation) {}
}
