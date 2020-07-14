package de.hackermuehle.pdfpresenter.model.document;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.VolatileImage;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import de.hackermuehle.pdfpresenter.model.CacheObserver;
import de.hackermuehle.pdfpresenter.model.Clipping;
import de.hackermuehle.pdfpresenter.model.ImmutableClipping;
import de.hackermuehle.pdfpresenter.model.slide.Grid;
import de.hackermuehle.pdfpresenter.model.slide.Slide;

/**
 * A document offers caching abilities.
 * 
 * A Document maintains a list of DocumentCacheEntry's that identify
 * certain images currently in use / displayed by external users.
 * These identified images are held in memory (= cached).
 */
public abstract class Document {
	public static final int PRIO_MIN		= 0;
	public static final int PRIO_THUMBNAIL	= 0;
	public static final int PRIO_PRECACHE	= 100;
	public static final int PRIO_MAIN		= 200;
	public static final int PRIO_HIGHEST	= 1000000;
	public static final int PRIO_MAX		= 1000000;
	
	private List<CacheMarker> _markersToDo;			// pages to cache
	private List<CacheMarker> _markersToMaintain;	// pages in cache
	private ConcurrentHashMap<String, BufferedImage> _cache; //the image cache
	private CachingThread _thread; //the caching thread

	/**
	 * construction, setting up cache
	 */
	public Document() {
		_markersToDo = Collections.synchronizedList(new LinkedList<CacheMarker>());
		_markersToMaintain = Collections.synchronizedList(new LinkedList<CacheMarker>());
		_cache = new ConcurrentHashMap<String, BufferedImage>();
		_thread = null;
	}
	
	/** 
	 * free memory
	 */
	@Override
	protected void finalize() throws Throwable {
		dispose();
		super.finalize();
	}
	
	/**
	 * A CacheIdentifier uniquely identifies a previously rendered page of this
	 * document in the cache.
	 * 
	 * Immutable.
	 */
	private class CacheIdentifier{
		private int _pageNr;
		private Rectangle _destination;
		private String _string;
		
		public CacheIdentifier(int pageNr, Rectangle destination) {
			super();
			_pageNr = pageNr;
			_destination = (Rectangle) destination.clone();
			_string = "p" + _pageNr + "d" + _destination.getWidth() + "h" + _destination.getHeight();
		}
		
		public int getPageNr() {
			return _pageNr;
		}
		
		public Rectangle getDestination() {
			return (Rectangle) _destination.clone();
		}
		
		/**
		 * Return a string representation. CacheIdentifier of equal values
		 * return equal strings.
		 * 
		 * @return string representation
		 */
		public String toString() {
			return _string;
		}
	}
	
	/**
	 * Private implementation of the CachedDocumentEntry.
	 * Additional priority and observer functionality.
	 */
	private class CacheMarker implements DocumentCacheEntry, Comparable<CacheMarker>{
		private int _priority;
		private long _time;
		private Clipping _clipping;
		private CacheIdentifier _cacheIdentifier;
		private CacheObserver _observer;
		
		public CacheMarker(int pageNr, Clipping clipping, int priority, CacheObserver observer) {
			if (priority < PRIO_MIN)
				throw new IllegalArgumentException("priority < PRIO_MIN");
			if (priority > PRIO_MAX)
				throw new IllegalArgumentException("priority > PRIO_MAX");
			
			update(pageNr, clipping, priority, observer);
		}
		/*
		CacheMarker: Zeigt auf Eintrag gleicher oder größerer Größe
		
		Neuer Marker:
		 -Suche Cache nach Image gleicher oder größerer Größe ab
		  -Image gefunden: Füge eigene Referenz hinzu
		  -Kein Image gefunden: Erzeuge neues Image und füge Referenz hinzu
		   -Speicher voll: cleanup & retry
		   
		Marker löschen:
		 -Referenz von referenziertem Image entfernen
		  -Image hat keine Referenzen mehr: Image aus Cache löschen
		
		Cache cleanup:
		 -gehe gecachte Bilder durch und lösche die mit niedrigster Priorität
		
		*/
		public int getPriority() {
			return _priority;
		}
		
		public CacheIdentifier getCacheIdentifier() {
			return _cacheIdentifier;
		}
		
		public CacheObserver getObserver() {
			return _observer;
		}
		
