/**  This code is provided for solely for use of students in the course COP5556 Programming Language Principles at the 
 * University of Florida during the Fall Semester 2022 as part of the course project.  No other use is authorized. 
 */

package edu.ufl.cise.plpfa22.ast;

import edu.ufl.cise.plpfa22.IToken;
import edu.ufl.cise.plpfa22.PLPException;

public class ProcDec extends Declaration {

	public final IToken ident;
	public final Block block;
	public String FQName;
	public String outerName;
	

	public ProcDec(IToken firstToken, IToken name, Block body) {
		super(firstToken);
		this.ident = name;
		this.block = body;
	}
	
	public String getName()
	 {
		String name = new String(ident.getText()); 
		return name;
	 }
	
	
	public void setFQName(String parentName)
	{
		this.outerName = parentName;
		this.FQName = parentName + "$" + getName();
	}
	
	public String getOuterName()
	{
		return this.outerName;
	}
	
	public String getFQName()
	{
		return this.FQName;
	}


	@Override
	public Object visit(ASTVisitor v, Object arg) throws PLPException {
		return v.visitProcedure(this, arg);
	}

	@Override
	public String toString() {
		return "ProcDec [" + (ident != null ? "ident=" + ident + ", " : "") + (block != null ? "block=" + block : "") + "]";
	}
	
	

}
