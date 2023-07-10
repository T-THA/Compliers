// package src;
import java.io.IOException;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class Main
{    
    private static boolean error = false;
    public static void main(String[] args) throws IOException {
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
                // Error type B at Line [lineNo]:[errorMessage]
                error = true;
                System.err.println("Error type B at Line " + line + ": mismatched input");
            }
        });
        ParseTree tree = sysYParser.program();
        // SysYParserBaseVisitor visitor = new SysYParserBaseVisitor();
        Visitor visitor = new Visitor();
        if(!error){
            visitor.visit(tree);
            help(tree.getChild(0), visitor, 1);
            if(!visitor.typeErrorFlag) System.err.print(visitor.treeMsg);
            // System.err.print(visitor.treeMsg);
        }
        }
    }

    public static void help(ParseTree tree, Visitor visitor, int depth){
        visitor.setdepth(depth);
        visitor.visit(tree);
        
        if(tree.getChildCount() != 0 && !visitor.stop){
            for(int i = 0; i < tree.getChildCount(); i++){
                help(tree.getChild(i), visitor, depth+1);
            }
        }
        visitor.stop = false;
    }
    
}
