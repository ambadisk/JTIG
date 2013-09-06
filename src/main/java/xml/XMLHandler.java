/**
 * 
 */
package xml;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import parser.AnchorStrategy;
import parser.RuleTree;

/**
 * Callback Handler for SAX-Parser.
 * Parses from the XML-File a List of {@link RuleTree} for processing on later in the main parser.
 * @author Fabian Gallenkamp
 */

public class XMLHandler extends DefaultHandler {
	
	/**
	 * True, if actual processing a grammar
	 */
	private boolean inltig;
	
	/**
	 * True, if actual processing a tree.
	 */
	private boolean intree;
	
	/**
	 * Contains actual node in the tree, which should be parsed. 
	 */
	private TreeNode actnode;
	
	/**
	 * Stores the actual tree id
	 */
	private long treeid;
	
	/**
	 * Stores the actual tree frequency
	 */
	private long treefreq;
	
	/**
	 * Stores the actual tree probability
	 */
	private double treeprob;
	
	/**
	 * Strategy for finding anchor elements in a TreeNode.
	 */
	private AnchorStrategy anchorstrategy;

	/**
	 * Stores a list of {@link RuleTree}'s, found so far.
	 */
	private List<RuleTree> ruletrees;
	
	/**
	 * Creates a {@link XMLHandler}.
	 * @param anchorstrategy Strategy for finding anchor elements in a TreeNode.
	 */
	public XMLHandler(AnchorStrategy anchorstrategy){
		ruletrees = new LinkedList<RuleTree>();
		this.anchorstrategy = anchorstrategy;
	}
	
	/**
	 * Receive notification of the start/end of an element. 
	 * Builds together with {@link #endElement(String, String, String)} the tree for a grammar rule.
	 * Those tree rules are converted into {@link RuleTree}- Objects. 
	 * Then they are added and stored in this object until the document is completely parsed.
	 */
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (qName.equals("ltig")){
			inltig = true;
		} else if (qName.equals("tree") && inltig){
			intree = true;
			this.treeid = Long.parseLong(attributes.getValue("id"));
			this.treefreq = Long.parseLong(attributes.getValue("freq"));
			this.treeprob = Double.parseDouble(attributes.getValue("prob"));
		} else if (qName.equals("node") && intree){
			TreeNode n = createfromXMLNode(this.actnode,attributes);
			if (this.actnode != null)
				this.actnode.addchild(n);
			this.actnode = n;
		}
	}
	
	/**
	 * See: {@link #startElement(String, String, String, Attributes)}
	 */
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (qName.equals("ltig")){
			inltig = false;
		} else if (qName.equals("tree") && inltig){
			//store RuleTree
			this.ruletrees.add(this.actnode.converttoruletree(
					treeid,
					treefreq,
					treeprob,
					this.anchorstrategy));
			//forget real tree representation
			this.actnode = null;
			intree = false;
		} else if (qName.equals("node") && intree){
			TreeNode parent = this.actnode.getparent();
			if (parent != null)
				this.actnode = parent;
		}
	}
	/**
	 * Converts the XML-node into an {@link TreeNode}.
	 * @param parent the parent tree node parsed out of the XML
	 * @param attributes the attributes of the xml-node-element
	 * @return the tree node for the xml node, with the specified type, label
	 * @throws IllegalStateException if type is unknown.
	 */
	private TreeNode createfromXMLNode(TreeNode parent,Attributes attributes){
		NodeType treenodetype = NodeType.SUBST;
		boolean hascat = false;
		for (NodeType nt : NodeType.values()){
			if (attributes.getValue("type").equals(nt.toString().toLowerCase())){
				treenodetype = nt;
				hascat = true;
			}
		}
		if (!hascat)
			throw new IllegalStateException("Type '"+attributes.getValue("type")+"' unknown.");
		String label;
		if (treenodetype == NodeType.TERM)
			label = attributes.getValue("label");
		else
			label = attributes.getValue("cat");
		return new TreeNode(parent,treenodetype,label);
	}
	
	/**
	 * 
	 * @return all extracted rule trees in the grammar.
	 */
	public List<RuleTree> getruletrees() {
		return this.ruletrees;
	}
}
