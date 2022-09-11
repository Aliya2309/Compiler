package edu.ufl.cise.plpfa22;

import java.util.ArrayList;

import edu.ufl.cise.plpfa22.IToken.Kind;

public class Lexer implements ILexer {
	
	public static enum State{
		START, 
		
		HAVE_COLON,
		
		//accepting states
		HAVE_LSTHAN, //less than
		HAVE_GRTHAN,
		HAVE_ASSGN, //:=
		HAVE_IDENT
		
	}

	public Lexer(String input) {
		
		char[] chinp = input.toCharArray();
		char EOF = 0;
		int len = input.length();
		char[] inp_eof= new char[len+1];
		for (int i=0; i<len-1; i++)
		{
			inp_eof[i]=chinp[i];
		}
		inp_eof[len-1]=EOF;
		
		int lexer_pos=0;
		int token_pos=0;
		int line=0;
		int gpos=0;
		State state = State.START;
		
		ArrayList<Token> tokenset = new ArrayList<Token>();
		
		while (true)
		{
			char ch = inp_eof[lexer_pos];
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
					Token t = new Token(String.valueOf(ch), Kind.DOT, token_pos, line, lexer_pos, 1);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
				}
				
				case ',' ->
				{
					Token t = new Token(String.valueOf(ch), Kind.COMMA, token_pos, line, lexer_pos, 1);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
				}
				
				case ';' ->
				{
					Token t = new Token(String.valueOf(ch), Kind.SEMI, token_pos, line, lexer_pos, 1);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
				}
				
				case '(' ->
				{
					Token t = new Token(String.valueOf(ch), Kind.LPAREN, token_pos, line, lexer_pos, 1);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
				}
				
				case ')' ->
				{
					Token t = new Token(String.valueOf(ch), Kind.RPAREN, token_pos, line, lexer_pos, 1);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
				}
				
				case '+' ->
				{
					Token t = new Token(String.valueOf(ch), Kind.PLUS, token_pos, line, lexer_pos, 1);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
				}
				
				case '-' ->
				{
					Token t = new Token(String.valueOf(ch), Kind.MINUS, token_pos, line, lexer_pos, 1);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
				}
				
				case '*' ->
				{
					Token t = new Token(String.valueOf(ch), Kind.TIMES, token_pos, line, lexer_pos, 1);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
				}
				
				case '%' ->
				{
					Token t = new Token(String.valueOf(ch), Kind.MOD, token_pos, line, lexer_pos, 1);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
				}
				
				case '?' ->
				{
					Token t = new Token(String.valueOf(ch), Kind.QUESTION, token_pos, line, lexer_pos, 1);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
				}
				
				case '!' ->
				{
					Token t = new Token(String.valueOf(ch), Kind.BANG, token_pos, line, lexer_pos, 1);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
				}
				
				case '=' ->
				{
					Token t = new Token(String.valueOf(ch), Kind.EQ, token_pos, line, lexer_pos, 1);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
				}
				
				case '#' ->
				{
					Token t = new Token(String.valueOf(ch), Kind.NEQ, token_pos, line, lexer_pos, 1);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
				}
				
				//Division or comment
				
				case '/' ->
				{
					char next = inp_eof[token_pos+1];
					
					//comment present
					if (next=='/')
					{
						gpos=gpos+2;
						next = inp_eof[gpos];
						char next2 = inp_eof[gpos+1];
						
						//while next characters aren't \n, \r or eof
						while((next!='\\' && (next2!='n' || next2!='r')) || next!=EOF)
						{
							gpos++;
							next = inp_eof[gpos];
							next2 = inp_eof[gpos+1];
							
						}
						
						//eof encountered
						if (next==EOF)
						{
							Token t = new Token("EOF", Kind.EOF, token_pos, line, gpos, 1);
							tokenset.add(t);
							
						}
						
						//line ended; reset position to 0 and go to next line
						else
						{
							if(next2=='r')
							{
								line++;
								gpos=gpos+3;
								lexer_pos=0;
							}
							if(next2=='n')
							{
								line++;
								gpos++;
								lexer_pos=0;
							}
						}
					}
					//comments handled
					
					//just division symbol
					else
					{
						Token t = new Token(String.valueOf(ch), Kind.DIV, token_pos, line, gpos, 1);
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
						Token t = new Token(":=", Kind.ASSIGN, token_pos, line, lexer_pos, 2);
						tokenset.add(t);
						lexer_pos = lexer_pos+2;
						gpos = gpos +2;
					}
					else
					{
						Token t = new Token(":=", Kind.ERROR, token_pos, line, lexer_pos, 1);
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
					state = State.HAVE_IDENT;
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
					Token t = new Token("<=", Kind.LE, lexer_pos-1, line, lexer_pos-1, 2);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
					state = State.START;
				}
				default ->
				{
					Token t = new Token("<", Kind.LT, lexer_pos-1, line, lexer_pos-1, 1);
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
					Token t = new Token(">=", Kind.GE, lexer_pos-1, line, lexer_pos-1, 2);
					tokenset.add(t);
					lexer_pos++;
					gpos++;
					state = State.START;
				}
				default ->
				{
					Token t = new Token(">", Kind.GT, lexer_pos-1, line, lexer_pos-1, 1);
					tokenset.add(t);
					state = State.START;
				}
				}
			} 
			
			
			//identifier
			//
			case HAVE_IDENT ->
			{
				token_pos = lexer_pos;
				lexer_pos++;
				gpos++;
				
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
					int ident_len = lexer_pos - token_pos + 1;
					char[] ident = new char[ident_len];
					for(int i=0;i<ident_len;i++)
					{
						ident[i]=inp_eof[gpos-lexer_pos+i];
					}
					String identifier = new String(ident);
					Token t = new Token(identifier, Kind.STRING_LIT, token_pos, line, gpos, ident_len);
					tokenset.add(t);
					gpos++;
					lexer_pos++;
					state = State.START;
					
					
				}
				}
			}
			}
			
		}
		
		
		/*while(true) {
			
			//char ch = chinp[pos];
			//switch(state)
			
		}*/
	}

	@Override
	public IToken next() throws LexicalException {
		// TODO Auto-generated method stub
		
		
		return null;
	}

	@Override
	public IToken peek() throws LexicalException {
		// TODO Auto-generated method stub
		return null;
	}

}