		private void addMarkerToDoSorterd(CacheMarker marker) {
			if(_markersToDo!=null) {
				boolean added = false;
				for(int i = 0; i < _markersToDo.size();i++) {
					if (_markersToDo.get(i).compareTo(marker) > 0) {
						_markersToDo.add(i, marker);
						added = true;
						break;
					}
				}
				if(!added) {
					_markersToDo.add(marker);
				}
			} else {
				System.err.println("Document.java: _markersToDo=null");
			}
		}
		
		@Override
		public void dispose() {
			_markersToDo.remove(this);
			_markersToMaintain.remove(this);
		}
		
		@Override
		public boolean isCached() {
			//returns isCached(this) isCachedLarger(this) // isCached should return true if paint() returns immediately.
			return Document.this.isCached(_cacheIdentifier) || Document.this.isCachedLarger(_cacheIdentifier.getPageNr(), _cacheIdentifier.getDestination()) != null;
		}
		
		@Override
		public void update(int pageNr, Clipping clipping, int priority, CacheObserver observer) {

			// If contained, remove this marker from the to-do queue:
			_markersToDo.remove(this);
			_markersToMaintain.remove(this);
			
			// Update the settings:
			_clipping = (Clipping) clipping.clone();
			_priority = priority;
			_time = System.currentTimeMillis();
			_cacheIdentifier = new CacheIdentifier(pageNr, calcDestination(pageNr, clipping));
			_observer = observer;
			
			if (Document.this.isCached(_cacheIdentifier)) {
				
				// Page is already cached in exactly the same dimension:
				if (_observer != null) {
					_observer.notify(this, "img.ready");
				}
				
				// The marker has to be maintained (if not already maintained):
				if (!_markersToMaintain.contains(this)) {
					_markersToMaintain.add(this);
				}
			} else {
				
				if (Document.this.isCachedLarger(_cacheIdentifier.getPageNr(), _cacheIdentifier.getDestination()) != null) {
					
					// Page is already cached in bigger dimensions:
					if (_observer != null) {
						_observer.notify(this, "img.ready.shrinked");
					}
					
					// Is priority high enough to allow for nice re-rendering
					// in exact dimensions?
					if (priority > PRIO_THUMBNAIL)  {
						
						// The nicer version of the rendering is not urgent:
						if (_priority >= 0)
							_priority -= PRIO_MAX - 1; // Reduce priority < 0
						
						addMarkerToDoSorterd(this);
						runThread();
					}
			
					// The marker has to be maintained (if not already maintained):
					if (!_markersToMaintain.contains(this)) {
						_markersToMaintain.add(this);
					}
				}
				else {
					addMarkerToDoSorterd(this);
					runThread();
				}
			}
		}

		@Override
		public int compareTo(CacheMarker marker) {
			if (marker.getPriority() == getPriority()) {
				
				double markerSize = marker.getCacheIdentifier().getDestination().getWidth() * marker.getCacheIdentifier().getDestination().getHeight();
				double thisSize = this.getCacheIdentifier().getDestination().getWidth() * this.getCacheIdentifier().getDestination().getHeight();
				
				// Commented out - only useful if thumbnail images are rendered 
				// on demand:
				/*if (markerSize == thisSize) {
					// Newer images first:
					return (marker._time - this._time < 0) ? -1 : 1;
				}*/
				
				// Bigger images first:
				return (markerSize - thisSize <= 0) ? -1 : 1;
			}
			// Priority images first:
			return marker.getPriority() - getPriority();
		}
		
		@Override
		public ImmutableClipping getClipping() {
			return _clipping;
		}
	}
		
	private class CachingThread extends Thread {

		private boolean _stop = false;
		
		/**
		 * Sets marker to stop thread
		 */
		public void stopMe() {
			_stop = true;
		}
		
