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

import org.jdom.DataConversionException;
import org.jdom.Element;

import de.dfki.mycbr.Helper;

public class KnowledgeSource implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -68277805205406509L;
	
	public static final String TOKEN_CONCEPT		= "$CONCEPT$";
	public static final String TOKEN_CONCEPT_REGEXP	= "\\$CONCEPT\\$";
	
	public static final String XML_TAG_KNOWLEDGE_SOURCE	= "KnowledgeSource";
	public static final String XML_ATT_NAME 		= "name";
	public static final String XML_ATT_PATTERN 		= "pattern";
	public static final String XML_ATT_TYPE 		= "type";

	public static final int TYPE_WEB_RESOURCE	= 0;
	public static final int TYPE_FILE 			= 1;
	public static final int TYPE_SYS_CALL		= 2;
	
	private String title = "";
	private String pattern = "http://www.XYZ.net/"+TOKEN_CONCEPT;
	private int sourceType = TYPE_WEB_RESOURCE;
	
	
	public KnowledgeSource() {
	}

	public KnowledgeSource(Element xmlElement) {
		try {
			title 		= Helper.decode(xmlElement.getAttributeValue(XML_ATT_NAME));
			pattern		= Helper.decode(xmlElement.getAttributeValue(XML_ATT_PATTERN));
			sourceType	= xmlElement.getAttribute(XML_ATT_TYPE).getIntValue();
		} catch (DataConversionException e) {
			e.printStackTrace();
		}
	}
	
	public String resolve(String conceptName) {
		return pattern.replaceAll(TOKEN_CONCEPT_REGEXP, conceptName);
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getPattern() {
		return pattern;
	}
	
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
	public int getSourceType() {
		return sourceType;
	}
	
	public void setSourceType(int sourceType) {
		this.sourceType = sourceType;
	}
	
	@Override
	public String toString() {
		return ("".equals(title)? "unnamed": title);
	}
	
	public Element toXML() {
		Element sourceElement = new Element(XML_TAG_KNOWLEDGE_SOURCE);
		sourceElement.setAttribute(XML_ATT_NAME, Helper.encode(title));
		sourceElement.setAttribute(XML_ATT_TYPE, Helper.encode(Integer.toString(sourceType)));
		sourceElement.setAttribute(XML_ATT_PATTERN, Helper.encode(pattern));
		return sourceElement;
	}

}
