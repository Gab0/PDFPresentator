package de.hackermuehle.pdfpresenter.model.document;

import de.hackermuehle.pdfpresenter.model.CacheEntry;
import de.hackermuehle.pdfpresenter.model.CacheObserver;
import de.hackermuehle.pdfpresenter.model.Clipping;

/**
 * The public accessible interface to cached document content.
 * 
 * @see {@link Document#cache(Clipping, Integer, int, CacheObserver) Document.cache(...)}
 */
public abstract interface DocumentCacheEntry extends CacheEntry {
	abstract public void update(int pageNr, Clipping clipping, int priority, CacheObserver observer);
}