		/**
		 * this function caches all elements in the _markersToDo queue
		 */
		private void cache() {
			
			// Add new pages, identified by markers, to the cache:
			while(_markersToDo!=null && !_markersToDo.isEmpty() && _stop==false) {

				CacheMarker m = _markersToDo.get(0);
				boolean outOfMemoryError = false;
				
				if (!_cache.contains(m.getCacheIdentifier().toString())) {
					//free mem
					cleanup();
					
					BufferedImage cacheImage = paintImageIntoCache(m.getCacheIdentifier().getPageNr(), m.getCacheIdentifier().getDestination());
					if(cacheImage==null) {
						if(m.getObserver() != null) {
							m.getObserver().notify(m, "img.error");
							outOfMemoryError = true;
						}
					} else {	
						if (m.getObserver() != null) {
							m.getObserver().notify(m, "img.ready");
						}	
					}
					// Check whether this rendering was previously of higher priority:
					if (m._priority < 0) {
						m._priority += PRIO_MAX + 1; // Restore previous priority
					}
				}
				
				// Possibly already contained (nice re-rendering), only add new ones:
				if (!_markersToMaintain.contains(m) && !outOfMemoryError) {
					_markersToMaintain.add(m);
				}
				
				_markersToDo.remove(m);
			}
		}
		
		// TODO: Niedrige Prioritäten zuerst rauswerfen!
		
		/**
		 * This function tries to clean up and free some allocated memory
		 */
		private void cleanup() {
			int mb100 = 1024*1024*100;

			if (estimateCacheSize() > mb100) {
				  //System.err.println("more than 100mb per cache!, running cleanup");
				  HashSet<String> check = new HashSet<String>();
				  for(CacheMarker m : _markersToMaintain) {
					  //check.add(m.getCacheIdentifier().toString());
					  if(isCached(m.getCacheIdentifier())) {
						  check.add(m.getCacheIdentifier().toString());
					  } else if(null!=isCachedLargerString(m.getCacheIdentifier().getPageNr(), m.getCacheIdentifier().getDestination())) {
						  check.add(isCachedLargerString(m.getCacheIdentifier().getPageNr(), m.getCacheIdentifier().getDestination()));
					  } else {
						  System.err.println("Document.java: Marker in _markersToMaintain without cache!");
					  }
					  //isCachedLarger(pageId, destination)
				  }
				  for(String key : _cache.keySet()) {
					  if(!check.contains(key)){
						  //System.out.println("Removing:" + key.toString());
						  if(_cache.get(key)!=null) {
							  _cache.get(key).flush();
						  }
						  _cache.remove(key);

					  }
				  }

			  }
			  /*double memRatio = (double) Runtime.getRuntime().totalMemory() / (double)Runtime.getRuntime().maxMemory(); 
			  if(memRatio > 0.9 || Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() < 10*1024*1024) {
				  //System.runFinalization();
				  //System.gc();
				  System.err.println("Garbage Collection - Memory Low");
				  //System.gc();
			  }*/
			  //System.out.println("After Max " + maxMemory + " allocated " + allocatedMemory + " free " + freeMemory); 
		}
		
		/** 
		 * main thread functions, Executes cleanup and caching
		 */
		public void run() {
			cleanup();
			cache();
		}
	}
	
	/**
	 * Generates a cache entry for this document for the given page and the
	 * given clipping.
	 * Subsequent calls to {@link Document#paint(Graphics2D, int, Clipping)
	 * Document.paint(...)} with the same clipping and pageNr will probably be
	 * much faster, depending on the concrete document.
	 * 
	 * @param pageNr
	 * @param clipping
	 * @param priority
	 * @param observer
	 * @return
	 */
	public DocumentCacheEntry cache(int pageNr, Clipping clipping, int priority, CacheObserver observer) {
		return new CacheMarker(pageNr, clipping, priority, observer);
	}
	
	/**
	 * @param pageNr the requested page number
	 * @param clipping the source clipping region to render
	 * @return true if requested page is cached
	 */
	public boolean isCached(int pageNr, Clipping clipping) {
		CacheIdentifier cacheId = new CacheIdentifier(pageNr, calcDestination(pageNr, clipping));
		return isCached(cacheId);
	}
	
	/**
	 * @return document title
	 */
	public abstract String getTitle();
	
	/**
	 * @return The total number of pages of the document.
	 */
	public abstract int getNumberOfPages();
	
	/**
	 * @param pageNr
	 * @return The width / size ratio of the given page.
	 */
	public abstract double getPageRatio(int pageNr);
	
	/**
	 * @param pageNr
	 * @return The bounding rectangle of the given page
	 */
	public abstract Rectangle2D getBounds(int pageNr);
	
