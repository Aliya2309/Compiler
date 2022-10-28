package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.ast.ASTVisitor;
import edu.ufl.cise.plpfa22.ast.Block;
import edu.ufl.cise.plpfa22.ast.ConstDec;
import edu.ufl.cise.plpfa22.ast.ExpressionBinary;
import edu.ufl.cise.plpfa22.ast.ExpressionBooleanLit;
import edu.ufl.cise.plpfa22.ast.ExpressionIdent;
import edu.ufl.cise.plpfa22.ast.ExpressionNumLit;
import edu.ufl.cise.plpfa22.ast.ExpressionStringLit;
import edu.ufl.cise.plpfa22.ast.Ident;
import edu.ufl.cise.plpfa22.ast.ProcDec;
import edu.ufl.cise.plpfa22.ast.Program;
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
	boolean firstpass = true;

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
		if(statementAssign.ident.getDec().getType() == null)
		{
			statementAssign.ident.getDec().setType(t);
		}
		else
		{
			Type id = statementAssign.ident.getDec().getType();
			if (t == id)
			{
				return null;
			}
			else
			{
				throw new TypeCheckException("Type mismatch in statement assign");
			}
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
		else
		{
			throw new TypeCheckException("Call not made to procedure.");
		}
		
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		
		if(statementInput.ident.getDec().getType() == Type.NUMBER || 
			statementInput.ident.getDec().getType() == Type.STRING ||
			statementInput.ident.getDec().getType() == Type.BOOLEAN)
		{
			return null;
		}
		else if(statementInput.ident.getDec().getType() == null)
		{
			if(firstpass)
			{
				fullytyped = false;
			}
			else
			{
				fullytyped = true;
			}
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
		return null;
	}

	@Override
	public Object visitStatementBlock(StatementBlock statementBlock, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
		// TODO Auto-generated method stub
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
		
		if(t1==null && t2==null)
		{
			throw new TypeCheckException("Type cannot be determined");
		}
		
		
		//PLUS
		//α x α  → α, where α ∈ {NUMBER, STRING, BOOLEAN}
		if(operator.getKind() == Kind.PLUS)
		{
			if(t1 == null && t2 == Type.NUMBER ||
				t1 == null && t2 == Type.STRING||
				t1 == null && t2 == Type.BOOLEAN)
			{
				expressionBinary.e0.setType(t2);
				expressionBinary.setType(t2);
			}
			else if (t1 == Type.NUMBER && t2 == null ||
					t1 == Type.STRING && t2 == null||
					t1 == Type.BOOLEAN && t2 == null)
			{
				expressionBinary.e1.setType(t1);
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
			if(t1 == null && t2 == Type.NUMBER)
			{
				expressionBinary.e0.setType(t2);
				expressionBinary.setType(t2);
			}
			else if (t1 == Type.NUMBER || t2 == null )
			{
				expressionBinary.e1.setType(t1);
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
			if(t1 == null && t2 == Type.NUMBER ||
				t1 == null && t2 == Type.BOOLEAN)
			{
				expressionBinary.setType(t2);
				expressionBinary.e0.setType(t2);
					
			}
			else if(t1 == Type.NUMBER && t2 == null ||
				t1 == Type.BOOLEAN && t2 == null)
			{
				expressionBinary.setType(t1);
				expressionBinary.e1.setType(t1);
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
			if(t1 == null && t2 == Type.NUMBER ||
				t1 == null && t2 == Type.STRING||
				t1 == null && t2 == Type.BOOLEAN)
			{
				expressionBinary.e0.setType(Type.BOOLEAN);
				expressionBinary.setType(Type.BOOLEAN);
			}
			else if(t1 == Type.NUMBER && t2 == null ||
					t1 == Type.STRING && t2 == null||
					t1 == Type.BOOLEAN && t2 == null)
			{
				expressionBinary.e1.setType(Type.BOOLEAN);
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
		expressionStringLit.setType(Type.NUMBER);
		return null;
	}

	@Override
	public Object visitExpressionBooleanLit(ExpressionBooleanLit expressionBooleanLit, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		expressionBooleanLit.setType(Type.NUMBER);
		return null;
	}

	@Override
	public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		
		procDec.setType(Type.PROCEDURE);
		procDec.block.visit(this, 1);
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
		
		ident.getDec().setType();
		return null;
	}


}
