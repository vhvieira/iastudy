/**
 * Contains classes for the basic definition of the project's model.<br />
 * The model is represented as a tree-like structure representing an inheritance
 * hierarchy.
 * Elements in this hierarchy are objects of class
 * <code>ConceptDescription</code>. The highest concept descriptions in this
 * hierarchy are called top concepts.
 * Each concept consists of several attribute description. Simple attributes
 * are described by IntegerDescription, FloatDescription,
 * SymbolDescription, etc. A special attribute description is the
 * MultipleDescription which is used for describing sets of attributes of one
 * type.<br />
 *
 * Moreover, concept description can themselves be attribute descriptions again
 * (representing composition). Attribute Descriptions describe restrictions to
 * the values which can be used for attributes of this description.
 * The proper values are described by <code>Attribute</code> classes introduced
 * in the package <code>de.dfki.mycbr.core.casebase</code>.
 * To allow computation of similarity assure that your model does not contain
 * cycles. Cycles can easily be modeled using composition
 * (for example: a component consists of several other components).
 * However, the computation of similarities is not properly defines
 * this case.
 *
 * @since myCBR 3.0.0
 */
package de.dfki.mycbr.core.model;

