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

package de.jose.util;

import de.jose.Application;
import de.jose.Util;
import de.jose.Version;
import de.jose.util.file.FileUtil;
import de.jose.util.print.Triplet;

import java.util.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;

/**
 * FontUtil
 * 
 * @author Peter Sch�fer
 */

public class FontUtil
{

	/** the set of currently installed fonts
	 * Maps Family names (+BI) to File
	 * */
	protected static SortedMap currentFonts = null;
    protected static Object currentFontsLock = new Object();
	/** maps font family names to TrueType file names
	 *  maps family names (+B/I) to file names
	 */
	protected static HashMap customFileMap = null;
	protected static HashMap systemFileMap = null;
	/** already scanned directories */
	protected static HashSet scannedDirs = new HashSet();

	/** soft cache  */
	protected static SoftCache gFontCache = new SoftCache();


	// --------------------------------------------------------------------
	//      Custom Font Loading & Caching
	// --------------------------------------------------------------------

	/**
	 * get currently installed fonts
	 * maps family names (+BI) to files
	 */
	public static Map getCurrentFonts()
	{
        if (currentFonts==null)
        synchronized (currentFontsLock) {
            if (currentFonts==null)
            {
                String[] sysfonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
                /**
                 * JRE 1.5.0_01 on Linux
                 *  is a bit smarter about finding the system font path.
                 *  Which is good. BUT if you have many fonts installed, it will takes ages to complete.
                 *  Especially dificult is the directory ~/.kde/share/fonts/TrueType
                 *  Try to find a way to eliminate this directory from the font path !
                 */
                currentFonts = new TreeMap(StringUtil.CASE_INSENSITIVE_COMPARATOR);
                for (int i=0; i<sysfonts.length; i++)
                    currentFonts.put(sysfonts[i],null); //  null means: not yet instantiated
            }
        }
        return currentFonts;
	}

	public static File getTrueTypeFile(Triplet trp, boolean tolerant)
	{
		return getTrueTypeFile(trp.family,trp.bold,trp.italic,tolerant);
	}

	public static File getTrueTypeFile(String family, boolean bold, boolean italic, boolean tolerant)
	{
		String key = Triplet.toString(family,bold,italic);
		File file = getSystemFontFile(key);
		if (file==null) file = getCustomFontFile(key);

		if ((file==null) && tolerant) {
			if (italic) return getTrueTypeFile(family,bold,false,true);
			if (bold) return getTrueTypeFile(family,false,italic,true);
		}
		return file;
	}

	/**
	 * maps family names of custom fonts (from jose/fonts) to file names
	 */
	public static HashMap getCustomFileMap()
	{
		if (customFileMap==null) {
			customFileMap = (HashMap)Application.theUserProfile.get("font.map");
//			updateFileMap(customFileMap);
		}
		if (customFileMap==null) {
			customFileMap = new HashMap();
			Application.theUserProfile.set("font.map",customFileMap);
		}
		return customFileMap;
	}
	/**
	 * maps family names of system fonts (from Windows/fonts) to file names
	 */
	public static HashMap getSystemFileMap()
	{
		if (systemFileMap==null) {
			systemFileMap = (HashMap)Application.theUserProfile.get("sys.font.map");
//			updateFileMap(systemFileMap);
		}
		if (systemFileMap==null) {
			systemFileMap = new HashMap();
			Application.theUserProfile.set("sys.font.map",systemFileMap);
		}
		return systemFileMap;
	}

	public static File getSystemFontFile(String key)
	{
		File file = (File)getSystemFileMap().get(key);
		if (file!=null && !file.exists()) {
			getSystemFileMap().remove(key);
			return null;
		}
		else
			return file;
	}

	public static File getCustomFontFile(String key)
	{
		File file = (File)getCustomFileMap().get(key);
		if (file!=null && !file.exists()) {
			getCustomFileMap().remove(key);
			return null;
		}
		else
			return file;
	}

	private static void updateFileMap(Map map)
	{
		Iterator i = map.entrySet().iterator();
		while (i.hasNext())
		{
			Map.Entry ety = (Map.Entry)i.next();
			File file = (File)ety.getValue();
			if (file!=null && !file.exists())
				i.remove(); //  entry is out-of-date
		}
	}

	/**
	 * load a custom font from jose/fonts
	 * @param file
	 * @return
	 * @throws IOException
	 */
	protected static Font loadCustomFont(File file) throws IOException
	{
		if (!file.exists()) return null;
//Object mark = de.jose.devtools.Profiler.set(file.getName());
		InputStream input = new FileInputStream(file);
//		input = new BufferedInputStream(input,4096);
		Font font = null;
		try {
			font = Font.createFont(Font.TRUETYPE_FONT,input);
		} catch (FontFormatException e) {
			Application.warning("while loading "+file.getName());
			Application.error(e);
			return null;
		} finally {
			input.close();
		}
//		System.err.println("loaded "+file.getName());

		String key = Triplet.toString(font);
		getCurrentFonts().put(key,font);
		getCustomFileMap().put(key,file);
//de.jose.devtools.Profiler.print(mark);
		return font;
	}


	public static Font getFont(String family, int style, boolean tolerant)
	{
		Font font = getCustomFont(family,Util.allOf(style,Font.BOLD),Util.allOf(style,Font.ITALIC),tolerant);
		if (font==null) {
			//  use new Font() to load system fonts
			font = new Font(family,style,1);
			getCurrentFonts().put(Triplet.toString(family,style),font);
		}
		return font;
	}

