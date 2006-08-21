/* Generated By:JJTree: Do not edit this line. CLVFStr2NumNode.java */

package org.jetel.interpreter.node;
import org.jetel.interpreter.ExpParser;
import org.jetel.interpreter.TransformLangParserVisitor;

public class CLVFStr2NumNode extends SimpleNode  {
   
  public int numType=-1; //default is numeric type not set  
  
  public CLVFStr2NumNode(int id) {
    super(id);
  }

  public CLVFStr2NumNode(ExpParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(TransformLangParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
  
  public void setType(int type){
      this.numType=type;
  }
}
