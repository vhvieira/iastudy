/**
 * All retrieval algorithms extend the abstract class <code>RetrievalEngine</code>
 * and can be used within <code>Retrieval</code> objects to obtain the retrieval results 
 * (possibly ordered pairs of case and corresponding similarity). For each retrieval you have
 * to define a query (given as a special <code>Instance</code> object). Based on the model and
 * the defined similarity measures, the retrieval algorithm is supposed to calculate a similarity
 * between the query and each case in the given case base.
 * 
 * @author myCBR Team
 * @since myCBR 3.0.0
 */
package de.dfki.mycbr.core.retrieval;
