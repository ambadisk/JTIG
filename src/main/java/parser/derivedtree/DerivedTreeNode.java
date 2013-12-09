/**
 * 
 */
package parser.derivedtree;

import grammar.tiggrammar.ElementaryTree;
import grammar.tiggrammar.Entry;
import grammar.tiggrammar.Layer;
import grammar.tiggrammar.NodeType;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import tools.GeneralTools;

import com.mxgraph.view.mxGraph;

/**
 * 
 * @author Fabian Gallenkamp
 */
public class DerivedTreeNode {

	protected DerivedTreeNode parent;
	
	protected Entry entry;
	
	private Object printed;
	
	protected int[] address;
	
	protected ArrayList<DerivedTreeNode> children = new ArrayList<DerivedTreeNode>();
	
	public DerivedTreeNode(ElementaryTree tree){
		this.entry = tree.getLayer(0).getEntry(0);
		this.address = new int[]{0};
		buildTree(tree,tree.getLayer(0) , null);
	}
	
	public DerivedTreeNode(DerivedTreeNode parent, Entry entry, int[] address){
		this.parent = parent;
		this.entry = entry;
		this.address = address;
	}
	
	private void buildTree(ElementaryTree tree,Layer actlayer,DerivedTreeNode parent){
		//create node for layer, if it isn't root node
		if (parent != null){
			DerivedTreeNode newnode = new DerivedTreeNode(parent,actlayer.getEntry(0),actlayer.getGornNumber());
			parent.children.add(newnode);
			parent = newnode;
		} else 
			parent = this;
		
		for (int i = 1;i < actlayer.getEntrys().length; i++){
			
			Layer child = tree.getChildLayer(actlayer, actlayer.getEntry(i));
			if (child == null){
				DerivedTreeNode leaf = new DerivedTreeNode(parent,actlayer.getEntry(i),GeneralTools.AppendToIntArray(actlayer.getGornNumber(), i));
				parent.children.add(leaf);
			} else {
				buildTree(tree, child, parent);
			}
		}
		
	}
	
	protected void storeToXML(OutputStream stream, String comment) {

	}
	
	protected void paintWithJGraphX(mxGraph graph) {
		Object graphparent = graph.getDefaultParent();
		Queue<DerivedTreeNode> rem = new LinkedList<DerivedTreeNode>();
		rem.offer(this);
		while (!rem.isEmpty()){
			DerivedTreeNode element = rem.poll();
			// draw vertex
			String style = "shape=ellipse;fontColor=black;";
			String description = "";
			switch (element.entry.getNodeType()){
			case EPS:
				style += "fillColor=#008080;";
				break;
			case SUBST:
				style += "fillColor=#FF8C00;";
				description = "\u2193"; // DOWN ARROW
				break;
			case NONTERM:
				style += "fillColor=#F5F5F5;";
				break;
			case TERM:
				style += "fillColor=#9ACD32;";
				break;
			case LFOOT:
			case RFOOT:
				style += "fillColor=#DC143C;";
				description = "*"; // STAR
				break;
			default:
				style += "fillColor=#800000;";
				break;
			}
			description = element.entry.getLabel() + description;
			element.printed = graph.insertVertex(graphparent, "",description, 0, 0, description.length() * 15 , 20,style);
		
			// draw edge for all except root
			if (element.parent != null)
				graph.insertEdge(graphparent, "", "" ,element.parent.printed, element.printed);
			
			// add children
			for (DerivedTreeNode child : element.children){
				rem.offer(child);
			}
		}
	}

	public ArrayList<DerivedTreeNode> getChildren() {
		return children;
	}

	protected DerivedTreeNode getAdjunctionFoot() {
		Queue<DerivedTreeNode> rem = new LinkedList<DerivedTreeNode>();
		rem.offer(this);
		while (!rem.isEmpty()){
			DerivedTreeNode element = rem.poll();
			if (element.children.size() <= 0 && 
					(element.entry.getNodeType() == NodeType.LFOOT || element.entry.getNodeType() == NodeType.RFOOT))
				return element;
			for (DerivedTreeNode child : element.children){
				rem.offer(child);
			}
		}
		throw new IllegalStateException("No adjunction foot found.");
	}

	protected void replaceNodeBeyond(DerivedTreeNode head) {
		if (parent != null){
			int toreplace = -1;
			for (int i = 0; i < parent.children.size(); i++){
				if (parent.children.get(i) == this){
					toreplace = i;
				}
			}
			if (toreplace == -1)
				throw new IllegalStateException();
			parent.children.set(toreplace, head);
			head.parent = this.parent;
			head.entry = entry;
			this.parent = null;
		}		
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DerivedTreeNode [entry=" + entry + ", printed=" + printed
				+ ", address=" + Arrays.toString(address) + "]";
	}

	public void replaceNodeBeneath(DerivedTreeNode foot) {
		for (int i = 0; i < children.size(); i++){
			children.get(i).parent = foot;
		}
		foot.children = children;
		//foot.entry = entry;
		children = null;
	}
	
	
}