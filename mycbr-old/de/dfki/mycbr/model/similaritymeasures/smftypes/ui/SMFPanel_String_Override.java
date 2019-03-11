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
package de.dfki.mycbr.model.similaritymeasures.smftypes.ui;

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_String_Override;

public class SMFPanel_String_Override extends SMFPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SMF_String_Override smf;
	private Component paContent;
	private JTextField txtQuerySymbol;
	private HashMap<String, HashMap<String, Double>> map;
	private String selectedSymbol = null;

	public SMFPanel_String_Override(AbstractSMFunction smf) {
		super(smf);
		this.smf = (SMF_String_Override) smf;
		map = this.smf.getMap();
		
		setLayout(new GridBagLayout());
		paContent = createContentPanel();
		add(createHeaderPanel(),  	new GridBagConstraints(0, 0, 1, 1, 1d, 0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		add(paContent, 				new GridBagConstraints(0, 1, 1, 1, 1d, 1d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
	}

	private Component createContentPanel() {
		return new JPanel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			private static final float MIN_FONT_SIZE 	= 8f;
			private static final float MAX_FONT_SIZE 	= 22f;
			private static final float DIST_STD 		= 200;
			private Graphics g;
			private Font font;

			@Override
			public void paint(Graphics g) {
				super.paint(g);
				this.g = g;
				font = g.getFont();
				if (selectedSymbol == null) return;
				
				int X_MIDDLE = getWidth()/2;
				int Y_MIDDLE = getHeight()/2;
				drawSymbol(X_MIDDLE, Y_MIDDLE, selectedSymbol, 1f);
				
				HashMap<String, Double> map2 = map.get(selectedSymbol);
				double amount = map2.size();
				
				int cnt = 0;
				for (Entry<String, Double> e : map2.entrySet()) {
					double rad = ((double)cnt*2d*Math.PI)/amount; 
					int x = (int) (X_MIDDLE + DIST_STD * Math.sin(rad));
					int y = (int) (Y_MIDDLE + DIST_STD * Math.cos(rad));
					drawSymbol(x, y, e.getKey(), e.getValue().floatValue());
					cnt++;
				}
			}

			private void drawSymbol(int x, int y, String symbol, float scale) {
				char[] charArray = symbol.toCharArray();
				g.setFont(font.deriveFont(MIN_FONT_SIZE+scale*(MAX_FONT_SIZE-MIN_FONT_SIZE)));
				Rectangle2D rect = g.getFontMetrics().getStringBounds(symbol, g);
				g.drawChars(charArray, 0, symbol.length(), x-(int)(rect.getWidth()/2d), y-(int)(rect.getHeight()/2d));
				g.fillRect(x-5, y-5, 10, 10);
			}
		};
	}

	private JPanel createHeaderPanel() {
		JLabel laQuerySymbol = new JLabel("Query symbol:");		
		txtQuerySymbol = new JTextField();
		JPanel panel = new JPanel(new GridBagLayout());
		panel.add(laQuerySymbol,  new GridBagConstraints(0, 0, 1, 1, 0d, 0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		panel.add(txtQuerySymbol, new GridBagConstraints(1, 0, 1, 1, 1d, 0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		txtQuerySymbol.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				updateGraph();
			}
		});
		return panel;
	}

	protected void updateGraph() {
		selectedSymbol = null;
		String querySymbol = txtQuerySymbol.getText();
		if (!"".equals(querySymbol)) {
			HashMap<String, Double> map2 = map.get(querySymbol);
			if (map2 == null) {
				for (String s: map.keySet()) {
					if (s.startsWith(querySymbol)) {
						selectedSymbol=s;
						break;
					}
				}
			} else {
				selectedSymbol = querySymbol;
			}
		}
		paContent.repaint();
	}

}
