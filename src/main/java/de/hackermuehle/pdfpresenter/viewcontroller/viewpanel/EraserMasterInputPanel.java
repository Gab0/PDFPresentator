package de.hackermuehle.pdfpresenter.viewcontroller.viewpanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.LinkedList;

import de.hackermuehle.pdfpresenter.model.Presentation;
import de.hackermuehle.pdfpresenter.model.annotations.Annotation;
import de.hackermuehle.pdfpresenter.model.slide.Slide;
import de.hackermuehle.pdfpresenter.model.tools.Eraser;

public class EraserMasterInputPanel extends MasterInputPanel {
	private static final long serialVersionUID = 1L;
	private Ellipse2D _eraserShape = null;
	private Eraser _eraser;
	private Collection<Integer> _eraserButtons = new LinkedList<Integer>();
	private PropertyChangeListener _eraserPropertyChangeListener = new EraserPropertyChangeListener();
	private Point _draggingOrigin = null;
	private Point2D _draggingSourceOrigin = null;
	
	public EraserMasterInputPanel(Slide slide, Rectangle2D source, Eraser eraser, Presentation presentation) {
		super(slide, source, presentation);
		_eraser = eraser;
		_eraser.addPropertyChangeListener(_eraserPropertyChangeListener);
		_eraserButtons.add(MouseEvent.BUTTON1);
    // FIXME
		_eraserButtons.add(MouseEvent.BUTTON3);
		addMouseListener(new InputPanelMouseListener());
		addMouseMotionListener(new InputPanelMouseMotionListener());
	}

	@Override
	protected void finalize() throws Throwable {
		_eraser.removePropertyChangeListener(_eraserPropertyChangeListener);
		super.finalize();
	}
	
