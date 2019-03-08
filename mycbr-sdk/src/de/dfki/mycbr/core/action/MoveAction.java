/*
 * myCBR License 3.0
 * 
 * Copyright (c) 2006-2015, by German Research Center for Artificial Intelligence (DFKI GmbH), Germany
 * 
 * Project Website: http://www.mycbr-project.net/
 * 
 * This library is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.
 * 
 * endOfLic */

package de.dfki.mycbr.core.action;

import java.util.Observable;

/**
 * When a concept or description is moved within a myCBR project,
 * the move action can be used to notify observers about that move action.
 * 
 * @author myCBR Team
 *
 */
public class MoveAction implements Action {

	private Object newParent;
	private Observable o;
	
	public MoveAction(Observable o, Object newParent) {
		this.o = o;
		this.newParent = newParent;
	}
	
	/* (non-Javadoc)
	 * @see de.dfki.mycbr.core.action.Action#getData()
	 */
	@Override
	public Object getData() {
		return newParent;
	}

	public Object getNewParent() {
		return newParent;
	}
	
	/* (non-Javadoc)
	 * @see de.dfki.mycbr.core.action.Action#getType()
	 */
	@Override
	public ActionType getType() {
		return ActionType.MoveAction;
	}

	/* (non-Javadoc)
	 * @see de.dfki.mycbr.core.action.Action#getObservable()
	 */
	@Override
	public Observable getObservable() {
		return o;
	}

}
