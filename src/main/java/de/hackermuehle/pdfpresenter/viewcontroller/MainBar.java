package de.hackermuehle.pdfpresenter.viewcontroller;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.Timer;

import de.hackermuehle.pdfpresenter.PdfPresenter;
import de.hackermuehle.pdfpresenter.model.Presentation;
import de.hackermuehle.pdfpresenter.model.State;
import de.hackermuehle.pdfpresenter.model.slide.Slide;
import de.hackermuehle.pdfpresenter.model.slide.SlideListener;
import de.hackermuehle.pdfpresenter.model.tools.Tool;
import de.hackermuehle.pdfpresenter.viewcontroller.stylebar.DropDownButton;
import de.hackermuehle.pdfpresenter.viewcontroller.stylebar.ExpandLayout;

/**
 * Provides direct access to system menu and the most important program
 * functions.
 */
public class MainBar extends JToolBar implements sun.awt.DisplayChangedListener {
	
	@Override
	public void displayChanged() {
		// TODO Auto-generated method stub
		System.out.println("MB displaychanged");
	}

	@Override
	public void paletteChanged() {
		// TODO Auto-generated method stub
		
	}   
	
	private static final long serialVersionUID = 1L;
	private State _state;
	private statePropertyChangeListener _statePropertyChangeListener;
	private PresentationPropertyChangeListener _presentationPropertyChangeListener;
	private SlideUpdateListener _slideUpdateListener;
	private Slide _slide;
	private Presentation _presentation;
	private JButton _buttonNext;
	private JButton _buttonPrevious;
	private JButton _buttonRedo;
	private JButton _buttonUndo;
	private JButton _buttonReset;
	private JToggleButton _buttonStart;
	private JToggleButton _buttonMagnifier;
	private JToggleButton _buttonAddBorder;
	private JToggleButton _buttonGrid;
	private JToggleButton _buttonMarker;
	private JToggleButton _buttonPen;
	private JToggleButton _buttonEraser;
	private JToggleButton _buttonText;
	private Timer _externalMonitorTimer;
	
	private int _indexAnnotationMarginOrGrid;
	private boolean _isInWhiteboardMode;
	
	private ImageIcon _startWithMonitorIcon;
	private ImageIcon _startNoMonitorIcon;
	private boolean _externalMonitorConnected;
	
