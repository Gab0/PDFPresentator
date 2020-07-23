package de.hackermuehle.pdfpresenter.model.tools;

import java.awt.Color;
import de.hackermuehle.pdfpresenter.model.Preferences;

public class Shape extends WriterTool {

    public String PREFERENCE_COLOR = "laser.color";
    public String PREFERENCE_SIZE = "laser.size";
    public Shape(Color color, double size) {
        super(color, size);
    }

    public Shape() {
        super();

    }


    public Shape(Preferences prefs) {
        super(prefs);
    }
}
