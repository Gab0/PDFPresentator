package de.hackermuehle.pdfpresenter.model;

import java.awt.GraphicsDevice;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.hackermuehle.pdfpresenter.model.document.Document;
import de.hackermuehle.pdfpresenter.model.document.ImageDocument;
import de.hackermuehle.pdfpresenter.model.document.PdfDocument;
import de.hackermuehle.pdfpresenter.model.document.TextDocument;
import de.hackermuehle.pdfpresenter.model.slide.DocumentSlide;
import de.hackermuehle.pdfpresenter.model.slide.Grid;
import de.hackermuehle.pdfpresenter.model.slide.Slide;
import de.hackermuehle.pdfpresenter.model.slide.WhiteboardSlide;
import de.hackermuehle.pdfpresenter.model.tools.Eraser;
import de.hackermuehle.pdfpresenter.model.tools.Magnifier;
import de.hackermuehle.pdfpresenter.model.tools.Marker;
import de.hackermuehle.pdfpresenter.model.tools.Pen;
import de.hackermuehle.pdfpresenter.model.tools.TextTool;
import de.hackermuehle.pdfpresenter.model.tools.Tool;
import de.intarsys.pdf.parser.COSLoadException;

/**
 * Representation of the runtime state of the model.
 */
public class State {
	private PropertyChangeSupport _propertyChangeSupport = new PropertyChangeSupport(this); 
	
	private final LinkedList<Presentation> _presentations = new LinkedList<Presentation>();
	private Presentation _activePresentation;

	private final Eraser _eraser;
	private final Pen _pen;
	private final Marker _marker;
	private final TextTool _text;
	private final Magnifier _magnifier;
	private Tool _activeTool;
	private Tool _previousTool;
	
	private Grid _grid;
	
	private int _whiteboardNumber = 0;
	
	private GraphicsDevice _externalDisplay = null;
	private boolean _presenting = false;
	
	private boolean _showRecents;
	private boolean _optimizeForPen;
	private boolean _showGridOnExternalMonitor;
	
	private LinkedList<String> _recentFilenames = new LinkedList<String>();
	private String _presentationPath;
	
	// XXX Preferences:
	private static final String PREFERENCE_LIST_RECENT_FILES = "quickaccess.listrecentfiles";
	private static final String PREFERENCE_OPTIMIZE_FOR_PEN = "optimizeForPen";
	private static final String PREFERENCE_SHOW_GRID_ON_EXTERNAL_MONITOR = "showgridonexternalmonitor";
	private static final int NUMBER_RECENTS = 3;
	private static final String PREFERENCE_PATH = "filechooser.path";
	private static final String PREREFRENCE_RECENT_FILE_PREFIX = "filechooser.recent";
	private Preferences _preferences;
	
	/**
	 * Constructs and initializes the runtime state.
	 */
	public State() {
		_preferences = new Preferences();
		_grid = new Grid(_preferences);
		_pen = new Pen(_preferences);
		_marker = new Marker(_preferences);
		_text = new TextTool(this);
		_eraser = new Eraser(_preferences);
		_magnifier = new Magnifier();
		_activeTool = _pen;
		
		// XXX Preferences entkoppeln, siehe weiter unten.
		// Load listRecentFiles from preferences. Default true
		String listOrNotString = _preferences.getPreference(PREFERENCE_LIST_RECENT_FILES);
		if (listOrNotString != null && listOrNotString.equalsIgnoreCase("false"))
			_showRecents = false;
		else {
			_preferences.setPreference(PREFERENCE_LIST_RECENT_FILES, String.valueOf(true));
			_showRecents = true;
		}
		
		// Load showGridOnExternalMonitor. Default false
		String showGrid = _preferences.getPreference(PREFERENCE_SHOW_GRID_ON_EXTERNAL_MONITOR);		
		if (showGrid != null && showGrid.equalsIgnoreCase("true")) {
			_showGridOnExternalMonitor = true;
		} else {
			_preferences.setPreference(PREFERENCE_SHOW_GRID_ON_EXTERNAL_MONITOR, String.valueOf(false));
			_showGridOnExternalMonitor = false;
		}

		
		// Load file path from preferences:
		_presentationPath = _preferences.getPreference(PREFERENCE_PATH);
        if (_presentationPath == null) _presentationPath = "";

        // Load recent files from preferences:
        _recentFilenames = new LinkedList<String>();
        for (int i = 0; i < NUMBER_RECENTS; i++) {
            String fileName = _preferences.getPreference(PREREFRENCE_RECENT_FILE_PREFIX + i);
            if (fileName != null) {
                File file = new File(fileName);
                if (file.exists())
                	_recentFilenames.add(fileName);
            }
        }
		
		// Load optimizeForPen (inertial scroll speed) from preferences. Default true
		String optimizeForPenString = _preferences.getPreference(PREFERENCE_OPTIMIZE_FOR_PEN);
		if (optimizeForPenString != null && optimizeForPenString.equalsIgnoreCase("false"))
			_optimizeForPen = false;
		else {
			_preferences.setPreference(PREFERENCE_OPTIMIZE_FOR_PEN, String.valueOf(true));
			_optimizeForPen = true;
		}
	}
	
