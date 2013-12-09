/**
 * 
 */
package parser.derivationtree;

import parser.lookup.ActivatedElementaryTree;

/**
 * 
 * @author Fabian Gallenkamp
 */
public class AdjunctionDerivationEdge extends DerivationEdge {

	protected AdjunctionDerivationEdge(ActivatedElementaryTree first, ActivatedElementaryTree second ,Integer[] connector) {
		super(first, second, connector);
		
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AdjunctionDerivationEdge [first=" + first + ", second="
				+ second + ", connector=" + connector + "]";
	}

}