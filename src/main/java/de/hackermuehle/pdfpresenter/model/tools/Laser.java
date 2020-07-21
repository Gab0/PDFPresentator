package de.hackermuehle.pdfpresenter.model.tools;

import java.awt.Color;
import de.hackermuehle.pdfpresenter.model.Preferences;

public class Laser extends WriterTool {

    public Laser(Color color, double size) {
        super(color, size);
    }

    public Laser() {
        super();
    }


    public Laser(Preferences prefs) {
        super(prefs);
    }
}
