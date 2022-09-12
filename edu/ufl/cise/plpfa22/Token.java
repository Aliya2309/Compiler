package edu.ufl.cise.plpfa22;

import java.util.ArrayList;
import java.util.List;

public class Token implements IToken {

	final Kind tkind; //token kind
	final int col; //column
	final int line; //line number
	final int gpos; //global position
	final int len; //length
	final String data;
	final char[] chardata;
	
	public Token(String data, Kind kind, int col, int line,  int gpos, int len, char[] chardata) {
		// populate token's data
		this.tkind = kind;
		this.col = col;
		this.line = line;
		this.gpos = gpos;
		this.len = len;
		this.data = data;
		this.chardata = chardata;
	}

	@Override
	public Kind getKind() {
		// return token kind
		return this.tkind;
	}

	@Override
	public char[] getText() {
		// TODO Auto-generated method stub
		if(this.tkind==Kind.STRING_LIT)
		{
			return this.chardata;
		}
		else {
			char[] text = this.data.toCharArray();
		return text;}
	}

	@Override
	public SourceLocation getSourceLocation() {
		// TODO Auto-generated method stub
		
		SourceLocation sc = new SourceLocation(this.line, this.col+1);
		return sc;
	}

	@Override
	public int getIntValue() {

			if (this.tkind==Kind.NUM_LIT) 
			{ 
				int val = Integer.parseInt(this.data);
				return val;
			}
			return 0;

	}

	@Override
	public boolean getBooleanValue() {
		if (this.tkind==Kind.BOOLEAN_LIT) 
		{ 
			if(this.data.equals("TRUE"))
			{
				return true;
			}
			return false;
		}
		return false;
	}

	@Override
	public String getStringValue() {
		// TODO Auto-generated method stub
		char[] chdata = this.chardata;	
		int len = chdata.length;
		int newlen = len;
		
		List<Character> nparsed = new ArrayList<>();
	
		/*char[] parsed = new char[newlen];
		int c=0;
		for (int i=1; i<len-1;i++)
		{
			if (chdata[i]=='\\')
			{
					if(chdata[i+1]=='n')
					{
						parsed[c]='\n';
					}
					if(chdata[i+1]=='b')
					{
						parsed[c]='\b';
					}
					if(chdata[i+1]=='t')
					{
						parsed[c]='\t';
					}
					if(chdata[i+1]=='f')
					{
						parsed[c]='\f';
					}
					if(chdata[i+1]=='r')
					{
						parsed[c]='\r';
					}
					if(chdata[i+1]=='"')
					{
						parsed[c]='"';
					}
					if(chdata[i+1]=='\'')
					{
						parsed[c]='\'';
					}
					if(chdata[i+1]=='\\')
					{
						parsed[c]='\\';
					}
					c++;
					i++;
					
			}

			
			else
			{
				parsed[c]=chdata[i];
				c++;
			}
		}*/
		
		for (int i=1; i<len-1;i++)
		{
			if (chdata[i]=='\\')
			{
					if(chdata[i+1]=='n')
					{
						nparsed.add('\n');
					}
					if(chdata[i+1]=='b')
					{
						nparsed.add('\b');
					}
					if(chdata[i+1]=='t')
					{
						nparsed.add('\t');
					}
					if(chdata[i+1]=='f')
					{
						nparsed.add('\f');
					}
					if(chdata[i+1]=='r')
					{
						nparsed.add('\r');
					}
					if(chdata[i+1]=='"')
					{
						nparsed.add('"');
					}
					if(chdata[i+1]=='\'')
					{
						nparsed.add('\'');
					}
					if(chdata[i+1]=='\\')
					{
						nparsed.add('\\');
					}
					
					i++;	
			}
			else
			{
				nparsed.add(chdata[i]);
			}
		}
		
		StringBuilder builder = new StringBuilder(nparsed.size());
	    for(Character ch: nparsed)
	    {
	        builder.append(ch);
	    }
	    String stringval =  builder.toString();
		
		System.out.println(nparsed);
		//String stringval = new String(nparsed);
		return stringval;
	}

}
