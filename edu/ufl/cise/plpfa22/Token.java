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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char[] getText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SourceLocation getSourceLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getIntValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getBooleanValue() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getStringValue() {
		// TODO Auto-generated method stub
		return null;
	}

}
