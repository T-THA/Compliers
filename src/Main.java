// package src;
import java.io.IOException;

import org.antlr.runtime.RecognitionException;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Vocabulary;


public class Main
{    
    private static boolean error = false;
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("input path is required");
        }

        String source = args[0];
        CharStream input = CharStreams.fromFileName(source);
        SysYLexer sysYLexer = new SysYLexer(input);
        sysYLexer.removeErrorListeners();
        
        sysYLexer.addErrorListener(new BaseErrorListener(){
            @Override
            public  void syntaxError(org.antlr.v4.runtime.Recognizer<?,?> recognizer, 
                java.lang.Object offendingSymbol, 
                int line, 
                int charPositionInLine, 
                java.lang.String msg, 
                org.antlr.v4.runtime.RecognitionException e) {

                // Error type A at Line [lineNo]:[errorMessage]
                error = true;
                System.err.println("Error type A at Line " + line + ": Mysterious character");
    }
        });
        Vocabulary vocabulary = sysYLexer.getVocabulary();
        
        for(var i : sysYLexer.getAllTokens()){
            if(!error){
                System.err.print(vocabulary.getSymbolicName(i.getType()) + " ");

                if(vocabulary.getSymbolicName(i.getType()).equals("INTEGER_CONST")){
                    if(i.getText().charAt(0) == '0' && i.getText().length() > 1 &&
                    i.getText().charAt(1) != 'x' && i.getText().charAt(1) != 'X'){
                        System.err.print(Long.parseLong(i.getText().substring(1), 8));
                    }
                    else if(i.getText().length() > 2 && 
                    (i.getText().substring(0,2).equals("0x") || i.getText().substring(0,2).equals("0X"))){
                        System.err.print(Long.parseLong(i.getText().substring(2), 16));
                    }else{
                        System.err.print(i.getText());
                    }
                }else{
                    System.err.print(i.getText());
                }

                System.err.println(" at Line " + i.getLine() + ".");
            }
        }

    }
}
