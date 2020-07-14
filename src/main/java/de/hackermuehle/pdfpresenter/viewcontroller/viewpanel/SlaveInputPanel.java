package de.hackermuehle.pdfpresenter.viewcontroller.viewpanel;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;

import de.hackermuehle.pdfpresenter.model.Clipping;
import de.hackermuehle.pdfpresenter.model.Presentation;
import de.hackermuehle.pdfpresenter.model.slide.Slide;
import de.hackermuehle.pdfpresenter.model.slide.SlideListener;

/**
 * A slave input panel receives listens to changes in the visualization of the
 * master input panel and to copies these changes (i.e it becomes a visual
 * clone of the master input panel).
 */
public class SlaveInputPanel extends InputViewPanel {
	private static final long serialVersionUID = 1L;
	private Rectangle2D _source;
	private Clipping _clipping;
	private MasterInputPanel _masterInputPanel;
	private SlideListener _slaveInputPanelGraphicsListener;
	
	/**
	 * Constructs and initializes an SlaveInputPanel.
	 * 
	 * @param masterInputPanel {@link MasterInputPanel} to "clone"
	 * @param source Source rectangle to "clone"
	 */
	public SlaveInputPanel(MasterInputPanel masterInputPanel, Rectangle2D source) {
		_masterInputPanel = masterInputPanel;
		if (_masterInputPanel != null)
			_masterInputPanel.addListener(this);
		
		_source = source;
		_clipping = null;

		setLayout(null);
		setOpaque(false);
		
		addComponentListener(new SlaveInputPanelComponentListener());
	}
	
	@Override
	public void finalize() {
		if (_masterInputPanel != null)
			_masterInputPanel.removeListener(this);
	}
	
	class SlaveInputPanelComponentListener implements ComponentListener {
		@Override
		public void componentHidden(ComponentEvent event) {}
		
		@Override
		public void componentMoved(ComponentEvent event) {}
		
		@Override
		public void componentResized(ComponentEvent event) {
			try {
				_clipping = createClipping(_source, event.getComponent().getWidth(), event.getComponent().getHeight());
			} catch (NoninvertibleTransformException e) {
				_clipping = null;
			}
		}
		
		@Override
		public void componentShown(ComponentEvent event) {}
	}
	
	public void update(Rectangle2D r) {
		if (_clipping != null)
			repaint(_clipping.getTransform().createTransformedShape(r).getBounds());
	}
	
	public void setMaster(MasterInputPanel masterInputPanel) {
		if (_masterInputPanel != null) 
			_masterInputPanel.removeListener(this);
		_masterInputPanel = masterInputPanel;
		if (_masterInputPanel != null)
			_masterInputPanel.addListener(this);
	}
	
	public void setSource(Rectangle2D source) {
		_source = source;
		try {
			_clipping =  createClipping(_source);
		} catch (NoninvertibleTransformException e) {
			_clipping = null;
		}
	}
	
	public void paintComponent(Graphics g) {
		if (_clipping == null || _masterInputPanel == null)  return;
		
		Graphics2D g2d = (Graphics2D) g;
		_masterInputPanel.paintSlave(g2d, _clipping);
		
		// Only for debugging purposes:
		//g2d.setPaint(new Color((int)(Math.random()*255),0,0));
		//g2d.fill(g2d.getClip());
	}

	@Override
	public void setPresentation(Presentation presentation) {}

	@Override
	public void setSlide(Slide slide) {}
}
