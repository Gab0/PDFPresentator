package de.hackermuehle.pdfpresenter.viewcontroller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;

import de.hackermuehle.pdfpresenter.PdfPresenter;
import de.hackermuehle.pdfpresenter.model.State;

/**
 * The main program window.
 * 
 * 	TODO V2 full screen mode - "Jeder Pixel zählt"
 */
@SuppressWarnings("restriction")
public class ApplicationFrame extends JFrame {
	
	//public static Display display;
	private static final long serialVersionUID = 1L;
	private static final int PREFERRED_MIN_WIDTH  = 810;
	private static final int PREFERRED_MIN_HEIGHT = 500;
	private JSplitPane _splitPane = null;
	private QuickAccessOutlinePanel _quickAccess;
	private State _state;
	
	public ApplicationFrame() {
		
		// Create and initialize the new runtime state (view and controller
		// are interdependent in this implementation):
		_state = new State();
		_state.addPropertyChangeListener(new StatePropertyChangeListener());
		
		// Initialize the application window:
		setTitle("PDF Presenter");
		setIconImage(ViewUtilities.loadImage("/applicationicon.png"));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(PREFERRED_MIN_WIDTH, PREFERRED_MIN_HEIGHT));
		setLocationAndSize();
		
		// Start maximized on Windows / Linux:
		if (!PdfPresenter.isOnMac()) {
			setLocationAndSize();
			setExtendedState(Frame.MAXIMIZED_BOTH);  
		}
		
		// MainBar:
	    add(new MainBar(this, _state), BorderLayout.NORTH);
	    
	   // Quick Access panel:
		_quickAccess = new QuickAccessOutlinePanel(this, _state);

		// SplitPane:
		 // NavigationPanel:
		NavigationPane navigationPane = new NavigationPane(_state);	
		
		 // Default view = navigation bar + presentation area:
	    MainPanel pagePanel = new MainPanel(_state); 
	    
	    // Provide minimum sizes for the two components in the split pane
	    pagePanel.setMinimumSize(new Dimension(0, 0));
	    navigationPane.setMinimumSize(new Dimension(0, 0));
	    
	    _splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pagePanel, navigationPane);
	    _splitPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
	    _splitPane.setOneTouchExpandable(false);
	    _splitPane.setDividerSize(15);
	    _splitPane.setResizeWeight(1.0);
	    

		if (_state.getActivePresentation() == null) {
			add(_quickAccess);
		}
		else {
			add(_splitPane);
		}
		
	    // Save Preferences on close:
	    addWindowListener(new WindowAdapter() {
	    	public void windowClosing(WindowEvent e) {
	    		_state.savePreferencesToDisk();
	    		dispose();
	    		System.exit(0);
	    	}
	    });
	    
	    
	    if (PdfPresenter.isOnMac()) {
	    	// Mac OS X specific settings
	    	Application application = Application.getApplication();
	    	// Handler for about, preferences and quit
	    	application.addApplicationListener(new OsXApplicationListener());
	    	// Enabled preferences menuitem in system-wide app menu
	    	application.setEnabledPreferencesMenu(true);
	    }
	    
	    setVisible(true);
	}
	
	
	private final class OsXApplicationListener implements ApplicationListener {
		@Override
		public void handleAbout(ApplicationEvent arg0) {
			arg0.setHandled(true);
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					new AboutDialog(ApplicationFrame.this);
				}
			});
		}

		@Override
		public void handleOpenApplication(ApplicationEvent arg0) {}

		@Override
		public void handleOpenFile(ApplicationEvent arg0) {}

		@Override
		public void handlePreferences(ApplicationEvent arg0) {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					new PreferencesDialog(null, _state);
				}
			});
		}

		@Override
		public void handlePrintFile(ApplicationEvent arg0) {}

		@Override
		public void handleQuit(ApplicationEvent arg0) {
			processWindowEvent(new WindowEvent(ApplicationFrame.this,
					WindowEvent.WINDOW_CLOSING));
		}

		@Override
		public void handleReOpenApplication(ApplicationEvent arg0) {}
	}
	
	
	/**
	 *  Listens to state events and reflects changes in the GUI.
	 */
	private class StatePropertyChangeListener implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals("activePresentation")) {
				
				if ((_state.getActivePresentation() != null) &&
					(_splitPane.getParent() == null)) {
		
					// Add before remove to ensure correct focus traversal:
				    add(_splitPane);
				    _splitPane.setDividerLocation(
				    		getExtendedState() == Frame.MAXIMIZED_BOTH ?
				    		GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width - 250 :
				    		getWidth() - 250);  
				    
				    remove(_quickAccess);
					validate();
					repaint();
				}
				else if (_state.getActivePresentation() == null) {
					remove(_splitPane);
					add(_quickAccess);
					validate();
					repaint();
				}
			} 
		}
	}
	
	/**
	 * Centers the frame on the screen.
	 */
	private void setLocationAndSize() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int height = Math.max(screenSize.height / 4 * 3, PREFERRED_MIN_HEIGHT);
		int width = Math.max(screenSize.width / 4 * 3, PREFERRED_MIN_WIDTH);
		setSize(width, height);
		
		setLocationRelativeTo(null);
	}
}
