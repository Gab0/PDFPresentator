package de.hackermuehle.pdfpresenter.viewcontroller.viewpanel;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.hackermuehle.pdfpresenter.PdfPresenter;
import de.hackermuehle.pdfpresenter.model.Presentation;
import de.hackermuehle.pdfpresenter.model.State;
import de.hackermuehle.pdfpresenter.model.slide.Slide;
import de.hackermuehle.pdfpresenter.model.tools.Tool;

public class MagnifierMasterInputPanel extends MasterInputPanel {
	private static final long serialVersionUID = 1L;
	private static final double MAX_ZOOM = 4;
	private Point2D _point = null;
	private Rectangle2D _rectangle = null;
	private Presentation _presentation = null;
	private Tool _tool = null;
	private State _controller = null;
	private PropertyChangeListener _controllerPropertyChangeListener = new ControllerPropertyChangeListener();
	
	public MagnifierMasterInputPanel(Slide slide, Rectangle2D source, Presentation presentation, Tool tool, State controller) {
		super(slide, source, controller.getActivePresentation());
		_presentation = presentation;
		_presentation.addPropertyChangeListener(_controllerPropertyChangeListener);
		_tool = tool;
		_controller = controller;
		addMouseListener(new InputPanelMouseListener());
		addMouseMotionListener(new InputPanelMouseMotionListener());
	}
	
	@Override
	protected void finalize() throws Throwable {
		_presentation.removePropertyChangeListener(_controllerPropertyChangeListener);
		super.finalize();
	}
	
	private class ControllerPropertyChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getPropertyName().equals("property")) {
				// TODO: No usage? -> remove this listener.
			}
		}
	}
	
	class InputPanelMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent event) {
		}

		@Override
		public void mouseEntered(MouseEvent event) {
		}

		@Override
		public void mouseExited(MouseEvent event) {
		}

		@Override
		public void mousePressed(MouseEvent event) {
			if (event.getButton() == MouseEvent.BUTTON1) {
				_point = new Point2D.Double(event.getX(), event.getY());
				getClipping().getInverseTransform().transform(_point, _point);
		
				_rectangle = new Rectangle2D.Double(_point.getX(), _point.getY(), 0, 0);
				
				repaint(getClipping().getTransform().createTransformedShape(_rectangle).getBounds());
				updateListeners(_rectangle);
			} else {
				_presentation.setSource(getSlide().getSize());
			}
		}

		@Override
		public void mouseReleased(MouseEvent event) {
			if (event.getButton() != MouseEvent.BUTTON1) return;
			
			// Set new source rect:
			Rectangle2D source = _rectangle;//.createIntersection(getSlide().getSize());
			source = normalizeSource(source);
			
			// No valid rectangle selected, just zoom in a bit:
			if (source.getWidth() == 0 || source.getHeight() == 0) {
				return; // TODO: Zoom in.
			}
			
			// Limit zooming factor:
			if (source.getWidth() < getSlide().getSize().getWidth() / MAX_ZOOM)  {
				double width = getSlide().getSize().getWidth() / MAX_ZOOM;
				double height = source.getHeight() * source.getWidth() / width;
				source.setFrame(source.getX(), source.getY(), width, height);
			}
			
			if (source.getHeight() < getSlide().getSize().getHeight() / MAX_ZOOM)  {
				double height = getSlide().getSize().getHeight() / MAX_ZOOM;
				double width = source.getWidth() * source.getHeight() / height;
				source.setFrame(source.getX(), source.getY(), width, height);
			}
			source = normalizeSource(source);
			source.setRect(Math.round(source.getX()*1000)/1000.0, Math.round(1000*source.getY()) / 1000.0 , Math.round(1000*source.getWidth()) / 1000.0, Math.round(1000*source.getHeight()) / 1000.0);
			
			_presentation.setSource(source);
			_controller.setActiveTool(_tool);
			
			repaint(getClipping().getTransform().createTransformedShape(_rectangle).getBounds());
			updateListeners(_rectangle);
			_rectangle = null;
		}
	}

	class InputPanelMouseMotionListener implements MouseMotionListener {

		@Override
		public void mouseDragged(MouseEvent event) {
			if (_rectangle == null) return;
			
			Rectangle2D oldRectangle = _rectangle;
			Point2D point = new Point2D.Double(event.getX(), event.getY());
			getClipping().getInverseTransform().transform(point, point);
			
			_rectangle = new Rectangle2D.Double(Math.min(point.getX(), _point.getX()), Math.min(point.getY(), _point.getY()), Math.abs(_point.getX() - point.getX()), Math.abs(_point.getY() - point.getY()));
			oldRectangle.add(_rectangle);
			repaint();//getClipping().getTransform().createTransformedShape(oldRectangle).getBounds());
			updateListeners(oldRectangle);
		}

		@Override
		public void mouseMoved(MouseEvent event) {
		}
	}
	
	public void setPresentation(Presentation presentation) {
		_presentation = presentation;
	}
	
	@Override
	protected void paintContent(Graphics2D g2d) {
		if (_rectangle != null) {
			g2d.setColor(new Color(0, 0, 0, 80));
			//g2d.setStroke(new BasicStroke(0));
			g2d.fill(_rectangle);
			
			//g2d.setColor(new Color(220, 0, 0,33));
			//g2d.fill(normalizeSource(_rectangle));
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// Draw short info text on how to use the magnifier:
		Graphics2D g2d = (Graphics2D) g;
		Composite originalComposite = g2d.getComposite();
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
	
		g2d.setColor(Color.BLACK);
		
		// Backup
		RenderingHints oldRenderingHints = g2d.getRenderingHints();
		
		// Turn on antialiasing, highest quality rendering
		RenderingHints renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHints(renderHints);
		
		String hintText = PdfPresenter.getLocalizedString("mmDragHint");
		FontMetrics fontMetrics = g2d.getFontMetrics();
		int width = fontMetrics.stringWidth(hintText);
		int height = fontMetrics.getHeight();
		
		int x = (getWidth() - width) / 2; // centered
		int y = getHeight() - height - 40; // bottom
		
		g2d.fillRoundRect(x - 10, y, width + 20, height + 10, 8, 8);
		
		g2d.setComposite(originalComposite);
		
		g2d.setColor(Color.WHITE);
		g2d.drawString(hintText, x, y + height + 1);
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.drawRoundRect(x - 10, y, width + 20, height + 10, 8, 8);
		
		// Restore original rendering hints
		g2d.setRenderingHints(oldRenderingHints);
	}
}
