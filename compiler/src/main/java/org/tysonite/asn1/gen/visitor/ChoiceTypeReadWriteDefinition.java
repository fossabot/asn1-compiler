package org.tysonite.asn1.gen.visitor;

import org.tysonite.asn1.gen.ContentProvider;
import org.tysonite.asn1.gen.DoNothingASTVisitor;
import org.tysonite.asn1.gen.GeneratorContext;
import org.tysonite.asn1.gen.utils.CodeBuilder;
import org.tysonite.asn1.gen.utils.GenerationUtils;
import org.tysonite.asn1.gen.utils.UniqueNameProducer;
import org.tysonite.asn1.gen.utils.VisitorUtils;
import org.tysonite.asn1.parser.ASTBuiltinType;
import org.tysonite.asn1.parser.ASTChoiceType;
import org.tysonite.asn1.parser.ASTElementType;
import org.tysonite.asn1.parser.ASTTaggedType;

public class ChoiceTypeReadWriteDefinition extends DoNothingASTVisitor implements ContentProvider,
        ConstantsForGeneration {

   private final CodeBuilder builder = new CodeBuilder();
   private final GeneratorContext context;
   private boolean isFirstWriteIf = true;
   private boolean isFirstAssertOr = true;
   private boolean isFirstReadIf = true;
   private boolean isFirstComma = true;

   public ChoiceTypeReadWriteDefinition(final GeneratorContext context) {
      this.context = context;
   }

   protected class TypeList extends DoNothingASTVisitor implements ContentProvider {

      private final CodeBuilder builder = new CodeBuilder();

      @Override
      public Object visit(ASTElementType node, Object data) {
         if (isFirstComma) {
            isFirstComma = false;
         } else {
            builder.append(" + \", \"");
         }

         builder.append(" + ").append("_").
                 append(GenerationUtils.asCPPToken(node.jjtGetFirstToken().toString())).
                 append("_Type.toString()");

         return data;
      }

      public String getContent() {
         return builder.toString();
      }

      public boolean hasValuableContent() {
         return true;
      }
   }

   protected class ReadDefinition extends DoNothingASTVisitor implements ContentProvider {

      private final CodeBuilder builder = new CodeBuilder();

      @Override
      public Object visit(ASTElementType node, Object data) {
         if (isFirstReadIf) {
            builder.append(1, "");
            isFirstReadIf = false;
         } else {
            builder.append(1, "else ");
         }

         builder.append("if (choosenType == &_").
                 append(GenerationUtils.asCPPToken(node.jjtGetFirstToken().toString())).
                 append("_Type)").newLine();

         VisitorUtils.visitNodeAndAccept(builder, node,
                 new SetOrSequenceReadWriteDefinition.ReadDefinition(context));
         return data;
      }

      public String getContent() {
         return builder.toString();
      }

      public boolean hasValuableContent() {
         return true;
      }
   }

   protected class AssertsForWriteDefinition extends DoNothingASTVisitor
           implements ContentProvider {

      private final CodeBuilder builder = new CodeBuilder();

      public AssertsForWriteDefinition() {
         builder.append(1, "assert(");
      }

      @Override
      public Object visit(ASTElementType node, Object data) {
         if (isFirstAssertOr) {
            builder.append("");
            isFirstAssertOr = false;
         } else {
            builder.append(" || ");
         }

         builder.append("value.has_").
                 append(GenerationUtils.asCPPToken(node.jjtGetFirstToken().toString())).
                 append("_Choosen()");

         return data;
      }

      public String getContent() {
         return builder.append(");").newLine().toString();
      }

      public boolean hasValuableContent() {
         return true;
      }
   }

   protected class WriteDefinition extends DoNothingASTVisitor implements ContentProvider {

      private final CodeBuilder builder = new CodeBuilder();

      @Override
      public Object visit(ASTElementType node, Object data) {
         if (isFirstWriteIf) {
            builder.append(1, "");
            isFirstWriteIf = false;
         } else {
            builder.append(1, "else ");
         }

         builder.append("if (value.has_").
                 append(GenerationUtils.asCPPToken(node.jjtGetFirstToken().toString())).
                 append("_Choosen())").newLine();

         // write code for type reading
         builder.append(2, "_").append(GenerationUtils.asCPPToken(node.jjtGetFirstToken().toString())).
                 append("_Type").append(".write(writer, value.get_").
                 append(GenerationUtils.asCPPToken(node.jjtGetFirstToken().toString())).
                 append("());").newLine();

         return data;
      }

      public String getContent() {
         return builder.toString();
      }

      public boolean hasValuableContent() {
         return true;
      }
   }

   @Override
   public Object visit(ASTBuiltinType node, Object data) {
      return node.childrenAccept(this, data);
   }

   @Override
   public Object visit(ASTTaggedType node, Object data) {
      if (!VisitorUtils.visitChildsAndAccept(null, node, new SimpleTypeName(context),
              new DefinedCPPTypeName())) {
         final CodeBuilder uniqueName = new CodeBuilder();
         VisitorUtils.visitChildsAndAccept(uniqueName, node, new UniqueNameProducer(context));
         if (context.hasExternalized(uniqueName.toString())) {
            // TODO: hack, need to understand (I don't remember) why declaration generator generates
            // also methods definitions. See queueGeneratedCode method.
            return null;
         }

         context.setTypeName(uniqueName.toString());
      }

      return node.childrenAccept(this, data);
   }

   @Override
   public Object visit(ASTChoiceType node, Object data) {
      builder.newLine();
      builder.append("void ").append(GenerationUtils.asCPPToken(context.getTypeName())).
              append("::read(ASN1ValueReader& reader, ").append("ValueType").
              append("& value) const").newLine();
      builder.append("{").newLine();
      builder.append(1, asn1NameSpace).append("Type* choosenType = nullptr;").newLine();
      builder.append(1, "reader.readChoice(*this, &choosenType);").newLine();
      builder.newLine();

      VisitorUtils.visitChildsAndAccept(builder, node, new ReadDefinition());

      builder.append(1, "else").newLine();
      builder.append(1, "{").newLine();
      builder.append(2, "throw ASN1Exception(\"Expected \" + toString() + \" must be one of: \"").
              newLine();
      builder.append(3, "");
      VisitorUtils.visitChildsAndAccept(builder, node, new TypeList());
      builder.append(");").newLine();
      builder.append(1, "}").newLine();

      builder.append("}").newLine();
      builder.newLine();

      builder.append("void ").append(GenerationUtils.asCPPToken(context.getTypeName())).
              append("::write(ASN1ValueWriter& writer, const ").append("ValueType").
              append("& value) const").newLine();
      builder.append("{").newLine();

      VisitorUtils.visitChildsAndAccept(builder, node, new AssertsForWriteDefinition());
      builder.newLine();

      builder.append(1, "writer.writeChoiceBegin(*this);").newLine();

      builder.newLine();
      VisitorUtils.visitChildsAndAccept(builder, node, new WriteDefinition());
      builder.newLine();

      builder.append(1, "writer.writeChoiceEnd(*this);").newLine();
      builder.append("}").newLine();

      builder.newLine();
      return node.childrenAccept(this, data);
   }

   public String getContent() {
      return builder.toString();
   }

   public boolean hasValuableContent() {
      return true;
   }
}
