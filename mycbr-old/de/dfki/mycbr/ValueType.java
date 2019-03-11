/*
 * myCBR License 1.1
 *
 * Copyright (c) 2008
 * Thomas Roth-Berghofer, Armin Stahl & Deutsches Forschungszentrum f&uuml;r K&uuml;nstliche Intelligenz DFKI GmbH
 * Further contributors: myCBR Team (see http://mycbr-project.net/contact.html for further information 
 * about the myCBR Team). 
 * All rights reserved.
 *
 * myCBR is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Since myCBR uses some modules, you should be aware of their licenses for
 * which you should have received a copy along with this program, too.
 * 
 * endOfLic */
package de.dfki.mycbr;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.logging.Logger;

import de.dfki.mycbr.model.vocabulary.ModelCls;


/**
 * Represents the value type of a given object.
 * myCBR supports BOOLEAN, CLASS, FLOAT, INTEGER, STRING. SYMBOL and INSTANCE
 * value types. Every other object is mapped to NO_TYPE.
 *   
 * @author myCBR Team
 */
public class ValueType implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private final static Logger log = Logger.getLogger ( ValueType.class.getName ( ) );

	private String typeName;
	private Class<?> javaType;

    private static final HashMap<String, ValueType> valueTypes = new HashMap<String, ValueType>();

    public static final ValueType BOOLEAN	= new ValueType("Boolean", Boolean.class);
    public static final ValueType CLS 		= new ValueType("Class", ModelCls.class);
    public static final ValueType FLOAT 	= new ValueType("Float", Float.class);
    public static final ValueType INTEGER 	= new ValueType("Integer", Integer.class);
    public static final ValueType STRING 	= new ValueType("String", String.class);
    public static final ValueType SYMBOL 	= new ValueType("Symbol", String.class);
    public static final ValueType INSTANCE	= new ValueType("Instance", Object.class);
    public static final ValueType NO_TYPE	= new ValueType("NoType", Object.class);
	
    /**
     * Constructor for a value type object. Maps the type name to this value type.
     * 
     * @param typeName the type name of the java type
     * @param javaType the type specified by typeName
     */
	private ValueType(String typeName, Class<?> javaType){
    	this.typeName = typeName;
    	this.javaType = javaType;
    	valueTypes.put(typeName, this);
    }
    
	/**
	 * Returns a new instance of the specified java type
	 * @param valueStr representing the name of the new instance
	 * @return null if parse error occurred. Returns an instance of value type. 
	 */
	public Object newInstance(String valueStr) {
		try {
			Constructor<?> constr = javaType.getDeclaredConstructor(new Class<?>[] { String.class });
			return constr.newInstance((Object[]) new String[]{valueStr});
		} catch (Exception e) {
			log.fine("could not instantiate instance of type ["+typeName+"]: value = ["+valueStr+"]"+ e.getMessage());
		}
		return null;
	}

	/**
	 * Returns the specified typeName.
	 * @return the type name
	 */
	public String toString() {
		return typeName;
	}

	/**
	 * Getter for the ValueType object represented by the given valueType string. 
	 * @param valueType String name of value type (e.g. String, Boolean, Float,...)
	 * @return ValueType corresponding ValueType object.
	 */
	public static ValueType getValueType(String valueType) {
		ValueType tmp = (ValueType)valueTypes.get(valueType);

		if (tmp==null) log.fine("Cannot find valueType ["+valueType+"]");

		return tmp;
	}

}
