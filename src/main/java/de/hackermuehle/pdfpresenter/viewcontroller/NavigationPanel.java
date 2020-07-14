package de.hackermuehle.pdfpresenter.viewcontroller;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import de.hackermuehle.pdfpresenter.PdfPresenter;
import de.hackermuehle.pdfpresenter.model.Presentation;
import de.hackermuehle.pdfpresenter.model.State;
import de.hackermuehle.pdfpresenter.model.document.Document;
import de.hackermuehle.pdfpresenter.model.slide.Grid;
import de.hackermuehle.pdfpresenter.model.slide.Slide;
import de.hackermuehle.pdfpresenter.model.slide.WhiteboardSlide;
import de.hackermuehle.pdfpresenter.viewcontroller.viewpanel.SlideViewPanel;

/**
 * The navigation panel displays slides of a presentation as thumbnails.
 * The active slide is highlighted.
 * 
 * Slides are displayed through SlideViewPanels.
 */
public class NavigationPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final Color BACKGROUND_COLOR = Color.DARK_GRAY;//new Color(233, 239, 247)
	private static final Color COLOR_SELECTED = Color.YELLOW; /*new Color(120, 187, 241)*/
	private static final int SHAKE_THRESHOLD = 30;
	private static final int GAP_HEIGHT = 15;
	
	private JPanel _innerPanel;
	private VerticalInertialScrollPane _scrollPane;
	
	private LinkedList<SlideViewPanel> _thumbnails = new LinkedList<SlideViewPanel>();
	
	private Presentation _presentation;
	private PropertyChangeListener _presentationPropertyChangeListener;
	
	private ThumbnailMouseListener _thumbnailMouseListener;
	private ThumbnailMouseMotionListener _thumbnailMouseMotionListener;
	
    private JToolBar _tabButtons;
	
	private SlideViewPanel _tentativeFontLabel;
	private Point _tentativePoint;
	private Point _tentativeHelpPoint;
	private boolean _isScrolling;
	
	private JButton _buttonAdd;
	private JButton _buttonDelete;
	private JumpField _jumpField;
	
	public NavigationPanel(Presentation presentation, State state) {
		_presentation = presentation;
		_presentationPropertyChangeListener = new PresentationPropertyChangeListener();
		_presentation.addPropertyChangeListener(_presentationPropertyChangeListener);
		
		_tentativePoint = new Point();
		_tentativeHelpPoint = new Point();
		
		// Panel for [+] / [-] - Buttons:
		_tabButtons = new JToolBar();
		_tabButtons.setFloatable(false);
		_tabButtons.setOpaque(false);
		
		// Linux only: PPStyleBarUI inherents from BasicStyleBarUI. The default implementation
		// of BasicStyleBarUI on Linux doesn't provide a Layout Manager. We have to set it
		// manually.
		if (PdfPresenter.isGtkLaF()) {
			_tabButtons.setLayout(new BoxLayout(_tabButtons, BoxLayout.X_AXIS));
		}

		_buttonAdd = new JButton(new ImageIcon(NavigationPanel.class.getResource("/plus.png")));
		_buttonAdd.setFocusable(false);
		_buttonAdd.setBorderPainted(false);
		_buttonAdd.setOpaque(false);
		_buttonAdd.setPreferredSize(new Dimension(100, _buttonAdd.getPreferredSize().height));
		_buttonAdd.setToolTipText(PdfPresenter.getLocalizedString("addSlide"));
		_buttonAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_presentation.addSlide(new WhiteboardSlide());
			}
		});
		
		_buttonDelete = new JButton(new ImageIcon(NavigationPanel.class.getResource("/minus.png")));
		_buttonDelete.setFocusable(false);
		_buttonDelete.setBorderPainted(false);
		_buttonDelete.setOpaque(false);
		_buttonDelete.setFocusable(false);
		_buttonDelete.setPreferredSize(new Dimension(100, _buttonDelete.getPreferredSize().height));
		if (_presentation.getActiveSlide().getClass().equals(WhiteboardSlide.class))
			_buttonDelete.setEnabled(_presentation.getActiveSlide() != null);
		else
			_buttonDelete.setEnabled(false);
		_buttonDelete.setToolTipText(PdfPresenter.getLocalizedString("deleteSlide"));
		_buttonDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_presentation.removeSlide();
			}
		});
		
		_jumpField = new JumpField();
		_jumpField.setToolTipText(PdfPresenter.getLocalizedString("jumpSlide"));
		_jumpField.setMinimumSize(new Dimension(0, 0));
		_jumpField.setPreferredSize(new Dimension(200, _buttonAdd.getPreferredSize().height));
		_jumpField.setMaximumSize(new Dimension(99999, _buttonAdd.getMinimumSize().height));
		
		_tabButtons.add(_jumpField);
		_tabButtons.add(Box.createHorizontalGlue());
		_tabButtons.addSeparator();
		_tabButtons.add(_buttonAdd);
		_tabButtons.add(_buttonDelete);
		
		// Inner panel holding thumbnails:
		_innerPanel = new JPanel();
		_innerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, GAP_HEIGHT));
		_innerPanel.setBackground(BACKGROUND_COLOR);
		
		_thumbnailMouseListener = new ThumbnailMouseListener();
		_thumbnailMouseMotionListener = new ThumbnailMouseMotionListener();
		
		// Create a thumbnail for each existing slide (usually one):
		for (Slide slide : _presentation.getSlides()) {
			SlideViewPanel thumbnail = new SlideViewPanel(slide, slide.getSize(), new Grid(Grid.Type.NONE, 0, state.getPreferences()), Document.PRIO_THUMBNAIL);
			thumbnail.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 2, Color.LIGHT_GRAY));
			thumbnail.addMouseListener(_thumbnailMouseListener);
			thumbnail.addMouseMotionListener(_thumbnailMouseMotionListener);
			_thumbnails.add(thumbnail);
		}
		
		setFocusable(false);
		setLayout(new BorderLayout());
		addComponentListener(new NavigationPanelResizedListener());
		
		// Add ScrollPanels to innerPanel:
		_scrollPane = new VerticalInertialScrollPane(_innerPanel, state);
		_scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		
		add(_scrollPane, BorderLayout.CENTER);
		
		// Buttons for Adding + Removing Slides from NavigationPanel
		add(_tabButtons, BorderLayout.SOUTH);
		
		// Highlight active slide:
		if (_presentation.getActiveSlide() != null) {
			SlideViewPanel thumbnail = getThumbnailPanel(_presentation.getActiveSlide()); 
			if (thumbnail != null) {
				thumbnail.setBorder(BorderFactory.createLineBorder(COLOR_SELECTED, 4));
				return;
			}
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		dispose() ;
		super.finalize();
	}
	
	/**
	 * The JumpField allows to navigate to a specific slide, given its number.
	 * This component releases the focus to the previous owner. 
	 */
	private class JumpField extends JTextField {
		private static final long serialVersionUID = -1650246908151730735L;
		private Component _previouslyFocused;
		
		public JumpField() {
			setFont(new Font("Arial", Font.PLAIN, 25));
			setForeground(Color.DARK_GRAY);
			setOpaque(false);
			setFocusable(false);
			setHorizontalAlignment(CENTER);
			setBorder(BorderFactory.createEmptyBorder());
			
			if (_presentation.getActiveSlide() != null)
				JumpField.super.setText(Integer.toString(_presentation.getSlides().indexOf(_presentation.getActiveSlide()) + 1));
			else 
				JumpField.super.setText("");
			
			// Release focus on mouse pressed outside:
			Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
			    public void eventDispatched(AWTEvent e) {
			    	if (e.getID() != MouseEvent.MOUSE_PRESSED) return;
			    	
				    if (e.getSource() != JumpField.this) {
				    	if (_previouslyFocused != null) {
							_previouslyFocused.requestFocusInWindow();
							_previouslyFocused = null;
				    	}
				    	_jumpField.setFocusable(false);
			    	}
				    else {
				    	_jumpField.setFocusable(true);
			    		_jumpField.requestFocusInWindow();
				    }
			    }
			}, AWTEvent.MOUSE_EVENT_MASK);
			
			// Release focus and jump to slide on enter pressed:
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						int pageNr = Integer.parseInt(getText()) - 1;
						_presentation.setActiveSlide(_presentation.getSlides().get(pageNr));
					} catch (Exception exception) {
						JumpField.super.setText("");
					}
					if (_previouslyFocused != null)
						_previouslyFocused.requestFocusInWindow();
			    	_previouslyFocused = null;
				}
			});
			
			// Clear text on mouse click, remember the original focus holder:
			addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {
					_previouslyFocused = e.getOppositeComponent();
					JumpField.super.setText("");
				}
		
				@Override
				public void focusLost(FocusEvent e) {
					if (_presentation.getActiveSlide() != null)
						JumpField.super.setText(Integer.toString(_presentation.getSlides().indexOf(_presentation.getActiveSlide()) + 1));
					else 
						JumpField.super.setText("");
				}
			});
		}
	}
    
	private class ThumbnailMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			SlideViewPanel slideViewPanel = (SlideViewPanel) e.getComponent();
			_presentation.setActiveSlide(slideViewPanel.getSlide());
		}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {}

		@Override
		public void mousePressed(MouseEvent e) {
			// Choose slide
			if (_scrollPane.isScrolling()) {
				_isScrolling = true;
			} else {
				_isScrolling = false;
				_tentativeFontLabel = (SlideViewPanel) e.getSource();
				_tentativePoint.x = e.getXOnScreen();
				_tentativePoint.y = e.getYOnScreen();
			}
						
			// Handle inertial scroll event
			_scrollPane.handleMouseEvent(e);	
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// Handle inertia scroll event
			_scrollPane.handleMouseEvent(e);
			
			// Choose slide
			if (!_isScrolling) {
				Object source = e.getSource();
				if (_tentativeFontLabel.equals(source)) {
					SlideViewPanel slideViewPanel = (SlideViewPanel) e.getComponent();
					_tentativeHelpPoint.x = e.getXOnScreen();
					_tentativeHelpPoint.y = e.getYOnScreen();
					if (_tentativePoint.distance(_tentativeHelpPoint) < SHAKE_THRESHOLD)
						_presentation.setActiveSlide(slideViewPanel.getSlide());	
				}
			}
		}
	}
	
	private class ThumbnailMouseMotionListener implements MouseMotionListener {

		@Override
		public void mouseDragged(MouseEvent e) {
			// Handle inertial scroll event:
			_scrollPane.handleMouseMotionEvent(e);
		}

		@Override
		public void mouseMoved(MouseEvent e) {}
	}

	private class PresentationPropertyChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent e) {
			
			// Slide removed, remove thumbnail
			if (e.getPropertyName().equals("slides")) {	
				
				// Delete thumbnail for each removed slide:
				ListIterator<SlideViewPanel> it = _thumbnails.listIterator();
				while (it.hasNext()) { 
					SlideViewPanel thumbnail = it.next();
					
					if (!_presentation.getSlides().contains(thumbnail.getSlide())) {
						_innerPanel.remove(thumbnail);
						it.remove();
					}
				}
				
				// Add thumbnail for each new slide
				if (_presentation.getActiveSlide() != null) {
					
					ListIterator<SlideViewPanel> it2 = _thumbnails.listIterator();
					for (Slide slide : _presentation.getSlides()) {

						if (getThumbnailPanel(slide) == null){
							SlideViewPanel thumbnail = new SlideViewPanel(_presentation.getActiveSlide(), _presentation.getActiveSlide().getSize(), new Grid(Grid.Type.NONE, 0), Document.PRIO_THUMBNAIL);
							thumbnail.addMouseListener(_thumbnailMouseListener);
							thumbnail.addMouseMotionListener(_thumbnailMouseMotionListener);
							it2.add(thumbnail);
						}
						
						if (it2.hasNext()){
							it2.next();
						}
					}
										
					for (JPanel thumbnail : _thumbnails) 
						_innerPanel.remove(thumbnail);
					
					
					int innerPanelWidth = _innerPanel.getWidth() - _scrollPane.getVerticalScrollBar().getWidth() - 2;;
					double innerPanelHeight = _thumbnails.size()*GAP_HEIGHT;
					int size = _innerPanel.getWidth();
					for (SlideViewPanel thumbnail : _thumbnails) {
						_innerPanel.add(thumbnail);
						//thumbnail.setPreferredSize(new Dimension(size, size));
						
						double height = size * thumbnail.getSlide().getSize().getHeight()/thumbnail.getSlide().getSize().getWidth();
						//if (height / size > 1) height = size;
						int width = (int)(height * thumbnail.getSlide().getSize().getWidth()/thumbnail.getSlide().getSize().getHeight());
						innerPanelHeight += height;
						
						thumbnail.setPreferredSize(new Dimension(width, (int)height));
						thumbnail.setMinimumSize(new Dimension(0, 0));
					}
					_innerPanel.setPreferredSize(new Dimension(innerPanelWidth, (int)innerPanelHeight));

					
					repaint();
				}
				
				_innerPanel.validate();
				repaint();
			}
			else if (e.getPropertyName().equals("activeSlide")) {
				if (_presentation.getActiveSlide() != null) {
					
					// Show active slide number:
					_jumpField.setText(Integer.toString(_presentation.getSlides().indexOf(e.getNewValue()) + 1));
					
					// Update focus border for the active slide:
					Slide prevActiveSlide = (Slide) e.getOldValue();
					if (prevActiveSlide != null) {
						SlideViewPanel thumbnail = getThumbnailPanel(prevActiveSlide); 
						if (thumbnail != null) {
							thumbnail.setBorder(BorderFactory.createEmptyBorder());
						}
					}
					SlideViewPanel thumbnail = getThumbnailPanel(_presentation.getActiveSlide()); 
					if (thumbnail != null) {
						thumbnail.setBorder(BorderFactory.createLineBorder(COLOR_SELECTED, 4));
					}
					
					// Scroll into view:
					SlideViewPanel _thumbnail = getThumbnailPanel(_presentation.getActiveSlide());

					//System.out.println(new Rectangle(0, _thumbnail.getY()-50, 1, _thumbnail.getY() + getHeight()-50).toString());
					if (_thumbnail != null) {
						float thumbnailsVisible = getHeight()/(_thumbnail.getHeight()+GAP_HEIGHT);
						//just display first slide if there a only two slides
						if(thumbnailsVisible<3) {
							_innerPanel.scrollRectToVisible(new Rectangle(0, _thumbnail.getY()-7, 1, getHeight()-50));//_thumbnail.getHeight()));
						}else { //else center focus
							int offset = ((int)Math.floor(thumbnailsVisible/2)+1) * (thumbnail.getHeight()+GAP_HEIGHT);
							//int heightHalf = getHeight()/2-_thumbnail.getHeight();
							_innerPanel.scrollRectToVisible(new Rectangle(0, _thumbnail.getY() - (getHeight()-offset) + GAP_HEIGHT + 15, 1, getHeight()));//_thumbnail.getHeight()));
						}
						
						//_innerPanel.repaint();
						//_innerPanel.scrollRectToVisible(new Rectangle(0, _thumbnail.getY()-50, 1, _thumbnail.getY() + getHeight()-50));//_thumbnail.getHeight()));
					}
					
					if (_presentation.getActiveSlide().getClass().equals(WhiteboardSlide.class))
						_buttonDelete.setEnabled(true);
					else
						_buttonDelete.setEnabled(false);
				}
				else {
					_jumpField.setText("");
					_buttonDelete.setEnabled(false);
				}
			}
		}
	}
	
	class NavigationPanelResizedListener implements ComponentListener {
		@Override
		public void componentResized(ComponentEvent event) {
			int size = event.getComponent().getWidth() -  
				_scrollPane.getVerticalScrollBar().getWidth() - 4;
			
			for (JPanel thumbnail : _thumbnails) 
				_innerPanel.remove(thumbnail);
			
			
			int innerPanelWidth = size;
			double innerPanelHeight = _thumbnails.size()*GAP_HEIGHT;
			
			for (SlideViewPanel thumbnail : _thumbnails) {
				_innerPanel.add(thumbnail);
				//thumbnail.setPreferredSize(new Dimension(size, size));
				
				double height = size * thumbnail.getSlide().getSize().getHeight()/thumbnail.getSlide().getSize().getWidth();
				//if (height / size > 1) height = size;
				int width = (int)(height * thumbnail.getSlide().getSize().getWidth()/thumbnail.getSlide().getSize().getHeight());
				innerPanelHeight += height;
				
				thumbnail.setPreferredSize(new Dimension(width, (int)height));
				thumbnail.setMinimumSize(new Dimension(0, 0));
			}
			_innerPanel.setPreferredSize(new Dimension(innerPanelWidth, (int)innerPanelHeight));
		
			repaint();
			validate();
			
			// Scroll into view:
			SlideViewPanel _thumbnail = getThumbnailPanel(_presentation.getActiveSlide());
			if (_thumbnail != null) {
				float thumbnailsVisible = getHeight()/(_thumbnail.getHeight()+GAP_HEIGHT);
				//just display first slide if there a only two slides
				if(thumbnailsVisible<3) {
					_innerPanel.scrollRectToVisible(new Rectangle(0, _thumbnail.getY()-7, 1, getHeight()-50));//_thumbnail.getHeight()));
				}else { //else center focus
					int offset = ((int)Math.floor(thumbnailsVisible/2)+1) * (_thumbnail.getHeight()+GAP_HEIGHT);
					//int heightHalf = getHeight()/2-_thumbnail.getHeight();
					_innerPanel.scrollRectToVisible(new Rectangle(0, _thumbnail.getY() - (getHeight()-offset) + GAP_HEIGHT + 15, 1, getHeight()));//_thumbnail.getHeight()));
				}
			}
		}
		
		@Override
		public void componentHidden(ComponentEvent e) {}
		@Override
		public void componentMoved(ComponentEvent e) {}
		@Override
		public void componentShown(ComponentEvent e) {}
		
	}
	public Presentation getPresentation() {
		return _presentation;
	}
	
	public void dispose() {
		for (SlideViewPanel thumbnail : _thumbnails) {
			thumbnail.dispose();
		}
	}
	
	private SlideViewPanel getThumbnailPanel(Slide slide) {
		for (SlideViewPanel thumbnail : _thumbnails) {
				if (thumbnail.getSlide().equals(slide)) {
					return thumbnail;
				}
		}
		return null;
	}
}