	private class EraserPropertyChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getPropertyName().equals("size")) {
				if (_eraserShape == null) return;

				double eraserSize = getClipping().getInverseTransform().getScaleX() * _eraser.getSize();
				_eraserShape = new Ellipse2D.Double(_eraserShape.getX()-eraserSize/2.0, _eraserShape.getY()-eraserSize/2.0, eraserSize, eraserSize);
				repaint();
				updateListeners(_eraserShape.getBounds2D());
			}
		}
	}

	private class InputPanelMouseListener implements MouseListener {

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
			if (getClipping() == null) return;

			if (_eraserButtons.contains(event.getButton()) &&
				(event.getModifiers() & MouseEvent.CTRL_MASK) == MouseEvent.CTRL_MASK) {

				if ((_presentation.getActiveSlide() != null) &&
					(!_presentation.getSource().equals(getSlide().getSize()))) {
					Rectangle2D source = _presentation.getSource();

					_draggingSourceOrigin = new Point2D.Double(source.getX() , source.getY());
					_draggingOrigin = event.getPoint();
				}
			}
			else if (_eraserButtons.contains(event.getButton())) {
				Point2D point = new Point2D.Double(event.getX(), event.getY());
				getClipping().getInverseTransform().transform(point, point);
				double eraserSize = getClipping().getInverseTransform().getScaleX() * _eraser.getSize();

				// Remove only the topmost annotation (only on mouse pressed):
				Annotation annotation;
				if ((annotation = getSlide().getAnnotation(point, eraserSize, Annotation.class)) != null) {
					getSlide().remove(annotation);
				}

				// Eraser has moved, paint at new position:
				_eraserShape = new Ellipse2D.Double(point.getX()-eraserSize/2.0, point.getY()-eraserSize/2.0, eraserSize, eraserSize);
				if (getClipping() != null) {
					Rectangle2D bounds = _eraserShape.getBounds2D();
					repaint(getClipping().getTransform().createTransformedShape(bounds).getBounds());
					updateListeners(bounds);
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent event) {
			_draggingOrigin = null;
			if (_eraserShape == null) return;

			if (_eraserButtons.contains(event.getButton())) {
				getSlide().concludeAction();

				Rectangle2D bounds = _eraserShape.getBounds2D();
				_eraserShape = null;

				if (getClipping() != null) {
					repaint(getClipping().getTransform().createTransformedShape(bounds).getBounds());
					updateListeners(bounds);
				}
			}
		}
	}

	private class InputPanelMouseMotionListener implements MouseMotionListener {

		@Override
		public void mouseDragged(MouseEvent event) {
			if (getClipping() == null) return;

			if (_eraserShape != null) {
				// TODO: Implement line intersection removal.

				Point2D point = new Point2D.Double(event.getX(), event.getY());
				getClipping().getInverseTransform().transform(point, point);
				double eraserSize = getClipping().getInverseTransform().getScaleX() * _eraser.getSize();

				Rectangle2D bounds = null;
				Annotation annotation;
				while ((annotation = getSlide().getAnnotation(point, eraserSize, Annotation.class)) != null) {
					getSlide().remove(annotation);
					if (bounds == null) bounds = annotation.getBounds().getBounds();
					else bounds.add(annotation.getBounds().getBounds());
				}
				if (bounds != null) updateListeners(bounds);

				Rectangle2D oldBounds = _eraserShape.getBounds2D();
				_eraserShape = new Ellipse2D.Double(point.getX()-eraserSize/2.0, point.getY()-eraserSize/2.0, eraserSize, eraserSize);

				if (getClipping() != null) {
					bounds = _eraserShape.getBounds2D().createUnion(oldBounds);
					repaint(getClipping().getTransform().createTransformedShape(bounds).getBounds());
					updateListeners(bounds);
				}
			}
			else if (((event.getModifiers() & MouseEvent.CTRL_MASK) == MouseEvent.CTRL_MASK) &&
					 (_draggingOrigin != null)) {

				if ((_presentation.getActiveSlide() != null) &&
					(!_presentation.getSource().equals(getSlide().getSize()))) {

					Point2D point = new Point2D.Double(event.getX() - _draggingOrigin.getX() + getClipping().getDestination().getX(),event.getY() - _draggingOrigin.getY() + getClipping().getDestination().getY());
					getClipping().getInverseTransform().transform(point, point);

					Rectangle2D source = _presentation.getSource();
					source.setFrame(source.getX() + _draggingSourceOrigin.getX() - point.getX(), source.getY() + _draggingSourceOrigin.getY() - point.getY(), source.getWidth(), source.getHeight());

					// The rectangle must be included in the source:
					source.setRect(Math.max(source.getX(), 0), Math.max(source.getY(), 0), source.getWidth(), source.getHeight());
					double dx = source.getX() + source.getWidth() - getSlide().getSize().getX() - getSlide().getSize().getWidth();
					if (dx > 0) {
						source.setRect(source.getX() - dx, source.getY(), source.getWidth(), source.getHeight());
					}
					double dy = source.getY() + source.getHeight() - getSlide().getSize().getY() - getSlide().getSize().getHeight();
					if (dy > 0) {
						source.setRect(source.getX(), source.getY() - dy, source.getWidth(), source.getHeight());
					}
					source.setRect(Math.round(source.getX()*1000)/1000.0, Math.round(1000*source.getY()) / 1000.0 , Math.round(1000*source.getWidth()) / 1000.0, Math.round(1000*source.getHeight()) / 1000.0);
					_presentation.setSource(source);
				}
			}
		}

		@Override
		public void mouseMoved(MouseEvent event) {}
	}
	
	protected void setEraserButtons(Collection<Integer> buttons) {
		_eraserButtons = buttons;
	}
	
	@Override
	protected void paintContent(Graphics2D g2d) {
		if (_eraserShape != null) {
			Object originalHintValue = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g2d.setColor(new Color(0, 0, 0, 100));
			g2d.fill(_eraserShape);

			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, originalHintValue);
		}
	}

	@Override
	public void setPresentation(Presentation presentation) {}
}
