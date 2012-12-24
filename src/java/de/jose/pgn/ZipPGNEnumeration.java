/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch�fer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.pgn;

import de.jose.Application;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * utility class for accesing zipped PGN files
 */
public class ZipPGNEnumeration
			implements Enumeration
{
	protected ZipFile zip;
	protected Enumeration entries;
	protected ZipEntry next;

	protected FilenameFilter filter;

	/**
	 * @return true of the given file contains any PGN data
	 */
	static public boolean contains(File f, FilenameFilter filter)
	{
		ZipPGNEnumeration en = null;
		try {
			en = new ZipPGNEnumeration(f,filter);
			return en.hasMoreElements();
		} catch (ZipException zipex) {
			//	bad zip file
			return false;
		} catch (IOException ioex) {
			Application.error(ioex);
			return false;
		} finally {
			if (en!=null) en.close();
		}
	}
	
	static public boolean matches(ZipEntry ety, FilenameFilter filter)
	{
		String entryName = ety.getName();
		if (filter!=null)
			return filter.accept(null,entryName);
		else
			return true;
	}
	
	
	/**	
	 * enumerates ZipEntries within a ZIP file
	 */
	public ZipPGNEnumeration(File f, FilenameFilter filter)
		throws ZipException, IOException
	{
		this.filter = filter;
		zip = new ZipFile(f, ZipFile.OPEN_READ);
		entries = zip.entries();
		next = null;
	}
	
	public boolean hasMoreElements()		
	{ 
		if (next==null) fetchNext();
		return next != null; 
	}
	
	public Object nextElement() 
	{
		if (next==null) fetchNext();
		Object result = next;
		next = null;
		return result;
	}
	
	public final ZipEntry nextZipEntry()	{ 
		return (ZipEntry)nextElement(); 
	}
	
	
	/**	
	 * return a *fresh* input stream for the current zip entry
	 */
	public final static InputStream getInputStream(File f, String name)
		throws IOException
	{
		ZipFile zf = new ZipFile(f, ZipFile.OPEN_READ);
		ZipEntry ety = zf.getEntry(name);
		return new BufferedInputStream(zf.getInputStream(ety));
	}
	
	public void close() 
	{
		try {
			zip.close();
		} catch (IOException ioex) {
			//	who cares ?
		}
	}
	
	protected void fetchNext() 
	{
		for(;;) {
			if (!entries.hasMoreElements()) {
				next = null;
				return;
			}
			
			next = (ZipEntry)entries.nextElement();
			
			if (next==null) return;
			if (matches(next,filter)) return;
		}
	}
}
