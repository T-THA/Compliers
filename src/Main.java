// package src;
import java.io.IOException;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.bytedeco.javacpp.BytePointer;

import org.bytedeco.llvm.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.*;

public class Main
{    
    private static boolean antlr_error = false;
    private static BytePointer llvm_error = new BytePointer();
    public static void main(String[] args) throws IOException {
        DoLLVM(args);
        // DoAntlr(args);
    }

    public static void DoLLVM(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("args error");
        }else{
            String source = args[0];
            CharStream input = CharStreams.fromFileName(source);
            SysYLexer sysYLexer = new SysYLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(sysYLexer);
            SysYParser sysYParser = new SysYParser(tokens);
            ParseTree tree = sysYParser.program();

            LLVMVisitor visitor = new LLVMVisitor(
                LLVMModuleCreateWithName("module"), 
                LLVMCreateBuilder(), LLVMInt32Type() );

            visitor.visit(tree);
            if (LLVMPrintModuleToFile(visitor.module, args[1], llvm_error) != 0) {    
                LLVMDisposeMessage(llvm_error);
            }
        }
    }

    public static void DoAntlr(String[] args) throws IOException {
        if (args.length < 1) {
                System.err.println("input path is required");
            }else{
    
            String source = args[0];
            CharStream input = CharStreams.fromFileName(source);
            SysYLexer sysYLexer = new SysYLexer(input);
    
            CommonTokenStream tokens = new CommonTokenStream(sysYLexer);
            SysYParser sysYParser = new SysYParser(tokens);
            
            sysYParser.removeErrorListeners();
            sysYParser.addErrorListener(new BaseErrorListener(){
                @Override
                public void syntaxError(org.antlr.v4.runtime.Recognizer<?,?> recognizer, java.lang.Object offendingSymbol, int line, int charPositionInLine, java.lang.String msg, org.antlr.v4.runtime.RecognitionException e) {
                    antlr_error = true;
                    System.err.println("Error type B at Line " + line + ": mismatched input");
                }
            });
            ParseTree tree = sysYParser.program();
            Visitor visitor = new Visitor();
            if(!antlr_error){
                visitor.visit(tree);
                help(tree.getChild(0), visitor, 1);
                if(!visitor.typeErrorFlag) System.err.print(visitor.treeMsg);
            }
        }
    }

    public static void help(ParseTree tree, Visitor visitor, int depth){
        visitor.setdepth(depth);
        visitor.visit(tree);
        
        if(tree.getChildCount() != 0 && !visitor.stop){
            for(int i = 0; i < tree.getChildCount(); i++) help(tree.getChild(i), visitor, depth+1);
        }
        visitor.stop = false;
    }
}