	/**
	 * @see {@link #setShowRecents(boolean)}
	 * @return List containing recently opened file names, if available.
	 */
	public List<String> getRecentFilenames() {
		return Collections.unmodifiableList(_recentFilenames);
	}
	
	/**
	 * Adds a new presentation to the list of opened presentations containing
	 * a slide containing the text of the given text file.
	 * 
	 * @see {@link TextDocument}
	 * @param fileName
	 * @throws IOException The file could not be opened / read / converted.
	 */
	public void openTextPresentation(String fileName) throws IOException {
		TextDocument textDocument = new TextDocument(fileName);
		Presentation presentation = new Presentation(textDocument.getTitle().equals("") ? extractFileName(fileName) : textDocument.getTitle());
		if (presentation.getTitle().length() > 13) 
			presentation.setTitle(presentation.getTitle().substring(0, 10) + "...");
		
		for (int i = 0; i < textDocument.getNumberOfPages(); ++i) {
			Slide slide = new DocumentSlide(textDocument, i);
			presentation.addSlide(slide);
		}
		presentation.setActiveSlide(presentation.getSlides().get(0));
		addPresentation(presentation);
		
		addRecent(fileName);
		// TODO: A document should be disposed immediately, before GC.
	}
	
	/**
	 * Adds a new presentation to the list of opened presentations containing
	 * a slide containing the given image.
	 * 
	 * @see {@link ImageDocument}
	 * @param fileName
	 * @throws IOException The file could not be opened / read / converted.
	 */
	public void openImagePresentation(String fileName) throws IOException, COSLoadException {
		ImageDocument imageDocument = new ImageDocument(fileName);
		Presentation presentation = new Presentation(imageDocument.getTitle().equals("") ? extractFileName(fileName) : imageDocument.getTitle());
		if (presentation.getTitle().length() > 13) 
			presentation.setTitle(presentation.getTitle().substring(0, 10) + "...");
		
		for (int i = 0; i < imageDocument.getNumberOfPages(); ++i) {
			Slide slide = new DocumentSlide(imageDocument, i);
			presentation.addSlide(slide);
		}
		presentation.setActiveSlide(presentation.getSlides().get(0));
		addPresentation(presentation);
		
		addRecent(fileName);
		// TODO: A document should be disposed immediately, before GC.
	}
	
	/**
	 * Adds a new presentation to the list of opened presentations containing
	 * a slide for each page of the given PDF file.
	 * 
	 * @see {@link PdfDocument}
	 * @param fileName
	 * @throws IOException The file could not be opened / read / converted.
	 * @throws COSLoadException The file is a PDF document that is not readable
	 */
	public void openPdfPresentation(String fileName) throws IOException, COSLoadException {
		PdfDocument pdfDocument = new PdfDocument(fileName);
		Presentation presentation = new Presentation(pdfDocument.getTitle().equals("") ? extractFileName(fileName) : pdfDocument.getTitle());
		if (presentation.getTitle().length() > 13) 
			presentation.setTitle(presentation.getTitle().substring(0, 10) + "...");
		
		for (int i = 0; i < pdfDocument.getNumberOfPages(); ++i) {
			Slide slide = new DocumentSlide(pdfDocument, i);
			presentation.addSlide(slide);
		}
		presentation.setActiveSlide(presentation.getSlides().get(0));
		addPresentation(presentation);
		
		addRecent(fileName);
		// TODO: A document should be disposed immediately, before GC.
	}
	
