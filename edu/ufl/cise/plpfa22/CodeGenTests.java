package edu.ufl.cise.plpfa22;

import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import edu.ufl.cise.plpfa22.CodeGenUtils.DynamicClassLoader;
import edu.ufl.cise.plpfa22.ast.ASTNode;


public class CodeGenTests {


	/**
	 * Generates  a classfile for the given source program.  The classfile has the given name and package.
	 * 
	 * @param input
	 * @param className
	 * @param packageName
	 * @return
	 * @throws Exception
	 */
	byte[] compile(String input, String className, String packageName) throws Exception {
		show("*****************");
		show(input);
		ILexer lexer = CompilerComponentFactory.getLexer(input);
		ASTNode ast = CompilerComponentFactory.getParser(lexer).parse();
		ast.visit(CompilerComponentFactory.getScopeVisitor(), null);
		ast.visit(CompilerComponentFactory.getTypeInferenceVisitor(), null);
		byte[] bytecode = (byte[]) ast.visit(CompilerComponentFactory.getCodeGenVisitor(className, packageName, ""), null);
		show(CodeGenUtils.bytecodeToString(bytecode));
		show("----------------");
		return bytecode;
	}


	/**
	 * Executes indicated method defined in bytecode and returns the result. args is
	 * an Object[] containing the parameters of the method, or may be null if the
	 * method does not have parameters.
	 * 
	 * Requires that the given method is not overloaded in the class file.
	 * 
	 * @param bytecode
	 * @param className
	 * @param methodName
	 * @param args
	 * @return
	 * @throws Exception
	 */
	Object loadClassAndRunMethod(byte[] bytecode, String className, String methodName, Object[] args) throws Exception {
		Class<?> testClass = getClass(bytecode, className);
		return runMethod(testClass,methodName, args);
	}

	private Method findMethod(String name, Method[] methods) {
		for (Method m : methods) {
			String methodName = m.getName();
			if (name.equals(methodName))
				return m;
		}
		throw new RuntimeException("Method " + name + " not found in generated bytecode");
	}

	Class<?> getClass(byte[] bytecode, String className) throws Exception {
		DynamicClassLoader loader = new DynamicClassLoader(Thread.currentThread().getContextClassLoader());
		Class<?> testClass = loader.define(className, bytecode);
		return testClass;
	}
	

	Object runMethod(Class<?> testClass, String methodName, Object[] args) throws Exception {
		Method[] methods = testClass.getDeclaredMethods();
		Method m = findMethod(methodName, methods);
		return m.invoke(null, args);
	}


	static boolean VERBOSE = true;
	void show(Object o) {
		if (VERBOSE) {
			System.out.println(o);
		}
	}


	
	@DisplayName("numOut")
	@Test
	public void numout(TestInfo testInfo)throws Exception {
		String input = """
				! 3=3
				.
				""";
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		byte[] bytecode = compile(input, shortClassName, JVMpackageName);
		
		show(CodeGenUtils.bytecodeToString(bytecode));
		Object[] args = new Object[1];  
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassAndRunMethod(bytecode, className, "main", args);
	
	}

	@DisplayName("stringOut")
	@Test
	public void stringout(TestInfo testInfo)throws Exception {
		String input = """
				! "hello world"
				.
				""";
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		byte[] bytecode = compile(input, shortClassName, JVMpackageName);
		show(CodeGenUtils.bytecodeToString(bytecode));
		
		Object[] args = new Object[1];  
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassAndRunMethod(bytecode, className, "main", args);
	}
	
	@DisplayName("booleanOut")
	@Test
	public void booleanOut(TestInfo testInfo)throws Exception {
		String input = """
				! TRUE
				.
				""";
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		byte[] bytecode = compile(input, shortClassName, JVMpackageName);
		show(CodeGenUtils.bytecodeToString(bytecode));
		
		Object[] args = new Object[1];  
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassAndRunMethod(bytecode, className, "main", args);
	}	
	
	@DisplayName("statementBlock")
	@Test
	public void statementBlock(TestInfo testInfo)throws Exception{
		String input = """
			BEGIN 
			! 3;
			! FALSE;
			! "hey, it works!"
			END
			.
			""";
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		byte[] bytecode = compile(input, shortClassName, JVMpackageName);
		show(CodeGenUtils.bytecodeToString(bytecode));
		

		Object[] args = new Object[1];  
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassAndRunMethod(bytecode, className, "main", args);
	}	
	
	@DisplayName("intOps")
	@Test
	public void intOps(TestInfo testInfo)throws Exception{
		String input = """
			BEGIN 
			! 1+3;
			! 7-3;
			! 2*2;
			! 16/4;
			! 20%8;
			END
			.
			""";
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		byte[] bytecode = compile(input, shortClassName, JVMpackageName);
		show(CodeGenUtils.bytecodeToString(bytecode));
		
		Object[] args = new Object[1];  
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassAndRunMethod(bytecode, className, "main", args);
	}		
	
