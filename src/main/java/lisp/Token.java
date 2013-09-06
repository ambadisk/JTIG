package lisp;


public class Token {
	
	private TokenType tokentype;
	
	private Object tokenstring;
	
	private int line;
	
	private int character;
	
	public Token(Object tokenstring,TokenType tokentype,int line,int character){
		this.tokentype = tokentype;
		this.tokenstring = tokenstring;
		this.line = line;
		this.character = character;
	}
	
	public TokenType gettype(){
		return this.tokentype;
	}
	
	public Object getstring(){
		return this.tokenstring;
	}

	@Override
	public String toString() {
		return "Token (" + tokentype + ", '" + tokenstring
				+ "', Line: " + line + ", Char: " + character + ")";
	}
	
}