package edu.ufl.cise.plpfa22;

import java.util.ArrayList;
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
import edu.ufl.cise.plpfa22.ast.Program;
import edu.ufl.cise.plpfa22.ast.Statement;
import edu.ufl.cise.plpfa22.ast.StatementAssign;
import edu.ufl.cise.plpfa22.ast.StatementBlock;
import edu.ufl.cise.plpfa22.ast.StatementCall;
import edu.ufl.cise.plpfa22.ast.StatementEmpty;
import edu.ufl.cise.plpfa22.ast.StatementIf;
import edu.ufl.cise.plpfa22.ast.StatementInput;
import edu.ufl.cise.plpfa22.ast.StatementOutput;
import edu.ufl.cise.plpfa22.ast.StatementWhile;
import edu.ufl.cise.plpfa22.ast.VarDec;

public class Parser implements IParser {
	
	ILexer lexer;
	IToken token;

	public Parser(ILexer lexer) throws LexicalException {
		// TODO Auto-generated constructor stub
		this.lexer = lexer;
		token = lexer.next();	
	}
	
	IToken consume() throws PLPException {
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
	
	public Program program() throws PLPException
	{
		IToken first = token;

		Block b = block();
		Program p = new Program(first, b);
		match(Kind.DOT);
		return p;
	}
	
	public Block block() throws PLPException 
	{
		//(CONST <ident> = <const_val> ( ,  <ident> = <const_val> )*  ; )* 
		Block b;
		List<ConstDec> constDecs = new ArrayList<ConstDec>();
		List<VarDec> varDecs = new ArrayList<VarDec>();
		List<ProcDec> procedureDecs = new ArrayList<ProcDec>();
		ConstDec constd;
		VarDec vard;
		ProcDec procd;
		IToken firstofblock = token;
		IToken first;
		IToken i;

			while((token.getKind() == Kind.KW_CONST))
			{
				//(CONST <ident> = <const_val>
				first = token;
				match(Kind.KW_CONST);
				i = token;
				match(Kind.IDENT);
				match(Kind.EQ);
				Expression constval = constval();
				switch(constval.firstToken.getKind())
				{
					case NUM_LIT ->
					{
						 Integer val = (constval.firstToken.getIntValue());
						 constd = new ConstDec(first, i, val);
					}
					case STRING_LIT ->
					{
						String val = constval.firstToken.getStringValue();
						constd = new ConstDec(first, i, val);
					}
					case BOOLEAN_LIT ->
					{
						Boolean val = constval.firstToken.getBooleanValue();
						constd = new ConstDec(first, i, val);
					}
					default ->
					{
						throw new SyntaxException("Unexpected constval"); 
					}
				}
				constDecs.add(constd);
				
				
				//( ,  <ident> = <const_val> )*
				while(token.getKind() == Kind.COMMA)
				{
					match(Kind.COMMA);
					first = token;
					i = token;
					match(Kind.IDENT);
					match(Kind.EQ);
					constval = constval();	
					switch(constval.firstToken.getKind())
					{
						case NUM_LIT ->
						{
							 Integer val = (constval.firstToken.getIntValue());
							 constd = new ConstDec(first, i, val);
						}
						case STRING_LIT ->
						{
							String val = constval.firstToken.getStringValue();
							constd = new ConstDec(first, i, val);
						}
						case BOOLEAN_LIT ->
						{
							Boolean val = constval.firstToken.getBooleanValue();
							constd = new ConstDec(first, i, val);
						}
						default ->
						{
							throw new SyntaxException("Unexpected constval"); 
						}
					}
					constDecs.add(constd);
				}
				
				//; )*
				match(Kind.SEMI);	
			}
		
		
		//(VAR   <ident> ( , <ident> )* ) ; )*
			while(token.getKind() == Kind.KW_VAR)
			{
				//(VAR   <ident>
				first = token;
				match(Kind.KW_VAR);
				i = token;
				match(Kind.IDENT);
				vard = new VarDec(first, i);
				varDecs.add(vard);
				
				//( , <ident> )*
				while(token.getKind() == Kind.COMMA)
				{
					match(Kind.COMMA);
					first = token;
					i = token;
					match(Kind.IDENT);	
					vard = new VarDec(first, i);
					varDecs.add(vard);
				}
				
				//; )*
				match(Kind.SEMI);
				
			}
		
		
		//(PROCEDURE <ident> ; <block> ;  )*

			while(token.getKind() == Kind.KW_PROCEDURE)
			{
				first = token;
				match(Kind.KW_PROCEDURE);
				i = token;
				match(Kind.IDENT);
				match(Kind.SEMI);
				Block bk = block();
				match(Kind.SEMI);
				procd = new ProcDec(first, i, bk);
				procedureDecs.add(procd);
				
			}
		
		
		//statement
		//predict set of statement is: ident, call, ?, !, begin, if, while, ., ;

			Statement s = statement();
			b = new Block(firstofblock, constDecs, varDecs, procedureDecs, s);
			
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
			return stat;
			
		}
		
		if(token.getKind() == Kind.KW_CALL)
		{
			match(Kind.KW_CALL);
			Ident i = new Ident(token);
			match(Kind.IDENT);
			stat = new StatementCall(t, i);
			return stat;

		}
		
		if(token.getKind() == Kind.QUESTION)
		{
			match(Kind.QUESTION);
			Ident i = new Ident(token);
			match(Kind.IDENT);
			stat = new StatementInput(t, i);
			return stat;
		}
		
		if(token.getKind() == Kind.BANG)
		{
			match(Kind.BANG);
			Expression e = expression();
			stat = new StatementOutput(t, e);
			return stat;
		}
		
		if(token.getKind() == Kind.KW_BEGIN)
		{
			match(Kind.KW_BEGIN);
			List<Statement> stats = new ArrayList<Statement>();
			stat = statement();
			stats.add(stat);
			while(token.getKind() == Kind.SEMI)
			{
				match(Kind.SEMI);
				stat = statement();
				stats.add(stat);
			}
			match(Kind.KW_END);
			stat = new StatementBlock(t, stats);
			return stat;
		}
		
		if(token.getKind() == Kind.KW_IF)
		{
			match(Kind.KW_IF);
			Expression e = expression();
			match(Kind.KW_THEN);
			stat = statement();
			stat = new StatementIf(t, e, stat);
			return stat;
			
		}
		
		if(token.getKind() == Kind.KW_WHILE)
		{
			match(Kind.KW_WHILE);
			Expression e = expression();
			match(Kind.KW_DO);
			stat = statement();
			stat = new StatementWhile(t, e, stat);
			return stat;
			
		}
		
		else
		{
			stat = new StatementEmpty(t);
			return stat;
		}
		
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
		Program p = program();
		try 
		{
			match(Kind.EOF);
		}
		catch (Exception e)
		{
			throw new SyntaxException("eof not found");
		}
		return p;
	}
	
	

}
