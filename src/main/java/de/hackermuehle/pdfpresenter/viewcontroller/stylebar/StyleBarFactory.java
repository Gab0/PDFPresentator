package de.hackermuehle.pdfpresenter.viewcontroller.stylebar;

import de.hackermuehle.pdfpresenter.model.State;
import de.hackermuehle.pdfpresenter.model.tools.Eraser;
import de.hackermuehle.pdfpresenter.model.tools.Magnifier;
import de.hackermuehle.pdfpresenter.model.tools.Marker;
import de.hackermuehle.pdfpresenter.model.tools.Pen;
import de.hackermuehle.pdfpresenter.model.tools.Shape;
import de.hackermuehle.pdfpresenter.model.tools.Laser;
import de.hackermuehle.pdfpresenter.model.tools.TextTool;

public class StyleBarFactory {

    private static StyleBar _markerStyleBar;
    private static StyleBar _penStyleBar;
    private static StyleBar _eraserStyleBar;
    private static StyleBar _textToolStyleBar;
    private static StyleBar _laserStyleBar;
    private static StyleBar _shapeStyleBar;

    public static StyleBar createStyleBar(State state) {
        if (state.getActiveTool().getClass().equals(Marker.class)) {
            return createMarkerStyleBar(state);
        }
        else if (state.getActiveTool().getClass().equals(Pen.class)) {
            return createPenStyleBar(state);
        }
        else if (state.getActiveTool().getClass().equals(Eraser.class)) {
            return createEraserStyleBar(state);
        }
        else if (state.getActiveTool().getClass().equals(Magnifier.class)) {
            return createMagnifierStyleBar(state);
        }
        else if (state.getActiveTool().getClass().equals(TextTool.class)) {
            return createTextToolStyleBar(state);
        }
        else if (state.getActiveTool().getClass().equals(Laser.class)) {
            return createLaserStyleBar(state);
        } 
        else if (state.getActiveTool().getClass().equals(Shape.class)) {
            return createShapeStyleBar(state);
        }
        return null;
    }

	public static StyleBar createMarkerStyleBar(State state) {
		if (_markerStyleBar == null)
			_markerStyleBar = new MarkerStyleBar(state.getMarker(),
                                           state.getPreferences());
		return _markerStyleBar;
	}

	public static StyleBar createPenStyleBar(State state) {
		if (_penStyleBar == null)
        _penStyleBar = new PenStyleBar(state, state.getPreferences());
		return _penStyleBar;
	}

    public static StyleBar createLaserStyleBar(State state) {
        if (_laserStyleBar == null)
            _laserStyleBar = new LaserStyleBar(state, state.getPreferences());
        return _laserStyleBar;
	}

    public static StyleBar createShapeStyleBar(State state) {
        if (_shapeStyleBar == null)
            _shapeStyleBar = new ShapeStyleBar(state, state.getPreferences());
        return _shapeStyleBar;
    }


	public static StyleBar createEraserStyleBar(State state) {
		if (_eraserStyleBar == null) 
			_eraserStyleBar = new EraserStyleBar(state.getEraser(), state.getPreferences());
		return _eraserStyleBar;
	}
	
	public static StyleBar createTextToolStyleBar(State state) {
		if (_textToolStyleBar == null) 
			_textToolStyleBar = new TextToolStyleBar(state.getTextTool(), state);
		return _textToolStyleBar;
	}
	
	public static StyleBar createMagnifierStyleBar(State state) {
		return null;
	}
}
