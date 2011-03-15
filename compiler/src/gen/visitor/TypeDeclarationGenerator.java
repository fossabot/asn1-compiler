package gen.visitor;

import gen.*;
import gen.utils.*;
import parser.*;

public class TypeDeclarationGenerator extends DoNothingASTVisitor implements Generator {

   private ASTTypeAssignment node = null;
   private CodeBuilder builder = new CodeBuilder();

   public TypeDeclarationGenerator(final ASTTypeAssignment node) {
      this.node = node;
   }

   public void generate(final GeneratorContext context) {
      final String typeName = node.getFirstToken().toString();
      context.setTypeName(typeName);

      // mark type as already generated
      context.addGeneratedType(typeName);

      // C++ class declaration
      builder.append("// TypeAssignment for ASN.1 type: ").append(typeName).newLine();
      builder.append("class ").append(GenerationUtils.asCPPToken(typeName)).append(" : public ");

      // base type of the class
      final CodeBuilder baseClassName = new CodeBuilder();
      if (VisitorUtils.visitChildsAndAccept(baseClassName, node, new SimpleTypeName(),
              new SetOfOrSequenceOfTypeName(context), new TaggedTypeName(context),
              new DefinedTypeName(), new SetOrSequenceTypeName())) {

         if (!context.isTypeGenerated(baseClassName.toString(), node)) {
            //context.queueGenerator(this);

            context.addDepForType(typeName, baseClassName.toString());
         }

         builder.append(baseClassName.toString());
      }

      // body of the class
      builder.newLine();
      builder.append("{").newLine();
      builder.append("public:").newLine().newLine();
      builder.append(1, "explicit ").append(GenerationUtils.asCPPToken(typeName)).append("()");

      /* check whether it is needed to add " : public " */
      VisitorUtils.visitChildsAndAccept(builder, node, new AddColonIfNeeded());

      VisitorUtils.visitChildsAndAccept(builder, node,
              new SetOfOrSequenceOfConstructorDeclaration(context));
      VisitorUtils.visitChildsAndAccept(builder, node,
              new TaggedTypeConstructorDeclaration(context));

      // constructor body
      builder.newLine();
      builder.append(1, "{").newLine();

      VisitorUtils.visitChildsAndAccept(builder, node, new SimpleTypeConstructorDefinition(context));
      VisitorUtils.visitChildsAndAccept(builder, node, new TaggedTypeConstructorDefinition(context));
      VisitorUtils.visitChildsAndAccept(builder, node, new SetOfOrSequenceOfConstructorDefinition());

      builder.append(1, "}").newLine();

      // body of the class
      VisitorUtils.visitChildsAndAccept(builder, node, new NamedIntegerValueType(typeName));
      VisitorUtils.visitChildsAndAccept(builder, node, new SetOrSequenceTypeBody(context));

      builder.newLine();
      builder.append("};").newLine().newLine();
   }

   public String getContent() {
      return builder.toString();
   }

   public boolean hasValuableContent() {
      return true;
   }

   public String typeName() {
      return node.getFirstToken().toString();
   }
}