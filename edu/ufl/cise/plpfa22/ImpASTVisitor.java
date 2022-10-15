package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.ast.ASTVisitor;
import edu.ufl.cise.plpfa22.ast.Block;
import edu.ufl.cise.plpfa22.ast.ConstDec;
import edu.ufl.cise.plpfa22.ast.Declaration;
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

public class ImpASTVisitor implements ASTVisitor {
	
	int nest = 0;
	SymbolTable st; 
	

	public ImpASTVisitor() {
		
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		if (arg == (Integer)1)
		{
			for(ConstDec c : block.constDecs)
			{
				
				c.visit(this, arg);
			}
			
			for(VarDec d : block.varDecs)
			{
				d.visit(this, arg);
			}
			
			for(ProcDec p : block.procedureDecs)
			{
				p.visit(this, 1);
			}
			
			
		}
		else
		{

			for(ProcDec p : block.procedureDecs)
			{
				p.visit(this, 2);
			}

			block.statement.visit(this, arg);
		}
		
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		st = new SymbolTable();
		program.block.visit(this, 1);
		st.clearStack();
		program.block.visit(this, 2);
		return null;
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		statementAssign.ident.visit(this, arg);
		statementAssign.expression.visit(this, arg);
		return null;
	}



	@Override
	public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		statementCall.ident.visit(this, arg);
		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		statementInput.ident.visit(this, arg);
		return null;
	}

	@Override
	public Object visitStatementOutput(StatementOutput statementOutput, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		statementOutput.expression.visit(this, arg);
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
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		statementWhile.expression.visit(this, arg);
		statementWhile.statement.visit(this, arg);
		return null;
	}
	
	@Override
	public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		//statementEmpty.visit(this, arg);
		return null;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		expressionBinary.e0.visit(this, arg);
		expressionBinary.e1.visit(this, arg);
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws PLPException {

		String name = new String(expressionIdent.firstToken.getText());
		Declaration idec = st.getEntry(name);
		expressionIdent.setNest(nest);
		expressionIdent.setDec(idec);
		return null;

	}

	@Override
	public Object visitExpressionNumLit(ExpressionNumLit expressionNumLit, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitExpressionStringLit(ExpressionStringLit expressionStringLit, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitExpressionBooleanLit(ExpressionBooleanLit expressionBooleanLit, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		if (arg == (Integer)1)
		{
			procDec.setNest(nest); 
			String name = new String(procDec.ident.getText());
			st.addEntry(name, procDec, false);
			st.enterScope();
			nest++;
			procDec.block.visit(this, 1);
			st.leaveScope();
			nest--;
			return null;
		}
		else
		{
			st.enterScope();
			nest++;
			procDec.block.visit(this, 2);
			st.leaveScope();
			nest--;
			return null;
		}
	}

	@Override
	public Object visitConstDec(ConstDec constDec, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		//System.out.println("here");
		constDec.setNest(nest); 
		String name = new String(constDec.ident.getText());
		//System.out.println(name);
		st.addEntry(name, constDec, false);
		
		return null;
	}
	
	@Override
	public Object visitVarDec(VarDec varDec, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		varDec.setNest(nest); 
		//System.out.println("hello");
		//System.out.println(varDec.ident.getText());
		String name = new String(varDec.ident.getText());
		st.addEntry(name, varDec, false);
		return null;
	}



	@Override
	public Object visitIdent(Ident ident, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		String name = new String(ident.getText());
		Declaration idec = st.getEntry(name);
		ident.setNest(nest);
		ident.setDec(idec);
		return null;
	}

}
