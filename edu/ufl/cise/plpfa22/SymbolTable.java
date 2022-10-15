package edu.ufl.cise.plpfa22;
import java.util.*;

import edu.ufl.cise.plpfa22.IToken;
import edu.ufl.cise.plpfa22.ast.Declaration;

public class SymbolTable {
	
	//Map<Declaration, Integer> IdentAttr = new HashMap<Declaration, Integer>();
	HashMap<String, LinkedHashMap<Integer, Declaration>> STEntry= new HashMap<String, LinkedHashMap<Integer, Declaration>>();
	Stack<Integer> STStack = new Stack<Integer>();
	int sc = 0;
	
	public SymbolTable()
	{
		STStack.push(0);
	}

	
	public int enterScope()
	{
		sc++;
		STStack.push(sc);
		System.out.println("syack:");
		System.out.println(STStack);
		return sc;
	}
	
	public int leaveScope()
	{
		int sc = STStack.pop();
		return sc;
	}
	
	public void clearStack()
	{
		while(!STStack.empty())
		{
			STStack.pop();
		}
		STStack.push(0);
		sc = 0;
	}

	public void addEntry(String name, Declaration dec, Boolean changeScope) throws PLPException
	{
		LinkedHashMap<Integer, Declaration> newmap =new LinkedHashMap<>();
		LinkedHashMap<Integer, Declaration> oldmap =new LinkedHashMap<>();
		int scope;
		
		if(changeScope)
		{
			scope = STStack.peek() + 1;
		}
		else
		{
			scope = STStack.peek();
			//System.out.println("inscope");
		}
		//
		
		if (STEntry.containsKey(name))
		{
			//System.out.println("name already exists");
			oldmap = STEntry.get(name);
			//System.out.println(oldmap);
			if(oldmap.containsKey(STStack.peek()))
			{
				throw new ScopeException("Identifier already exists");
			}
			else
			{
				newmap.putAll(oldmap);
				//System.out.println(newmap);
				newmap.put(scope, dec);
				//System.out.println(newmap);
				STEntry.put(name, newmap);
				//System.out.println(STEntry);
			}
		}
		else
		{
			//System.out.println("adding");
			newmap.put(scope, dec);
			//System.out.println(newmap);
			STEntry.put(name, newmap);
			//System.out.println(name);
			//System.out.println(STEntry);
		}	
		
	}
	
	public Declaration getEntry(String name) throws PLPException
	{
		if(STEntry.containsKey(name)) 
		{
			List<Integer> scopes  = new ArrayList<Integer>(STEntry.get(name).keySet()); 
			Collections.reverse(scopes);
			System.out.println(scopes);

			
			for(Integer sc: scopes)
			{
				Stack<Integer> temp = (Stack<Integer>)STStack.clone();
				System.out.println(temp);
				while(!(temp.empty()))
				{
					if(sc == temp.peek())
					{
						return STEntry.get(name).get(temp.pop());
					}
					else
					{
						temp.pop();
					}
				}
			}
			throw new ScopeException("Identifier out of scope");
		}
		else
		{
			throw new ScopeException("Identifier does not exists");
		}
	}
}
