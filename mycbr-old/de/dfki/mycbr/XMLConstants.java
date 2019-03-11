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


/**
 * Providing public access to strings representing the XML tags for loading/saving 
 * myCBR projects information.
 * 
 * @author myCBR Team
 */
public class XMLConstants {
	
	// name extensions for project files
	public static final String FILENAME_SUFFIX_EXPLANATIONS	= "_CBR_EXPLANATIONS.XML";
	public static final String FILENAME_SUFFIX_OPTIONS 		= "_CBR_OPTIONS.XML";
	public static final String FILENAME_SUFFIX_CASEBASE 	= "_CBR_CASEBASE.XML";
	public static final String FILENAME_SUFFIX_SMF 			= "_CBR_SMF.XML";
	public static final String FILENAME_SUFFIX_TRAINING_DB  = "_CBR_TRAINING_DB.XML";
	
	// xml constants for smf
	public static final String XML_TAG_DOCUMENT 			= "Document";
	public static final String XML_TAG_HOLDER 				= "Holder";
	public static final String XML_TAG_SMFUNCTION 			= "SMFunction";
	public static final String XML_TAG_MULTIMEASURE			= "MultiMeasure";
	public static final String XML_TAG_INHERITANCEMEASURE	= "InheritanceMeasure";

	// xml constants for general project information
	public static final String XML_ATT_VERSION 				= "MyCBR_version";
	public static final String XML_ATT_PROJECT_VERSION 		= "Project_version";
	public static final String XML_ATT_PROJECT_AUTHOR 		= "Project_author";
	public static final String XML_ATT_ACTIVE 				= "active";
	public static final String XML_ATT_TYPE 				= "type";
	public static final String XML_ATT_SMFNAME 				= "smfname";
	public static final String XML_ATT_MODEL_INSTNAME 		= "model_instname";
	public static final String XML_ATT_SIMMODE 				= "simMode";
	public static final String XML_ATT_TOPCLS 				= "topCls";

	// xml constants for model information
	public static final String XML_TAG_INSTANCES_FOR_CLASS 	= "Instances_for_Class";
	public static final String XML_TAG_CASE_INSTANCE 		= "Instance";
	public static final String XML_ATT_CLASSNAME  			= "class";
	public static final String XML_TAG_SLOTVALUE			= "slotvalue";
	public static final String XML_ATT_SLOT 				= "slot";
	public static final String XML_ATT_VALUE				= "value";
	public static final String XML_TAG_PROTOTYPE 			= "Prototype";
	public static final String XML_ATT_SUPERCLASS			= "super_class";
	public static final String XML_ATT_MIN_VALUE			= "minval";
	public static final String XML_ATT_MAX_VALUE			= "maxval";
	public static final String XML_ATT_ALLOWED_VALUES		= "allowed_values";
	public static final String XML_ATT_MULTIPLE				= "multiple";
	
	// xml constants for the training database:
	public static final String XML_TAG_TRAINING_DATA        = "TrainingData";
	public static final String XML_TAG_TRAINING_EXAMPLE     = "TrainingExample";
	public static final String XML_TAG_QUERY                = "Query";
	public static final String XML_TAG_ASSESSED_INSTANCE    = "AssessedInstance";
	public static final String XML_ATTR_ASSESSMENT          = "assessment";
	public static final String XML_ATTR_EXPLICIT_USABILITY  = "explicit_usability";
	public static final String XML_ATT_TRAINING_DATA_NAME   = "training_data_name";
	
	public static final String XML_TAG_SMEXPLANATION 		= "SMExplanation";

}