	/**
	 * Paints a page of this document. Makes use of caching abilities.
	 * 
	 * Do not overwrite this method if you want to paint your own page content
	 * in a subclass. Overwrite {@link Document#paintContent(Graphics2D, int,
	 * Clipping) Document.paintContent(...)} instead.
	 * 
	 * @param g2d Graphics object in source coordinate space
	 * @param pageNr
	 * @param clipping
	 */
	public final void paint(Graphics2D g2d, int pageNr, Clipping clipping) {
		AffineTransform originalTransform = g2d.getTransform();
		
		Rectangle destination = calcDestination(pageNr, clipping);
		CacheIdentifier cacheId = new CacheIdentifier(pageNr, destination);
		//System.out.println(cacheId.toString());
		
		BufferedImage cachedPage;
		if (isCached(cacheId)) {
			
			// Page is cached with exact dimensions!
			// Paint cached image:
			cachedPage = _cache.get(cacheId.toString());
			
			// Transform (upper-left slide corner at (0, 0), width / height = slide.width / slide.height):
			g2d.transform(clipping.getInverseTransform());//setTransform(new AffineTransform());
			
			int x = (int)(clipping.getSource().getX()/getBounds(pageNr).getWidth()*cachedPage.getWidth());
			int y = (int)(clipping.getSource().getY()/getBounds(pageNr).getHeight()*cachedPage.getHeight());
			int w = (int)(clipping.getSource().getWidth()/getBounds(pageNr).getWidth()*cachedPage.getWidth());
			int h = (int)(clipping.getSource().getHeight()/getBounds(pageNr).getHeight()*cachedPage.getHeight());
			
			g2d.translate((int)clipping.getDestination().getX(), (int)clipping.getDestination().getY());
			g2d.drawImage(cachedPage, 0, 0, (int)clipping.getDestination().getWidth(),(int)clipping.getDestination().getHeight(), x, y, x+w, y+h, null);
			
		} else {
			cachedPage = isCachedLarger(pageNr, destination);
			
			if (cachedPage != null) { 
				// Page is cached with bigger dimensions!
				// Paint cached image:
				// Transform (upper-left slide corner at (0, 0), width / height = slide.width / slide.height):
				g2d.transform(clipping.getInverseTransform());
				//g2d.setTransform(new AffineTransform());
				
				int x =(int)((double) clipping.getSource().getX()/getBounds(pageNr).getWidth()*cachedPage.getWidth());
				int y =(int)((double) clipping.getSource().getY()/getBounds(pageNr).getHeight()*cachedPage.getHeight());
				int w =(int)((double) clipping.getSource().getWidth()/getBounds(pageNr).getWidth()*cachedPage.getWidth());
				int h =(int)((double) clipping.getSource().getHeight()/getBounds(pageNr).getHeight()*cachedPage.getHeight());

//				System.out.println("clipping.X" + clipping.getSource().getX() + " page.width " + getBounds(pageNr).getWidth());
//				System.out.println("x " + x  + " cachedPage.width " + cachedPage.getWidth());
//				
//				System.out.println("clipping.Y" + clipping.getSource().getY() + " page.height " + getBounds(pageNr).getHeight());
//				System.out.println("y " + y  + " cachedPage.height " + cachedPage.getHeight());
//				
//				System.out.println("cachedPage.w " + cachedPage.getWidth() + " w " + w + " clipping.w " + clipping.getSource().getWidth());
//				System.out.println("cachedPage.h " + cachedPage.getHeight() +" h " + h + " clipping.h " + clipping.getSource().getHeight());
//				
				if (x<0) {
					x=0;
				}
				if (y<0) {
					y=0;
				}
				if (x >= cachedPage.getWidth()) {
					x = cachedPage.getWidth() - 1;
				}
				if (y >= cachedPage.getHeight()) {
					y = cachedPage.getHeight() - 1;
				}
				if (w+x > cachedPage.getWidth()) { 
					w = cachedPage.getWidth() - x;
				}
				if (h+y > cachedPage.getHeight()) {
					h = cachedPage.getHeight() - y;
				}
				
				//g2d.translate(clipping.getDestination().getX(), clipping.getDestination().getY());
//				BufferedImage temp = new BufferedImage((int)clipping.getDestination().getWidth(), (int)clipping.getDestination().getHeight(), BufferedImage.TYPE_INT_RGB);
//				temp.getGraphics().drawImage(cachedPage, x,y,x+w,y+w,null);
//				
				g2d.translate(clipping.getDestination().getX(), clipping.getDestination().getY());
				g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2d.drawImage(cachedPage, 0, 0, (int)clipping.getDestination().getWidth(), (int)clipping.getDestination().getHeight() , x, y, x+w, y+h, null);
				g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
				
			} else {
				// Page not cached, paint immediately (no caching):
				paintContent(g2d, pageNr, clipping);
			}		
		}
		
		g2d.setTransform(originalTransform);
	}
	
