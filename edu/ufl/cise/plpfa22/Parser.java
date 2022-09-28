package edu.ufl.cise.plpfa22;

import java.util.List;

import edu.ufl.cise.plpfa22.IToken;
import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.ast.ASTNode;
import edu.ufl.cise.plpfa22.ast.Block;
import edu.ufl.cise.plpfa22.ast.ConstDec;
import edu.ufl.cise.plpfa22.ast.Expression;
import edu.ufl.cise.plpfa22.ast.ExpressionBinary;
import edu.ufl.cise.plpfa22.ast.ExpressionBooleanLit;
import edu.ufl.cise.plpfa22.ast.ExpressionIdent;
import edu.ufl.cise.plpfa22.ast.ExpressionNumLit;
import edu.ufl.cise.plpfa22.ast.ExpressionStringLit;
import edu.ufl.cise.plpfa22.ast.Ident;
import edu.ufl.cise.plpfa22.ast.ProcDec;
import edu.ufl.cise.plpfa22.ast.Statement;
import edu.ufl.cise.plpfa22.ast.StatementAssign;
import edu.ufl.cise.plpfa22.ast.VarDec;

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
	
	public Block block() throws PLPException 
	{
		//(CONST <ident> = <const_val> ( ,  <ident> = <const_val> )*  ; )* 
		Block b;
		List<ConstDec> constDecs;
		List<VarDec> varDecs;
		List<ProcDec> procedureDecs;
		//ConstDec const;
		VarDec var;
		ProcDec proc;

			while((token.getKind() == Kind.KW_CONST))
			{
				//(CONST <ident> = <const_val>
				IToken first = token;
				match(Kind.KW_CONST);
				IToken i = token;
				match(Kind.IDENT);
				match(Kind.EQ);
				IToken constval = constval();
				ConstDec constd = new ConstDec(first, i, constval);
				
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
		
		
		//(VAR   <ident> ( , <ident> )* ) ; )*
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
		
		
		//(PROCEDURE <ident> ; <block> ;  )*

			while(token.getKind() == Kind.KW_PROCEDURE)
			{
				match(Kind.KW_PROCEDURE);
				match(Kind.IDENT);
				match(Kind.SEMI);
				block();
				match(Kind.SEMI);
			}
		
		
		//statement
		//predict set of statement is: ident, call, ?, !, begin, if, while, ., ;

			statement();
			
			return b;
		
	}
	
	public Statement statement() throws PLPException
	{
		IToken t = token;
		Statement stat;
		if(token.getKind() == Kind.IDENT)
		{
			Ident i = new Ident(t);
			match(Kind.IDENT);
			match(Kind.ASSIGN);
			Expression e = expression();
			stat = new StatementAssign(t, i, e);
			
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
		return stat;
	}
	
	public Expression expression() throws PLPException
	{
		Expression left, right;
		IToken op, t = token;
		left = additive_expression();
		while(token.getKind() == Kind.LT || token.getKind() == Kind.LE || token.getKind() == Kind.GT || token.getKind() == Kind.GE || token.getKind() == Kind.EQ || token.getKind() == Kind.NEQ)
		{
			op = token;
			consume();
			right = additive_expression();
			left = new ExpressionBinary(t, left, op, right);
		}
		return left;
	}
	
	public Expression additive_expression() throws PLPException
	{
		Expression left, right;
		IToken op, t = token;
		left = multiplicative_expression();
		while(token.getKind() == Kind.PLUS || token.getKind() == Kind.MINUS)
		{
			op = token;
			consume();
			right = multiplicative_expression();
			left = new ExpressionBinary(t, left, op, right);
		}
		return left;
	}
	
	public Expression multiplicative_expression() throws PLPException
	{
		Expression left, right;
		IToken op, t = token;
		left = primary_expression();
		while(token.getKind() == Kind.TIMES || token.getKind() == Kind.DIV|| token.getKind() == Kind.MOD)
		{
			op = token;
			consume();
			right = primary_expression();
			left = new ExpressionBinary(t, left, op, right);
		}
		return left;
	}
	
	public Expression primary_expression() throws PLPException
	{
		if(token.getKind() == Kind.IDENT)
		{
			ExpressionIdent e = new ExpressionIdent(token);
			match(Kind.IDENT);
			return e;
		}
		if(token.getKind() == Kind.LPAREN)
		{
			match(Kind.LPAREN);
			Expression e = expression();
			match(Kind.RPAREN);
			return e;
		}
		if(token.getKind() == Kind.NUM_LIT || token.getKind() == Kind.STRING_LIT || token.getKind() == Kind.BOOLEAN_LIT)
		{
			Expression e = constval();
			return e;
		}
		else 
		{
			throw new SyntaxException("Unexpected token when evaluating Primary_Expression");
		}
	}
	
	public Expression constval() throws PLPException
	{
		IToken t = token;
		if(token.getKind() == Kind.NUM_LIT)
		{
			ExpressionNumLit e = new ExpressionNumLit(t);
			consume();
			return e;
			
		}
		if(token.getKind() == Kind.STRING_LIT)
		{
			ExpressionStringLit e = new ExpressionStringLit(t);
			consume();
			return e;
		}
		if(token.getKind() == Kind.BOOLEAN_LIT)
		{
			ExpressionBooleanLit e = new ExpressionBooleanLit(t);
			consume();
			return e;
		}
		else 
		{
			throw new SyntaxException("Unexpected token when evaluating Const_Val");
		}
		
	}

	@Override
	public ASTNode parse() throws PLPException {
		// TODO Auto-generated method stub
		return null;
	}

}
