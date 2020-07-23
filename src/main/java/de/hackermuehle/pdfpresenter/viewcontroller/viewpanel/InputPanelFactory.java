package de.hackermuehle.pdfpresenter.viewcontroller.viewpanel;

import java.awt.geom.Rectangle2D;

import de.hackermuehle.pdfpresenter.model.State;
import de.hackermuehle.pdfpresenter.model.slide.Slide;
import de.hackermuehle.pdfpresenter.model.tools.Eraser;
import de.hackermuehle.pdfpresenter.model.tools.Magnifier;
import de.hackermuehle.pdfpresenter.model.tools.Marker;
import de.hackermuehle.pdfpresenter.model.tools.Pen;
import de.hackermuehle.pdfpresenter.model.tools.Shape;
import de.hackermuehle.pdfpresenter.model.tools.TextTool;
import de.hackermuehle.pdfpresenter.model.tools.Laser;

public class InputPanelFactory {

    public static MasterInputPanel createInputPanel(Slide slide,
                                                    Rectangle2D source, State state) {
        if (state.getActiveTool().getClass().equals(Marker.class)) {
            return createMarkerInputPanel(slide, source, state);
        }
        else if (state.getActiveTool().getClass().equals(Pen.class)) {
            return createPenInputPanel(slide, source, state);
        }
        else if (state.getActiveTool().getClass().equals(Eraser.class)) {
            return createEraserInputPanel(slide, source, state);
        }
        else if (state.getActiveTool().getClass().equals(Magnifier.class)) {
            return createMagnifierInputPanel(slide, source, state);
        }
        else if (state.getActiveTool().getClass().equals(TextTool.class)) {
            return createTextToolInputPanel(slide, source, state);
        }
        else if (state.getActiveTool().getClass().equals(Laser.class)) {
            return createLaserInputPanel(slide, source, state);
        }
        else if (state.getActiveTool().getClass().equals(Shape.class)) {
            return createShapeInputPanel(slide, source, state);
        }
        return null;
    }

    public static MasterInputPanel createMarkerInputPanel(Slide slide, Rectangle2D source, State state) {
        return new PenMasterInputPanel(slide, source, state.getMarker(), state);
    }
	
    public static MasterInputPanel createPenInputPanel(Slide slide, Rectangle2D source, State state) {
        return new PenMasterInputPanel(slide, source, state.getPen(), state);
    }
	
    public static MasterInputPanel createEraserInputPanel(Slide slide, Rectangle2D source, State state) {
        return new EraserMasterInputPanel(slide, source, state.getEraser(), state.getActivePresentation());
    }

    public static MasterInputPanel createShapeInputPanel(Slide slide, Rectangle2D source, State state) {
        return new ShapeMasterInputPanel(slide, source, state.getPen(), state);
    }


    public static MasterInputPanel createLaserInputPanel(Slide slide, Rectangle2D source, State state) {
        return new LaserPointerMasterInputPanel(slide, source, state.getLaser(), state.getActivePresentation());
    }

    public static MasterInputPanel createMagnifierInputPanel(Slide slide, Rectangle2D source, State state) {
        if (state.getActivePresentation() == null) throw new IllegalArgumentException("controller must have an active presentation");
        return new MagnifierMasterInputPanel(slide, source, state.getActivePresentation(), state.getPreviousTool(), state);
    }
	
    public static MasterInputPanel createTextToolInputPanel(Slide slide, Rectangle2D source, State state) {
        return new TextToolMasterInputPanel(slide, source, state.getTextTool(), state);
    }
}
