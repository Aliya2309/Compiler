package edu.ufl.cise.plpfa22;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import edu.ufl.cise.plpfa22.ast.ASTVisitor;
import edu.ufl.cise.plpfa22.CodeGenUtils.GenClass;
import edu.ufl.cise.plpfa22.IToken.Kind;
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
import edu.ufl.cise.plpfa22.ast.Types.Type;
import edu.ufl.cise.plpfa22.ast.VarDec;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	final String packageName;
	final String className;
	final String sourceFileName;
	final String fullyQualifiedClassName; 
	final String classDesc;
	int nest;
	ProcDec currentProc = null;
	public record InnerClass(String FQName, String OuterName, String smallname) {};
	public record ObjectPasser(Object obj1, Object obj2) {};
	List<InnerClass> innerClassList = new ArrayList<InnerClass>();
	List<GenClass> genclasslist = new ArrayList<GenClass>();
	
	

	
	public CodeGenVisitor(String className, String packageName, String sourceFileName) {
		super();
		this.packageName = packageName;
		this.className = className;
		this.sourceFileName = sourceFileName;
		this.fullyQualifiedClassName = packageName + "/" + className;
		this.classDesc="L"+this.fullyQualifiedClassName+';';
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws PLPException {
		
		ObjectPasser obj = (ObjectPasser) arg;
		ClassWriter classWriter = (ClassWriter) obj.obj2;

		if (obj.obj1 == (Integer)1)
		{
			
			for (ConstDec constDec : block.constDecs) {
				constDec.visit(this, null);
			}
			for (VarDec varDec : block.varDecs) {
				varDec.visit(this, classWriter);
			}
			
			//add instructions from statement to method
			
			MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
			//methodVisitor = (MethodVisitor)arg;
			methodVisitor.visitCode();
			block.statement.visit(this, methodVisitor);
			methodVisitor.visitInsn(RETURN);
			methodVisitor.visitMaxs(0,0);
			methodVisitor.visitEnd();
			
		}
		
		else if(obj.obj1 == (Integer)2)
		{
			for (ProcDec procDec: block.procedureDecs) {
				procDec.visit(this, 1);
			}
		}
		
		
		
		else
		{
			for (ProcDec procDec: block.procedureDecs) {
				procDec.visit(this, obj.obj1);
			}
		}
		return null;


	}

	@Override
	public Object visitProgram(Program program, Object arg) throws PLPException {
		//create a classWriter and visit it
		ClassWriter classWriter;
		classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		nest = 0;
		
		classWriter.visit(V18, ACC_PUBLIC | ACC_SUPER, fullyQualifiedClassName, null, "java/lang/Object", new String[] { "java/lang/Runnable" });
		classWriter.visitSource("prog.java", null);
		
		//to calculate class names 
		ObjectPasser obj = new ObjectPasser(fullyQualifiedClassName, classWriter);
		program.block.visit(this, obj);
		
		
		setinnerclasses(classWriter);
		
		//init method
		
		initmethod(classWriter);
		
		
		//run method start
		ObjectPasser obj2 = new ObjectPasser(1, classWriter);
		program.block.visit(this, obj2);

		//get a method visitor for the main method.		
		MethodVisitor methodVisitorMain = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		methodVisitorMain.visitCode();
		methodVisitorMain.visitTypeInsn(NEW, fullyQualifiedClassName);
		methodVisitorMain.visitInsn(DUP);
		methodVisitorMain.visitMethodInsn(INVOKESPECIAL, fullyQualifiedClassName, "<init>", "()V", false);
		methodVisitorMain.visitMethodInsn(INVOKEVIRTUAL, fullyQualifiedClassName, "run", "()V", false);
		methodVisitorMain.visitInsn(RETURN);
		methodVisitorMain.visitMaxs(0, 0);
		methodVisitorMain.visitEnd();
		//visit the block, passing it the methodVisitor
		
		//finish up the class
        classWriter.visitEnd();
        //return the bytes making up the classfile
        
        GenClass gc = new GenClass(fullyQualifiedClassName, classWriter.toByteArray());
        genclasslist.add(gc);
        
        //make class files for procedures
        ObjectPasser obj3 = new ObjectPasser(2, classWriter);
		program.block.visit(this, obj3);
		//System.out.println("Gen class list: ");
		//System.out.println(genclasslist);
		return genclasslist;
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws PLPException {
		//throw new UnsupportedOperationException();
		MethodVisitor methodVisitor = (MethodVisitor)arg;
		methodVisitor.visitVarInsn(ALOAD, 0);
		ObjectPasser ob =  (ObjectPasser) statementAssign.ident.visit(this, methodVisitor);
		String identowner = (String) ob.obj2;
		String name = (String) ob.obj1;
		
		//pos = lastowner.lastIndexOf("$");
		//String identowner = lastowner.substring(0, pos);
		//ObjectPasser ob = new ObjectPasser(methodVisitor, identowner);
		statementAssign.expression.visit(this, arg);
		
		methodVisitor.visitFieldInsn(PUTFIELD, identowner, name, statementAssign.ident.getDec().getDescriptor());
		
		return null;
	}



	@Override
	public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
		//throw new UnsupportedOperationException();
		MethodVisitor methodVisitor = (MethodVisitor)arg;
		String procname = new String(statementCall.ident.getText());
		Declaration d = statementCall.ident.getDec();
		String fqname = ((ProcDec) d).getFQName();
		String fqownername = ((ProcDec) d).getOuterName();
		InnerClass inclass;
			
		methodVisitor.visitTypeInsn(NEW, fqname);
		methodVisitor.visitInsn(DUP);
				
		methodVisitor.visitVarInsn(ALOAD, 0);
		ObjectPasser ob = (ObjectPasser) statementCall.ident.visit(this, methodVisitor);
		String descriptor = "(L" + fqownername + ";)V";
		methodVisitor.visitMethodInsn(INVOKESPECIAL, fqname, "<init>", descriptor, false);
		methodVisitor.visitMethodInsn(INVOKEVIRTUAL, fqname, "run", "()V", false);
		
		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementOutput(StatementOutput statementOutput, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor)arg;
		mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		statementOutput.expression.visit(this, arg);
		Type etype = statementOutput.expression.getType();
		String JVMType = (etype.equals(Type.NUMBER) ? "I" : (etype.equals(Type.BOOLEAN) ? "Z" : "Ljava/lang/String;"));
		String printlnSig = "(" + JVMType +")V";
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", printlnSig, false);
		return null;
	}

	@Override
	public Object visitStatementBlock(StatementBlock statementBlock, Object arg) throws PLPException {
		//todo
		//throw new UnsupportedOperationException();
		for(Statement s : statementBlock.statements)
		{
			s.visit(this, arg);
		}
		return null;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws PLPException {
		//todo
		//throw new UnsupportedOperationException();
		MethodVisitor mv = (MethodVisitor) arg;
		statementIf.expression.visit(this, arg);
		Label l1 = new Label();
		mv.visitJumpInsn(IFEQ, l1);
		statementIf.statement.visit(this, arg);
		
		mv.visitLabel(l1);
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
		//throw new UnsupportedOperationException();
		MethodVisitor mv = (MethodVisitor)arg;
		Label l1 = new Label();
		Label l2 = new Label();
		mv.visitJumpInsn(GOTO, l1);
		mv.visitLabel(l2);
		statementWhile.statement.visit(this, arg);
		
		mv.visitLabel(l1);
		statementWhile.expression.visit(this, arg);
		mv.visitJumpInsn(IFNE, l2);
		
		return null;
	}
	
	@Override
	public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg) throws PLPException {
		return null;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor) arg;
		Type argType = expressionBinary.e0.getType();
		Kind op = expressionBinary.op.getKind();
		switch (argType) {
		case NUMBER -> {
			expressionBinary.e0.visit(this, arg);
			expressionBinary.e1.visit(this, arg);
			switch (op) {
			case PLUS -> mv.visitInsn(IADD);
			case MINUS -> mv.visitInsn(ISUB);
			case TIMES -> mv.visitInsn(IMUL);
			case DIV -> mv.visitInsn(IDIV);
			case MOD -> mv.visitInsn(IREM);
			case EQ -> {
				Label labelNumEqFalseBr = new Label();
				mv.visitJumpInsn(IF_ICMPNE, labelNumEqFalseBr);
				mv.visitInsn(ICONST_1);
				Label labelPostNumEq = new Label();
				mv.visitJumpInsn(GOTO, labelPostNumEq);
				mv.visitLabel(labelNumEqFalseBr);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(labelPostNumEq);
			}
			case NEQ -> {
				//throw new UnsupportedOperationException();
				Label labelNumEqFalseBr = new Label();
				mv.visitJumpInsn(IF_ICMPNE, labelNumEqFalseBr);
				mv.visitInsn(ICONST_0);
				Label labelPostNumEq = new Label();
				mv.visitJumpInsn(GOTO, labelPostNumEq);
				mv.visitLabel(labelNumEqFalseBr);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(labelPostNumEq);
			}
			case LT -> {
				//throw new UnsupportedOperationException();
				Label labelLT = new Label();
				mv.visitJumpInsn(IF_ICMPLT, labelLT);
				mv.visitInsn(ICONST_0);
				Label labelNLT = new Label();
				mv.visitJumpInsn(GOTO, labelNLT);
				mv.visitLabel(labelLT);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(labelNLT);
				
			}
			case LE -> {
				//throw new UnsupportedOperationException();
				Label labelLT = new Label();
				mv.visitJumpInsn(IF_ICMPLE, labelLT);
				mv.visitInsn(ICONST_0);
				Label labelNLT = new Label();
				mv.visitJumpInsn(GOTO, labelNLT);
				mv.visitLabel(labelLT);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(labelNLT);
			}
			case GT -> {
				//throw new UnsupportedOperationException();
				Label labelLT = new Label();
				mv.visitJumpInsn(IF_ICMPGT, labelLT);
				mv.visitInsn(ICONST_0);
				Label labelNLT = new Label();
				mv.visitJumpInsn(GOTO, labelNLT);
				mv.visitLabel(labelLT);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(labelNLT);
			}
			case GE -> {
				//throw new UnsupportedOperationException();
				Label labelLT = new Label();
				mv.visitJumpInsn(IF_ICMPGE, labelLT);
				mv.visitInsn(ICONST_0);
				Label labelNLT = new Label();
				mv.visitJumpInsn(GOTO, labelNLT);
				mv.visitLabel(labelLT);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(labelNLT);
			}
			default -> {
				throw new IllegalStateException("code gen bug in visitExpressionBinary NUMBER");
			}
			}
			;
		}
		
		
		case BOOLEAN -> {
			//throw new UnsupportedOperationException();
			expressionBinary.e0.visit(this, arg);
			expressionBinary.e1.visit(this, arg);
			switch (op) {
				case PLUS ->
				{
					Label l1 = new Label();
					Label l2 = new Label();
					Label l3 = new Label();
					mv.visitJumpInsn(IFNE, l1);  //e0 is true, result is true
					mv.visitJumpInsn(IFNE, l2);  //e1 is true, result is true
					mv.visitInsn(ICONST_0);      //both false
					mv.visitJumpInsn(GOTO, l3);
					mv.visitLabel(l1);
					mv.visitInsn(POP);    //discard value of e1
					mv.visitInsn(ICONST_1); 
					mv.visitJumpInsn(GOTO, l3);//e0 is true
					mv.visitLabel(l2);
					mv.visitInsn(ICONST_1);   //e1 is true
					mv.visitLabel(l3);
				}
				
				case TIMES ->
				{
					Label l1 = new Label();
					Label l2 = new Label();
					Label l3 = new Label();
					mv.visitJumpInsn(IFNE, l1);
					mv.visitInsn(POP);
					mv.visitInsn(ICONST_0);
					
					mv.visitJumpInsn(GOTO, l3);
					mv.visitLabel(l1);
					mv.visitJumpInsn(IFNE, l2); 
					mv.visitInsn(ICONST_0);
					mv.visitJumpInsn(GOTO, l3);
					mv.visitLabel(l2);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(l3);
				}
				
				case EQ -> {
					Label labelNumEqFalseBr = new Label();
					mv.visitJumpInsn(IF_ICMPNE, labelNumEqFalseBr);
					mv.visitInsn(ICONST_1);
					Label labelPostNumEq = new Label();
					mv.visitJumpInsn(GOTO, labelPostNumEq);
					mv.visitLabel(labelNumEqFalseBr);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(labelPostNumEq);
				}
				case NEQ -> {
					//throw new UnsupportedOperationException();
					Label labelNumEqFalseBr = new Label();
					mv.visitJumpInsn(IF_ICMPNE, labelNumEqFalseBr);
					mv.visitInsn(ICONST_0);
					Label labelPostNumEq = new Label();
					mv.visitJumpInsn(GOTO, labelPostNumEq);
					mv.visitLabel(labelNumEqFalseBr);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(labelPostNumEq);
				}
				case LT -> {
					//throw new UnsupportedOperationException();
					Label labelLT = new Label();
					mv.visitJumpInsn(IF_ICMPLT, labelLT);
					mv.visitInsn(ICONST_0);
					Label labelNLT = new Label();
					mv.visitJumpInsn(GOTO, labelNLT);
					mv.visitLabel(labelLT);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(labelNLT);
					
				}
				case LE -> {
					//throw new UnsupportedOperationException();
					Label labelLT = new Label();
					mv.visitJumpInsn(IF_ICMPLE, labelLT);
					mv.visitInsn(ICONST_0);
					Label labelNLT = new Label();
					mv.visitJumpInsn(GOTO, labelNLT);
					mv.visitLabel(labelLT);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(labelNLT);
				}
				case GT -> {
					//throw new UnsupportedOperationException();
					Label labelLT = new Label();
					mv.visitJumpInsn(IF_ICMPGT, labelLT);
					mv.visitInsn(ICONST_0);
					Label labelNLT = new Label();
					mv.visitJumpInsn(GOTO, labelNLT);
					mv.visitLabel(labelLT);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(labelNLT);
				}
				case GE -> {
					//throw new UnsupportedOperationException();
					Label labelLT = new Label();
					mv.visitJumpInsn(IF_ICMPGE, labelLT);
					mv.visitInsn(ICONST_0);
					Label labelNLT = new Label();
					mv.visitJumpInsn(GOTO, labelNLT);
					mv.visitLabel(labelLT);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(labelNLT);
				}
				
				default -> {
					throw new IllegalStateException("code gen bug in visitExpressionBinary BOOLEAN");
				}
			}
			;
		}
		case STRING -> {
			//throw new UnsupportedOperationException();
			expressionBinary.e0.visit(this, arg);
			expressionBinary.e1.visit(this, arg);
			switch (op) {
				case PLUS ->
				{
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;", false);
				}
				
				case EQ ->
				{
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
				}
				
				case NEQ ->
				{
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
					Label l1 = new Label();
					Label l2 = new Label();
					mv.visitJumpInsn(IFEQ, l1);
					mv.visitInsn(ICONST_0);
					mv.visitJumpInsn(GOTO, l2);
					mv.visitLabel(l1);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(l2);
				}
				
				case LT ->
				{
					Label l1 = new Label();
					Label l2 = new Label();
					mv.visitInsn(SWAP);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false);
					
					mv.visitJumpInsn(IFEQ, l1);
					expressionBinary.e0.visit(this, arg);
					expressionBinary.e1.visit(this, arg);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
					mv.visitJumpInsn(IFNE, l1);
					mv.visitInsn(ICONST_1);
					mv.visitJumpInsn(GOTO, l2);
					mv.visitLabel(l1);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(l2);
					
				}
				
				case LE ->
				{
					Label l1 = new Label();
					Label l2 = new Label();
					mv.visitInsn(SWAP);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false);
					mv.visitJumpInsn(IFEQ, l1);
					
					mv.visitInsn(ICONST_1);
					mv.visitJumpInsn(GOTO, l2);
					mv.visitLabel(l1);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(l2);
				}
				
				case GT ->
				{
					Label l1 = new Label();
					Label l2 = new Label();
					//mv.visitInsn(SWAP);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "endsWith", "(Ljava/lang/String;)Z", false);
					
					mv.visitJumpInsn(IFEQ, l1);
					expressionBinary.e0.visit(this, arg);
					expressionBinary.e1.visit(this, arg);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
					mv.visitJumpInsn(IFNE, l1);
					mv.visitInsn(ICONST_1);
					mv.visitJumpInsn(GOTO, l2);
					mv.visitLabel(l1);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(l2);
				}
				
				case GE ->
				{
					Label l1 = new Label();
					Label l2 = new Label();
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "endsWith", "(Ljava/lang/String;)Z", false);
					mv.visitJumpInsn(IFEQ, l1);
					
					mv.visitInsn(ICONST_1);
					mv.visitJumpInsn(GOTO, l2);
					mv.visitLabel(l1);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(l2);
				}
				default -> 
				{
					throw new IllegalStateException("code gen bug in visitExpressionBinary BOOLEAN");
				}
				
				
			};
		}
		default -> {
			throw new IllegalStateException("code gen bug in visitExpressionBinary");
		}
		}
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws PLPException {
		//throw new UnsupportedOperationException();
		MethodVisitor mv = (MethodVisitor)arg;
		
		if (expressionIdent.getDec() instanceof ConstDec)
		{
			mv.visitLdcInsn(((ConstDec) expressionIdent.getDec()).getVal());
		}
		else if (expressionIdent.getDec() instanceof VarDec)
		{
			
			mv.visitVarInsn(ALOAD, 0);
			int thisnest = 0, pos = 0;
			String owner = "", desc= "", thisname= "", lastowner= "";
			String identowner = ((VarDec) expressionIdent.getDec()).getOwnerClass();;
			if(currentProc != null)
			{
				thisnest = currentProc.getNest();
				owner = currentProc.getFQName(); 
				desc = "L" + currentProc.getOuterName() + ";";
				thisname = "this$" + String.valueOf(thisnest);
				lastowner = owner;
				
			
				while(expressionIdent.getDec().getNest() <= thisnest)
				{
					
					mv.visitFieldInsn(GETFIELD, owner, thisname, desc);
					thisnest --;
					thisname = "this$" + String.valueOf(thisnest);
					lastowner = owner;
					pos = owner.lastIndexOf("$");
					owner = owner.substring(0, pos);
					if(desc.contains("$"))
					{
						
						pos = desc.lastIndexOf("$");
						desc = desc.substring(0, pos);
						desc = desc + ";";
					}
					
				}
				
//				pos = lastowner.lastIndexOf("$");
//				identowner = lastowner.substring(0, pos);
//				
//				if(expressionIdent.getDec().getNest() == 1)
//				{
//					identowner = currentProc.getFQName();
//				}
			}
			
//			if(currentProc != null)
//			{
//				pos = lastowner.lastIndexOf("$");
//				identowner = lastowner.substring(0, pos);
//			}
			mv.visitFieldInsn(GETFIELD, identowner, expressionIdent.getDec().getName(), expressionIdent.getDec().getDescriptor());
		}
		return null;
	}

	@Override
	public Object visitExpressionNumLit(ExpressionNumLit expressionNumLit, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor)arg;
		mv.visitLdcInsn(expressionNumLit.getFirstToken().getIntValue());
		return null;
	}

	@Override
	public Object visitExpressionStringLit(ExpressionStringLit expressionStringLit, Object arg) throws PLPException {
		//todo
		//throw new UnsupportedOperationException();
		MethodVisitor mv = (MethodVisitor)arg;
		mv.visitLdcInsn(expressionStringLit.getFirstToken().getStringValue());
		return null;
	}

	@Override
	public Object visitExpressionBooleanLit(ExpressionBooleanLit expressionBooleanLit, Object arg) throws PLPException {
		//todo
		//throw new UnsupportedOperationException();
		MethodVisitor mv = (MethodVisitor)arg;
		if(expressionBooleanLit.getFirstToken().getBooleanValue() == true)
		{
			mv.visitInsn(ICONST_1);
		}
		else
		{
			mv.visitInsn(ICONST_0);
		}
		return null;
	}

	@Override
	public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
		//throw new UnsupportedOperationException();
		ClassWriter classWriterProc;
		
		if (arg == (Integer)1)
		{
			
			classWriterProc = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			nest ++;
			classWriterProc.visit(V18, ACC_SUPER, procDec.getFQName(), null, "java/lang/Object", new String[] { "java/lang/Runnable" });
			classWriterProc.visitSource("prog.java", null);
			classWriterProc.visitNestHost(fullyQualifiedClassName);
			// rest of inner classes
			setinnerclasses_procedure(classWriterProc);
			classWriterProc.visitInnerClass(procDec.getFQName(), procDec.getOuterName(), procDec.getName(), 0);
			FieldVisitor fieldVisitor;
			
			//field visitor for this field of inner classes
			String thisname = "this$" + String.valueOf(procDec.getNest());
			String descriptor = "L" + procDec.getOuterName() + ";";
			fieldVisitor = classWriterProc.visitField(ACC_FINAL | ACC_SYNTHETIC, thisname, descriptor, null, null);
			fieldVisitor.visitEnd();
			
			procinitmethod(procDec, thisname, descriptor, classWriterProc);
			
			currentProc = procDec;
			
			ObjectPasser obj2 = new ObjectPasser(1, classWriterProc);
			procDec.block.visit(this, obj2);
			
			classWriterProc.visitEnd();
	        //return the bytes making up the classfile
	        
	        GenClass gc = new GenClass(procDec.getFQName(), classWriterProc.toByteArray());
	        genclasslist.add(gc);
			
			ObjectPasser obj3 = new ObjectPasser(2, classWriterProc);
			procDec.block.visit(this, obj3);
			nest --;
		}
		else
		{
			procDec.setFQName((String) arg);
			
			InnerClass nc = new InnerClass(procDec.getFQName(), procDec.getOuterName(), procDec.getName());
			innerClassList.add(nc);
			
			ObjectPasser obj2 = new ObjectPasser(procDec.getFQName(), null);
			procDec.block.visit(this, obj2);
			
			
		}
		return null;
	}

	@Override
	public Object visitConstDec(ConstDec constDec, Object arg) throws PLPException {
		//throw new UnsupportedOperationException();
		return null;
	}
	
	@Override
	public Object visitVarDec(VarDec varDec, Object arg) throws PLPException {
		//throw new UnsupportedOperationException();
		String name = new String(varDec.ident.getText());
		ClassWriter classWriter = (ClassWriter) arg;

		if(varDec.getDescriptor() != null)
		{
			//System.out.println(name);
			//System.out.println(varDec.getDescriptor());
			FieldVisitor fieldVisitor = classWriter.visitField(0, name, varDec.getDescriptor(), null, null);
			fieldVisitor.visitEnd();
			
			if(currentProc == null)
			{
				varDec.setOwnerClass(fullyQualifiedClassName);
			}
			
			else
			{
				varDec.setOwnerClass(currentProc.getFQName());
			}
		}

		return null;
	}



	@Override
	public Object visitIdent(Ident ident, Object arg) throws PLPException {
		//throw new UnsupportedOperationException();
		MethodVisitor methodVisitor = (MethodVisitor) arg;
		int thisnest = 0, pos = 0;
		String owner = "", desc= "", thisname= "", lastowner= "";
		String identowner;
		if(ident.getDec() instanceof VarDec)
		{
			identowner=((VarDec) ident.getDec()).getOwnerClass();
		}
		else
		{
			identowner = "";
		}
		
		if(currentProc != null)
		{
			//System.out.println("here");
			
			thisnest = currentProc.getNest();
			owner = currentProc.getFQName(); 
			desc = "L" + currentProc.getOuterName() + ";";
			thisname = "this$" + String.valueOf(thisnest);
			lastowner = owner;
			
		
			while(ident.getDec().getNest() <= thisnest)
			{
				
				methodVisitor.visitFieldInsn(GETFIELD, owner, thisname, desc);
				thisnest --;
				thisname = "this$" + String.valueOf(thisnest);
				lastowner = owner;
				
				pos = owner.lastIndexOf("$");
				owner = owner.substring(0, pos);
				if(desc.contains("$"))
				{
					
					pos = desc.lastIndexOf("$");
					desc = desc.substring(0, pos);
					desc = desc + ";";
				}
				
			}
			
//			pos = lastowner.lastIndexOf("$");
//			identowner = lastowner.substring(0, pos);
//			
//			if(ident.getDec().getNest() == 1)
//			{
//				identowner = currentProc.getFQName();
//			}
		}
		

		
//		ObjectPasser ob = (ObjectPasser)arg;
//		MethodVisitor methodVisitor = (MethodVisitor) ob.obj1;
//		String identowner = (String) ob.obj2;
		String name = new String(ident.getText());
		//methodVisitor.visitFieldInsn(PUTFIELD, identowner, name, ident.getDec().getDescriptor());
		
		
		
		
		//ObjectPasser ob = new ObjectPasser(methodVisitor, identowner);
		ObjectPasser ob = new ObjectPasser(name, identowner);
		
		return ob;
	}
	
	public void initmethod(ClassWriter classWriter)
	{
		MethodVisitor methodVisitorInit = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		methodVisitorInit.visitCode();
		methodVisitorInit.visitVarInsn(ALOAD, 0);
		methodVisitorInit.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		methodVisitorInit.visitInsn(RETURN);
		methodVisitorInit.visitMaxs(0, 0);
		methodVisitorInit.visitEnd();
		//init method end
	}
	
	public void setinnerclasses(ClassWriter classWriter)
	{
		for(InnerClass inc : innerClassList)
		{
			classWriter.visitNestMember(inc.FQName);
			classWriter.visitInnerClass(inc.FQName, inc.OuterName, inc.smallname, 0);
		}
	}
	
	public void setinnerclasses_procedure(ClassWriter classWriterProc)
	{
		for(InnerClass inc : innerClassList)
		{
			classWriterProc.visitInnerClass(inc.FQName, inc.OuterName, inc.smallname, 0);
		}
	}
	
	public void procinitmethod(ProcDec procDec, String thisname, String descriptor, ClassWriter classWriterProc)
	{
		MethodVisitor methodVisitor;
		String desc = "(L" + procDec.getOuterName() + ";)V";
		methodVisitor = classWriterProc.visitMethod(0, "<init>", desc, null, null);
		methodVisitor.visitCode();
		methodVisitor.visitVarInsn(ALOAD, 0);
		methodVisitor.visitVarInsn(ALOAD, 1);
		methodVisitor.visitFieldInsn(PUTFIELD, procDec.getFQName(), thisname, descriptor);
		methodVisitor.visitVarInsn(ALOAD, 0);
		methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		methodVisitor.visitInsn(RETURN);
		methodVisitor.visitMaxs(0, 0);
		methodVisitor.visitEnd();
	}

}
