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
import de.hackermuehle.pdfpresenter.model.tools.Laser;


import org.apache.log4j.Logger;

public class LaserPointerMasterInputPanel extends MasterInputPanel {
	private static final long serialVersionUID = 1L;
	private Ellipse2D _laserShape = null;
	private Laser _laser;
	private Collection<Integer> _laserButtons = new LinkedList<Integer>();
	private PropertyChangeListener _laserPropertyChangeListener = new LaserPropertyChangeListener();
	private Point _draggingOrigin = null;
	private Point2D _draggingSourceOrigin = null;

	public LaserPointerMasterInputPanel(Slide slide, Rectangle2D source, Laser laser, Presentation presentation) {
		super(slide, source, presentation);
		_laser = laser;
		_laser.addPropertyChangeListener(_laserPropertyChangeListener);

		_laserButtons.add(MouseEvent.BUTTON1);
		_laserButtons.add(MouseEvent.BUTTON3);

		addMouseListener(new InputPanelMouseListener());
		addMouseMotionListener(new InputPanelMouseMotionListener());
	}

	@Override
	protected void finalize() throws Throwable {
		_laser.removePropertyChangeListener(_laserPropertyChangeListener);
		super.finalize();
	}

    private class LaserPropertyChangeListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getPropertyName().equals("size")) {
                if (_laserShape == null) return;

                double laserSize = getClipping().getInverseTransform().getScaleX() * _laser.getSize();
                _laserShape = new Ellipse2D.Double(_laserShape.getX()-laserSize/2.0, _laserShape.getY()-laserSize/2.0, laserSize, laserSize);
                repaint();
                updateListeners(_laserShape.getBounds2D());
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

        Logger.getLogger(LaserPointerMasterInputPanel.class).warn("OK");
			if (getClipping() == null) return;

			else if (_laserButtons.contains(event.getButton())) {
				Point2D point = new Point2D.Double(event.getX(), event.getY());
				getClipping().getInverseTransform().transform(point, point);
				double laserSize = getClipping().getInverseTransform().getScaleX() * _laser.getSize();


				// Laser has moved, paint at new position:
				_laserShape = new Ellipse2D.Double(point.getX()-laserSize/2.0, point.getY()-laserSize/2.0, laserSize, laserSize);
				if (getClipping() != null) {
					Rectangle2D bounds = _laserShape.getBounds2D();
					repaint(getClipping().getTransform().createTransformedShape(bounds).getBounds());
					updateListeners(bounds);
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent event) {
			_draggingOrigin = null;
			if (_laserShape == null) return;

			if (_laserButtons.contains(event.getButton())) {
				getSlide().concludeAction();

				Rectangle2D bounds = _laserShape.getBounds2D();
				_laserShape = null;

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

			if (_laserShape != null) {
				// TODO: Implement line intersection removal.

				Point2D point = new Point2D.Double(event.getX(), event.getY());
				getClipping().getInverseTransform().transform(point, point);
				double laserSize = getClipping().getInverseTransform().getScaleX() * _laser.getSize();

				Rectangle2D bounds = null;

				if (bounds != null) updateListeners(bounds);

				Rectangle2D oldBounds = _laserShape.getBounds2D();
				_laserShape = new Ellipse2D.Double(point.getX()-laserSize/2.0, point.getY()-laserSize/2.0, laserSize, laserSize);

				if (getClipping() != null) {
					bounds = _laserShape.getBounds2D().createUnion(oldBounds);
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
	
	protected void setLaserButtons(Collection<Integer> buttons) {
		_laserButtons = buttons;
	}
	
	@Override
	protected void paintContent(Graphics2D g2d) {
		if (_laserShape != null) {
			Object originalHintValue = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color color = new Color(255, 20, 20, 200);
        //switch (_laser.getColor()) {
        //case "red":
        //    break;
        //case "green":
        //    color = new Color(20, 210, 20, 100);
        //    break;
        //}

			g2d.setColor(color);
			g2d.fill(_laserShape);

			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, originalHintValue);
		}
	}

	@Override
	public void setPresentation(Presentation presentation) {}


}
