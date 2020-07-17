package de.hackermuehle.pdfpresenter.model.annotations;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * A PolyLine is a shape consisting of multiple line segments. New segments to 
 * the initially empty set of line segments can be added with addPoint().
 */
class PolyLine implements Shape, Cloneable {
    final int INITIAL_CAPACITY = 50;

    double[] _xCoords;
    double[] _yCoords;
    int _nPoints = -1;
    int _capacity;

    Rectangle2D.Double _bounds = new Rectangle2D.Double();

    public PolyLine() {
        _capacity = INITIAL_CAPACITY;
        _xCoords = new double[INITIAL_CAPACITY];
        _yCoords = new double[INITIAL_CAPACITY];
    }

    public void addPoint(Point2D point) {

        ++_nPoints;
        if (_nPoints >= _capacity)
            resize();

        _xCoords[_nPoints] = point.getX();
        _yCoords[_nPoints] = point.getY();

        if (_nPoints == 0) {
            _bounds = new Rectangle2D.Double(point.getX(), point.getY(), 0, 0);
        }

        if (!_bounds.contains(point))
            _bounds.add(point);
    }

    public Point2D getLastPoint() {
        if (_nPoints >= 0) return new Point2D.Double(_xCoords[_nPoints], _yCoords[_nPoints]);
        else return null;
    }

    public Point2D getFirstPoint() {
        if (_nPoints >=0) return new Point2D.Double(_xCoords[0], _yCoords[0]);
        else return null;
    }

    private void resize() {
        _capacity = _nPoints + INITIAL_CAPACITY;

        double[] xCoordsNew = new double[_capacity];
        double[] yCoordsNew = new double[_capacity];

        System.arraycopy(_xCoords, 0, xCoordsNew, 0, _nPoints);
        System.arraycopy(_yCoords, 0, yCoordsNew, 0, _nPoints);

        _xCoords = xCoordsNew;
        _yCoords = yCoordsNew;
    }

    public Rectangle2D getBounds2D() {
        return (Rectangle2D)_bounds.clone();
    }

    public Rectangle getBounds() {
        return _bounds.getBounds();
    }

    public boolean contains(Point2D p) {
        return false;
    }

    public boolean contains(Rectangle2D r) {
        return false;
    }

    public boolean contains(double x, double y) {
        return false;
    }

    public boolean contains(double x, double y, double w, double h) {
        return false;
    }

    public boolean intersects(Rectangle2D r) {
        for (int n = 1; n < _nPoints; ++n) {
            if (r.intersectsLine(_xCoords[n - 1], _yCoords[n - 1], _xCoords[n], _yCoords[n]))
                return true;
        }

        return false;
    }

    public boolean intersects(double x, double y, double w, double h) {
        return intersects(new Rectangle2D.Double(x, y, w, h));
    }

    public double getDistance(Point2D point) {
        Point2D start = new Point2D.Double(_xCoords[0], _yCoords[0]);

        double minDistance = start.distance(point);
        for (int n = 1; n <= _nPoints; ++n) {
            Line2D segment = new Line2D.Double(_xCoords[n - 1], _yCoords[n - 1], _xCoords[n], _yCoords[n]);
            double distance = segment.ptSegDist(point);
            if (distance < minDistance) minDistance = distance;
        }

        return minDistance;
    }

    public double getDistance(Line2D line) {
        double minDistance = Double.MAX_VALUE;

        for (int n = 1; n < _nPoints; ++n) {
            //Line2D segment = new Line2D.Double(_xCoords[n - 1], _yCoords[n - 1], _xCoords[n], _yCoords[n]);

            // FIXME: calculate distance!
        }

        return minDistance;
    }

    public PathIterator getPathIterator(final AffineTransform transform) {
        return new PathIterator() {
            int iPoint = -1;
            int nPoints = PolyLine.this._nPoints;

            public boolean isDone() {
                return iPoint >= nPoints;
            }

            public void next() {
                iPoint++;
            }

            public int currentSegment(float[] data) {
                data[0] = (float) (_xCoords[iPoint + 1]);
                data[1] = (float) (_yCoords[iPoint + 1]);

                if (transform != null)
                    transform.transform(data, 0, data, 0, 1);

                return iPoint == -1 ? SEG_MOVETO : SEG_LINETO;
            }

            public int currentSegment(double[] data) {
                data[0] = _xCoords[iPoint + 1];
                data[1] = _yCoords[iPoint + 1];

                if (transform != null)
                    transform.transform(data, 0, data, 0, 1);

                return iPoint == -1 ? SEG_MOVETO : SEG_LINETO;
            }

            public int getWindingRule() {
                return WIND_NON_ZERO;
            }
        };
    }

    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return getPathIterator(at);
    }

    @Override
    public Object clone() {
        Object object = null;
        try {
            object = super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace(System.err);
        }
        ((PolyLine) object)._xCoords = new double[_capacity];
        ((PolyLine) object)._yCoords = new double[_capacity];
        System.arraycopy(_xCoords, 0, ((PolyLine) object)._xCoords, 0, _nPoints + 1);
        System.arraycopy(_yCoords, 0, ((PolyLine) object)._yCoords, 0, _nPoints + 1);
        return object;
    }
}
