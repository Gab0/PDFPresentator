package de.hackermuehle.pdfpresenter.viewcontroller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JPanel;

import de.hackermuehle.pdfpresenter.model.Presentation;
import de.hackermuehle.pdfpresenter.model.State;
import de.hackermuehle.pdfpresenter.model.slide.Grid;
import de.hackermuehle.pdfpresenter.model.slide.Slide;
import de.hackermuehle.pdfpresenter.model.slide.WhiteboardSlide;
import de.hackermuehle.pdfpresenter.viewcontroller.viewpanel.InputPanelFactory;
import de.hackermuehle.pdfpresenter.viewcontroller.viewpanel.InputViewPanel;
import de.hackermuehle.pdfpresenter.viewcontroller.viewpanel.MasterInputPanel;

public class InnerPresentationPanel extends PresentationPanel {
	private static final long serialVersionUID = 7545445840609856742L;
	private static final Color BACKGROUND_COLOR = Color.DARK_GRAY; // new Color(220,220,220)
	
	private StatePropertyChangeListener _statePropertyChangeListener;
	private ExternalFrame _externalFrame = null;
	//private JComponent _slider;
	
	public InnerPresentationPanel(State controller) {
		super(controller);
		_statePropertyChangeListener = new StatePropertyChangeListener();
		getState().addPropertyChangeListener(_statePropertyChangeListener);
		
		addComponentListener(new PanelComponentListener());
		validate();
		
		setBackground(BACKGROUND_COLOR);		
		//_slider = new VerticalPositioner(this, getState());
		//add(_slider);
		
		// Make this panel the focused one (most of the time):
		setFocusCycleRoot(true);
		setFocusable(true);
		addKeyListener(new ApplicationKeyListener());
		addNotify();
		
		setVisible(true);
		requestFocus();
	}
	
	private class ApplicationKeyListener implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {}
		
		@Override
		public void keyReleased(KeyEvent e) {}
		
