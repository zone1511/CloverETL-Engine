/* Generated By:JJTree: Do not edit this line. CLVFLowercaseNode.java */

package org.jetel.interpreter;

public class CLVFLowercaseNode extends SimpleNode {
	
	StringBuffer strBuf;
	
	public CLVFLowercaseNode(int id) {
		super(id);
		strBuf=new StringBuffer();
	}
	
	public CLVFLowercaseNode(FilterExpParser p, int id) {
	    super(p, id);
	    strBuf=new StringBuffer();
	  }
	
	/** Accept the visitor. **/
	public Object jjtAccept(FilterExpParserVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}
}
