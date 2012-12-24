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

package de.jose;

import java.awt.Frame;
import java.awt.Dialog;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.AWTEvent;
import java.awt.Image;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;


/**
 * the Splash Screen shown at startup
 * to display this frame as quickly as possible, avoid unnecessary references
 * to application classes or Swing.
 */
public class SplashScreen
		extends Frame
		implements AWTEventListener
{
    public static final String version = "1.4.4";
    public static final String author = "Peter Sch�fer";
    public static final String year = "2002-06";
	public static final String url = "jose-chess.sourceforge.net";

    protected static SplashScreen theSplashScreen;

	private File imageDir;
	private Image splash;
//	private ArrayList logos;

/*
    static class Logo
    {
        ImageIcon icon;
        Rectangle bounds;
    }
*/

	private SplashScreen()
	{
		super("jose");
		setUndecorated(true);

		Rectangle screen = getGraphicsConfiguration().getBounds();
		setSize(400,300);
		setLocation(screen.x+(screen.width-getWidth())/2, screen.y+(screen.height-getHeight())/2);
//		setIconImage(Application.theApplication.theIconImage);

		setImageDir(new File("images"));

//        logos = new ArrayList();
//        addLogo(new File(imageDir,"java.logo.gif"), getWidth()-83, 80);
	}

    public static SplashScreen get()       { return theSplashScreen; }

    public static SplashScreen open()
    {
        if (theSplashScreen==null)
            theSplashScreen = new SplashScreen();
        theSplashScreen.show();
        return theSplashScreen;
    }

    public void setImageDir(File dir)
    {
        imageDir = dir;
        if (splash==null) {
            File imageFile = new File(imageDir,"splash.jpg");
            if (imageFile.exists())
                splash = Toolkit.getDefaultToolkit().createImage(imageFile.getAbsolutePath());
        }
    }

    public static void close()
    {
        if (theSplashScreen!=null) theSplashScreen.hide();
    }


	public void setDatabase(String dbname)
	{
/*
		File imageFile = new File(imageDir,"db."+dbname+".small.gif");
		if (!imageFile.exists())
			imageFile = new File(imageDir,"db."+dbname+".gif");
		if (imageFile.exists()) {
            addLogo(imageFile, getWidth()-83, -1);
            drawLogo(getGraphics(),logos.size()-1);
		}
*/
	}

	public void set3d(boolean on)
	{
/*
		if (on) {
			File imageFile = new File(imageDir,"j3d.logo3.gif");
			if (imageFile.exists()) {
                addLogo(imageFile, getWidth()-83, -1);
				drawLogo(getGraphics(),logos.size()-1);
			}
		}
*/
	}

    public void showSponsor(String logo)
    {
/*
       File imageFile = new File(imageDir,logo);
       addLogo(imageFile, getWidth()-229, -1);
	   drawLogo(getGraphics(),logos.size()-1);
*/
    }

	public void paint(Graphics g)
	{
/*		String db = AboutDialog.getDBIdentifier(Application.theApplication.theDatabaseId);
		if (db != currentDB)
		{
			currentDB = db;
			String imageDir = Application.theWorkingDirectory.getAbsolutePath()
						  +File.separator+"images"+File.separator;
			dbIcon = new ImageIcon(imageDir+"db."+currentDB+".gif");
		}
*/
        if (splash!=null) g.drawImage(splash, 0,0, this);

		int x = getWidth()-83;

/*
		for (int i=0; i < logos.size(); i++)
            drawLogo(g,i);
*/

//		g.setColor(Color.white);
		g.setFont(new Font("SansSerif",Font.PLAIN,12));
		g.drawString("version "+version, 10,224);
		g.drawString("Copyright "+year+" "+author, 10,248);
		g.drawString(url, 10, 272);
//		g.drawString("this program is distributed under the terms of the GNU General Public License", 16,400);
//		g.drawString("(see Help / License for details)", 16,420);
//		g.drawString(Version.getSystemProperty("java.runtime.name")+Version.getSystemProperty("java.runtime.version"), 16,440);
	}

	static long EVENT_MASK = AWTEvent.KEY_EVENT_MASK | 
							 AWTEvent.MOUSE_EVENT_MASK |
							 AWTEvent.WINDOW_EVENT_MASK;
	
	public void show() {
		Toolkit.getDefaultToolkit().addAWTEventListener(this, EVENT_MASK);
		super.show();
	}
	
	public void hide() {
		Toolkit.getDefaultToolkit().removeAWTEventListener(this);
		super.hide();
	}

	public void eventDispatched(AWTEvent event) {
		switch (event.getID()) {
		case KeyEvent.KEY_PRESSED:
		case KeyEvent.KEY_RELEASED:
		case MouseEvent.MOUSE_PRESSED:
		case MouseEvent.MOUSE_RELEASED:
				hide();
				break;

		case WindowEvent.WINDOW_ACTIVATED:
		case WindowEvent.WINDOW_DEACTIVATED:
				toFront();
				break;
				
		case WindowEvent.WINDOW_OPENED:
				if (event.getSource() instanceof Dialog)
					hide();	//	don't let the splash screen obscure message dialogs
				else
					toFront();
				break;
		}

	}
/*
    protected void addLogo(File file, int x, int y)
    {
        if (file.exists())
        {
            if (x < 0) {
                if (logos.size()>=1) {
                    Logo prev = (Logo)logos.get(logos.size()-1);
                    x = prev.bounds.x+prev.bounds.width;
                }
                else
                    x = 0;
            }
            if (y < 0) {
                if (logos.size()>=1) {
                    Logo prev = (Logo)logos.get(logos.size()-1);
                    y = prev.bounds.y+prev.bounds.height;
                }
                else
                    y = 0;
            }

            Logo logo = new Logo();
            logo.icon = new ImageIcon(file.getAbsolutePath());
            logo.bounds = new Rectangle(x,y, logo.icon.getIconWidth(), logo.icon.getIconHeight());
            logos.add(logo);
        }
    }

    protected void drawLogo(Graphics g, int i)
    {
        if (i < 0 || i >= logos.size()) return;

        Logo logo = (Logo)logos.get(i);
        g.drawImage(logo.icon.getImage(),
                logo.bounds.x, logo.bounds.y,
                logo.bounds.width, logo.bounds.height, null);
    }
*/

/*
	public void toFront()
	{	
		super.toFront();
//		paint(getGraphics());
		repaint();
	}
*/
}