	/**
	 * Adds a new presentation to the list of opened presentations containing
	 * an empty {@link WhiteboardSlide}.
	 * 
	 * @param fileName
	 * @throws IOException The file could not be opened / read / converted.
	 */
	public void openWhiteboardPresentation() {
		Presentation presentation = new Presentation("Whiteboard " + ++_whiteboardNumber);
		presentation.addSlide(new WhiteboardSlide());
		addPresentation(presentation);
	}
	
	/**
	 * Adds a new presentation to the list of opened presentations containing
	 * a slide for each page of the given document.
	 * 
	 * @see {@link Document}
	 * @param fileName
	 * @throws IOException The file format is unknown (no pdf, images, text)
	 * @throws COSLoadException The file is a PDF document that is not readable
	 * @throws FileNotFoundException The file could not be located
	 */
	public void openPresentation(String fileName) throws IOException, COSLoadException, FileNotFoundException {
		
		File file = new File(fileName);
		if (!file.exists()) {
			removeRecent(fileName);
			throw new FileNotFoundException();
		}
	
		if (ImageDocument.isAcceptedFileName(fileName)) {
			openImagePresentation(fileName);
		} else if (PdfDocument.isAcceptedFileName(fileName)) {
			openPdfPresentation(fileName);
		} else if (TextDocument.isAcceptedFileName(fileName)) {
			openTextPresentation(fileName);
		} else {
			throw new IOException("Unsupported file format");
		}
	}
	
	/**
	 * Removes and disposes the given presentation from the list of opened
	 * presentations.
	 * 
	 * @param presentation
	 * @return true if the presentation was removed / was part of this state.
	 */
	public boolean removePresentation(Presentation presentation) {
		if (!_presentations.contains(presentation)) { 
			return false; 
		}
		if (presentation.equals(_activePresentation)) {
			Presentation oldValue = _activePresentation;
			
			int previousIndex = _presentations.indexOf(presentation) - 1;
			if (previousIndex < 0) _activePresentation = null;
			else _activePresentation = _presentations.get(previousIndex);
			
			_propertyChangeSupport.firePropertyChange("activePresentation", oldValue, _activePresentation);
		}
		
		_presentations.remove(presentation);
		_propertyChangeSupport.firePropertyChange("presentations", null, _presentations);
		
		presentation.dispose();
		return true;
	}
	
	/**
	 * @return The presentation in the list of opened presentations that is active.
	 */
	public Presentation getActivePresentation() {
		return _activePresentation;
	}
	
	public void setActivePresentation(Presentation presentation) {
		if(!_presentations.contains(presentation)) 
			return;
		Presentation tempPresentation = _activePresentation;
		_activePresentation = presentation;
		_propertyChangeSupport.firePropertyChange("activePresentation", tempPresentation, _activePresentation); 
	}
	
	public List<Presentation> getPresentations() {
		return Collections.unmodifiableList(_presentations);
	}
	
	/**
	 * Set the presenting state. A true presenting state indicates that the
	 * active slide of the active presentation should be presented on an 
	 * external display.
	 * 
	 * @param presenting The presenting state
	 */
	public void setPresenting(boolean presenting) {
		if (presenting != _presenting) {
			_presenting = presenting;
			_propertyChangeSupport.firePropertyChange("presenting", null, _presenting);
		}
	}
	
	/**
	 * @see {@link #setPresenting(boolean)}
	 * @return The presenting state
	 */
	public boolean isPresenting() {
		return _presenting;
	}
	
