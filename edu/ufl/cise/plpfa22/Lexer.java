package edu.ufl.cise.plpfa22;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.ufl.cise.plpfa22.IToken.Kind;

public class Lexer implements ILexer {
	
	public static enum State{
		START, 	
		HAVE_LSTHAN, //less than
		HAVE_GRTHAN,
		HAVE_IDENT,
		HAVE_NUM,
		HAVE_STRING,
		HAVE_WHITESPACE
		
	}
	
	public int current_token = 0;
	public ArrayList<Token> tokenset = new ArrayList<Token>();
	static final char EOF = 0;

	public Lexer(String input) {
		
		// adding eof to input
		char[] chinp = input.toCharArray();
		
		int len = input.length();
		char[] inp_eof= new char[len+1];
		for (int i=0; i<len; i++)
		{
			inp_eof[i]=chinp[i];
		}
		inp_eof[len]=EOF;
		
		//initializing variables
		int lexer_pos=0;
		int token_pos=0;
		int line=1;
		int gpos=0;
		State state = State.START;
	
		boolean eof_enc = true;
		char[] dummy = new char[1];
		

		
		//creating map for reserved words
		Map<String, Kind> reserved = new HashMap<String, Kind>();
		reserved.put("TRUE",Kind.BOOLEAN_LIT);
		reserved.put("FALSE",Kind.BOOLEAN_LIT);
		reserved.put("CONST",Kind.KW_CONST);
		reserved.put("VAR",Kind.KW_VAR);
		reserved.put("PROCEDURE",Kind.KW_PROCEDURE);
		reserved.put("CALL",Kind.KW_CALL);
		reserved.put("BEGIN",Kind.KW_BEGIN);
		reserved.put("END",Kind.KW_END);
		reserved.put("IF",Kind.KW_IF);
		reserved.put("THEN",Kind.KW_THEN);
		reserved.put("WHILE",Kind.KW_WHILE);
		reserved.put("DO",Kind.KW_DO);
		
		while (eof_enc)
		{
			char ch = inp_eof[gpos];
			switch(state)
			{
			case START ->
			{
				token_pos=lexer_pos;
				switch(ch)
				{
				// single character tokens
				case '.' ->
				{
					Token t = new Token(String.valueOf(ch), Kind.DOT, token_pos, line, lexer_pos, 1, dummy);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
				}
				
				case ',' ->
				{
					Token t = new Token(String.valueOf(ch), Kind.COMMA, token_pos, line, lexer_pos, 1, dummy);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
				}
				
				case ';' ->
				{
					Token t = new Token(String.valueOf(ch), Kind.SEMI, token_pos, line, lexer_pos, 1, dummy);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
				}
				
				case '(' ->
				{
					Token t = new Token(String.valueOf(ch), Kind.LPAREN, token_pos, line, lexer_pos, 1, dummy);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
				}
				
				case ')' ->
				{
					Token t = new Token(String.valueOf(ch), Kind.RPAREN, token_pos, line, lexer_pos, 1, dummy);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
				}
				
				case '+' ->
				{
					Token t = new Token(String.valueOf(ch), Kind.PLUS, token_pos, line, lexer_pos, 1, dummy);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
				}
				
				case '-' ->
				{
					Token t = new Token(String.valueOf(ch), Kind.MINUS, token_pos, line, lexer_pos, 1, dummy);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
				}
				
				case '*' ->
				{
					Token t = new Token(String.valueOf(ch), Kind.TIMES, token_pos, line, lexer_pos, 1, dummy);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
				}
				
				case '%' ->
				{
					Token t = new Token(String.valueOf(ch), Kind.MOD, token_pos, line, lexer_pos, 1, dummy);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
				}
				
				case '?' ->
				{
					Token t = new Token(String.valueOf(ch), Kind.QUESTION, token_pos, line, lexer_pos, 1, dummy);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
				}
				
				case '!' ->
				{
					Token t = new Token(String.valueOf(ch), Kind.BANG, token_pos, line, lexer_pos, 1, dummy);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
				}
				
				case '=' ->
				{
					Token t = new Token(String.valueOf(ch), Kind.EQ, token_pos, line, lexer_pos, 1, dummy);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
				}
				
				case '#' ->
				{
					Token t = new Token(String.valueOf(ch), Kind.NEQ, token_pos, line, lexer_pos, 1, dummy);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
				}
				
				case EOF ->
				{
					Token t = new Token("EOF", Kind.EOF, token_pos, line, gpos, 1, dummy);
					tokenset.add(t);
					eof_enc=false;
				}
				
				case ' ' ->
				{
					lexer_pos++;
					gpos++;
				}
				
				case '\n' ->
				{
					lexer_pos = 0;
					gpos++;
					line++;

				}
				
				case '\r' ->
				{
					lexer_pos++;
					gpos++;
				}
				
				case '\t' ->
				{
					lexer_pos++;
					gpos++;
				}
				//Division or comment
				
				case '/' ->
				{
					char next = inp_eof[gpos+1];
					
					//comment present
					if (next=='/')
					{
						gpos=gpos+2;
						char n = inp_eof[gpos];
						
						//while next characters aren't \n, \r or eof
						while(n!=(char)10 && n!='\r' && n!=EOF)
						{
							gpos++;
							n = inp_eof[gpos];
							
						}
						
						//eof encountered
						if (n==EOF)
						{
							Token t = new Token("EOF", Kind.EOF, token_pos, line, gpos, 1, dummy);
							tokenset.add(t);
							
						}
						
						//line ended; reset position to 0 and go to next line
						
						
						if(n=='\r')
						{
								line++;
								gpos=gpos++;
								lexer_pos=0;
						}
						if(n=='\n')
						{
								line++;
								gpos++;
								lexer_pos=0;
								System.out.println("in line end");
						}
						
					}
					//comments handled
					
					//just division symbol
					else
					{
						Token t = new Token(String.valueOf(ch), Kind.DIV, token_pos, line, gpos, 1, dummy);
						tokenset.add(t);
						lexer_pos++;
						gpos++;
					}
				}
				
				//assign
				case ':' ->
				{
					if(inp_eof[gpos+1] == '=')
					{
						Token t = new Token(":=", Kind.ASSIGN, token_pos, line, lexer_pos, 2, dummy);
						tokenset.add(t);
						lexer_pos = lexer_pos+2;
						gpos = gpos +2;
					}
					else
					{
						Token t = new Token(":=", Kind.ERROR, token_pos, line, lexer_pos, 1, dummy);
						tokenset.add(t);
						lexer_pos++;
						gpos++;
					}
				}
				
				//LESS THAN
				case '<' ->
				{
					state = State.HAVE_LSTHAN;
					lexer_pos++;
					gpos++;
				}
				
				//greater than
				case'>' ->
				{
					state = State.HAVE_GRTHAN;
					lexer_pos++;
					gpos++;
				}
				
				//identifier
				case 'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q',
				'r','s','t','u','v','w','x','y','z',
				'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q',
				'R','S','T','U','V','W','X','Y','Z',
				'_','$' ->
				{
					token_pos=lexer_pos;
					lexer_pos++;
					gpos++;
					state = State.HAVE_IDENT;
				}
				
				//0
				case '0'->
				{
					Token t = new Token("0", Kind.NUM_LIT, token_pos, line, gpos, 1, dummy);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
				}
				
				//numbers
				case '1','2','3','4','5','6','7','8','9' ->
				{
					token_pos=lexer_pos;
					lexer_pos++;
					gpos++;
					state = State.HAVE_NUM;
				}
				
				//string literal
				case '"' ->
				{
					token_pos=lexer_pos;

					lexer_pos++;
					gpos++;
					state = State.HAVE_STRING;
				}
				
				//whitespace
				case '\\' ->
				{
					lexer_pos++;
					gpos++;
					state = State.HAVE_WHITESPACE;
				}
				
				default ->
				{
					Token t = new Token("Character is Invalid!", Kind.ERROR, token_pos, line, gpos, 1, dummy);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
				}
				}
			}
			//START ends
			
			//case <
			//
			case HAVE_LSTHAN ->
			{
				switch(ch)
				{
				case '=' ->
				{
					Token t = new Token("<=", Kind.LE, lexer_pos-1, line, lexer_pos-1, 2, dummy);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
					state = State.START;
				}
				default ->
				{
					Token t = new Token("<", Kind.LT, lexer_pos-1, line, lexer_pos-1, 1, dummy);
					tokenset.add(t);
					state = State.START;
				}
				}
			}
			
			//case >
			//
			case HAVE_GRTHAN ->
			{
				switch(ch)
				{
				case '=' ->
				{
					Token t = new Token(">=", Kind.GE, lexer_pos-1, line, lexer_pos-1, 2, dummy);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
					state = State.START;
				}
				default ->
				{
					Token t = new Token(">", Kind.GT, lexer_pos-1, line, lexer_pos-1, 1, dummy);
					tokenset.add(t);
					state = State.START;
				}
				}
			} 
			
			
			//identifier
			//
			case HAVE_IDENT ->
			{

				ch=inp_eof[gpos];
				
				switch(ch)
				{
				
				//identifier continues
				case 'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q',
				'r','s','t','u','v','w','x','y','z',
				'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q',
				'R','S','T','U','V','W','X','Y','Z',
				'_','$','0','1','2','3','4','5','6','7','8','9' ->
				{
					lexer_pos++;
					gpos++;
				}
				
				//parsed identifier
				default ->
				{
					int ident_len = lexer_pos - token_pos;
					char[] ident = new char[ident_len];
					for(int i=0;i<ident_len;i++)
					{
						ident[i]=inp_eof[gpos-ident_len+i];
					}
					
					//ident stored in identifier
					String identifier = new String(ident);
					
					//is it reserved?
					if(reserved.containsKey(identifier))
					{
						Token t = new Token(identifier, reserved.get(identifier), token_pos, line, gpos-ident_len, ident_len, dummy);
						tokenset.add(t);	
					}
					
					//it is identifier
					else 
					{
						//System.out.println(identifier);
						//System.out.println(gpos);
						Token t = new Token(identifier, Kind.IDENT, token_pos, line, gpos-ident_len, ident_len, dummy);
						tokenset.add(t);
					}

					state = State.START;							
				}
				}
			}
			//identifier case over
			
			//number 
			case HAVE_NUM ->
			{
				
				ch = inp_eof[gpos];
				
				switch(ch)
				{
				
				case '0','1','2','3','4','5','6','7','8','9' ->
				{
					lexer_pos++;
					gpos++;
				}
				
				default ->
				{
					int num_len = lexer_pos - token_pos;
					char[] num = new char[num_len];
					for(int i=0;i<num_len;i++)
					{
						num[i]=inp_eof[gpos-num_len+i];
					}
					
					//number converted to string
					String snum = new String(num);
					System.out.println(snum);
					
					//catch number too big exceptions
					try 
					{
						int number = Integer.parseInt(snum);
						Token t = new Token(snum, Kind.NUM_LIT, token_pos, line, gpos-num_len, num_len, dummy);
						tokenset.add(t);
					}
					catch (NumberFormatException e)
					{
						Token t = new Token("Number too big!", Kind.ERROR, token_pos, line, gpos-num_len, num_len, dummy);
						tokenset.add(t);
					}
					
					state = State.START;
				}
				}
			}
			//number handled
			
			//string literal
			case HAVE_STRING ->
			{

				ch = inp_eof[gpos];
				
				switch(ch)
				{
				
				//escape sequence in string
				case '\\' ->
				{
					char nextchar = inp_eof[gpos+1];
					switch(nextchar)
					{
					
					//acceptable escape sequences
					case 'b','t','\\','n','f','r','"','\'' ->
					{
						gpos=gpos+2;
						lexer_pos=lexer_pos+2;
					}
					
					//error
					default ->
					{
						Token t = new Token("Illegal character!", Kind.ERROR, token_pos, line, gpos, 2, dummy);
						tokenset.add(t);
						gpos++;
						lexer_pos++;
						state = State.START;
					}
					}
				}
				
				//end string
				case '\"' ->
				{
					lexer_pos++;
					gpos++;
					int string_len = lexer_pos - token_pos;
					char[] strg = new char[string_len];
					for(int i=0;i<string_len;i++)
					{
						strg[i]=inp_eof[gpos-string_len+i];
					}
					
					//strg stored in stringval
					String stringval = new String(strg);
					Token t = new Token(stringval, Kind.STRING_LIT, token_pos, line, gpos-string_len, string_len, strg);
					tokenset.add(t);
					state = State.START;
					System.out.println("hello");
					System.out.println(strg);
				}
				
				//default: string continues
				default ->
				{
					lexer_pos++;
					gpos++;
				}
				}
			}
			
			
			
			}
			
		}
		
		

	}

	@Override
	public IToken next() throws LexicalException {
		
		Token tk =  tokenset.get(current_token);
		current_token++;
		if (tk.tkind == Kind.ERROR)
		{
			throw new LexicalException(tk.data, tk.line, tk.col);
		}
		return tk;
	}

	@Override
	public IToken peek() throws LexicalException {
		
		return tokenset.get(current_token);
	}

}