		@Override
		public void keyPressed(KeyEvent e) {
			int ctrlCmdMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
			if ((e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) || 
				(e.getKeyCode() == KeyEvent.VK_DOWN ||
				(e.getKeyCode() == KeyEvent.VK_SPACE))) {
				if (getState().getActivePresentation() != null)
					getState().getActivePresentation().nextSlide();
			}
			else if ((e.getKeyCode() == KeyEvent.VK_W) && // Ctrl on win/linux, Cmd on mac
					 (e.getModifiers() == ctrlCmdMask)) {
				if (getState().getActivePresentation() != null) {
					getState().removePresentation(getState().getActivePresentation());
				}
			}
			else if (e.getKeyCode() == KeyEvent.VK_HOME) {
				if (getState().getActivePresentation() != null)
					getState().getActivePresentation().firstSlide();
			}
			else if (e.getKeyCode() == KeyEvent.VK_END) {
				if (getState().getActivePresentation() != null)
					getState().getActivePresentation().lastSlide();
			}
			else if ((e.getKeyCode() == KeyEvent.VK_PAGE_UP) ||
					 (e.getKeyCode() == KeyEvent.VK_UP)) {
				if (getState().getActivePresentation() != null)
					getState().getActivePresentation().previousSlide();
			}
			else if ((e.getKeyCode() == KeyEvent.VK_Z) &&
					 (e.getModifiers() == ctrlCmdMask)) {
				if ((getState().getActivePresentation() != null) &&
					(getState().getActivePresentation().getActiveSlide() != null))
					if (getState().getActivePresentation().getActiveSlide().canUndo())
						getState().getActivePresentation().getActiveSlide().undo();
			}
			else if ((e.getKeyCode() == KeyEvent.VK_Y) &&
					 (e.getModifiers() == ctrlCmdMask)) {
				if ((getState().getActivePresentation() != null) &&
					(getState().getActivePresentation().getActiveSlide() != null))
					if (getState().getActivePresentation().getActiveSlide().canRedo())
						getState().getActivePresentation().getActiveSlide().redo();
			}
			else if ((e.getKeyCode() == KeyEvent.VK_1) ||
					 (e.getKeyCode() == KeyEvent.VK_P)) {
				getState().setActiveTool(getState().getPen());
			}
			else if ((e.getKeyCode() == KeyEvent.VK_2) ||
					 (e.getKeyCode() == KeyEvent.VK_M)) {
				getState().setActiveTool(getState().getMarker());
			}
			else if ((e.getKeyCode() == KeyEvent.VK_3) ||
					 (e.getKeyCode() == KeyEvent.VK_T)) {
				getState().setActiveTool(getState().getTextTool());
			}
			else if ((e.getKeyCode() == KeyEvent.VK_4) ||
					 (e.getKeyCode() == KeyEvent.VK_E)) {
				getState().setActiveTool(getState().getEraser());
			}
			else if (e.getKeyCode() == KeyEvent.VK_Z) {
				if ((getState().getActivePresentation() != null) &&
	    			(getState().getActivePresentation().getActiveSlide() != null) &&
	    			(!getState().getActivePresentation().getSource().equals(getState().getActivePresentation().getActiveSlide().getSize()))) {
					
					if (getState().getActiveTool() == getState().getMagnifier()) {
						getState().setActiveTool(getState().getPreviousTool());
	    			} else {
	    				getState().setActiveTool(getState().getActiveTool());
	    			}
	    			
	    			if (getState().getActivePresentation() != null &&
	    				getState().getActivePresentation().getActiveSlide() != null)
	    				getState().getActivePresentation().setSource(getState().getActivePresentation().getActiveSlide().getSize());
				} else {
					if (getState().getActiveTool() == getState().getMagnifier()) {
						getState().setActiveTool(getState().getPreviousTool());
	    			} else {
	    				getState().setActiveTool(getState().getMagnifier());
	    			}
	    		}
			}
			else if (e.getKeyCode() == KeyEvent.VK_A) {
				// Annotation margin (B not included in the name, M conflict with Marker)
				if ((getState().getActivePresentation() != null) &&
					(getState().getActivePresentation().getActiveSlide() != null)) {
					
					Slide slide = getState().getActivePresentation().getActiveSlide();
	        		
					if (slide.getSize().equals(slide.getDefaultSize())) {
						
						// Add annotation margin:
	            		double ratio = 0.333;
	            		try {
	            			String ratioString = getState().getPreferences().getPreference("general.annotationmargin.ratio");
	            			ratio = Double.valueOf(ratioString);
	            		} catch (Exception exception) {}
	        			
	        			Rectangle2D defaultSize = slide.getDefaultSize();
	        			Rectangle2D size = slide.getSize();
	        			size.setRect(0, 0, size.getWidth() + defaultSize.getWidth() * ratio, size.getHeight() + defaultSize.getHeight() * ratio);
	        			slide.setSize(size);
	        		} else {
	        			
	        			// Reset size to default:
	        			slide.setSize(slide.getDefaultSize());
	        		}
					
					getState().getActivePresentation().setSource(slide.getSize());
				}
			}
			else if (e.getKeyCode() == KeyEvent.VK_G) {
				// Grid
				if (getState().getActivePresentation() != null)
					getState().getActivePresentation().setGridVisibility(!getState().getActivePresentation().getGridVisibility());
			}
			else if (e.getKeyChar() == '+') {
				// Add Whiteboard slide
				if (getState().getActivePresentation() != null) {
					getState().getActivePresentation().addSlide(new WhiteboardSlide());
				}
			}
			else if (e.getKeyChar() == '-') {
				// Remove slide
				if ((getState().getActivePresentation() != null) &&
		    		(getState().getActivePresentation().getActiveSlide() != null)) {
					if (getState().getActivePresentation().getActiveSlide().getClass().equals(WhiteboardSlide.class))
					getState().getActivePresentation().removeSlide();
				}
			}
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		getState().removePropertyChangeListener(_statePropertyChangeListener);
		super.finalize();
	}
	