	/**
	 * The external display identifier used for presenting.
	 * 
	 * @param externalDisplay
	 */
	public void setExternalDisplay(GraphicsDevice externalDisplay) {
		_externalDisplay = externalDisplay;
		_propertyChangeSupport.firePropertyChange("externalDisplay", null, _externalDisplay);
	}
	
	/**
	 * @see {@link #setExternalDisplay(GraphicsDevice)}
	 * @return The external display identifier
	 */
	public GraphicsDevice getExternalDisplay() {
		return _externalDisplay;
	}
	
	/**
	 * The active tool to use for drawing on slides.
	 * @param tool
	 */
	public void setActiveTool(Tool tool) {
		//if (_activeTool != _magnifier) 
		_previousTool = _activeTool;
		
		_activeTool = tool;
		_propertyChangeSupport.firePropertyChange("activeTool", null, _activeTool);
	}
	
	/**
	 * @see {@link #setActiveTool(Tool)}
	 * @return The active tool
	 */
	public Tool getActiveTool() {
		return _activeTool;
	}
	
	/**
	 * @see {@link #getActiveTool()}
	 * @return The previously active tool
	 */
	public Tool getPreviousTool() {
		return _previousTool;
	}
	
	public Eraser getEraser() {
		return _eraser;
	}
	
	public Pen getPen() {
		return _pen;
	}
	
	public Marker getMarker() {
		return _marker;
	}
	
	public TextTool getTextTool() {
		return _text;
	}
	
	public Magnifier getMagnifier() {
		return _magnifier;
	}
	
	/**
	 * Sets the grid that should be used for drawing slides of presentations
	 * whose grid is enabled.
	 * 
	 * @param grid The grid that should be used for drawing slides
	 */
	public void setGrid(Grid grid) {
		_grid = grid;
		_propertyChangeSupport.firePropertyChange("grid", null, _grid);
	}
	
	/**
	 * @see {@link #setGrid(Grid)}
	 * @return The grid that should be used for drawing slides
	 */
	public Grid getGrid() {
		return (Grid) _grid.clone();
	}
	
	public void addPropertyChangeListener(PropertyChangeListener l) {
		_propertyChangeSupport.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		_propertyChangeSupport.removePropertyChangeListener(l);
	} 
	
	/**
	 * Enables / disables the saving of recently opened file names.
	 * 
	 * @see {@link #getRecentFilenames()}
	 * @param showRecents True to enable saving recent filenames
	 */
	public void setShowRecents(boolean showRecents) {
		if (showRecents != _showRecents) {
			_showRecents = showRecents;
			_preferences.setPreference(PREFERENCE_LIST_RECENT_FILES, String.valueOf(_showRecents));
			_propertyChangeSupport.firePropertyChange("showRecents", null, _showRecents);
		}
	}
	
	/**
	 * @see {@link #setShowRecents(boolean)}
	 * @return True if recent filenames are saved and shown.
	 */
	public boolean getShowRecents() {
		return _showRecents;
	}
	
	/**
	 * The optimize for pen state indicates that certain inputs should be
	 * configured for optimal pen input instead of mouse input.
	 * 
	 * @param optimizeForPen True to enable optimization
	 */
	public void setOptimizeForPen(boolean optimizeForPen) {
		if (!optimizeForPen == _optimizeForPen) {
			_optimizeForPen = optimizeForPen;
			_preferences.setPreference(PREFERENCE_OPTIMIZE_FOR_PEN, String.valueOf(_optimizeForPen));
			_propertyChangeSupport.firePropertyChange("optimizeForPen", null, _optimizeForPen);
		}
	}
	
	/**
	 * @see {@link #setOptimizeForPen(boolean)}
	 * @return The current optimize for pen state
	 */
	public boolean getOptimizeForPen() {
		return _optimizeForPen;
	}
	
	
	public void setShowGridOnExternalMonitor(boolean showGridOnExternalMonitor) {
		if (_showGridOnExternalMonitor != showGridOnExternalMonitor) {
			_showGridOnExternalMonitor = showGridOnExternalMonitor;
			_preferences.setPreference(PREFERENCE_SHOW_GRID_ON_EXTERNAL_MONITOR, String.valueOf(showGridOnExternalMonitor));
			_propertyChangeSupport.firePropertyChange("showGridExternal", null, showGridOnExternalMonitor);
		}
	}
	
