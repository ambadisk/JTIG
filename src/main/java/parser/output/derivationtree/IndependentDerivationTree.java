/**
 * 
 */
package parser.output.derivationtree;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import parser.early.JTIGParser;
import parser.early.components.DerivationType;
import parser.early.components.Item;
import parser.early.components.ItemDerivation;
import parser.lookup.ActivatedElementaryTree;
import tools.GeneralTools;

/**
 * 
 * @author Fabian Gallenkamp
 */
public class IndependentDerivationTree extends DerivationTree{

	public static long multiple_derivations = 1;

	public static boolean derivationAllowed(){
		String prop = JTIGParser.getProperty("parser.core.amountderivations");
		if ("infinity".equals(prop))
			return true;
		long amount = Long.parseLong(prop);
		if (amount > 1)
			return multiple_derivations <= amount;
		return false;
	}
	
	/**
	 * 
	 * @param items
	 *            - a list with all {@link Item} representing different
	 *            derivations.
	 * @return All {@link IndependentDerivationTree}'s resulting from the item-structure
	 *         obtained by the parsing process.
	 */
	public static List<IndependentDerivationTree> createDerivationTrees(List<Item> items) {
		// list for result
		List<IndependentDerivationTree> derivationtrees = new LinkedList<IndependentDerivationTree>();

		// iterate over all items to be analyzed
		for (Item currentitem : items) {
			// create derivation tree add to result
			IndependentDerivationTree actualtree = new IndependentDerivationTree(currentitem.getActivatedElementaryTree());
			derivationtrees.add(actualtree);
			
			Stack<Item> itemstack = new Stack<Item>();
			itemstack.add(currentitem);
			// call recursion
			analyzeItemsRecursively(derivationtrees, actualtree,itemstack);
		}

		return derivationtrees;
	}

	private static void analyzeItemsRecursively(List<IndependentDerivationTree> derivationtrees, IndependentDerivationTree acttree, Stack<Item> itemstack) {
		while (!itemstack.isEmpty()) {
			// pop actual item
			Item actitem = itemstack.pop();
			
			// initialize temporary variables, which store the actual stack and derivation tree
			Stack<Item> tmpstack = null;
			IndependentDerivationTree tmptree = null;
			
			if (actitem.getDerivations().size() > 1){ // if there are ambiguous derivations
				// do a real copy into temporary variables
				tmpstack = new Stack<Item>();
				tmpstack.addAll(itemstack);
				tmptree  = acttree.copy();
			}
			
			boolean first = true; // indicates the first derivation in for-loop
			
			for (ItemDerivation itemderiv : actitem.getDerivations()) {		
				// create variables for !first
				DerivationEdge tobeadded = null;
				IndependentDerivationTree other = null;
				Stack<Item> newstack = null;	
				boolean predict = false;
				// calculate DerivationEdge on completion, stop on prediction, continue on traversation
				
				switch (itemderiv.getType()) {
					// predictions => stop criterion
					case PredictLeftAux:
					case PredictRightAux:
					case PredictSubstitution:
					case PredictTraversation:
						predict = true;
						break;
					// completions => add nodes, make edges
					case CompleteSubstitution:
					case CompleteLeftAdjunction:
					case CompleteRightAdjunction:
						tobeadded = extractDerivationEdge(itemderiv, actitem);
						break;
						// Traversation => do nothing but traverse
					default:
						break;
				}
				if(predict) // stop loop on predict items
					continue;
				
				if (!first) { // more than one derivation case
					other = tmptree.copy(); // copy actual derivation and make new one
					derivationtrees.add(other);
					
					newstack = new Stack<Item>();
					newstack.addAll(tmpstack);
				}
				
				if (tobeadded != null) // if completion occurred a derivation-edge was created
					if (first) // add to acttree
						acttree.addDerivation(tobeadded);
					else // add to copy
						other.addDerivation(tobeadded);

				Item[] items = itemderiv.getItems();
				for (int i = items.length-1;i>=0;i--){ // push remaining items to stack
					if (first) // if first derivation push to itemstack
						itemstack.push(items[i]);
					else // else to newly created one
						newstack.push(items[i]);
				}
				
				if (!first && derivationAllowed()) { // make recursive calls for all non-first derivations
					multiple_derivations++;
					analyzeItemsRecursively(derivationtrees, other, newstack);
					
				}
				

				if (first) // set !first if not already set
					first = false;
			}
		}
	}
	
	private static DerivationEdge extractDerivationEdge(ItemDerivation itemderiv, Item actualitem){
		
		ActivatedElementaryTree inserted = null;
		for (Item deriveditem : itemderiv.getItems()) {
			
			ActivatedElementaryTree deriveditematree = deriveditem.getActivatedElementaryTree();
			if (!actualitem.getActivatedElementaryTree().equals(deriveditematree))
				inserted = deriveditematree;
			
		}
		if (inserted == null)
			throw new IllegalStateException();
		// Add derivation edge, substitution or adjunction
		if (itemderiv.getType() == DerivationType.CompleteSubstitution)
			return new SubstitutionDerivationEdge(actualitem.getActivatedElementaryTree(), inserted , 
					GeneralTools.IntArrayToIntegerArray(
							GeneralTools.AppendToIntArray(
									actualitem.getLayer().getGornNumber(),
									actualitem.getDotPosition() - 1
							)
					));
		else
			return new AdjunctionDerivationEdge(actualitem.getActivatedElementaryTree(), inserted , 
					GeneralTools.IntArrayToIntegerArray(actualitem.getLayer().getGornNumber()));
	}
	

	public IndependentDerivationTree(ActivatedElementaryTree root) {
		super(root);
	}

	public IndependentDerivationTree(Map<ActivatedElementaryTree, Object> nodesprinted,List<DerivationEdge> edges, ActivatedElementaryTree root) {
		super(nodesprinted,edges,root);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Independent derivation";
	}
}
