package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.ast.ASTVisitor;
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
import edu.ufl.cise.plpfa22.ast.Types;
import edu.ufl.cise.plpfa22.ast.Types.Type;
import edu.ufl.cise.plpfa22.ast.VarDec;

public class TypeCheckVisitor implements ASTVisitor {
	
	boolean change = false;
	boolean fullytyped = true;
	//boolean firstpass = true;

	public TypeCheckVisitor() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		
		for(ConstDec c : block.constDecs)
		{
			
			c.visit(this, arg);
		}
		
//		for(VarDec d : block.varDecs)
//		{
//			d.visit(this, arg);
//		}
		
		for(ProcDec p : block.procedureDecs)
		{
			p.visit(this, 1);
		}
		
		block.statement.visit(this, arg);
		
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		
		program.block.visit(this, 1);
//		firstpass=false;
		while(change)
		{
			//System.out.println("Next Pass!\n\n");
			fullytyped = true;
			change = false;
			program.block.visit(this, 1);
		}
		
		if(fullytyped == false)
		{
			throw new TypeCheckException("program is not fully typed");
		}
		return null;
	}


	@Override
	public Object visitVarDec(VarDec varDec, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		//
		statementAssign.expression.visit(this, arg);
		Type t = statementAssign.expression.getType();
		statementAssign.ident.visit(this, arg);
		Type id = statementAssign.ident.getDec().getType();
		
		if(statementAssign.ident.getDec() instanceof ConstDec )
		{
			throw new TypeCheckException("cannot reassign constant");
		}
		if(statementAssign.ident.getDec() instanceof ProcDec)
		{
			throw new TypeCheckException("cannot reassign procedure");
		}
		if(id == null && t!=null)
		{
			//System.out.println("in cond 1");
			//System.out.println(t);
			//System.out.println(id);
			
			statementAssign.ident.getDec().setType(t);
			//System.out.println(statementAssign.ident.getDec().getType());
			change = true;
		}
		else if (t==null && id!=null)
		{
			//System.out.println("in cond 2");
			//System.out.println(t);
			//System.out.println(id);
			statementAssign.expression.setType(id);
			change = true;
		}

		else if (t == id)
		{
			//System.out.println("in cond 3");
			//System.out.println(change);
			//System.out.println(t);
			//System.out.println(id);
			//change = false;
			return null;
		}
		else
		{
			throw new TypeCheckException("Type mismatch in statement assign");
		}
		
		return null;
	}

	@Override
	public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		if(statementCall.ident.getDec().getType() == Type.PROCEDURE)
		{
			return null;
		}
		else if(statementCall.ident.getDec().getType() == null)
		{
			//System.out.println("in statement call making fullytyped false");
			
			fullytyped = false;
			return null;
		}
		else
		{
			throw new TypeCheckException("Call not made to procedure.");
		}
		
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		
		if(statementInput.ident.getDec() instanceof ConstDec)
		{
			throw new TypeCheckException("Cannot take input on constant");
		}
		if(statementInput.ident.getDec() instanceof ProcDec)
		{
			throw new TypeCheckException("Cannot take input on procedure");
		}
		else if(statementInput.ident.getDec().getType() == Type.NUMBER || 
			statementInput.ident.getDec().getType() == Type.STRING ||
			statementInput.ident.getDec().getType() == Type.BOOLEAN)
		{
			return null;
		}
		else if(statementInput.ident.getDec().getType() == null)
		{
			//System.out.println("in statement input making fullytyped false");
			fullytyped = false;
			return null;
		}
		else
		{
			throw new TypeCheckException("Input variable not valid");
		}
	}

	@Override
	public Object visitStatementOutput(StatementOutput statementOutput, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		
		statementOutput.expression.visit(this, arg);
		Type t = statementOutput.expression.getType();
		//System.out.println(statementOutput.expression);
		if(t == Type.NUMBER ||t == Type.BOOLEAN ||t == Type.STRING)
		{
			
			return null;
		}
		else
		{
			//System.out.println("in statement output making fullytyped false");
			fullytyped = false;
		}
		return null;
	}

	@Override
	public Object visitStatementBlock(StatementBlock statementBlock, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		
		for(Statement s : statementBlock.statements)
		{
			s.visit(this, arg);
		}
		return null;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		
		statementIf.expression.visit(this, arg);
		statementIf.statement.visit(this, arg);
		if(statementIf.expression.getType() == Type.BOOLEAN)
		{
			return null;
		}
		else if (statementIf.expression.getType() == null)
		{
			//System.out.println("in statement if making fullytyped false");
			fullytyped = false;
		}
		else
		{
			throw new TypeCheckException("Illegal guard expression");
		}
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		//System.out.println("in while");
		statementWhile.expression.visit(this, arg);
		statementWhile.statement.visit(this, arg);
		
		if(statementWhile.expression.getType() == Type.BOOLEAN)
		{
			return null;
		}
		else if (statementWhile.expression.getType() == null)
		{
			//System.out.println("in statement while making fullytyped false");
			fullytyped = false;
		}
		else
		{
			throw new TypeCheckException("Illegal guard expression");
		}
		return null;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		expressionBinary.e0.visit(this, arg);
		Type t1 = expressionBinary.e0.getType();
		expressionBinary.e1.visit(this, arg);
		Type t2 = expressionBinary.e1.getType();
		IToken operator = expressionBinary.op;
		
		if(t1 != null && t2 !=null && expressionBinary.getType()!=null)
		{
			//fullytyped = true;
			//change = false;
			return null;
		}
		
		change = true;
		
		if(t1==null && t2==null && expressionBinary.getType() == null)
		{
			//System.out.println("in exp binary making fullytyped false");
			//System.out.println(change);
			//System.out.println(expressionBinary.e0);
			//System.out.println(expressionBinary.e1);
			fullytyped = false;
			return null;
		}
		
		
		
		//PLUS
		//α x α  → α, where α ∈ {NUMBER, STRING, BOOLEAN}
		if(operator.getKind() == Kind.PLUS)
		{
			
			//if the expression already has a type from previous calculations
			//for example (a+b) = 2 in which (a+b) got the type NUMBER
			if(expressionBinary.getType() == Type.BOOLEAN|| expressionBinary.getType() == Type.NUMBER || 
					expressionBinary.getType() == Type.STRING)
			{
				expressionBinary.e0.setType(expressionBinary.getType());
				expressionBinary.e1.setType(expressionBinary.getType());
				checkIfExpressionIdentB(expressionBinary.e0, expressionBinary.e1, expressionBinary.getType());
				
			}
			
			
			//cases where type of expressions is determined from children
			else if(t1 == null && t2 == Type.NUMBER ||
				t1 == null && t2 == Type.STRING||
				t1 == null && t2 == Type.BOOLEAN)
			{
				expressionBinary.e0.setType(t2);
				checkIfExpressionIdent(expressionBinary.e0, t2);
				expressionBinary.setType(t2);
			}
			else if (t1 == Type.NUMBER && t2 == null ||
					t1 == Type.STRING && t2 == null||
					t1 == Type.BOOLEAN && t2 == null)
			{
				expressionBinary.e1.setType(t1);
				checkIfExpressionIdent(expressionBinary.e1, t1);
				expressionBinary.setType(t1);
			}
			else if(t1 == Type.NUMBER && t2 == Type.NUMBER ||
				t1 == Type.STRING && t2 == Type.STRING||
				t1 == Type.BOOLEAN && t2 == Type.BOOLEAN)
			{
				expressionBinary.setType(t1);
			}
			else
			{
				throw new TypeCheckException("Invalid type"); 
			}
		}
		
		//MINUS, DIV, MOD
		//NUMBER x NUMBER → NUMBER
		else if(operator.getKind() == Kind.MINUS||operator.getKind() == Kind.DIV||operator.getKind() == Kind.MOD)
		{
			
			if(expressionBinary.getType() == Type.NUMBER)
			{
				expressionBinary.e0.setType(expressionBinary.getType());
				expressionBinary.e1.setType(expressionBinary.getType());
				checkIfExpressionIdentB(expressionBinary.e0, expressionBinary.e1, expressionBinary.getType());
				
				
			}
			
			else if(t1 == null && t2 == Type.NUMBER)
			{
				expressionBinary.e0.setType(t2);
				//System.out.println("\n in expression binary line 332");
				//System.out.println( expressionBinary.e0);
				checkIfExpressionIdent(expressionBinary.e0, t2);
				expressionBinary.setType(t2);
			}
			else if (t1 == Type.NUMBER || t2 == null )
			{
				expressionBinary.e1.setType(t1);
				checkIfExpressionIdent(expressionBinary.e1, t1);
				expressionBinary.setType(t1);
			}
			else if(t1 == Type.NUMBER && t2 == Type.NUMBER)
			{
				expressionBinary.setType(t1);
			}
			else
			{
				throw new TypeCheckException("Invalid type"); 
			}
		}
		
		
		//TIMES
		//α x α  → α, where α ∈ {NUMBER, BOOLEAN}
		else if (operator.getKind() == Kind.TIMES)
		{
			if(expressionBinary.getType() == Type.BOOLEAN|| expressionBinary.getType() == Type.NUMBER)
			{
				expressionBinary.e0.setType(expressionBinary.getType());
				expressionBinary.e1.setType(expressionBinary.getType());
				checkIfExpressionIdentB(expressionBinary.e0, expressionBinary.e1, expressionBinary.getType());
			}
			
			
			if(t1 == null && t2 == Type.NUMBER ||
				t1 == null && t2 == Type.BOOLEAN)
			{
				expressionBinary.setType(t2);
				expressionBinary.e0.setType(t2);
				checkIfExpressionIdent(expressionBinary.e0, t2);
					
			}
			else if(t1 == Type.NUMBER && t2 == null ||
				t1 == Type.BOOLEAN && t2 == null)
			{
				expressionBinary.setType(t1);
				expressionBinary.e1.setType(t1);
				checkIfExpressionIdent(expressionBinary.e1, t1);
			}
			else if(t1 == Type.NUMBER && t2 == Type.NUMBER ||
				t1 == Type.BOOLEAN && t2 == Type.BOOLEAN)
			{
				expressionBinary.setType(t1);
			}
			else
			{
				throw new TypeCheckException("Invalid type"); 
			}
		}

		
		//EQ, NEQ, LT, LE, GT, GE
		//α x α  → BOOLEAN, where α ∈ {NUMBER, STRING, BOOLEAN}
		else if (operator.getKind() == Kind.EQ ||operator.getKind() == Kind.NEQ|| operator.getKind() == Kind.LT || 
				operator.getKind() == Kind.LE || operator.getKind() == Kind.GT|| operator.getKind() == Kind.GE)
		{
			
			//not sure if this situation would occur
//			if(expressionBinary.getType() == Type.BOOLEAN)
//			{
//				//expressionBinary.e0.setType(expressionBinary.getType());
//				//expressionBinary.e1.setType(expressionBinary.getType());
//				
//			}
			
			if(t1 == null && t2 == Type.NUMBER ||
				t1 == null && t2 == Type.STRING||
				t1 == null && t2 == Type.BOOLEAN)
			{
				expressionBinary.e0.setType(t2);
				checkIfExpressionIdent(expressionBinary.e0, t2);
				expressionBinary.setType(Type.BOOLEAN);
			}
			
			else if(t1 == Type.NUMBER && t2 == null ||
					t1 == Type.STRING && t2 == null||
					t1 == Type.BOOLEAN && t2 == null)
			{
				expressionBinary.e1.setType(t1);
				checkIfExpressionIdent(expressionBinary.e1, t1);
				expressionBinary.setType(Type.BOOLEAN);
			}
			
			else if(t1 == Type.NUMBER && t2 == Type.NUMBER ||
					t1 == Type.STRING && t2 == Type.STRING||
					t1 == Type.BOOLEAN && t2 == Type.BOOLEAN)
			{
				expressionBinary.setType(Type.BOOLEAN);
			}
			
			else
			{
				throw new TypeCheckException("Invalid type"); 
			}
		}
		
		else
		{
			throw new TypeCheckException("Invalid operator");
		}
		
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		Type ident = expressionIdent.getDec().getType();
		Type exp = expressionIdent.getType();
		if(ident != null && exp ==null)
		{
			expressionIdent.setType(ident);
		}
		else if(ident == null && exp !=null)
		{
			expressionIdent.getDec().setType(exp);
		}
		else if (ident == exp)
		{
			return null;
		}
		else
		{
			fullytyped = false;
		}
		
		return null;
	}

	@Override
	public Object visitExpressionNumLit(ExpressionNumLit expressionNumLit, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		expressionNumLit.setType(Type.NUMBER);
		return null;
	}

	@Override
	public Object visitExpressionStringLit(ExpressionStringLit expressionStringLit, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		expressionStringLit.setType(Type.STRING);
		return null;
	}

	@Override
	public Object visitExpressionBooleanLit(ExpressionBooleanLit expressionBooleanLit, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		expressionBooleanLit.setType(Type.BOOLEAN);
		return null;
	}

	@Override
	public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		if(procDec.getType() == null)
		{
			procDec.setType(Type.PROCEDURE);
			change  = true;
			procDec.block.visit(this, 1);
			
		}
		else
		{
			procDec.setType(Type.PROCEDURE);
			procDec.block.visit(this, 1);
		}
		
		return null;
	}

	@Override
	public Object visitConstDec(ConstDec constDec, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		
		if (constDec.val instanceof Integer)
		{
			constDec.setType(Type.NUMBER);
		}
		else if (constDec.val instanceof String)
		{
			constDec.setType(Type.STRING);
		}
		else if (constDec.val instanceof Boolean)
		{
			constDec.setType(Type.BOOLEAN);
		}
		else
		{
			throw new TypeCheckException("Invalid type for constant");
		}
		return null;
	}

	@Override
	public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitIdent(Ident ident, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		
		//ident.getDec().setType();
		return null;
	}
	
	public void checkIfExpressionIdentB(Expression e0, Expression e1, Type t)
	{
		if(e0 instanceof ExpressionIdent)
		{
			((ExpressionIdent) e0).getDec().setType(t);
		}
		if (e1 instanceof ExpressionIdent)
		{
			((ExpressionIdent) e1).getDec().setType(t);
		}
	}
	public void checkIfExpressionIdent(Expression e0, Type t)
	{
		if(e0 instanceof ExpressionIdent)
		{
			((ExpressionIdent) e0).getDec().setType(t);
		}
	}



}
