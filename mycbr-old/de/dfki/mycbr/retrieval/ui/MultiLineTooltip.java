/**
 MyCBR License 1.1

 Copyright (c) 2008
 Thomas Roth-Berghofer, Armin Stahl & Deutsches Forschungszentrum f&uuml;r K&uuml;nstliche Intelligenz DFKI GmbH
 Further contributors: myCBR Team (see http://mycbr-project.net/contact.html for further information 
 about the mycbr Team). 
 All rights reserved.

 MyCBR is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 Since MyCBR uses some modules, you should be aware of their licenses for
 which you should have received a copy along with this program, too.
 
 endOfLic**/
package de.dfki.mycbr.retrieval.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JToolTip;

public class MultiLineTooltip extends JToolTip
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	JToolTip[] tooltips;
	String[] lines;
	Color borderCol = Color.black;
	Dimension prefSize;
	
	public void setTipText(String txt)
	{
//		log.fine("settext ["+txt+"]");
		if (txt == null)
		{
			lines = null;
			return;
		}
		lines = txt.split("\n");
		int maxIndex = -1;
		int maxLength = -1;
		for (int i=0; i<lines.length; i++)
		{
			if (maxLength < lines[i].length())
			{
				maxIndex = i;
				maxLength=lines[i].length();
			}
		}
		
		if (tooltips==null || tooltips.length!= lines.length)
		{
			tooltips = new JToolTip[lines.length];
			for (int i=0; i<tooltips.length; i++)
			{
				tooltips[i] = new JToolTip();
				tooltips[i].setBorder(BorderFactory.createMatteBorder((i==0?1:0), 1, (i==lines.length-1?1:0), 1, borderCol));
			}
		}
		
		for (int i=0; i<lines.length; i++)
		{
			tooltips[i].setTipText(lines[maxIndex]);
		}
		
		prefSize = tooltips[maxIndex].getPreferredSize();
//		log.info("pref size for tooltip = "+size);
		setPreferredSize(new Dimension(prefSize.width, prefSize.height*tooltips.length));
	}
	
	
	public void paint(Graphics g)
	{
//		log.info("paint "+(lines==null));
		if (lines==null) return;
		
		Graphics currentG = g;
//		Dimension prefSize = tooltips[0].getPreferredSize();
		Rectangle bounds = getBounds();
//		log.info("prefSize = "+prefSize);
		for (int i=0; i<lines.length; i++)
		{
			tooltips[i].setTipText(lines[i]);
			tooltips[i].setBounds(bounds.x, bounds.y +(i*prefSize.height), bounds.width, prefSize.height);
			tooltips[i].paint(currentG);
			currentG = g.create(0, prefSize.height*(i+1), prefSize.width, prefSize.height);
		}
	}

	public void setComponent(JComponent c)
	{
		super.setComponent(c);
		if (tooltips==null || tooltips.length==0) return;
		
		for (int i=0; i<tooltips.length; i++)
		{
			tooltips[i].setComponent(c);
		}
	}

	public void updateUI()
	{
		super.updateUI();
		if (tooltips==null || tooltips.length==0) return;
		
		for (int i=0; i<tooltips.length; i++)
		{
			tooltips[i].updateUI();
		}
	}
}
