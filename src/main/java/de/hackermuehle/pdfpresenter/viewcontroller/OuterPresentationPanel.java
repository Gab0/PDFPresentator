package de.hackermuehle.pdfpresenter.viewcontroller;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.hackermuehle.pdfpresenter.model.State;
import de.hackermuehle.pdfpresenter.model.slide.Grid;
import de.hackermuehle.pdfpresenter.model.slide.Slide;
import de.hackermuehle.pdfpresenter.viewcontroller.viewpanel.InputViewPanel;
import de.hackermuehle.pdfpresenter.viewcontroller.viewpanel.MasterInputPanel;
import de.hackermuehle.pdfpresenter.viewcontroller.viewpanel.SlaveInputPanel;

public class OuterPresentationPanel extends PresentationPanel {
	private final class GridPropertyChangeListener implements
			PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("showGridExternal")) {
				_showGrid = getState().doesShowGridOnExternalMonitor();
				gridChanged();
			}
			
		}
	}

	private static final long serialVersionUID = 1L;
	private MasterInputPanel _masterInputPanel;
	private SlaveInputPanel _slaveInputPanel;
	
	private boolean _showGrid;
	
	public OuterPresentationPanel(State state, MasterInputPanel masterInputPanel) {
		super(state);

		setBackground(Color.black);
		_masterInputPanel = masterInputPanel;
		setMasterInputPanel(masterInputPanel);
		
		_showGrid = state.doesShowGridOnExternalMonitor();
		
		state.addPropertyChangeListener(new GridPropertyChangeListener());
	}

	protected InputViewPanel createInputPanel(Slide slide, Rectangle2D source, State controller) {
		if (_slaveInputPanel == null) _slaveInputPanel = new SlaveInputPanel(_masterInputPanel, source);
		return _slaveInputPanel;
	}
	
	public void setMasterInputPanel(MasterInputPanel masterInputPanel) {
		_masterInputPanel = masterInputPanel;
		if (_slaveInputPanel != null) _slaveInputPanel.setMaster(masterInputPanel);
	}
	
	protected void gridChanged() {
		if(_showGrid) {
			if (_masterInputPanel == null ||
				_masterInputPanel.getClipping() == null) return;
			
			// Grid adaption:
			if (getState().getActivePresentation() != null &&
				getViewPanel() != null && getViewPanel().getClipping() != null) {
				
				Grid grid = new Grid(getState().getGrid().getType(), getState().getGrid().getDistance() * _masterInputPanel.getClipping().getInverseTransform().getScaleX(), getState().getPreferences());
				getViewPanel().setGrid(getState().getActivePresentation().getGridVisibility() ? grid : null);
			}
		} else {
			//suggestion?
			if(getViewPanel()!=null) {
				getViewPanel().setGrid(null);
			}
		}
	}
}
