# About

`PDF Presentator` is a revival of the awesome software for pdf presentations called `PDF Presenter`
which was released in 2011 and still has an available [jar](http://pdfpresenter.sourceforge.net).

The source code is also provided on the project's page, except the instructions points to a recreation of an build environment inside the Eclipse IDE which I don't have.
So here is a very simple Maven project to build `PDF Presenter`.

`PDF Presenter` is a full-featured software to present slides stored as pdf files.
It allows to display slides in fullscreen on a secondary window/monitor, while the presentation is controlled from the main window/laptop screen/monitor.
The novelty here is that you can draw and highlight over the slides, create annotation margins, and more.
Navigation across slides is simple but efficient, and
performance is good.

I'm cooking some ideas for new features and I'll try to implement those here... enjoy.

# How to Build

Requires Apache Maven.

`make build`


# How to Run

`pdfpresenter.jar` is already compiled on the repo. 

The directory `presenter_lib` contains dependencies, so it is required at runtime.

Method #1: `make run`

Method #2: `java -jar pdfpresenter.jar`

# New features

## Laser Pointer

Hold the right mouse button to make a red dot appear and follow your cursor.
It will show up on the presentation screen.

This is a modified eraser that doesn't erase x}

## Better File select dialog

Removed `JFileChooser`, so we'll stick to the default file selector of the user's OS.

## Draw straight lines with the pen

By holding CTRL and dragging the pen around you'll draw a straight line on the slide.

# Original Authors

Full credit goes to `PDF Presenter` authors:

* Shuo Yang
* Martin Tschirsich
* Jürgen Benjamin Ronshausen
* Niklas Büscher

http://pdfpresenter.sourceforge.net/
