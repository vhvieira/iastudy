/**
 * Contains classes for the basic definition of <code>DefaultCaseBase</code> objects.
 * For each attribute type there is one class that represents values of this
 * type.<br />
 * These attributes are maintained by ranges (classes implementing IRange).
 * For each attribute description there
 * is one range, so that each value contained in a range knows its description
 * (and therefore its similarity functions).
 * Ranges are introduced to avoid unnecessary objects,
 * meaning that for a fixed description we have one attribute object for each
 * value occurring in a query/case.
 * If a value occurs more than once, this object is then referenced.
 * <p>
 * This is the basic idea for the representation of values. Not all attributes
 * follow this
 * concept. For example strings: we expect that you do not use a string more
 * than once. To save runtime in looking
 * for an object having this string as value, we always create a new string
 * object.
 * <br />
 * <code>Instance</code> objects represent real-world objects. A case base is a collection
 * of <code>Instance</code> objects. From point of view of a case base, an <code>DefaultCaseBase</code> object
 * is called <em>case</em>.
 * </p>
 * @author myCBR Team
 * @since myCBR 3.0.0
 */
package de.dfki.mycbr.core.casebase;

