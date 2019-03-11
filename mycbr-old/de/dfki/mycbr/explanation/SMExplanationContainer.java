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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.jdom.Element;
import org.jdom.filter.ElementFilter;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.XMLConstants;

public class SMExplanationContainer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 658516159283050623L;
	
	public static final String XML_TAG_ENTRY	= "Entry";
	public static final String XML_ATT_QVAL		= "qValue";
	public static final String XML_ATT_CVAL		= "cValue";
	
	private ValueType valueType;
	
	
	private HashMap<Object,HashMap<Object,DefaultSMExplanation>> map = new HashMap<Object,HashMap<Object,DefaultSMExplanation>>(); 

	public SMExplanationContainer(ValueType valueType) {
		this.valueType = valueType;
	}

	public DefaultSMExplanation getExplanation(Object q, Object c) {
		HashMap<Object, DefaultSMExplanation> map2 = map.get(q);
		if (map2 == null) {
			return null;
		}
		DefaultSMExplanation exp = map2.get(c);
		return exp;
	}

	public void setExplanation(Object q, Object c, DefaultSMExplanation exp) {
		HashMap<Object, DefaultSMExplanation> map2 = map.get(q);
		if (map2 == null) {
			map2 = new HashMap<Object, DefaultSMExplanation>();
			map.put(q, map2);
		}
		map2.put(c, exp);
	}
	
	@SuppressWarnings("unchecked")
	public void init(Element element) {
		valueType = ValueType.getValueType(Helper.decode(element.getAttributeValue(XMLConstants.XML_ATT_TYPE)));
		for (Iterator it = element.getDescendants(new ElementFilter(XML_TAG_ENTRY)); it.hasNext();) {
			Element entryElement = (Element) it.next();
			Object q = valueType.newInstance(Helper.decode(entryElement.getAttributeValue(XML_ATT_QVAL)));
			Object c = valueType.newInstance(Helper.decode(entryElement.getAttributeValue(XML_ATT_CVAL)));
			DefaultSMExplanation exp = new DefaultSMExplanation(entryElement);
			setExplanation(q, c, exp);
		}
	}

	public void toXML(Element parentElement) {
		parentElement.setAttribute(XMLConstants.XML_ATT_TYPE, Helper.encode(valueType.toString()));
		
		for (Object q: map.keySet()) {
			String qStr = q.toString();
			for (Entry<Object,DefaultSMExplanation> e : map.get(q).entrySet()) {
				String cStr = e.getKey().toString();
				DefaultSMExplanation exp = e.getValue();
				if (exp == null) continue;
				
				Element entryElement = new Element(XML_TAG_ENTRY);
				entryElement.setAttribute(XML_ATT_QVAL, Helper.encode(qStr));
				entryElement.setAttribute(XML_ATT_CVAL, Helper.encode(cStr));
				exp.toXML(entryElement);
				parentElement.addContent(entryElement);
			}
		}
	}

	public HashMap<Object, HashMap<Object, DefaultSMExplanation>> getMap() {
		return map;
	}

}
