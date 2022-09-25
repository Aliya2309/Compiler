package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.IToken;
import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.ast.ASTNode;

public class Parser implements IParser {
	
	ILexer lexer;
	IToken token;

	public Parser(ILexer lexer) throws LexicalException {
		// TODO Auto-generated constructor stub
		this.lexer = lexer;
		token = lexer.next();	
	}
	
	IToken consume() throws LexicalException {
		token = lexer.next();
		return token;
	}
	
	IToken match(Kind kind) throws PLPException
	{
		if(token.getKind() == kind)
		{
			return consume();
		}
		else
		{
			throw new SyntaxException("Unexpected token");
		}
	}
	
	public void program() throws PLPException
	{
		block();
	}
	
	public void block() throws PLPException 
	{
		//(CONST <ident> = <const_val> ( ,  <ident> = <const_val> )*  ; )* 
		if (token.getKind() == Kind.KW_CONST)
		{
			while((token.getKind() == Kind.KW_CONST))
			{
				//(CONST <ident> = <const_val>
				match(Kind.KW_CONST);
				match(Kind.IDENT);
				match(Kind.EQ);
				constval();
				
				//( ,  <ident> = <const_val> )*
				while(token.getKind() == Kind.COMMA)
				{
					match(Kind.COMMA);
					match(Kind.IDENT);
					match(Kind.EQ);
					constval();	
				}
				
				//; )*
				match(Kind.SEMI);	
			}
		}
		
		//(VAR   <ident> ( , <ident> )* ) ; )*
		if(token.getKind() == Kind.KW_VAR)
		{
			while(token.getKind() == Kind.KW_VAR)
			{
				//(VAR   <ident>
				match(Kind.KW_VAR);
				match(Kind.IDENT);
				
				//( , <ident> )*
				while(token.getKind() == Kind.COMMA)
				{
					match(Kind.COMMA);
					match(Kind.IDENT);	
				}
				
				//; )*
				match(Kind.SEMI);
				
			}
		}
		
		//(PROCEDURE <ident> ; <block> ;  )*
		if (token.getKind() == Kind.KW_PROCEDURE)
		{
			while(token.getKind() == Kind.KW_PROCEDURE)
			{
				match(Kind.KW_PROCEDURE);
				match(Kind.IDENT);
				match(Kind.SEMI);
				block();
				match(Kind.SEMI);
			}
		}
		
		//statement
		//predict set of statement is: ident, call, ?, !, begin, if, while, ., ;
		if(token.getKind() == Kind.IDENT || token.getKind() == Kind.KW_CALL || token.getKind() == Kind.QUESTION || token.getKind() == Kind.BANG || token.getKind() == Kind.KW_BEGIN || token.getKind() == Kind.KW_IF || token.getKind() == Kind.KW_WHILE || token.getKind() == Kind.DOT || token.getKind() == Kind.SEMI)
		{
			statement();
		}
	}
	
	public void statement() throws PLPException
	{
		if(token.getKind() == Kind.IDENT)
		{
			match(Kind.IDENT);
			match(Kind.ASSIGN);
			expression();
		}
		
		if(token.getKind() == Kind.KW_CALL)
		{
			match(Kind.KW_CALL);
			match(Kind.IDENT);

		}
		
		if(token.getKind() == Kind.QUESTION)
		{
			match(Kind.QUESTION);
			match(Kind.IDENT);
		}
		
		if(token.getKind() == Kind.BANG)
		{
			match(Kind.BANG);
			expression();
		}
		
		if(token.getKind() == Kind.KW_BEGIN)
		{
			statement();
			while(token.getKind() == Kind.SEMI)
			{
				match(Kind.SEMI);
				statement();
			}
			match(Kind.KW_END);
		}
		
		if(token.getKind() == Kind.KW_IF)
		{
			match(Kind.KW_IF);
			expression();
			match(Kind.KW_THEN);
			statement();
			
		}
		
		if(token.getKind() == Kind.KW_WHILE)
		{
			match(Kind.KW_WHILE);
			expression();
			match(Kind.KW_DO);
			statement();
			
		}
		
		else
		{
			consume();
		}
	}
	
	public void expression() throws PLPException
	{
		additive_expression();
		while(token.getKind() == Kind.LT || token.getKind() == Kind.LE || token.getKind() == Kind.GT || token.getKind() == Kind.GE || token.getKind() == Kind.EQ || token.getKind() == Kind.NEQ)
		{
			consume();
			additive_expression();
		}
	}
	
	public void additive_expression() throws PLPException
	{
		multiplicative_expression();
		while(token.getKind() == Kind.PLUS || token.getKind() == Kind.MINUS)
		{
			consume();
			multiplicative_expression();
		}
	}
	
	public void multiplicative_expression() throws PLPException
	{
		primary_expression();
		while(token.getKind() == Kind.TIMES || token.getKind() == Kind.DIV|| token.getKind() == Kind.MOD)
		{
			consume();
			primary_expression();
		}
	}
	
	public void primary_expression() throws PLPException
	{
		if(token.getKind() == Kind.IDENT)
		{
			match(Kind.IDENT);
		}
		if(token.getKind() == Kind.LPAREN)
		{
			match(Kind.LPAREN);
			expression();
			match(Kind.RPAREN);
		}
		if(token.getKind() == Kind.NUM_LIT || token.getKind() == Kind.STRING_LIT || token.getKind() == Kind.BOOLEAN_LIT)
		{
			constval();
		}
	}
	
	public void constval() throws PLPException
	{
		consume();
	}

	@Override
	public ASTNode parse() throws PLPException {
		// TODO Auto-generated method stub
		return null;
	}

}
