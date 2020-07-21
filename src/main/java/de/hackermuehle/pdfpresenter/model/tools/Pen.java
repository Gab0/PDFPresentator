package de.hackermuehle.pdfpresenter.model.tools;

import java.awt.Color;
import de.hackermuehle.pdfpresenter.model.Preferences;


public class Pen extends WriterTool {


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
