package de.hackermuehle.pdfpresenter.viewcontroller;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLayeredPane;

import de.hackermuehle.pdfpresenter.model.Presentation;
import de.hackermuehle.pdfpresenter.model.State;
import de.hackermuehle.pdfpresenter.model.document.Document;
import de.hackermuehle.pdfpresenter.model.slide.Slide;
import de.hackermuehle.pdfpresenter.viewcontroller.viewpanel.InputViewPanel;
import de.hackermuehle.pdfpresenter.viewcontroller.viewpanel.SlideViewPanel;
import de.hackermuehle.pdfpresenter.viewcontroller.viewpanel.ViewPanel;

/**
 * The presentation panel and derived panels (Inner- and
 * OuterPresentationPanel) display the active slide of the active presentation,
 * update the grid, synchronize inner- with outer panel etc.
 * 
 * These classes do not fulfill our coding standards and should be reworked in
 * future.
 */
public abstract class PresentationPanel extends JLayeredPane {
	private static final long serialVersionUID = 1L;
	private static final int CACHE_PRIORITY_PRECACHE = Document.PRIO_PRECACHE;
	private static final int CACHE_PRIORITY = Document.PRIO_MAIN; 
	private State _state;
	private SlideViewPanel _viewPanel;
	private InputViewPanel _inputPanel;
	private ControllerPropertyChangeListener _controllerPropertyChangeListener;
	private PresentationPropertyChangeListener _presentationPropertyChangeListener;
	private Presentation _activePresentation;
	
	public PresentationPanel(State controller) {
		_state = controller;
		_controllerPropertyChangeListener = new ControllerPropertyChangeListener();
		_state.addPropertyChangeListener(_controllerPropertyChangeListener);
		_presentationPropertyChangeListener = new PresentationPropertyChangeListener();
		
		_activePresentation = null;
		_viewPanel = null;
		_inputPanel = null;
		
		setLayout(new PresentationPanelLayoutManager());
		setOpaque(true);
		
		// Add viewPanel for active slide:
		if (_state.getActivePresentation() != null) {
			_activePresentation = _state.getActivePresentation();
			_activePresentation.addPropertyChangeListener(_presentationPropertyChangeListener);
			
			if (_state.getActivePresentation().getActiveSlide() != null) {
				_viewPanel = new SlideViewPanel(_state.getActivePresentation().getActiveSlide(), _state.getActivePresentation().getSource(), _state.getActivePresentation().getGridVisibility() ? _state.getGrid() : null, CACHE_PRIORITY, _state.getActivePresentation().getNextSlide(), CACHE_PRIORITY_PRECACHE);				
				_inputPanel = createInputPanel(_state.getActivePresentation().getActiveSlide(), _state.getActivePresentation().getSource(), _state);
				add(_viewPanel, JLayeredPane.FRAME_CONTENT_LAYER);
				_viewPanel.addComponentListener(new PresentationViewPanelComponentListener());
				add(_inputPanel, JLayeredPane.DEFAULT_LAYER);
			}
		}

		validate();
    }
	
	@Override
	protected void finalize() throws Throwable {
		dispose();
		super.finalize();
	}
	
	protected abstract InputViewPanel createInputPanel(Slide slide, Rectangle2D source, State controller);
	
	protected State getState() {
		return _state;
	}
	
	protected InputViewPanel getInputPanel() {
		return _inputPanel;
	}	
	
	protected SlideViewPanel getViewPanel() {
		return _viewPanel;
	}	
	
	class PresentationViewPanelComponentListener implements ComponentListener {
		@Override
		public void componentHidden(ComponentEvent arg0) {}
		
		@Override
		public void componentMoved(ComponentEvent arg0) {}
		
		@Override
		public void componentResized(ComponentEvent e) {
			gridChanged(); // TODO: Move Grid to InputPanel.
		}

		@Override
		public void componentShown(ComponentEvent e) {}
	}
	