	public boolean doesShowGridOnExternalMonitor() {
		return _showGridOnExternalMonitor;
	}
	
	
	
	/**
	 * Sets the base directory to read presentation files from. The GUI should
	 * make use of this state for enhanced end user comfort.
	 * 
	 * @param path
	 */
    public void setPresentationDirectory(String path) {
    	_presentationPath = path;
    	_propertyChangeSupport.firePropertyChange("presentationPath", null, _presentations);
    }
    
	/**
	 * @see {@link #setOptimizeForPen(String)}
	 * @return The current presentation directory
	 */
    public String getPresentationDirectory() {
    	return _presentationPath;
    }
    
	/**
	 * Extracts the filename without extension of a given filename (with 
	 * extension).
	 * 
	 * @param fileName Filename (with extension)
	 * @return Filename without the last file extension.
	 */
	public static String extractFileName(String fileName) {
		int index = fileName.lastIndexOf(java.io.File.separatorChar);
		if (index == -1) {
			return fileName;
		} else {
			return fileName.substring(index + 1);
		}
	}
	
	/**
	 * Removes the fileName from the list of recent fileNames, if contained.
	 * 
	 * @param fileName fileName to be removed from the list.
	 */
	private void removeRecent(String fileName) {
        _recentFilenames.remove(fileName);
		
		_propertyChangeSupport.firePropertyChange("recentFilenames", null, _recentFilenames);
	}
	
	
	// FIXME Preferences unbedingt entkoppeln.
	// -> Klassen unnötig gekoppelt
	// -> Unabhängige Unittests stark erschwert
	// -> equals()-Semantik korrumpiert!
	// See respective tasks.
	
	// TODO V2: Preferences entkoppeln.
	public void savePreferencesToDisk() {
		
		// Store presentation path to preferences:
		_preferences.setPreference(PREFERENCE_PATH, _presentationPath);
		
		// Store listRecentFiles to preferences:
		_preferences.setPreference(PREFERENCE_LIST_RECENT_FILES, String.valueOf(_showRecents));
		
		// Store recent file names to preferences:
        int i = 0;
        
        if (_showRecents) {
		    for (String recentFile : _recentFilenames) {
	            _preferences.setPreference(PREREFRENCE_RECENT_FILE_PREFIX + i++, recentFile);
		    }
        }
	    if (i < NUMBER_RECENTS) {
            // Remove the rest if there isn't so many recent files left
            for (; i < NUMBER_RECENTS; i++) {
                  _preferences.removePreference(PREREFRENCE_RECENT_FILE_PREFIX + i);
            }
	    }
		
		_preferences.saveToDisk();
	}
	
	// TODO V2: Preferences entkoppeln.
	public Preferences getPreferences() {
		return _preferences;
	}

	// TODO V2: Preferences entkoppeln.
	public void setPreference(String key, String value) {
		_preferences.setPreference(key, value);
	}
	
	// TODO V2: Preferences entkoppeln.
	public String getPreference(String key) {
		return _preferences.getPreference(key);
	}
	
	private void addPresentation(Presentation presentation) {
		_presentations.add(presentation);
		setActivePresentation(presentation);
		_propertyChangeSupport.firePropertyChange("presentations", null, _presentations);
	}
	
	private void addRecent(String fileName) {
		if (_recentFilenames.isEmpty() || !_recentFilenames.getFirst().equals(fileName)) {
            
			// Not opening the same file twice in a row. Update recent list.
            // Update position for the same file instead of storing twice:
            if (_recentFilenames.contains(fileName)) { // FIXME: Can't work, equals vs. "=="
            	_recentFilenames.remove(fileName);
            }
            _recentFilenames.addFirst(fileName);

            while (_recentFilenames.size() > NUMBER_RECENTS) {
            	_recentFilenames.removeLast();
            }
        }
		
		_propertyChangeSupport.firePropertyChange("recentFilenames", null, _recentFilenames);
	}
}