	@DisplayName("intEqOps")
	@Test
	public void intEqOps(TestInfo testInfo) throws Exception {
		String input = """
				BEGIN
				! 3 = 4;
				! 3 = 3;
				! 3 # 4;
				! 3 # 3
				END
				.
				""";
		
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		byte[] bytecode = compile(input, shortClassName, JVMpackageName);
		show(CodeGenUtils.bytecodeToString(bytecode));
		
		Object[] args = new Object[1];  
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassAndRunMethod(bytecode, className, "main", args);
		
	}

	@DisplayName("boolEqOps")
	@Test
	public void boolEqOps(TestInfo testInfo) throws Exception {
		String input = """
				BEGIN
				! TRUE = TRUE;
				! TRUE # TRUE;
				! FALSE = FALSE;
				! FALSE # FALSE;
				! TRUE = FALSE;
				! TRUE # FALSE;
				! FALSE = TRUE;
				! FALSE # TRUE;
				END
				.
				""";
		
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		byte[] bytecode = compile(input, shortClassName, JVMpackageName);
		show(CodeGenUtils.bytecodeToString(bytecode));
		
		Object[] args = new Object[1];  
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassAndRunMethod(bytecode, className, "main", args);
		
	}
	
	
	
	@DisplayName("intRelOps")
	@Test
	public void intRelOps(TestInfo testInfo) throws Exception {
		String input = """
				BEGIN
				! 3 < 4;
				! 3 <= 4;
				! 3 > 4;
				! 3 >= 4;
				! 4 < 4;
				! 4 <= 4;
				! 4 > 4;
				! 4 >= 4;
				! 4 < 3;
				! 4 <= 3;
				! 4 > 3;
				! 4 >= 3						
				END
				.
				""";
		
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		byte[] bytecode = compile(input, shortClassName, JVMpackageName);
		show(CodeGenUtils.bytecodeToString(bytecode));
		
		Object[] args = new Object[1];  
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassAndRunMethod(bytecode, className, "main", args);		
	}
	
	@DisplayName("boolRelOps")
	@Test
	public void boolRelOps(TestInfo testInfo) throws Exception {
		String input = """
				BEGIN
				! FALSE < TRUE;
				! FALSE <= TRUE;
				! FALSE > TRUE;
				! FALSE >= TRUE;
				! TRUE < TRUE;
				! TRUE <= TRUE;
				! TRUE > TRUE;
				! TRUE >= TRUE;
				! TRUE < FALSE;
				! TRUE <= FALSE;
				! TRUE > FALSE;
				! TRUE >= FALSE	;
				! FALSE < FALSE;
				! FALSE <= FALSE;
				! FALSE > FALSE;
				! FALSE >= FALSE					
				END
				.
				""";
		
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		byte[] bytecode = compile(input, shortClassName, JVMpackageName);
		show(CodeGenUtils.bytecodeToString(bytecode));
		
		Object[] args = new Object[1];  
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassAndRunMethod(bytecode, className, "main", args);		
	}
	
	
	@DisplayName("stringEqOps")
	@Test
	public void stringEqOps(TestInfo testInfo) throws Exception {
		String input = """
				BEGIN
				! "red" = "blue";
				! "red"= "red";
				! "red" # "blue";
				! "red" # "red"
				END
				.
				""";
		
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		byte[] bytecode = compile(input, shortClassName, JVMpackageName);
		show(CodeGenUtils.bytecodeToString(bytecode));
		
		Object[] args = new Object[1];  
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassAndRunMethod(bytecode, className, "main", args);
		
	}
	
	
	@DisplayName("stringRelOps")
	@Test
	public void stringRelOps(TestInfo testInfo) throws Exception {
		String input = """
				BEGIN
				! "FA" < "FALSE";
				! "FA" <= "FALSE";
				! "FA" > "FALSE";
				! "FA" >= "FALSE";
				! "FALSE" < "FALSE";
				! "FALSE" <= "FALSE";
				! "FALSE" > "FALSE";
				! "FALSE" >= "FALSE";
				! "FALSE" < "FA";
				! "FALSE" <= "FA";
				! "FALSE" > "FA";
				! "FALSE" >= "FA"	;
				! "FA" < "FA";
				! "FA" <= "FA";
				! "FA" > "FA";
				! "FA" >= "FA"					
				END
				.
				""";
		
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		byte[] bytecode = compile(input, shortClassName, JVMpackageName);
		show(CodeGenUtils.bytecodeToString(bytecode));
		
		Object[] args = new Object[1];  
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassAndRunMethod(bytecode, className, "main", args);		
	}
	
	}





