# About

`PDF Presentator` is a revival of the awesome software for pdf presentations called `PDF Presenter`. 
It was released in 2011, and the original `jar` still works and can be found [here](http://pdfpresenter.sourceforge.net).
The source code, in the other hand, needed some love to compile again... at least outside the intended Eclipse compilation environment.

PDF Presenter is a full-featured software to present slides stored as pdf files.
It allows to display the contents in secondary window/monitor, while you control the presentation in your laptop screen or whatever.
The novelty here is that you can draw and highlight over the slides, create annotation margins, and more.
Navigation across slides is simple but efficient, and
the performance is very good.

So here it is, PDF Presenter adapted for Apache Maven builds.
The `pom` file and underlying organization of the build should be clumsy at the moment, but I'll try to improve it a bit (it works).
I'm cooking some ideas for new features and I'll try to implement those here... enjoy.

# Build

Requires Apache Maven.

`make build`


# Run

`pdfpresenter.jar` is already compiled on the repo.

`make run`

OR

`java -jar pdfpresenter.jar`


# Original Authors

Full credit goes to `PDF Presenter` authors:

* Shuo Yang
* Martin Tschirsich
* Jürgen Benjamin Ronshausen
* Niklas Büscher

http://pdfpresenter.sourceforge.net/