	public MainBar(JFrame frame, State state) {
		_state = state;
		_statePropertyChangeListener = new statePropertyChangeListener();
		_state.addPropertyChangeListener(_statePropertyChangeListener);
		_presentationPropertyChangeListener = new PresentationPropertyChangeListener();
		_slideUpdateListener = new SlideUpdateListener();
		
		setFloatable(false);
		setOpaque(false);
		setLayout(new ExpandLayout(this, getOrientation()));
		_state.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("isWhiteboard")) {
					boolean isWhiteboard = evt.getNewValue() != null;
					if (_isInWhiteboardMode != isWhiteboard) {
						_isInWhiteboardMode = isWhiteboard;
						// Uncomment to show only Grid or AnnotationButton, depending on slide class:
						//showGridButtonInsteadOfAnnotationMargin(_isInWhiteboardMode);
					}
				}
			}
		});
		
		int buttonLength = 48;
		
		// Hauptmenu:
		DropDownButton mainMenu = new SystemMenu(buttonLength, frame, state);
		
		// Präsentation starten:
		_startWithMonitorIcon = ViewUtilities.createIcon("/start.png", buttonLength);
		_startNoMonitorIcon = ViewUtilities.createIcon("/startNoMonitor.png", buttonLength);
		
		// Automatic external display detection 
		ImageIcon icon;
		String buttonStartTooltip;
		if (PdfPresenter.isOnLinux()) {
			// Java's native implementation on Linux has problem and it cannot update
			// display configuration at runtime. We thus turn off automatic detection.
			icon = _startWithMonitorIcon;
			buttonStartTooltip = PdfPresenter.getLocalizedString("startPresentation");
		} else {
			// On Windows and Mac OS X it is supported
			_externalMonitorConnected = GraphicsEnvironment.getLocalGraphicsEnvironment().
			getScreenDevices().length > 1;
			if(_externalMonitorConnected) {
				icon = _startWithMonitorIcon;
				buttonStartTooltip = PdfPresenter.getLocalizedString("startPresentation");
			} else {
				icon = _startNoMonitorIcon;
				buttonStartTooltip = PdfPresenter.getLocalizedString("startPresentationNoMonitor");
			}
		}
		
		_buttonStart = new JToggleButton(icon);
		_buttonStart.setToolTipText(buttonStartTooltip);
		_buttonStart.addActionListener(new ButtonStartActionListener());
		_buttonStart.setBorderPainted(PdfPresenter.isOnMac());
		_buttonStart.setOpaque(false);
		_buttonStart.setFocusable(false);
		_buttonStart.setEnabled(true);
		
		if (!PdfPresenter.isOnLinux()) {
			// Poll every five seconds to check external Monitor
			_externalMonitorTimer = new Timer(5000, new ExternalMonitorTimer());
			_externalMonitorTimer.setInitialDelay(0);
			_externalMonitorTimer.start();
		}
		
		
		icon = ViewUtilities.createIcon("/previousPage.png", buttonLength);
		_buttonPrevious = new JButton(icon);
		_buttonPrevious.setToolTipText(PdfPresenter.getLocalizedString("previousPage"));
		_buttonPrevious.addActionListener(new ButtonPreviousActionListener());
		_buttonPrevious.setBorderPainted(false);
		_buttonPrevious.setOpaque(false);
		_buttonPrevious.setFocusable(false);
		
		// Nächste Seite:
		icon = ViewUtilities.createIcon("/nextPage.png", buttonLength);
		_buttonNext = new JButton(icon);
		_buttonNext.setToolTipText(PdfPresenter.getLocalizedString("nextPage"));
		_buttonNext.addActionListener(new ButtonNextActionListener());
		_buttonNext.setBorderPainted(false);
		_buttonNext.setOpaque(false);
		_buttonNext.setFocusable(false);
		
		icon = ViewUtilities.createIcon("/zoom.png", buttonLength);
		_buttonMagnifier = new JToggleButton(icon);
		_buttonMagnifier.setToolTipText(PdfPresenter.getLocalizedString("zoomIn"));
		_buttonMagnifier.addActionListener(new ButtonZoomActionListener());
		_buttonMagnifier.setBorderPainted(PdfPresenter.isOnMac());
		_buttonMagnifier.setOpaque(false);
		_buttonMagnifier.setFocusable(false);
		
		icon = ViewUtilities.createIcon("/noteMargin.png", buttonLength);
		_buttonAddBorder = new JToggleButton(icon);
		_buttonAddBorder.setToolTipText(PdfPresenter.getLocalizedString("noteMarginOn"));
		_buttonAddBorder.addActionListener(new ButtonAnnotationActionListener());
		_buttonAddBorder.setBorderPainted(PdfPresenter.isOnMac());
		_buttonAddBorder.setOpaque(false);
		_buttonAddBorder.setFocusable(false);
		
		icon = ViewUtilities.createIcon("/grid.png", buttonLength);
		_buttonGrid = new JToggleButton(icon);
		_buttonGrid.setToolTipText(PdfPresenter.getLocalizedString("gridOn"));
		_buttonGrid.addActionListener(new ButtonGridActionListener());
		_buttonGrid.setBorderPainted(PdfPresenter.isOnMac());
		_buttonGrid.setOpaque(false);
		_buttonGrid.setFocusable(false);
		
		icon = ViewUtilities.createIcon("/undo.png", buttonLength);
		_buttonUndo = new JButton(icon);
		_buttonUndo.setToolTipText(PdfPresenter.getLocalizedString("undo"));
		_buttonUndo.addActionListener(new ButtonUndoActionListener());
		_buttonUndo.setBorderPainted(false);
		_buttonUndo.setOpaque(false);
		_buttonUndo.setFocusable(false);
		
		icon = ViewUtilities.createIcon("/redo.png", buttonLength);
		_buttonRedo = new JButton(icon);
		_buttonRedo.setToolTipText(PdfPresenter.getLocalizedString("redo"));
		_buttonRedo.addActionListener(new ButtonRedoActionListener());
		_buttonRedo.setBorderPainted(false);
		_buttonRedo.setOpaque(false);
		_buttonRedo.setFocusable(false);
		
		icon = ViewUtilities.createIcon("/pen.png", buttonLength);
		_buttonPen = new JToggleButton(icon);
		_buttonPen.setToolTipText(PdfPresenter.getLocalizedString("pen"));
		_buttonPen.addActionListener(new ButtonPenActionListener());
		_buttonPen.setBorderPainted(PdfPresenter.isOnMac());
		_buttonPen.setOpaque(false);
		_buttonPen.setFocusable(false);
		
		icon = ViewUtilities.createIcon("/highlighter.png", buttonLength);
		_buttonMarker = new JToggleButton(icon);
		_buttonMarker.setToolTipText(PdfPresenter.getLocalizedString("highlighter"));
		_buttonMarker.addActionListener(new ButtonMarkerActionListener());
		_buttonMarker.setBorderPainted(PdfPresenter.isOnMac());
		_buttonMarker.setOpaque(false);
		_buttonMarker.setFocusable(false);
		
		icon = ViewUtilities.createIcon("/text.png", buttonLength);
		_buttonText = new JToggleButton(icon);
		_buttonText.setToolTipText(PdfPresenter.getLocalizedString("text"));
		_buttonText.addActionListener(new ButtonTextActionListener());
		_buttonText.setBorderPainted(PdfPresenter.isOnMac());
		_buttonText.setOpaque(false);
		_buttonText.setFocusable(false);
		
		icon = ViewUtilities.createIcon("/eraser.png", buttonLength);
		_buttonEraser = new JToggleButton(icon);
		_buttonEraser.setToolTipText(PdfPresenter.getLocalizedString("eraser"));
		_buttonEraser.addActionListener(new ButtonEraserActionListener());
		_buttonEraser.setBorderPainted(PdfPresenter.isOnMac());
		_buttonEraser.setOpaque(false);
		_buttonEraser.setFocusable(false);
		
		icon = ViewUtilities.createIcon("/reset.png", buttonLength);
		_buttonReset = new JButton(icon);
		_buttonReset.setToolTipText(PdfPresenter.getLocalizedString("resetAnnotations"));
		_buttonReset.addActionListener(new ButtonResetActionListener());
		_buttonReset.setBorderPainted(false);
		_buttonReset.setOpaque(false);
		_buttonReset.setFocusable(false);
		
		add(mainMenu);
		add(_buttonStart);
		
		add(Box.createHorizontalGlue());
		add(_buttonPrevious);
		add(_buttonNext);
		
		add(Box.createHorizontalGlue());
		
		add(_buttonPen);
		add(_buttonMarker);
		add(_buttonText);
		add(_buttonEraser);
		addSeparator();
		add(_buttonReset);
		
		add(Box.createHorizontalGlue());
		
		add(_buttonUndo);
		add(_buttonRedo);		
		
		add(Box.createHorizontalGlue());

		add(_buttonMagnifier);
		_indexAnnotationMarginOrGrid = getComponentCount();
		add(_buttonAddBorder);
		add(_buttonGrid);

		presentationChanged(_state.getActivePresentation());
		toolChanged(_state.getActiveTool());
	}
	
	public void showGridButtonInsteadOfAnnotationMargin(boolean showGridButton) {
		if (showGridButton) {
			remove(_buttonAddBorder);
			add(_buttonGrid, _indexAnnotationMarginOrGrid);
			revalidate();
		} else {
			remove(_buttonGrid);
			add(_buttonAddBorder, _indexAnnotationMarginOrGrid);
			revalidate();
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		_state.removePropertyChangeListener(_statePropertyChangeListener);
		super.finalize();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D) g;

		// Draw gradient
		GradientPaint gradient = 
			new GradientPaint(0f, 0f, 
				new Color(250, 250, 250), 
				0f,
				getHeight(), 
				new Color(170, 170, 170));

		g2d.setPaint(gradient);
		g2d.fillRect(0, 0, getWidth(), getHeight());
	}

	private class statePropertyChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals("activePresentation")) {
				presentationChanged((Presentation) e.getNewValue());
			}
			else if (e.getPropertyName().equals("activeTool")) {
				toolChanged((Tool) e.getNewValue());
			}
			else if (e.getPropertyName().equals("presenting")) {
				_buttonStart.setSelected(_state.isPresenting());
			}
		}
	}
	
	private class PresentationPropertyChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals("activeSlide")) {
				slideChanged((Slide) e.getNewValue());
			}
			else if (e.getPropertyName().equals("source")) {
				slideChanged(_slide);
			}
			else if (e.getPropertyName().equals("gridVisibility")) {
				if (_presentation != null)
					_buttonGrid.setSelected(_presentation.getGridVisibility());
			}
		}
	}
	
	private class SlideUpdateListener implements SlideListener {
		@Override
		public void update(Rectangle2D rectangle2D) {
			slideContentChanged(_slide);
		}
	}
	
	/**
	 * Enables/Disables the start/stop presentation button. Connecting an external device
	 * enables the start button and vice versa.
	 *
	 */
	private class ExternalMonitorTimer implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			
			if(!_state.isPresenting()) {
				GraphicsDevice[] displays = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
				boolean externalMonitorConnected = displays.length > 1;
				if (_externalMonitorConnected != externalMonitorConnected) {
					_externalMonitorConnected = externalMonitorConnected;
					if (externalMonitorConnected) {
						_buttonStart.setIcon(_startWithMonitorIcon);
						_buttonStart.setToolTipText(PdfPresenter.getLocalizedString("startPresentation"));
					} else {
						_buttonStart.setIcon(_startNoMonitorIcon);
						_buttonStart.setToolTipText(PdfPresenter.getLocalizedString("startPresentationNoMonitor"));
					}
				}
			}
		}
		
	}
	
	private class ButtonStartActionListener implements ActionListener {
    	public void actionPerformed(ActionEvent event) {
    		JToggleButton buttonStart = (JToggleButton) event.getSource();
    		if (buttonStart.isSelected()) {
    			
    			// Return if there is no external display:
				GraphicsDevice[] displays = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
				
				if (displays.length <= 1) {
					JOptionPane.showMessageDialog(MainBar.this, 
							PdfPresenter.getLocalizedString("emNotConnectedBody"), 
							PdfPresenter.getLocalizedString("emNotConnectedTitle"), 
							JOptionPane.INFORMATION_MESSAGE);
					buttonStart.setSelected(false);
					return;
				}

				// TODO V2: Allow user to choose external display.
				
				// Find Display not used by the presenter:
				GraphicsDevice currentDisplay = getGraphicsConfiguration().getDevice();
				for (int i = 0; i < displays.length; ++i) {
					if (!displays[i].equals(currentDisplay)) {
						_state.setExternalDisplay(displays[i]);
						_state.setPresenting(true);
						return;
					}
				}
				
				// Debug: Presenting on primary screen: 
				//_state.setExternalDisplay(displays[0]);
    			//_state.setPresenting(true);
    		}
    		else _state.setPresenting(false);
    	}
    }
	
    private class ButtonNextActionListener implements ActionListener {
    	public void actionPerformed(ActionEvent event) {
    		if (_state.getActivePresentation() != null)
        		_state.getActivePresentation().nextSlide();
    	}
    }
    
    private class ButtonPreviousActionListener implements ActionListener {
    	public void actionPerformed(ActionEvent event) {
    		if (_state.getActivePresentation() != null)
            	_state.getActivePresentation().previousSlide();
    	}
    }
    
    private class ButtonUndoActionListener implements ActionListener {
    	public void actionPerformed(ActionEvent event) {
    		if (_state.getActivePresentation() != null &&
    			_state.getActivePresentation().getActiveSlide() != null)
    			_state.getActivePresentation().getActiveSlide().undo();
    	}
    }
    
    private class ButtonRedoActionListener implements ActionListener {
    	public void actionPerformed(ActionEvent event) {
    		if (_state.getActivePresentation() != null &&
    			_state.getActivePresentation().getActiveSlide() != null)
    			_state.getActivePresentation().getActiveSlide().redo();
    	}
    }
    
    private class ButtonPenActionListener implements ActionListener {
    	public void actionPerformed(ActionEvent event) {
    		_state.setActiveTool(_state.getPen());
    	}
    }
    
    private class ButtonTextActionListener implements ActionListener {
    	public void actionPerformed(ActionEvent event) {
    		_state.setActiveTool(_state.getTextTool());
    	}
    }
    
    private class ButtonMarkerActionListener implements ActionListener {
    	public void actionPerformed(ActionEvent event) {
    		_state.setActiveTool(_state.getMarker());
    	}
    }
    
    private class ButtonEraserActionListener implements ActionListener {
    	public void actionPerformed(ActionEvent event) {
    		_state.setActiveTool(_state.getEraser());
    	}
    }
    
    private class ButtonResetActionListener implements ActionListener {
    	public void actionPerformed(ActionEvent event) {
    		if (_state.getActivePresentation() != null &&
    			_state.getActivePresentation().getActiveSlide() != null)
    			_state.getActivePresentation().getActiveSlide().reset();
    	}
    }
    
    private class ButtonGridActionListener implements ActionListener {
    	public void actionPerformed(ActionEvent event) {
    		if (_state.getActivePresentation() != null) {
    			JToggleButton button = (JToggleButton) event.getSource();
        		if (button.isSelected()) {
        			//_state.getActivePresentation().setGrid(new Grid(_state.getGrid().getType(), _state.getGrid().getDistance() * );
        			_state.getActivePresentation().setGridVisibility(true);
        			button.setToolTipText(PdfPresenter.getLocalizedString("gridOff"));
        		} else {
        			_state.getActivePresentation().setGridVisibility(false);
        			button.setToolTipText(PdfPresenter.getLocalizedString("gridOn"));
        		}
    		}
    	}
    }
    
    private class ButtonZoomActionListener implements ActionListener {
    	public void actionPerformed(ActionEvent event) {
    		JToggleButton button = (JToggleButton) event.getSource();
    		if (button.isSelected()) {
    			_state.setActiveTool(_state.getMagnifier());
    			
    		} else {
    			if (_state.getActiveTool() == _state.getMagnifier()) {
    				_state.setActiveTool(_state.getPreviousTool());
    			} else {
    				_state.setActiveTool(_state.getActiveTool());
    			}
    			
    			if (_state.getActivePresentation() != null &&
    	        	_state.getActivePresentation().getActiveSlide() != null)
    				_state.getActivePresentation().setSource(
    						_state.getActivePresentation().getActiveSlide().getSize());
    		}
    	}
    }
    
    private class ButtonAnnotationActionListener implements ActionListener {
    	public void actionPerformed(ActionEvent event) {
    		if (_state.getActivePresentation() != null &&
        		_state.getActivePresentation().getActiveSlide() != null) {
    			
    			JToggleButton button = (JToggleButton) event.getSource();
    			Slide slide = _state.getActivePresentation().getActiveSlide();
    			
        		if (button.isSelected()) {
        			button.setToolTipText(PdfPresenter.getLocalizedString("noteMarginOff"));
        			
            		String ratioString = _state.getPreferences().getPreference("general.annotationmargin.ratio");
            		
            		double ratio = 0.333;
            		try {
            			ratio = Double.valueOf(ratioString);
            		} catch (Exception exception) {}
        			
        			// Increase size:
        			Rectangle2D defaultSize = slide.getDefaultSize();
        			Rectangle2D size = slide.getSize();
        			size.setRect(0, 0, size.getWidth() + defaultSize.getWidth() * ratio, size.getHeight() + defaultSize.getHeight() * ratio);
        			slide.setSize(size);
        		} else {
        			button.setToolTipText(PdfPresenter.getLocalizedString("noteMarginOn"));
        			
        			// Reset default size:
        			slide.setSize(slide.getDefaultSize());
        		}
        		
        		// Show entire slide:
        		_state.getActivePresentation().setSource(slide.getSize());
    		}
    	}
    }
  
    private void presentationChanged(Presentation presentation) {
    	if (_presentation != null) {
    		_presentation.removePropertyChangeListener(_presentationPropertyChangeListener);
		}
    	_presentation = presentation;
		
		if (_presentation != null) {
			_presentation.addPropertyChangeListener(_presentationPropertyChangeListener);
    		_buttonGrid.setEnabled(true);
    		_buttonGrid.setSelected(presentation.getGridVisibility());
			_buttonMagnifier.setEnabled(true);
			slideChanged(presentation.getActiveSlide());
    	} else {
    		_buttonGrid.setEnabled(false);
			_buttonMagnifier.setEnabled(false);
    		slideChanged(null);
    	}
    }
    
    private void toolChanged(Tool tool) {
    	_buttonEraser.setSelected(tool == _state.getEraser());
		_buttonPen.setSelected(tool == _state.getPen());
		_buttonMarker.setSelected(tool == _state.getMarker());
		_buttonText.setSelected(tool == _state.getTextTool());
		_buttonMagnifier.setSelected((_state.getActiveTool() == _state.getMagnifier()) || (_slide != null && _presentation != null && !_slide.getSize().equals(_presentation.getSource())));
		if (_buttonMagnifier.isSelected()) 
			_buttonMagnifier.setToolTipText(PdfPresenter.getLocalizedString("zoomOff"));
		else
			_buttonMagnifier.setToolTipText(PdfPresenter.getLocalizedString("zoomIn"));
	}
    
    private void slideChanged(Slide slide) {
		if (_slide != null) {
			_slide.removeListener(_slideUpdateListener);
		}
		_slide = slide;
		
		if (_slide != null) {
			_slide.addListener(_slideUpdateListener);
			_buttonAddBorder.setSelected(!_slide.getSize().equals(_slide.getDefaultSize()));
			_buttonAddBorder.setEnabled(true);
			_buttonPrevious.setEnabled(_slide != _state.getActivePresentation().getSlides().get(0));
			_buttonNext.setEnabled(_state.getActivePresentation().getSlides().indexOf(_slide) != _state.getActivePresentation().getSlides().size() - 1);
		
			// Show special buttons for special subclasses:
			// Uncomment to show only Grid or AnnotationButton, depending on slide class:
			//showGridButtonInsteadOfAnnotationMargin(_slide.getClass().equals(WhiteboardSlide.class));
		}
		else {
			_buttonAddBorder.setEnabled(false);
			_buttonNext.setEnabled(false);
			_buttonPrevious.setEnabled(false);
			
			// Show default buttons for no class:
			// Uncomment to show only Grid or AnnotationButton, depending on slide class:
			//showGridButtonInsteadOfAnnotationMargin(false);
		}
		slideContentChanged(_slide);
	}
    
    private void slideContentChanged(Slide slide) {
		if (slide != null) {
			Slide activeSlide = slide;//_state.getActivePresentation().getActiveSlide();?
			_buttonUndo.setEnabled(activeSlide.canUndo());
			_buttonReset.setEnabled(!activeSlide.getAnnotations().isEmpty());
			_buttonRedo.setEnabled(activeSlide.canRedo());
			_buttonMagnifier.setSelected(_state.getActiveTool() == _state.getMagnifier() || !_slide.getSize().equals(_presentation.getSource()));
		}
		else {
			_buttonUndo.setEnabled(false);
			_buttonReset.setEnabled(false);
			_buttonRedo.setEnabled(false);
			_buttonMagnifier.setSelected(_state.getActiveTool() == _state.getMagnifier());
		}
    }


}
