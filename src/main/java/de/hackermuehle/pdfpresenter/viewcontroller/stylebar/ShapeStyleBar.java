package de.hackermuehle.pdfpresenter.viewcontroller.stylebar;


import de.hackermuehle.pdfpresenter.model.State;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import de.hackermuehle.pdfpresenter.model.Preferences;
import de.hackermuehle.pdfpresenter.model.tools.Shape;
import de.hackermuehle.pdfpresenter.model.tools.Tool;


public class ShapeStyleBar extends PenStyleBar {

    public ShapeStyleBar(State state, Preferences preferences) {
        super(state, preferences);
        updatePreferencePaths("laserstylepalette");
        _tool = state.getShape();
    }
}