	private class PanelComponentListener implements ComponentListener {

		@Override
		public void componentHidden(ComponentEvent arg0) {}

		@Override
		public void componentMoved(ComponentEvent arg0) {}

		@Override
		public void componentResized(ComponentEvent arg0) {
			//_slider.setLocation(getWidth()-_slider.getWidth(), getHeight()/2 - _slider.getHeight()/2);
		}

		@Override
		public void componentShown(ComponentEvent arg0) {}
	}
	
	private class StatePropertyChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals("presenting")) {
				if (getState().isPresenting()) {
					if (_externalFrame != null) {
						_externalFrame.setMasterInputPanel((MasterInputPanel) getInputPanel());
					}
					else {
						_externalFrame = new ExternalFrame(getState(), (MasterInputPanel) getInputPanel(), getState().getExternalDisplay().getDefaultConfiguration());
					}
				}
				else {
					if(_externalFrame != null) {
						_externalFrame.setVisible(false);
						_externalFrame.dispose();
						_externalFrame = null;
					}
				}
			}
			else if (e.getPropertyName().equals("activePresentation")) {
				requestFocusInWindow();
			}
		}
	}
	
	protected void gridChanged() {
		
		// Grid adaption:
		if (getState().getActivePresentation() != null &&
			getViewPanel() != null && getViewPanel().getClipping() != null) {
			
			Grid grid = new Grid(getState().getGrid().getType(), getState().getGrid().getDistance() * getViewPanel().getClipping().getInverseTransform().getScaleX(), getState().getPreferences());
			getViewPanel().setGrid(getState().getActivePresentation().getGridVisibility() ? grid : null);
		}
		
		if (_externalFrame != null) {
			_externalFrame.getOuterPresentationViewPanel().gridChanged();
		}
	}

	protected InputViewPanel createInputPanel(Slide slide, Rectangle2D source, State controller) {
		MasterInputPanel inputViewPanel = InputPanelFactory.createInputPanel(slide, source, controller);
		if(_externalFrame!=null) _externalFrame.setMasterInputPanel(inputViewPanel);
		return inputViewPanel;
	}
}

// For future releases:
class VerticalPositioner extends JPanel {
	private static final long serialVersionUID = 1950079886839709231L;
	private JComponent _parent;
	private State _state;
	private Image _image;
	private int _previousMouseY;

	public VerticalPositioner(JComponent parent, State state) {
		_parent = parent;
		_state = state;
		
		_image = ViewUtilities.loadImage("/trackbody.png");
		setPreferredSize(new Dimension(_image.getWidth(null), _image.getHeight(null)));
		setOpaque(false);
		
		addMouseMotionListener(new VerticalPositionerMouseMotionListener());
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D) g;
		g2d.drawImage(_image, 0, 0, null);
	}
	
	class VerticalPositionerMouseMotionListener implements MouseMotionListener {

		@Override
		public void mouseDragged(MouseEvent e) {
			if (_previousMouseY == 0) {	
				_previousMouseY = e.getYOnScreen();
			} else {
				Presentation activePresentation = _state.getActivePresentation();
				if (activePresentation != null) {
					Slide activeSlide = activePresentation.getActiveSlide();
					if (activeSlide != null) {
						Rectangle2D source = _state.getActivePresentation().getSource();
						Rectangle2D size = _state.getActivePresentation().getActiveSlide().getSize();
						
						int mouseY = e.getYOnScreen();
						int deltaY = (int)((mouseY - _previousMouseY) * size.getHeight() / source.getHeight());
						_previousMouseY = mouseY;
						
						double y = source.getY() + deltaY;
						if (y + source.getHeight() > size.getHeight()) y = size.getHeight() - source.getHeight();
						if (y < 0) y = 0;
						
						source.setRect(source.getX(), y, source.getWidth(), source.getHeight());
						_state.getActivePresentation().setSource(source);
					}
				}
			}			
		}

		@Override
		public void mouseMoved(MouseEvent e) {}
	}
}