package edu.ufl.cise.plpfa22;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import edu.ufl.cise.plpfa22.IToken;
import edu.ufl.cise.plpfa22.ast.Declaration;

public class SymbolTable {
	
	//Map<Declaration, Integer> IdentAttr = new HashMap<Declaration, Integer>();
	Map<String, LinkedHashMap<Integer, Declaration>> STEntry= new HashMap<String, LinkedHashMap<Integer, Declaration>>();
	Stack<Integer> STStack = new Stack<Integer>();

	
	public int enterScope()
	{
		int sc = STStack.peek();
		int newsc = sc + 1;
		STStack.push(newsc);
		return newsc;
	}
	
	public int leaveScope()
	{
		int sc = STStack.pop();
		return sc;
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
		}
		//
		
		if (STEntry.containsKey(name))
		{
			oldmap = STEntry.get(name);
			if(oldmap.containsKey(STStack.peek()))
			{
				throw new ScopeException("Identifier already exists");
			}
			else
			{
				newmap.putAll(oldmap);
				newmap.put(scope, dec);
				STEntry.put(name, newmap);
			}
		}
		else
		{
			newmap.put(scope, dec);
			STEntry.put(name, newmap);
		}	
		
	}
	
	public Declaration getEntry(String name) throws PLPException
	{
		if(STEntry.containsKey(name)) 
		{
			List<Integer> scopes  = new ArrayList<Integer>(STEntry.get(name).keySet()); 
			Collections.reverse(scopes);

			
			for(Integer sc: scopes)
			{
				Stack<Integer> temp = (Stack<Integer>)STStack.clone();
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
