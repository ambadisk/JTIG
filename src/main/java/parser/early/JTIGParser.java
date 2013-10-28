/**
 * 
 */
package parser.early;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.InvalidPropertiesFormatException;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

import parser.early.inferencerules.CompleteSubstitution;
import parser.early.inferencerules.CompleteTraversation;
import parser.early.inferencerules.InferenceRule;
import parser.early.inferencerules.PredictSubstitution;
import parser.early.inferencerules.PredictTraversation;
import parser.early.inferencerules.Scanning;
import parser.lookup.ActivatedLexicon;
import parser.lookup.ActivatedTIGRule;
import parser.lookup.Lookup;
import tools.tokenizer.MorphAdornoSentenceTokenizer;
import tools.tokenizer.Token;
import grammar.buildJtigGrammar.Lexicon;
import grammar.readXML.XMLReader;
/**
 * 
 * @author Fabian Gallenkamp
 */
public class JTIGParser {
	
	private static final String parserpropertypath = "resources/parser.properties";
	
	private static final StringBuilder stringbuilder = new StringBuilder();
	
	private static final Properties  parserproperties = new Properties();
	
	private Lexicon lexicon;
	
	private List<InferenceRule> inferencerules;
	
	private StringBuilder log = new StringBuilder();
	
	public JTIGParser() throws IOException{
		// Load preferences from property-file
		readproperties();
	}
	
	private void readproperties() throws InvalidPropertiesFormatException, IOException {
		InputStream is = null;
		is = new FileInputStream(parserpropertypath);
		JTIGParser.parserproperties.loadFromXML(is);
	}
	
	public static String getProperty(String key){
		String s = JTIGParser.parserproperties.getProperty(key);
		if (s != null)
			return s.trim();
		return null;
	}
	
	public static void setProperty(String key,String value){
		JTIGParser.parserproperties.setProperty(key,value);
		//os = new FileInputStream(parserpropertypath);
		//JTIGParser.parserproperties.storeToXML(parserpropertypath,null );
	}
	
	public static boolean getBooleanProperty(String key){
		return "true".equals(JTIGParser.getProperty(key).toLowerCase());
	}
	
	public static StringBuilder getStringBuilder(){
		return JTIGParser.stringbuilder;
	}
	
	public boolean readLexicon(){		
		// TODO possibly read more than one file to lexicon
		XMLReader xp = new XMLReader(getLexiconPaths()[0]);
		try {
			this.lexicon = xp.read();
		} catch (SAXException | IOException | ParserConfigurationException | XMLStreamException e) {
			this.lexicon = null;
			return false;
		}
		return true;
	}
	
	public List<Item> parseSentence(String originalsentence, Token[] tokens){
		this.log = new StringBuilder();
		appendToLog("Starting parse-process.");
		// extract all important TIGRule's and store in activatedlexicon
		ActivatedLexicon activatedlexicon = preprocessSentence(tokens);
		
		// Create necessary objects
		List<Item> results = new LinkedList<Item>();
		ItemFilter isterm = new TerminationCriterion(lexicon.getStartSymbols(),tokens.length);
		DefaultItemFactory factory = new DefaultItemFactory();
		Chart chart = new Chart();
		ItemComparator itemcomp = new ItemComparator(isterm);
		PriorityQueue<Item> agenda = new PriorityQueue<Item>(10, itemcomp);
		boolean finishedgood = false;
		
		// Initialize inference rules, which should be used in the parsing process
		initializeinferencerules(factory,chart,agenda,activatedlexicon);
		
		// initialize the chart with items created by the tokens
		chart.initialize(tokens , factory);
		
		//System.out.println(chart);
		
		//initialize the agenda with items created by the activated ruletrees with start-symbols
		if (!initializeAgenda(agenda , factory,activatedlexicon))
			return null;
		
		// Main loop
		appendToLog("Started main loop using following inference rules: "+inferencerules.toString());
		Item current;
		while ((current = agenda.poll()) != null){
			chart.addItem(current);
			System.out.println(current.getID());
			if (JTIGParser.getBooleanProperty("parser.stoponfirsttermitem") && isterm.apply(current)){
				System.out.println("breaked");
				results.add(current);
				finishedgood = true;
				break;
			}
			
			for (InferenceRule inferencerule : inferencerules){
				
				if (inferencerule.isApplicable(current)){
					inferencerule.apply(current);
				}
			}
			System.out.println(agenda.toString());
		}
		if (! JTIGParser.getBooleanProperty("parser.stoponfirsttermitem")){
			results = chart.getChartItems(isterm);
			if (results.size() > 0)
				finishedgood = true;
		}
		appendToLog("Finished main loop. "+factory.getAmountCreatedItems()+" items were created.");
		
		if (finishedgood)
			appendToLog("Success.");
		else 
			appendToLog("Failure.");
		
		return results;
	}
	
