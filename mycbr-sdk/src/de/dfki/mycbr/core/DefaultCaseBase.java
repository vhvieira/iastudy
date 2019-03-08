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

package de.dfki.mycbr.core;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Observable;

import de.dfki.mycbr.core.action.RenameAction;
import de.dfki.mycbr.core.casebase.Instance;

/**
 * Class DefaultCasebase. Represents cases as a c attribute. Therefore, uses
 * myCBR's default classes for internal case representation contained in the
 * package <code>de.dfki.mycbr.core.casebase</code>.
 *
 * @author myCBR Team
 * @since myCBR 3.0.0
 */
public final class DefaultCaseBase extends Observable implements ICaseBase {

    /**
     * Maps c description to a list of c attributes. For a faster access of
     * cases belonging to a given c
     */
    private HashMap<String, Instance> cases;

    /**
     */
    private String name;

    /**
     */
    private Project prj;

	private String author;

	private Date date;
	private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	
    /**
     * Initializes this with the given list of concept descriptions. The list
     * specifies the highest concepts in the inheritance hierarchy. To create a
     * DefaultCase base call {@link Project#createDefaultCB(String)}.
     *
     * @param cbName
     *            the name of the case base
     * @param p
     *            the project this cb should belong to
     * @throws Exception
     *             if name is null or empty or if the given project is null or
     *             already has a case base with this name
     */
    DefaultCaseBase(final Project p, final String cbName) throws Exception {
        cases = new HashMap<String, Instance>();
        if (cbName == null || cbName.trim().equals("")) {
            throw new Exception("Cannot add case base with empty name");
        } else if (p.getCB(cbName) != null) {
            throw new Exception("Project already has a case base with name \""
                    + cbName + "\"");
        }
        this.name = cbName;
        this.prj = p;
        addObserver(prj);
    }

    /**
	 * @param cbName
     *            the name of the case base
     * @param p
     *            the project this cb should belong to
     * @param count
     * 				the number of cases expected for this case base
     * 
     * @throws Exception
     *             if name is null or empty or if the given project is null or
     *             already has a case base with this name
	 */
	public DefaultCaseBase(Project p, String cbName, int count) throws Exception {
		cases = new HashMap<String, Instance>();
        if (cbName == null || cbName.trim().equals("")) {
            throw new Exception("Cannot add case base with empty name");
        } else if (p.getCB(cbName) != null) {
            throw new Exception("Project already has a case base with name \""
                    + cbName + "\"");
        }
        this.name = cbName;
        this.prj = p;
        addObserver(p);
	}

	/**
     * Returns the list of cases known for this case base
     * @return cases for the given description
     */
    public Collection<Instance> getCases() {
        LinkedList<Instance> res = new LinkedList<Instance>();
        res.addAll(cases.values());
        return res;
    }

    /**
     * Adds a new case to the list of known cases for the given description. If
     * there is no case known for this description yet, a new list is created
     * and the new case is added to this list.
     *
     * @param caze the case to be added to this case base
     */
    public void addCase(Instance caze) {
    	String name = caze.getName();
    	if (cases.get(name)==null) {
    		cases.put(name, caze);
    	}
    }

    /**
     * Removes the given c attribute from the list of known cases. Returns true,
     * if att has really been a case and if this case has been removed
     * successfully, false otherwise.
     *
     * @param c the case to be removed
     * @return true, if the case has be removed successfully, false otherwise
     */
    public boolean removeCase(Instance c) {
    	return cases.remove(c.getName())!=null;
    }

    /**
     * Returns the unique name of this case base.
     *
     * @return the unique name of this case base
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this case base to the given name.
     * This method should not be called when name
     * is not unique.
     *
     * @param cbName the new name of this case base
     * @throws Exception if name is null or empty or there is a case base with
     *  this name in the corresponding project
     */
    @Override
    public void setName(final String cbName) throws Exception {
        if (cbName == null || cbName.trim().equals("")) {
            throw new Exception("Cannot give a case base an empty name!");
        }
        ICaseBase cbTMP = prj.getCaseBases().get(cbName);
        if (cbTMP != null) {
            // remove this case base from the list
            String oldName = new String(this.name);
            this.name = cbName;
            setChanged();
            notifyObservers(new RenameAction(this, oldName));
        } else {
            throw new Exception("Case base with name \"" + name + "\" "
                    + "already exists in project \"" + prj.getName() + "\"");
        }
    }

    /**
     * Tells whether this case base contains a case with the given name.
     *
     * @param caseName the name of the case to be checked for
     * @return the case, if there is a case with the given name, null otherwise.
     */
    @Override
    public Instance containsCase(final String caseName) {
        return cases.get(caseName);
    }

    /**
     * Removes the first case with the given name. If there is no case with the
     * given name, does nothing.
     *
     * @param caseName the name of the case to be removed
     */
    @Override
    public boolean removeCase(final String caseName) {
        return cases.remove(caseName)!=null;
    }

    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    /**
     * This observes all cases.
     * On change of any of those cases, the observers of this are notified
     * such that they can react on the changes.
     *
     * @param arg0 observable
     * @param arg1 object
     */
    @Override
    public void update(Observable arg0, Object arg1) {
    	if (arg1!=null && arg1 instanceof RenameAction) {
    		if (arg0 != null && arg0 instanceof Instance) {
    			Instance i = (Instance)arg0;
    			RenameAction r = (RenameAction)arg1;
    			Instance tmp = cases.get(r.getOldName()); 
    			if (i.equals(tmp)) {
    				cases.remove(r.getOldName());
    				cases.put(i.getName(), i);
        			setChanged();
        			notifyObservers();
    			}
    		}
    	}
    }

	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @return the current date format
	 */
	public DateFormat getDateFormat() {
		return df;
	}
    
    public Project getProject() {
    	return prj;
    }

}
