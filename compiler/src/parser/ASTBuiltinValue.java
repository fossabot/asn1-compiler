/* Generated By:JJTree: Do not edit this line. ASTBuiltinValue.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package parser;

public
class ASTBuiltinValue extends SimpleNode {
  public ASTBuiltinValue(int id) {
    super(id);
  }

  public ASTBuiltinValue(AsnParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(AsnParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=71a1c21317ab7dbaddf7cd709ebd4b9a (do not edit this line) */