	private ActivatedLexicon preprocessSentence(Token[] tokens){
		if (lexicon == null)
			throw new IllegalArgumentException("Please read some lexicon first.");
		Lookup l = new Lookup(tokens , lexicon);
		ActivatedLexicon tmp = l.findlongestmatches();
		appendToLog("Found "+tmp.getSize()+" trees in lexicon which can possibly match in the sentence.");
		return tmp;
	}
	
	private void initializeinferencerules( DefaultItemFactory factory,Chart chart,PriorityQueue<Item> agenda,ActivatedLexicon activatedlexicon) {
		inferencerules = new LinkedList<InferenceRule>();
		
		inferencerules.add(new Scanning(factory,chart, agenda));
		
		inferencerules.add(new PredictTraversation(factory, agenda));
		inferencerules.add(new CompleteTraversation(factory,chart, agenda));
		
		inferencerules.add(new PredictSubstitution(factory, agenda, activatedlexicon));
		inferencerules.add(new CompleteSubstitution(factory, chart, agenda));
		
	}
	
	private boolean initializeAgenda(PriorityQueue<Item> agenda, DefaultItemFactory factory,ActivatedLexicon activatedlexicon) {
		boolean added = false;
		for (String startsymbol : lexicon.getStartSymbols()){
			List<ActivatedTIGRule> result = activatedlexicon.get(startsymbol);
			
			if (result != null)
				for (ActivatedTIGRule art : result){
					Item item = factory.createItemInstance(art,0); // initialize trees with span (0,0)
					agenda.add(item);
					
					if (!added)
						added = true;
				}
		}
		return added;
	}
	
	private String[] getLexiconPaths(){
		// select lexicon-path from properties
		String pathtolexicon = parserproperties.getProperty("grammar.lexicon.path");
		File f = new File(pathtolexicon);
		if (f.isDirectory()){ // TODO: return all paths of the xml files located in that directory
			throw new UnsupportedOperationException("Directory's not yet implemented as lexicon source.");
		} else{
			String [] ret = {pathtolexicon}; 
			return ret;
		}
	}
	
	public Lexicon getLexicon(){
		return lexicon;
	}
	
	private void appendToLog(String message) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		log.append( sdf.format(new Date()) + " : " + message + "\n");
	}
	
	public String getLog() {
		return log.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		return sb.toString();
	}

	public static void main(String[] args) throws IOException {
		String input = null;
		
		for (int i = 0;i<args.length;i++){
			if ("-h".equals(args[i])){
				
				System.out.println("Usage: jtig [OPTIONS] text\n"
						+ "Options:\n"
						+ "See recources/parser.properties");
				return;			
			} else if (i == (args.length-1)){
				input = args[i];
			} else {
				System.err.println("Wrong usage. Use -h for detailed description.");
				return;
			}
		}
		//Create components
		MorphAdornoSentenceTokenizer st = new MorphAdornoSentenceTokenizer();
		JTIGParser parser = new JTIGParser();
		// get tokens out of string
		Token[] tokens = st.getTokens(input);
		// read lexicon
		if (parser.readLexicon()){
			parser.parseSentence(input, tokens);
			//System.out.println();
		}
		
		//System.out.println(parser.toString());
	}

	public boolean hasLexicon() {
		return lexicon != null;
	}
	
}
