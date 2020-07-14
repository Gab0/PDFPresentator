package de.hackermuehle.pdfpresenter.model.slide;

import de.hackermuehle.pdfpresenter.model.CacheEntry;
import de.hackermuehle.pdfpresenter.model.CacheObserver;
import de.hackermuehle.pdfpresenter.model.Clipping;

/**
 * The public accessible interface to cached slide content.
 * 
 * @see {@link Slide#cache(Clipping, Integer, CacheObserver) Slide.cache(...)}
 */
public interface SlideCacheEntry extends CacheEntry {
	public void update(Clipping clipping, int Priority, CacheObserver observer);
}
