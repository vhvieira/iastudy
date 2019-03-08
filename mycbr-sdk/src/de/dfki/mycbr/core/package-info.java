/**
 * Contains all classes that represent core functionality of a CBR application
 * such as the domain model, case bases, similarity functions and retrieval algorithms.   
 * Classes contained in this package allow <code>de.dfki.mycbr.Project</code>
 * objects to access the sub packages <code>de.dfki.mycbr.casebase</code> and
 * <code>de.dfki.mycbr.model</code>. Each <code>Project</code> has one model and
 * several case bases for this model. The standard case base
 * <code>DefaultCaseBase</code> uses the classes defined in
 * <code>de.dfki.mycbr.casebase</code> for its representation.
 * However, in some cases it might be useful not to load the whole case base.
 * For example, when using data bases it might be useful to load only those
 * values currently needed. All case bases have to implement the interface
 * <code>ICasebase</code>.
 *
 * @author myCBR Team
 * @since myCBR 3.0.0
 */
package de.dfki.mycbr.core;