	/**
	 * Releases resources hold by this document.
	 */
	public void dispose() {
		if(_thread!=null && _thread.isAlive()) {
			_thread.stopMe();
		}
		if (_markersToDo != null) {
			_markersToDo.clear();
		}
		if (_markersToMaintain != null) {
			_markersToMaintain.clear();
		}
		if (_cache != null) {
			_cache.clear();
		}
	}
	
	/**
	 * call to documents render library to render the requested page
	 * 
	 * @param g2d	output Graphics object in source coordinate space
	 * @param pageNr the requested page number
	 * @param clipping the requested clipping region
	 */
	protected abstract void paintContent(Graphics2D g2d, int pageNr, Clipping clipping);
	
	
	/**
	 * checks if the requested pair (pageNr, clipping) is cached 
	 * 
	 * @param cacheId
	 * @return true if requested page, identified by cachId is cacheds
	 */
	protected boolean isCached(CacheIdentifier cacheId) {
		return _cache.containsKey(cacheId.toString());
	}
	
	/**
	 * checks if requested page is cached in any larger resolution
	 * 
	 * @param pageId the requested page number
	 * @param destination the requested output resolution
	 * @return true if requested page is cached in larger resolution
	 */
	protected BufferedImage isCachedLarger(int pageId, Rectangle2D destination) {
		for(String k : _cache.keySet()) {
			int i = k.indexOf('d');
			int iPage = Integer.parseInt((k.substring(1, i)));
			if (iPage == pageId) {
				if (destination.getWidth() <= _cache.get(k).getWidth() && 
					destination.getHeight() <= _cache.get(k).getHeight()) {
					return _cache.get(k);
				}
			}
		}
		return null;
	}
	
	/**
	 * checks if requested page is cached in any larger resolution
	 * 
	 * @param pageId the requested page number
	 * @param destination the requested output resolution
	 * @return true if requested page is cached in larger resolution
	 */
	protected String isCachedLargerString(int pageId, Rectangle2D destination) {
		for(String k : _cache.keySet()) {
			int i = k.indexOf('d');
			int iPage = Integer.parseInt((k.substring(1, i)));
			if (iPage == pageId) {
				if (destination.getWidth() <= _cache.get(k).getWidth() && 
					destination.getHeight() <= _cache.get(k).getHeight()) {
					return k;
				}
			}
		}
		return null;
	}
	/**
	 * this function provides a linkedlist with all cached images to a given page number
	 * 
	 * @param pageId the requested page number
	 * @return LinkedList of images
	 */
	protected LinkedList<BufferedImage> getCachedPages(int pageId) {
		LinkedList<BufferedImage> b = new LinkedList<BufferedImage>();
		for(String k : _cache.keySet()) {
			int i = k.indexOf('d');
			int iPage = Integer.parseInt((k.substring(1, i)));
			if(iPage == pageId) {
				b.add( _cache.get(k));
			}
		}
		return b;
	}

	/**
	 * Will start the thread, if thread is not running
	 */
	private void runThread() {
		if(_thread == null || !_thread.isAlive()) {
			_thread = new CachingThread();
			_thread.setPriority(Thread.MIN_PRIORITY);
			_thread.start();
		}
	}

	/**
	 * Estimates the size of all cached pages in bytes.
	 * 
	 * @return Estimated size in bytes
	 */
	private int estimateCacheSize() {
		int result = 0;
		for(String key : _cache.keySet()) {
			BufferedImage cachedPage = _cache.get(key);
			int bpp = DataBuffer.getDataTypeSize(cachedPage.getRaster().getDataBuffer().getDataType());
			result += (cachedPage.getWidth() * cachedPage.getHeight()) * bpp / 8; 
		}
		return result;
	}
	
