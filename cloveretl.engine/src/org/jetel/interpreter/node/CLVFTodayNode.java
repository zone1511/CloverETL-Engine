/* Generated By:JJTree: Do not edit this line. CLVFTodayNode.java */

package org.jetel.interpreter.node;
import org.jetel.interpreter.ExpParser;
import org.jetel.interpreter.TransformLangParserVisitor;
public class CLVFTodayNode extends SimpleNode {
  public CLVFTodayNode(int id) {
    super(id);
  }

  public CLVFTodayNode(ExpParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(TransformLangParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