	public static Font getCustomFont(String family, boolean bold, boolean italic, boolean tolerant)
	{
		Font font = getCustomFont(family,bold,italic);
		if ((font==null) && tolerant) {
			//  try another font face
			if (italic) return getCustomFont(family, bold,false, true);
			if (bold) return getCustomFont(family, false,italic, true);
		}
		return font;
	}

	protected static Font getCustomFont(String family, boolean bold, boolean italic)
	{
		String key = Triplet.toString(family,bold,italic);
		synchronized(getCurrentFonts()) {
			if (getCurrentFonts().containsKey(key)) {
				Font font = (Font)getCurrentFonts().get(key);
				if (font!=null || !isCustomFont(family))
					return font;
					// for system fonts, we return null (indicating that they must be loaded with new Font()
					// custom fonts are loaded below
			}
			//  examine file name map
			File file = (File)getCustomFileMap().get(key);
			if (file!=null)
				try {
					Font font = loadCustomFont(file);
					if (font != null)
						return font;
					else
						getCustomFileMap().remove(key);
				} catch (Exception ex) {
					Application.error(ex);
				}

			//  not found, scan custom font folder
			File customDir = new File(Application.theWorkingDirectory,"fonts");
			if (scanFontDirectory(customDir,true))
				return getCustomFont(family,bold,italic);    //  try again
			return null;
		}
	}

	public static boolean isCustomFont(String family)
	{
		return getCustomFileMap().containsKey(family);
	}

	public static Font newFont(String family, int style, float size)
	{
		String hash = family+"/"+style+"/"+size;
		Font font = (Font)gFontCache.get(hash);
		if (font!=null) return font;

		//  else
		font = getFont(family,style,true);

		font = font.deriveFont(style,size);
		gFontCache.put(hash,font);
		return font;
	}

	/**
	 * @return an array of all fonts that are currently available on this system, sorted by name
	 */
	public static Map getInstalledFonts(boolean loadCustom)
	{
//		if (!inited) config();

		if (loadCustom) {
			File customDir = new File(Application.theWorkingDirectory,"fonts");
			scanFontDirectory(customDir,true);
		}
		return getCurrentFonts();
	}

	public static boolean isInstalled(String family, int style)
	{
		return getInstalledFonts(true).containsKey(Triplet.toString(family,style));
	}



	// --------------------------------------------------------------------
	//      Font Path Utils
	// --------------------------------------------------------------------

	/**
	public static final String getFontPath()
	{
		String prop;
		if (Version.java13)
			prop = Version.getSystemProperty("java.awt.fonts");
		else
			prop = Version.getSystemProperty("sun.java2d.fontpath");

		if (prop != null)
			return prop;
		else
			return getSystemFontPath()+File.pathSeparator+getJavaFontPath();
	}
	 */

	/**
	 */
	public static final String getSystemFontPath()
	{
		Class[] types = { boolean.class };
		Object[] values = { Boolean.TRUE };

		try {
			if (Version.java15orLater)
			{
				return (String)ReflectionUtil.invoke("sun.font.FontManager",null,"getFontPath",types,values);
			}
			else
			{
/*
			String s = Version.getSystemProperty("sun.java2d.noType1Font");
			if(s == null)
				type1 = sun.awt.font.NativeFontWrapper.getType1FontVar();
			if("true".equals(s))
				type1 = true;
*/
				values[0] = Boolean.FALSE;
				return (String)ReflectionUtil.invoke("sun.awt.font.NativeFontWrapper",null,"getFontPath",types,values);
			}
		} catch (Exception e) {
			Application.error(e);
			return null;
		}
	}

	public static final String getJavaFontPath()
	{
		return Version.getSystemProperty("java.home") + File.separator + "lib" + File.separator + "fonts";
	}

    public static void main(String[] args) {
        System.out.println("system font path = "+getSystemFontPath());;
        System.out.println("java font path = "+getJavaFontPath());

        long time = System.currentTimeMillis();
        String[] sysfonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        Util.printTime(System.getProperty("java.version"),time);
        System.out.println(sysfonts.length+" system fonts");
	}

	public static void scanFontDirectories(String path, boolean custom)
	{
		StringTokenizer tok = new StringTokenizer(path,File.pathSeparator);
		while (tok.hasMoreTokens())
			scanFontDirectory(new File(tok.nextToken()),custom);
	}

	public static boolean scanFontDirectory(File dir, boolean custom)
	{
		if (scannedDirs.contains(dir)) return false;

		synchronized(scannedDirs)
        {
			if (scannedDirs.contains(dir)) return false;

			String[] fileNames = dir.list();
			if (fileNames!=null)
				for (int i=0; i < fileNames.length; i++)
					if (FileUtil.hasExtension(fileNames[i],"ttf"))
						try {
							TrueTypeInfo tti = new TrueTypeInfo(new File(dir,fileNames[i]));
							String key  = tti.toString();
							if (custom)
								getCustomFileMap().put(tti.toString(), tti.file);
							else
								getSystemFileMap().put(tti.toString(), tti.file);

							if (!getCurrentFonts().containsKey(key) && !tti.bold && !tti.italic)
								getCurrentFonts().put(key,null);    //  loaded on demand

						} catch (Exception ex) {
							//  can't help it ?
						}

			scannedDirs.add(dir);
			return true;
		}
	}

}