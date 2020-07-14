package de.hackermuehle.pdfpresenter.viewcontroller;

import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import de.hackermuehle.pdfpresenter.model.State;
import de.hackermuehle.pdfpresenter.viewcontroller.viewpanel.MasterInputPanel;

/**
 * The audience view.
 * 
 * Contains an OuterPresentationViewPanel.
 */
public class ExternalFrame extends JFrame {
	private static final long serialVersionUID = 4300246975063006197L;
	private State _state;
	private OuterPresentationPanel _viewPanel;

	public ExternalFrame(State state, MasterInputPanel masterInputPanel, GraphicsConfiguration gc) {
		super(gc);
		_state = state;
		
		setUndecorated(true);
		setIconImage(ViewUtilities.loadImage("/applicationicon.png"));
		setLayout(new BorderLayout());
		
		_viewPanel = new OuterPresentationPanel(state, masterInputPanel);
		add(_viewPanel, BorderLayout.CENTER);
	
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				_state.setPresenting(false);
			}
		});
		
		// Present this panel on external display in full screen:
		/* Does not work properly, see http://forums.java.net/jive/thread.jspa?messageID=299090
		if (controller.getExternalDisplay().isFullScreenSupported()) {
			controller.getExternalDisplay().setFullScreenWindow(this);
		}
		else {
			throw new RuntimeException("Fullscreen not supported");
		}
		*/
		
		setVisible(true);
		setExtendedState(MAXIMIZED_BOTH);
	}
	
	public OuterPresentationPanel getOuterPresentationViewPanel() {
		return _viewPanel;
	}
	
	public void setMasterInputPanel(MasterInputPanel masterInputPanel) {
		_viewPanel.setMasterInputPanel(masterInputPanel);
	}
	
	@Override
	public void dispose() {
		_viewPanel.dispose();
		super.dispose();
	}
}