	class ControllerPropertyChangeListener implements PropertyChangeListener {
		
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			
			if (e.getPropertyName().equals("activePresentation")) {
				
				// The active presentation has changed:
				if (e.getOldValue() != null) {
					((Presentation) e.getOldValue()).removePropertyChangeListener(_presentationPropertyChangeListener);
				}
				
				if (e.getNewValue() != null) {
					
					// A new active presentation has to be displayed:
					_activePresentation = (Presentation) e.getNewValue();
					_activePresentation.addPropertyChangeListener(_presentationPropertyChangeListener);
					
					if (_activePresentation.getActiveSlide() != null) {
						if (_viewPanel != null) {
							_viewPanel.setSlide(_state.getActivePresentation().getActiveSlide(), _state.getActivePresentation().getNextSlide(),_activePresentation.getSource(), CACHE_PRIORITY_PRECACHE);
							//_viewPanel.setSlide(_controller.getActivePresentation().getActiveSlide(),  _controller.getActivePresentation().getSource());
							_viewPanel.setGrid(_state.getActivePresentation().getGridVisibility() ? _state.getGrid() : null);
							_inputPanel.setSlide(_activePresentation.getActiveSlide());
							_inputPanel.setPresentation(_activePresentation);
							_inputPanel.setSource(_activePresentation.getSource());
						}
						else {
							_viewPanel = new SlideViewPanel(_activePresentation.getActiveSlide(), _activePresentation.getSource(), _activePresentation.getGridVisibility() ? _state.getGrid() : null, CACHE_PRIORITY, _state.getActivePresentation().getNextSlide(), CACHE_PRIORITY_PRECACHE);
							_inputPanel = createInputPanel(_activePresentation.getActiveSlide(), _activePresentation.getSource(), _state);
							add(_viewPanel, JLayeredPane.FRAME_CONTENT_LAYER);
							_viewPanel.addComponentListener(new PresentationViewPanelComponentListener());
							add(_inputPanel, JLayeredPane.DEFAULT_LAYER);
							validate();
							repaint();
						}
					}
					else {

						// No Slide has to be displayed:
						if (_viewPanel != null) {
							remove(_viewPanel);
							remove(_inputPanel);
							_viewPanel = null;
							_inputPanel = null;
							validate();
							repaint();
						}
					}
					gridChanged();
				}
				else {
					_activePresentation = null;
					
					// No presentation has to be displayed:
					if (_viewPanel != null) {
						remove(_viewPanel);
						remove(_inputPanel);
						_viewPanel = null;
						_inputPanel = null;
						validate();
						repaint();
					}
				}
			}
			else if (e.getPropertyName().equals("activeTool")) {
				if (_state.getActivePresentation() != null &&
					_state.getActivePresentation().getActiveSlide() != null) {
					if (_inputPanel != null) {
						remove(_inputPanel);
						_inputPanel = null;
					}
					_inputPanel = createInputPanel(_state.getActivePresentation().getActiveSlide(), 
							_state.getActivePresentation().getSource(), _state);
					add(_inputPanel, JLayeredPane.DEFAULT_LAYER);
					validate();
					repaint();
				}
				gridChanged();
			}
			else if (e.getPropertyName().equals("grid")) {
				
				// The Grid has been activated / deactivated:
				if (_state.getActivePresentation() != null &&
					_viewPanel != null) {
					gridChanged();
					//_viewPanel.setGrid(_controller.getActivePresentation().getGridVisibility() ? 
					// _controller.getGrid() : null);
				}
			}
		}
	}
	
	class PresentationPropertyChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			gridChanged();
			if(e.getPropertyName().equals("activeSlide")) {

				// The active slide has changed:
				if (_state.getActivePresentation() != null &&
					_state.getActivePresentation().getActiveSlide() != null) {
					if (_viewPanel != null) {
						_viewPanel.setSlide(_state.getActivePresentation().getActiveSlide(), _state.getActivePresentation().getNextSlide(),  _state.getActivePresentation().getSource(), CACHE_PRIORITY_PRECACHE);
						_inputPanel.setSlide(_state.getActivePresentation().getActiveSlide());
						_inputPanel.setPresentation(_state.getActivePresentation());
						_inputPanel.setSource(_state.getActivePresentation().getSource());
					}
					else {
						_viewPanel = new SlideViewPanel(_state.getActivePresentation().getActiveSlide(), _state.getActivePresentation().getSource(), _state.getActivePresentation().getGridVisibility() ? _state.getGrid() : null, CACHE_PRIORITY,_state.getActivePresentation().getNextSlide(), CACHE_PRIORITY_PRECACHE);
						_inputPanel = createInputPanel(_state.getActivePresentation().getActiveSlide(), _state.getActivePresentation().getSource(), _state);
						add(_viewPanel, JLayeredPane.FRAME_CONTENT_LAYER);
						add(_inputPanel, JLayeredPane.DEFAULT_LAYER);
						validate();
						repaint();
					}
					gridChanged();
				}
				else {
					// No slide to display:
					if (_viewPanel != null) {
						remove(_viewPanel);
						remove(_inputPanel);
						_viewPanel = null;
						_inputPanel = null;
						validate();
						repaint();
					}
				}
			}
			else if (e.getPropertyName().equals("gridVisibility")) {
				
				// The Grid has been activated / deactivated:
				if (_state.getActivePresentation() != null &&
					_viewPanel != null) {
					_viewPanel.setGrid(_state.getActivePresentation().getGridVisibility() ? _state.getGrid() : null);
				}
				gridChanged();
			}
			else if (e.getPropertyName().equals("source")) {
				
				if (_state.getActivePresentation() != null &&
					_viewPanel != null) {
					_viewPanel.setSource(_state.getActivePresentation().getSource());
					_inputPanel.setSource(_state.getActivePresentation().getSource());
				}
				gridChanged();
			}
		}
	}
	
    class PresentationPanelLayoutManager implements LayoutManager {
    	@Override
		public void addLayoutComponent(String string, Component component) {}

		@Override
		public void layoutContainer(Container container) {
	       for (Component component : getComponents()) {
	        	if (ViewPanel.class.isInstance(component))
	        		component.setBounds(0, 0, getWidth(), getHeight());
	        	else
	        		component.setBounds(new Rectangle(component.getLocation(), component.getPreferredSize()));
	        }
		}

		@Override
		public Dimension minimumLayoutSize(Container container) {
			return new Dimension(getWidth(), getHeight());
		}

		@Override
		public Dimension preferredLayoutSize(Container container) {
			return new Dimension(getWidth(), getHeight());
		}

		@Override
		public void removeLayoutComponent(Component component) {}
    }
    
    public void dispose() {
    	_state.removePropertyChangeListener(_controllerPropertyChangeListener);
		if (_activePresentation != null) 
			_activePresentation.removePropertyChangeListener(_presentationPropertyChangeListener);

		if (_viewPanel != null) 
			_viewPanel.dispose();
    }
    
	protected void gridChanged() {}
}