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
package de.dfki.mycbr.explanation;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.jdom.DataConversionException;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;

import de.dfki.mycbr.Helper;

public class DefaultSMExplanation implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4008282621990526611L;
	
	public static final String XML_ATT_AUTHOR		= "author";
	public static final String XML_ATT_RATIONALE	= "rationale";
	public static final String XML_ATT_CREATIONDATE	= "creationDate";
	public static final String XML_ATT_USERDATA		= "userData";
	
	public static final String XML_TAG_USERDATA		= "userData";
	public static final String XML_ATT_INDEX		= "index";
	public static final String XML_ATT_USEROBJECT	= "object";
	
	private String rationale;
	private String author;
//	private Object context;
	private Date creationDate;
	private Vector<String> userData = new Vector<String>();

	
	public DefaultSMExplanation(String author, String rationale, Date creationDate) {
		this.author 		= author;
		this.rationale 		= rationale;
		this.creationDate 	= creationDate;
//		this.context 	= context;
	}
	
	public DefaultSMExplanation(String author, String rationale, Date creationDate, Vector<String> userData) {
		this.author 		= author;
		this.rationale 		= rationale;
		this.creationDate 	= creationDate;
		this.userData 		= userData;
//		this.context 	= context;
	}
	
	@SuppressWarnings("unchecked")
	public DefaultSMExplanation(Element element) {
		this.author 		= Helper.decode(element.getAttributeValue(XML_ATT_AUTHOR));
		this.rationale		= Helper.decode(element.getAttributeValue(XML_ATT_RATIONALE));
		try {
			this.creationDate	= new Date(element.getAttribute(XML_ATT_CREATIONDATE).getLongValue());
		} catch (DataConversionException e) {
			creationDate = new Date(0L);
			e.printStackTrace();
		}

		HashMap<Integer, String> map = new HashMap<Integer,String>();
		for (Iterator it = element.getDescendants(new ElementFilter(XML_TAG_USERDATA)); it.hasNext();) {
			Element userElement = (Element) it.next();
			try {
				int index = userElement.getAttribute(XML_ATT_INDEX).getIntValue();
				String o = Helper.decode(userElement.getAttributeValue(XML_ATT_USEROBJECT));
				map.put(index, o);				
			} catch (DataConversionException e) {
				e.printStackTrace();
			}
		}
		for (int i = 0; i < map.size(); i++) {
			userData.add(map.get(i));
		}
		
	}
	
	public String getRationale() {
		return rationale;
	}
	
	public String getAuthor() {
		return author;
	}
	
//	public Object getContext()
//	{
//		return context;
//	}

	public void toXML(Element element) {
		element.setAttribute(XML_ATT_AUTHOR, Helper.encode(author));
		element.setAttribute(XML_ATT_RATIONALE, Helper.encode(rationale));
		element.setAttribute(XML_ATT_CREATIONDATE, Helper.encode(Long.toString(creationDate.getTime())));
//		if (userData!=null) element.setAttribute(XML_ATT_USERDATA, userData);
		
		for (int i = 0; i < userData.size(); i++) {
			String o = userData.get(i);
			Element userElement = new Element(XML_TAG_USERDATA);
			userElement.setAttribute(XML_ATT_INDEX, Helper.encode(Integer.toString(i)));
			userElement.setAttribute(XML_ATT_USEROBJECT, Helper.encode(o));
			element.addContent(userElement);
		}

	}

	public Date getCreationDate() {
		return creationDate;
	}

	public Vector<String> getUserData() {
		return userData;
	}
}
