/* Generated By:JJTree: Do not edit this line. ./TransformLangParserVisitor.java */

package org.jetel.interpreter;

import org.jetel.interpreter.ASTnode.CLVFAddNode;
import org.jetel.interpreter.ASTnode.CLVFAnd;
import org.jetel.interpreter.ASTnode.CLVFAssignment;
import org.jetel.interpreter.ASTnode.CLVFBlock;
import org.jetel.interpreter.ASTnode.CLVFBreakStatement;
import org.jetel.interpreter.ASTnode.CLVFBreakpointNode;
import org.jetel.interpreter.ASTnode.CLVFCaseExpression;
import org.jetel.interpreter.ASTnode.CLVFComparison;
import org.jetel.interpreter.ASTnode.CLVFContinueStatement;
import org.jetel.interpreter.ASTnode.CLVFDivNode;
import org.jetel.interpreter.ASTnode.CLVFDoStatement;
import org.jetel.interpreter.ASTnode.CLVFEvalNode;
import org.jetel.interpreter.ASTnode.CLVFForStatement;
import org.jetel.interpreter.ASTnode.CLVFForeachStatement;
import org.jetel.interpreter.ASTnode.CLVFFunctionCallStatement;
import org.jetel.interpreter.ASTnode.CLVFFunctionDeclaration;
import org.jetel.interpreter.ASTnode.CLVFIfStatement;
import org.jetel.interpreter.ASTnode.CLVFIffNode;
import org.jetel.interpreter.ASTnode.CLVFImportSource;
import org.jetel.interpreter.ASTnode.CLVFIncrDecrStatement;
import org.jetel.interpreter.ASTnode.CLVFInputFieldLiteral;
import org.jetel.interpreter.ASTnode.CLVFIsNullNode;
import org.jetel.interpreter.ASTnode.CLVFLiteral;
import org.jetel.interpreter.ASTnode.CLVFLookupNode;
import org.jetel.interpreter.ASTnode.CLVFMapping;
import org.jetel.interpreter.ASTnode.CLVFModNode;
import org.jetel.interpreter.ASTnode.CLVFMulNode;
import org.jetel.interpreter.ASTnode.CLVFNVL2Node;
import org.jetel.interpreter.ASTnode.CLVFNVLNode;
import org.jetel.interpreter.ASTnode.CLVFOperator;
import org.jetel.interpreter.ASTnode.CLVFOr;
import org.jetel.interpreter.ASTnode.CLVFPostfixExpression;
import org.jetel.interpreter.ASTnode.CLVFPrintErrNode;
import org.jetel.interpreter.ASTnode.CLVFPrintLogNode;
import org.jetel.interpreter.ASTnode.CLVFPrintStackNode;
import org.jetel.interpreter.ASTnode.CLVFRaiseErrorNode;
import org.jetel.interpreter.ASTnode.CLVFRegexLiteral;
import org.jetel.interpreter.ASTnode.CLVFReturnStatement;
import org.jetel.interpreter.ASTnode.CLVFSequenceNode;
import org.jetel.interpreter.ASTnode.CLVFStart;
import org.jetel.interpreter.ASTnode.CLVFStartExpression;
import org.jetel.interpreter.ASTnode.CLVFSubNode;
import org.jetel.interpreter.ASTnode.CLVFSwitchStatement;
import org.jetel.interpreter.ASTnode.CLVFSymbolNameExp;
import org.jetel.interpreter.ASTnode.CLVFTryCatchStatement;
import org.jetel.interpreter.ASTnode.CLVFUnaryExpression;
import org.jetel.interpreter.ASTnode.CLVFVarDeclaration;
import org.jetel.interpreter.ASTnode.CLVFVariableLiteral;
import org.jetel.interpreter.ASTnode.CLVFWhileStatement;
import org.jetel.interpreter.ASTnode.SimpleNode;

public interface TransformLangParserVisitor
{
  public Object visit(SimpleNode node, Object data);
  public Object visit(CLVFStart node, Object data);
  public Object visit(CLVFStartExpression node, Object data);
  public Object visit(CLVFImportSource node, Object data);
  public Object visit(CLVFFunctionDeclaration node, Object data);
  public Object visit(CLVFVarDeclaration node, Object data);
  public Object visit(CLVFAssignment node, Object data);
  public Object visit(CLVFMapping node, Object data);
  public Object visit(CLVFOr node, Object data);
  public Object visit(CLVFAnd node, Object data);
  public Object visit(CLVFComparison node, Object data);
  public Object visit(CLVFAddNode node, Object data);
  public Object visit(CLVFSubNode node, Object data);
  public Object visit(CLVFMulNode node, Object data);
  public Object visit(CLVFDivNode node, Object data);
  public Object visit(CLVFModNode node, Object data);
  public Object visit(CLVFOperator node, Object data);
  public Object visit(CLVFPostfixExpression node, Object data);
  public Object visit(CLVFUnaryExpression node, Object data);
  public Object visit(CLVFLiteral node, Object data);
  public Object visit(CLVFInputFieldLiteral node, Object data);
  public Object visit(CLVFVariableLiteral node, Object data);
  public Object visit(CLVFRegexLiteral node, Object data);
  public Object visit(CLVFSymbolNameExp node, Object data);
  public Object visit(CLVFBlock node, Object data);
  public Object visit(CLVFIncrDecrStatement node, Object data);
  public Object visit(CLVFIfStatement node, Object data);
  public Object visit(CLVFSwitchStatement node, Object data);
  public Object visit(CLVFCaseExpression node, Object data);
  public Object visit(CLVFWhileStatement node, Object data);
  public Object visit(CLVFForStatement node, Object data);
  public Object visit(CLVFForeachStatement node, Object data);
  public Object visit(CLVFDoStatement node, Object data);
  public Object visit(CLVFTryCatchStatement node, Object data);
  public Object visit(CLVFBreakStatement node, Object data);
  public Object visit(CLVFContinueStatement node, Object data);
  public Object visit(CLVFReturnStatement node, Object data);
  public Object visit(CLVFFunctionCallStatement node, Object data);
  public Object visit(CLVFIsNullNode node, Object data);
  public Object visit(CLVFNVLNode node, Object data);
  public Object visit(CLVFNVL2Node node, Object data);
  public Object visit(CLVFIffNode node, Object data);
  public Object visit(CLVFPrintStackNode node, Object data);
  public Object visit(CLVFBreakpointNode node, Object data);
  public Object visit(CLVFRaiseErrorNode node, Object data);
  public Object visit(CLVFPrintErrNode node, Object data);
  public Object visit(CLVFEvalNode node, Object data);
  public Object visit(CLVFPrintLogNode node, Object data);
  public Object visit(CLVFSequenceNode node, Object data);
  public Object visit(CLVFLookupNode node, Object data);
}
