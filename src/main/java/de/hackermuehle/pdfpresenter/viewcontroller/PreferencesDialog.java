package de.hackermuehle.pdfpresenter.viewcontroller;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.Hashtable;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import de.hackermuehle.pdfpresenter.PdfPresenter;
import de.hackermuehle.pdfpresenter.model.Preferences;
import de.hackermuehle.pdfpresenter.model.State;
import de.hackermuehle.pdfpresenter.model.slide.Grid;
import de.hackermuehle.pdfpresenter.model.slide.Slide;
import de.hackermuehle.pdfpresenter.model.slide.Grid.Type;


/**
 * The preferences dialog
 * 
 * @author shuo
 *
 */
public class PreferencesDialog extends JDialog {
		
	private static final long serialVersionUID = -5220021393136269373L;
	
	// Grid
	private JRadioButton _checkeredGridButton;
	private JRadioButton _linedGridButton;
	private JSlider _gridDensitySlider;
	private JCheckBox _showGridOnExternalMonitorCheckBox;
	
	// Annotation margin
	private JSlider _annotationMarginSizeSlider;
	// Misc
//	JRadioButton _leftPositionButton;
//	JRadioButton _rightPositionButton;
	private JRadioButton _optimizedForPenButton;
	private JRadioButton _optimizedForMouseButton;
	private JCheckBox _showFileListCheckBox;
	
	private State _state;
	private Preferences _preferences;
	private boolean _wasNothingOpen = false;
	
	// Backup for cancel
	private boolean _gridWasOn;
	private Grid _originalGrid;
	private boolean _wasGridOriginallyShownOnExternalMonitor;

	private boolean _annotationMarginWasOn = true;
	private double _originalAnnotationMarginRatio;
	private Rectangle2D _originalAnnotationMarginSize;
	
	private boolean _wasGridOriginallyCheckered;
	private boolean _wasOriginallyOptimizedForPen;
	private boolean _didOriginallyShowFileListCheckBox;
	
	private boolean _wasOriginallyZoomedIn;
	private Rectangle2D _originalZoomRect;		
		
