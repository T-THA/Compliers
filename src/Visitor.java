import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.tree.RuleNode;

public class Visitor extends SysYParserBaseVisitor<Void>{

    private int depth = 0;
    public void setdepth(int x){
        depth = x;
    }
    public int getdepth(){
        return depth;
    }

    @Override
    public Void visitChildren(RuleNode node){
        String[] rulename = SysYParser.ruleNames;
        String display = rulename[node.getRuleContext().getRuleIndex()];
        System.err.println("  ".repeat(depth)
                    + display.substring(0, 1).toUpperCase()
                    + display.substring(1));
        return null;
    }

    @Override
    public Void visitTerminal(TerminalNode node){
        Vocabulary vocabulary = SysYParser.VOCABULARY;
        int typenum = node.getSymbol().getType();
        String highlight = "";
        if(typenum >= 1 && typenum <= 9) highlight = "orange";
        else if(typenum >= 10 && typenum <=24) highlight = "blue";
        else if(typenum == 33) highlight = "red";
        else if(typenum == 34) highlight = "green";
        if(typenum == -1 || (typenum >=25 && typenum <=32)){
            return null;
        }else{
            String typename = vocabulary.getSymbolicName(typenum);
            if(typenum == 34){
                System.err.print("  ".repeat(depth));
                String ret = node.getText();
                if(ret.charAt(0) == '0' && ret.length() > 1 &&
                    ret.charAt(1) != 'x' && ret.charAt(1) != 'X'){
                        System.err.print(Long.parseLong(ret.substring(1), 8));
                    }
                else if(ret.length() > 2 && 
                (ret.substring(0,2).equals("0x") || ret.substring(0,2).equals("0X"))){
                        System.err.print(Long.parseLong(ret.substring(2), 16));
                }else{
                        System.err.print(ret);
                }
                System.err.println(" "+ typename + "[" + highlight +"]");
            }else{
                System.err.println("  ".repeat(depth) + 
                node.getText() + " "+ typename + 
                "[" + highlight +"]");
            }
            return null;
        }
    }
}
