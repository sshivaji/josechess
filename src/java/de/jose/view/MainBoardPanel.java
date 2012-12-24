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

package de.jose.view;

import de.jose.profile.LayoutProfile;

public class MainBoardPanel
		extends BoardPanel
{
	
	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------
	
	public MainBoardPanel(LayoutProfile profile, boolean withContextMenu, boolean withBorder)
	{
		super(profile,withContextMenu,withBorder);
	}
}
