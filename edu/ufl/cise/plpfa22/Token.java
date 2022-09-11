package edu.ufl.cise.plpfa22;

public class Token implements IToken {

	final Kind tkind; //token kind
	final int col; //column
	final int line; //line number
	final int gpos; //global position
	final int len; //length
	final String data;
	
	public Token(String data, Kind kind, int col, int line,  int gpos, int len) {
		// populate token's data
		this.tkind = kind;
		this.col = col;
		this.line = line;
		this.gpos = gpos;
		this.len = len;
		this.data = data;
	}

	@Override
	public Kind getKind() {
		// return token kind
		return this.tkind;
	}

	@Override
	public char[] getText() {
		// TODO Auto-generated method stub
		char[] text = this.data.toCharArray();
		return text;
	}

	@Override
	public SourceLocation getSourceLocation() {
		// TODO Auto-generated method stub
		
		SourceLocation sc = new SourceLocation(this.line, this.col);
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
		
		return null;
	}

}
