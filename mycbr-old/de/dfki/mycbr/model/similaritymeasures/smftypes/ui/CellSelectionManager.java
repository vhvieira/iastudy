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

import java.util.ArrayList;

public class CellSelectionManager {
	private ArrayList<String> selection = new ArrayList<String>();
	
	int[] rows;
	int[] columns;

	boolean hasChanged = true;

	public void clear() {
		selection.clear();
		hasChanged = true;
	}

	public void addSelectedCell(int row, int column) {
		if (row == column) {
			return;
		}
		String key = key(row,column);
		if (selection.contains(key)) {
			return;
		}
		selection.add(key);
		hasChanged = true;
	}

	public void removeSelectedCell(int row, int column) {
		selection.remove(key(row,column));
		hasChanged = true;
	}

	public void toggleSelectedCell(int row, int column) {
		String key = key(row, column);
		if (selection.contains(key)) {
			removeSelectedCell(row,column); 
		} else { 
			addSelectedCell(row, column);
		}
	}

	public boolean isSelected(int row, int column) {
		return selection.contains(key(row, column));
	}
	
	private String key(int row, int column) {
		String key = String.format("%d,%d",row,column);
		return key;
	}

	public int[] getSelectedRows() {
		updateArrays();
		return rows;
	}
	
	public int[] getSelectedColumns() {
		updateArrays();
		return columns;
	}
	
	private void updateArrays() {
		if (!hasChanged) {
			return;
		}
		hasChanged = false;
		int size = selection.size();
		rows 	= new int[size];
		columns	= new int[size];
		int index = 0;
		for (String key : selection) {
			String[] split = key.split(",");
			rows[index] 	= Integer.parseInt(split[0]);
			columns[index] 	= Integer.parseInt(split[1]);
			index++;
		}
	}

}