	/**
	 * calcs the required output resolution(destination) for a give pair of page number and clipping
	 * 
	 * @param pageNr the requested page number
	 * @param clipping the requested source rect
	 * @return Rectangel2D resolution of destination
	 */
	private Rectangle calcDestination(int pageNr, Clipping clipping) {
		Rectangle2D bounds = clipping.getTransform().createTransformedShape(getBounds(pageNr)).getBounds2D();
		return new Rectangle((int) bounds.getX(), (int) bounds.getY(), (int) bounds.getWidth(), (int) bounds.getHeight());
	}
	
	private BufferedImage paintImageIntoCache(int pageNr, Rectangle destination) {
		
//		
//		try {
//			Thread.sleep(2000);
//			} catch(InterruptedException e) {} 
		
		if (destination.getWidth() <= 0 || destination.getHeight() <= 0) return null;
		BufferedImage img = null;
		try {
			img = GraphicsEnvironment.getLocalGraphicsEnvironment().
				getDefaultScreenDevice().getDefaultConfiguration().
				createCompatibleImage((int)destination.getWidth(), (int)destination.getHeight());
		} catch(java.lang.OutOfMemoryError e) {
			System.err.println("Error: Document.java. Could not render image, reason: out of memory");
			System.err.println("Increase your Java VM Heap Size setting.");
			return null;
		}
		//BufferedImage img = new BufferedImage((int)destination.getWidth(), (int)destination.getHeight(),BufferedImage.TYPE_3BYTE_BGR);
		//BufferedImage img = new BufferedImage((int)clipping.getDestination().getWidth(),(int) clipping.getDestination().getHeight(), BufferedImage.TYPE_INT_RGB);
		
		// Paint the given page with destination width / height into the buffer:
		Graphics2D g2d = (Graphics2D) img.createGraphics();
		try {
			Clipping clipping = new Clipping(getBounds(pageNr), new Rectangle(0, 0, (int) destination.getWidth(), (int) destination.getHeight()));
			g2d.setTransform(clipping.getTransform());
			paintContent(g2d, pageNr, clipping);
		} catch (NoninvertibleTransformException e) {
			// Never happens given getBounds(pageNr) has dimensions > 0.
			e.printStackTrace();
		}
		g2d.dispose();

		_cache.put((new CacheIdentifier(pageNr, destination).toString()), img);
		return img;
	}
	
	// TODO: CacheMarkers with priority higher than / equal to 1 should have
	// an additional VolatileImage (created with ...getDefaultConfiguration().
	// createVolatileImage()).
	// This volatile image residues in VRAM and is probably much faster to draw.
	// -> Disadvantage: VRAM is limited, volatile images can be destroyed by
	//    the system. Therefore use the following drawing function:
	
	// This method draws a volatile image and returns it or possibly a
	// newly created volatile image object. Subsequent calls to this method
	// should always use the returned volatile image.
	// If the contents of the image is lost, it is recreated using orig.
	// img may be null, in which case a new volatile image is created.
	private VolatileImage drawVolatileImage(Graphics2D g, VolatileImage img, int x, int y, Image orig) {
		final int MAX_TRIES = 100;
		for (int i = 0; i < MAX_TRIES; i++) {
			if (img != null) {
				// Draw the volatile image
				g.drawImage(img, x, y, null);

				// Check if it is still valid
				if (!img.contentsLost()) {
					return img;
				}
			} else {
				// Create the volatile image
				img = g.getDeviceConfiguration().createCompatibleVolatileImage(
						orig.getWidth(null), orig.getHeight(null));
			}

			// Determine how to fix the volatile image
			switch (img.validate(g.getDeviceConfiguration())) {
			case VolatileImage.IMAGE_OK:
				// This should not happen
				break;
			case VolatileImage.IMAGE_INCOMPATIBLE:
				// Create a new volatile image object;
				// this could happen if the component was moved to another
				// device
				img.flush();
				img = g.getDeviceConfiguration().createCompatibleVolatileImage(
						orig.getWidth(null), orig.getHeight(null));
			case VolatileImage.IMAGE_RESTORED:
				// Copy the original image to accelerated image memory
				Graphics2D gc = (Graphics2D) img.createGraphics();
				gc.drawImage(orig, 0, 0, null);
				gc.dispose();
				break;
			}
		}

		// The image failed to be drawn after MAX_TRIES;
		// draw with the non-accelerated image
		g.drawImage(orig, x, y, null);
		return img;
	}
}
