package de.hackermuehle.pdfpresenter.model.tools;

import java.awt.Color;
import de.hackermuehle.pdfpresenter.model.Preferences;


public class Pen extends WriterTool {

    public String PREFERENCE_COLOR = "pen.color";
    public String PREFERENCE_SIZE = "pen.size";

    public Pen(Color color, double size) {
        super(color, size);
    }

    public Pen() {
        super();

    }


    public Pen(Preferences prefs) {
        super(prefs);
    }
}