	public PreferencesDialog(Frame frame, State state) {
		super(frame);
		_state = state;
		_preferences = state.getPreferences();
		Grid grid = state.getGrid();
		_originalGrid = new Grid(grid.getType(), grid.getDistance(), _preferences);
		
		// Save state before opening preferences
		if (_state.getActivePresentation() != null) {
			// Grid
			_gridWasOn = _state.getActivePresentation().getGridVisibility();
			
			// Annotation margin
			Slide activeSlide = _state.getActivePresentation().getActiveSlide();
			_originalAnnotationMarginSize = activeSlide.getSize();
			if (_originalAnnotationMarginSize.equals(activeSlide.getDefaultSize())) {
				_annotationMarginWasOn = false;
			}
			
			_originalZoomRect = state.getActivePresentation().getSource();
			_wasOriginallyZoomedIn = !_originalZoomRect.equals(_originalAnnotationMarginSize);
		} else {
			_wasNothingOpen = true;
		}
		
		_wasGridOriginallyShownOnExternalMonitor = state.doesShowGridOnExternalMonitor();
		
		// Set window properties
		setTitle(PdfPresenter.getLocalizedString("pfTitle"));
		setFocusable(true);
		setResizable(false);
		setModal(true);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				cancel();
			}
		});
		
		
		addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {}
			
			@Override
			public void keyReleased(KeyEvent e) {}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// Ctrl/Cmd-W saves and closes. Esc cancels
				int controlOrCommandKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
				if (e.getKeyCode() == KeyEvent.VK_W && e.getModifiers() == controlOrCommandKeyMask) {
					dismissThisDialog();
				} else
					if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					cancel();
				}
			}
		});
		
		
		// Add widgets
		JPanel panel = new JPanel(
				new MigLayout("wrap 3, insets 12 10 10 10", 
						"[right]10[left]50[left]", 
						""));
		
		addGridStyleWidgets(panel);
		addSeparator(panel);
		addAnnotationMarginWidgets(panel);
		addSeparator(panel);
		addMiscWidgets(panel);
		addGoodOldButtons(panel);
		loadSavedValues();
		
		getContentPane().add(panel);
		
		
		if (!_gridWasOn) flashGrid(1500);
		if (!_annotationMarginWasOn) flashAnnotationMargin(1500);
	
		
		// Show
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}


	private void flashGrid(int ms) {
		if (!_wasNothingOpen) {
			_state.getActivePresentation().setGridVisibility(true);
			restoreOriginalGridStateDelayed(ms);
		}
	}
	
	
	private void flashAnnotationMargin(int ms) {
		if (!_wasNothingOpen && !_annotationMarginWasOn) {
			setAnnotationMarginRatio(_originalAnnotationMarginRatio);
			removeAnnotationMarginDelayed(ms);
		}
	}
	
	
	private void loadSavedValues() {
		Grid grid = _state.getGrid();

		if (grid.getType().equals(de.hackermuehle.pdfpresenter.model.slide.Grid.Type.BOTH)) {
			_checkeredGridButton.setSelected(true);
			_linedGridButton.setSelected(false);
			
			_wasGridOriginallyCheckered = true;
		} else {
			_checkeredGridButton.setSelected(false);
			_linedGridButton.setSelected(true);
			
			_wasGridOriginallyCheckered = false;
		}
		_gridDensitySlider.setValue((int) grid.getDistance());
		_showGridOnExternalMonitorCheckBox.setSelected(_state.doesShowGridOnExternalMonitor());
		
		
		// TODO V2: get directly from grid tool
		String ratioString = _state.getPreferences().getPreference("general.annotationmargin.ratio");
		if (ratioString == null) ratioString = "0.333";
		
		try {
			_originalAnnotationMarginRatio = Double.valueOf(ratioString);
		} catch (NumberFormatException exception) {
			Logger.getLogger(getClass()).warn("Load annotation margin ratio failed. " + 
					exception.getLocalizedMessage());
			_originalAnnotationMarginRatio = 0.333;
		}
		_annotationMarginSizeSlider.setValue((int) (_originalAnnotationMarginRatio * 100));
	}


	private void addSeparator(JPanel panel) {
		JSeparator line = new JSeparator(JSeparator.HORIZONTAL);

		panel.add(line, "growx, span 3, gaptop 8, gapbottom 8");
	}

	private void restoreOriginalGridStateDelayed(int ms) {
		Timer timer = new Timer(0, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!_wasNothingOpen)
					_state.getActivePresentation().setGridVisibility(_gridWasOn);
			}
		});
		timer.setInitialDelay(ms);
		timer.setRepeats(false);
		timer.start();
	}

	
	private void addGridStyleWidgets(JPanel panel) {
		// Label
		JLabel gridLabel = new JLabel(PdfPresenter.getLocalizedString("pfGridTitle"));
		gridLabel.setFont(gridLabel.getFont().deriveFont(Font.BOLD));
		
		
		// Radio buttons
		// JRadioButton replaces the button with the icon, instead of adding the icon
		// to the label. To do so, we have to define an HTML fragment with an icon as the label
		URL url = PreferencesDialog.class.getResource("/checkered.png");
		_checkeredGridButton = new JRadioButton(
				"<html><img src=" + url + " /></html>", true);
		_checkeredGridButton.addChangeListener(new CheckeredButtonChangeListener());
		_checkeredGridButton.setFocusable(false);
		_checkeredGridButton.setRolloverEnabled(false);
		
		url = PreferencesDialog.class.getResource("/lined.png");
		_linedGridButton = new JRadioButton(
				"<html><img src=" + url + " /></html>");
		_linedGridButton.addChangeListener(new LinedButtonChangeListener());
		_linedGridButton.setFocusable(false);
		_linedGridButton.setRolloverEnabled(false);		
		
		// Group the radio buttons
		ButtonGroup group = new ButtonGroup();
		group.add(_checkeredGridButton);
		group.add(_linedGridButton);
		
		// Density slider
		_gridDensitySlider = new JSlider(JSlider.HORIZONTAL, 10, 100, 50);
		_gridDensitySlider.setMajorTickSpacing(18);
		_gridDensitySlider.setPaintTicks(true);
		_gridDensitySlider.setPreferredSize(new Dimension(265, 60));
		_gridDensitySlider.setPaintLabels(true);
		_gridDensitySlider.setInverted(true);
		_gridDensitySlider.setFocusable(false);
		
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(10, new JLabel(new ImageIcon(
				PreferencesDialog.class.getResource("/grid_more_dense.png"))));
		labelTable.put(100, new JLabel(new ImageIcon(
				PreferencesDialog.class.getResource("/grid_less_dense.png"))));
		_gridDensitySlider.setLabelTable(labelTable);

		// Behavior
		_gridDensitySlider.addMouseListener(new GridDensityMouseListener());
		
		_gridDensitySlider.addChangeListener(new GridDensitySliderChangeListener());
		
		JLabel styleLabel = new JLabel(PdfPresenter.getLocalizedString("pfGridStyle"));
		JLabel densityLabel = new JLabel(PdfPresenter.getLocalizedString("pfGridDensity"));
		
		JLabel showGridOnExternalMonitorLabel = new JLabel(
				PdfPresenter.getLocalizedString("pfShowGridOnExternalMonitorLabel"));
		_showGridOnExternalMonitorCheckBox = new JCheckBox(
				PdfPresenter.getLocalizedString("pfShowGridOnExternalMonitorCheckBox"));
		_showGridOnExternalMonitorCheckBox.addItemListener(new ShowGridOnExternalMonitorItemListener());		
		_showGridOnExternalMonitorCheckBox.setFocusable(false);

		panel.add(gridLabel, "wrap");
		panel.add(styleLabel);
		panel.add(_checkeredGridButton);
		panel.add(_linedGridButton, "wrap");
		panel.add(densityLabel);
		panel.add(_gridDensitySlider, "span 2, gapright 5");
		panel.add(showGridOnExternalMonitorLabel);
		panel.add(_showGridOnExternalMonitorCheckBox, "span 2");
	}


	private void addAnnotationMarginWidgets(JPanel panel) {
		// Label
		JLabel sectionLabel = new JLabel(PdfPresenter.getLocalizedString("pfNoteMarginTitle"));
		sectionLabel.setFont(sectionLabel.getFont().deriveFont(Font.BOLD));

		// Size slider
		_annotationMarginSizeSlider = new JSlider(JSlider.HORIZONTAL, 10, 100, 50);
		_annotationMarginSizeSlider.setMajorTickSpacing(18);
		_annotationMarginSizeSlider.setPaintTicks(true);
		_annotationMarginSizeSlider.setPreferredSize(new Dimension(265, 60));
		_annotationMarginSizeSlider.setPaintLabels(true);
		_annotationMarginSizeSlider.setInverted(true);
		_annotationMarginSizeSlider.setFocusable(false);
		
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(10, new JLabel(new ImageIcon(
				PreferencesDialog.class.getResource("/annotationmargin_smaller.png"))));
		labelTable.put(100, new JLabel(new ImageIcon(
				PreferencesDialog.class.getResource("/annotationmargin_bigger.png"))));
		_annotationMarginSizeSlider.setLabelTable(labelTable);

		
		// Behavior
		_annotationMarginSizeSlider.addChangeListener(new AnnotationMarginSliderChangeListener());
		_annotationMarginSizeSlider.addMouseListener(new AnnotationMarginSliderMouseListener());
		
		
		JLabel sizeLabel = new JLabel(PdfPresenter.getLocalizedString("pfNoteMarginSize"));
		
		panel.add(sectionLabel, "wrap");
		panel.add(sizeLabel);
		panel.add(_annotationMarginSizeSlider, "span 2, gapright 5");
	}

	
	private void setAnnotationMarginRatio(double ratio) {
		Slide slide = _state.getActivePresentation().getActiveSlide();

		// Reset
		slide.setSize(slide.getDefaultSize());
		// Renew
		Rectangle2D defaultSize = slide.getDefaultSize();
		Rectangle2D size = slide.getSize();
		size.setRect(0, 0, size.getWidth() + defaultSize.getWidth() * ratio, size.getHeight() + defaultSize.getHeight() * ratio);
		slide.setSize(size);
		
		// Show entire slide:
		_state.getActivePresentation().setSource(slide.getSize());
	}
	
	
	private void removeAnnotationMargin() {
		Slide slide = _state.getActivePresentation().getActiveSlide();

		slide.setSize(_originalAnnotationMarginSize);
		_state.getActivePresentation().setSource(slide.getSize());
		
		// Restore zoom
		if (_wasOriginallyZoomedIn)
			_state.getActivePresentation().setSource(_originalZoomRect);

	}
	
	
	private void removeAnnotationMarginDelayed(int ms) {
		Timer timer = new Timer(0, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!_wasNothingOpen)
					removeAnnotationMargin();
			}
		});
		timer.setInitialDelay(ms);
		timer.setRepeats(false);
		timer.start();
	}

	

		
	private void addMiscWidgets(JPanel panel) {
		// Label
		JLabel sectionLabel = new JLabel(PdfPresenter.getLocalizedString("pfMisc"));
		sectionLabel.setFont(sectionLabel.getFont().deriveFont(Font.BOLD));
	
		// Position of Navigation Bar
//		_leftPositionButton = new JRadioButton("Left");
//		
//		_rightPositionButton = new JRadioButton("Right");
//		_rightPositionButton.setSelected(true); // Should be loaded from preferences
//		
//		// Group the radio buttons
//		ButtonGroup group = new ButtonGroup();
//		group.add(_leftPositionButton);
//		group.add(_rightPositionButton);
		
		
		// Show recently opened list upon startup
		_showFileListCheckBox = new JCheckBox(PdfPresenter.getLocalizedString("pfShowRecents"));
		_showFileListCheckBox.setSelected(_state.getShowRecents());
		// TODO use item listener
		_showFileListCheckBox.addChangeListener(new ShowRecentsChangeListener());
		_showFileListCheckBox.setFocusable(false);
		_didOriginallyShowFileListCheckBox = _state.getShowRecents();
		
		// Optimize for pen
		_optimizedForPenButton = new JRadioButton(PdfPresenter.getLocalizedString("pfPen"));
		_optimizedForPenButton.addChangeListener(new OptimizeForPenButtonChangeListener());
		_optimizedForPenButton.setFocusable(false);
		
		_optimizedForMouseButton = new JRadioButton(PdfPresenter.getLocalizedString("pfMouse"));
		_optimizedForMouseButton.addChangeListener(new OptimizeForMouseButtonChangeListener());
		_optimizedForMouseButton.setFocusable(false);
		
		if (_state.getOptimizeForPen())
			_optimizedForPenButton.setSelected(true);
		else
			_optimizedForMouseButton.setSelected(true);
		_wasOriginallyOptimizedForPen = _state.getOptimizeForPen();
		
		// Group the radio buttons
		ButtonGroup group = new ButtonGroup();
		group.add(_optimizedForPenButton);
		group.add(_optimizedForMouseButton);
		
		JLabel scrollingOptimizationLabel = new JLabel(PdfPresenter.getLocalizedString("pfScrollingOptimizedFor"));
		JLabel privacyLabel = new JLabel(PdfPresenter.getLocalizedString("pfPrivacy"));
		
		panel.add(sectionLabel, "wrap");
		panel.add(privacyLabel);
		panel.add(_showFileListCheckBox, "span 2");
		panel.add(scrollingOptimizationLabel, "gapleft 5");
		panel.add(_optimizedForPenButton);
		panel.add(_optimizedForMouseButton);
	}

	private void addGoodOldButtons(JPanel panel) {		
		// Create buttons
		JButton resetButton = new JButton(PdfPresenter.getLocalizedString("pfResetButton"));
		resetButton.addActionListener(new ResetButtonActionListener());
		resetButton.setFocusable(false);
		
		JButton cancelButton = new JButton(PdfPresenter.getLocalizedString("pfCancelButton"));
		cancelButton.addActionListener(new CancelButtonActionListener());
		
		JButton okButton = new JButton(PdfPresenter.getLocalizedString("pfOkButton"));
		okButton.addActionListener(new OkButtonActionListener());
		
		getRootPane().setDefaultButton(okButton);
		
		
		panel.add(resetButton, "align right, gaptop 20");
		panel.add(cancelButton, "split 2, span, center");
		panel.add(okButton, "span 2, center");
	}


	private final class ShowGridOnExternalMonitorItemListener implements
			ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			boolean selected = e.getStateChange() == ItemEvent.SELECTED;
			_state.setShowGridOnExternalMonitor(selected);
			if (!_gridWasOn) flashGrid(300);
		}
	}


	private final class LinedButtonChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			System.out.println(_linedGridButton.getModel().isSelected());

			if (_linedGridButton.isSelected()) {
          _state.setGrid(new Grid(de.hackermuehle.pdfpresenter.model.slide.Grid.Type.VERTICAL,
					_state.getGrid().getDistance()));


				if (!_wasNothingOpen)
					_state.getActivePresentation().setGridVisibility(true);

				restoreOriginalGridStateDelayed(300);
			}
			
		}
	}


	private final class CheckeredButtonChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			System.out.println(_checkeredGridButton.getModel().isSelected());
			
			if (_checkeredGridButton.isSelected()) {
				_state.setGrid(new Grid(de.hackermuehle.pdfpresenter.model.slide.Grid.Type.BOTH, 
					_state.getGrid().getDistance()));

				if (!_wasNothingOpen)
					_state.getActivePresentation().setGridVisibility(true);

				restoreOriginalGridStateDelayed(300);
			}
		}
	}


	private final class AnnotationMarginSliderMouseListener implements
			MouseListener {
		@Override
		public void mouseReleased(MouseEvent e) {
			if (!_wasNothingOpen && !_annotationMarginWasOn)
				removeAnnotationMarginDelayed(300);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (!_wasNothingOpen)
				setAnnotationMarginRatio(((double) _annotationMarginSizeSlider.getValue()) / 100.0);
		}

		@Override
		public void mouseExited(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseClicked(MouseEvent e) {}
	}


	private final class AnnotationMarginSliderChangeListener implements
			ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			double ratio = (double) _annotationMarginSizeSlider.getValue() / 100;
			
			if (!_wasNothingOpen) {
				setAnnotationMarginRatio(ratio);
			}
			
			_preferences.setPreference("general.annotationmargin.ratio", String.valueOf(ratio));
		}
	}


	private final class OptimizeForMouseButtonChangeListener implements
			ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			_state.setOptimizeForPen(false);
		}
	}


	private final class OptimizeForPenButtonChangeListener implements
			ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			_state.setOptimizeForPen(_optimizedForPenButton.isSelected());
		}
	}


	private final class ShowRecentsChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			_state.setShowRecents(_showFileListCheckBox.isSelected());				
		}
	}


	private final class GridDensitySliderChangeListener implements
			ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {	
			int value = _gridDensitySlider.getValue();
			_state.setGrid(new Grid(
				_state.getGrid().getType(), value));

		}
	}


	private final class GridDensityMouseListener implements MouseListener {
		@Override
		public void mouseReleased(MouseEvent e) {
			restoreOriginalGridStateDelayed(300);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (!_wasNothingOpen)
				_state.getActivePresentation().setGridVisibility(true);
		}

		@Override
		public void mouseExited(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseClicked(MouseEvent e) {}
	}

	
	class CancelButtonActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			cancel();
		}
		
	}
	
	private void cancel() {
		// Grid
		if (_wasGridOriginallyCheckered)
			_checkeredGridButton.setSelected(true);
		else 
			_linedGridButton.setSelected(true);
		
		_gridDensitySlider.setValue((int) _originalGrid.getDistance());
		_showGridOnExternalMonitorCheckBox.setSelected(_wasGridOriginallyShownOnExternalMonitor);
		
		// Margin
		_annotationMarginSizeSlider.setValue((int) (_originalAnnotationMarginRatio * 100));
		
		// Misc
		_showFileListCheckBox.setSelected(_didOriginallyShowFileListCheckBox);
		if (_wasOriginallyOptimizedForPen)
			_optimizedForPenButton.setSelected(true);
		else
			_optimizedForMouseButton.setSelected(true);
		
		dismissThisDialog();
		
		// Feedback to user
		// TODO flash only when settings changed ?
		if (!_gridWasOn) flashGrid(700);
		if (!_annotationMarginWasOn) flashAnnotationMargin(700);
	}
	

	class OkButtonActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			dismissThisDialog();
		}
	}
	
	private void dismissThisDialog() {
		if (!_wasNothingOpen) {
			// TODO Should be in listeners
			// XXX should not be using event queue / remove event queue?
			// check getActivePresentation != null is not very clean
			if (!_annotationMarginWasOn) {
				Slide slide = _state.getActivePresentation().getActiveSlide();
				slide.setSize(slide.getDefaultSize());

				_state.getActivePresentation().setSource(slide.getSize());
			} 

			// Restore zoom
			if (_wasOriginallyZoomedIn)
				_state.getActivePresentation().setSource(_originalZoomRect);
		}
		
		setVisible(false);
		dispose();
	}
	
	
	class ResetButtonActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {			
			// Grid
			_checkeredGridButton.setSelected(true);
			_gridDensitySlider.setValue(20);
			_state.setGrid(new Grid(Grid.DEFAULT_TYPE, Grid.DEFAULT_DENSITY, _preferences));
			_showGridOnExternalMonitorCheckBox.setSelected(false);
			
			// Annotation Margin
			_annotationMarginSizeSlider.setValue(50);
			
			// Misc
//			_rightPositionButton.setSelected(true);
			_showFileListCheckBox.setSelected(true);
			_optimizedForPenButton.setSelected(true);
			
			// Feedback to user
			if (!_gridWasOn) flashGrid(700);
			if (!_annotationMarginWasOn) flashAnnotationMargin(700);
		}
		
	}

}
