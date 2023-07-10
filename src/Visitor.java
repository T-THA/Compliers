import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;

import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.tree.RuleNode;

public class Visitor extends SysYParserBaseVisitor<Void>{

    public Frame map = new Frame();
    public boolean stop = false;
    private int depth = 0;
    public void setdepth(int x){ depth = x;}
    public int getdepth(){return depth;}

    public String treeMsg = ""; //语法树打印信息
    public boolean typeErrorFlag = false; // 类型检查错误标志位

    public void errorAlert(int type, int lineNo){
        typeErrorFlag = true;
        // if(type == 6)
        System.err.println("Error type " + type + " at Line " +  lineNo + ": error");
    }

    @Override
    public Void visitChildren(RuleNode node){
        // 存储语法树内容
        String[] rulename = SysYParser.ruleNames;
        String display = rulename[node.getRuleContext().getRuleIndex()];
        treeMsg = treeMsg + "  ".repeat(depth) + display.substring(0, 1).toUpperCase() + display.substring(1) + "\n";
        return null;
    }


    // 访问终结符
    @Override
    public Void visitTerminal(TerminalNode node){
        // 存储语法树内容
        Vocabulary vocabulary = SysYParser.VOCABULARY;
        int typenum = node.getSymbol().getType();
        String highlight = "";
        if(typenum >= 1 && typenum <= 9) highlight = "orange";
        else if(typenum >= 10 && typenum <=24) highlight = "blue";
        else if(typenum == 33) highlight = "red";
        else if(typenum == 34) highlight = "green";
        if(typenum == -1 || (typenum >=25 && typenum <=32)){
            return null;
        // if(typenum == -1 || (typenum >=30 && typenum <=32)){
        //     return null;
        }else{
            String typename = vocabulary.getSymbolicName(typenum);
            if(typenum == 34){
                treeMsg = treeMsg + "  ".repeat(depth);
                String ret = node.getText();
                if(ret.charAt(0) == '0' && ret.length() > 1 &&
                    ret.charAt(1) != 'x' && ret.charAt(1) != 'X'){
                        treeMsg = treeMsg + Long.parseLong(ret.substring(1), 8);
                    }
                else if(ret.length() > 2 && 
                (ret.substring(0,2).equals("0x") || ret.substring(0,2).equals("0X"))){
                        treeMsg = treeMsg + Long.parseLong(ret.substring(2), 16);
                }else{
                        treeMsg = treeMsg + ret;
                }
                treeMsg = treeMsg + " "+ typename + "[" + highlight +"]\n";
            }else{
                treeMsg = treeMsg + "  ".repeat(depth) + node.getText() + " "+ typename + "[" + highlight +"]\n";
            }
            return null;
        }
    }
	
	@Override 
    public Void visitDecl(SysYParser.DeclContext ctx) { 
        if(depth == 2){
            while(map.depth >= depth) map = map.parent;
        }
        return visitChildren(ctx); 
    }
	
	@Override 
    public Void visitConstDef(SysYParser.ConstDefContext ctx) {
        String name = ctx.IDENT().getText();
        if(ctx.IDENT() != null){
            if(map.containsKeyLocal(name)){
                errorAlert(3, ctx.getStart().getLine());
            }else{
                if(ctx.L_BRACKT().size() != 0){
                    Array array = new Array();
                    array.size = ctx.L_BRACKT().size();
                    map.put(name, array);
                }else{
                    map.put(name, new Int());
                }
            }
        }
        if(ctx.ASSIGN() != null && ctx.constInitVal().constExp() != null){
            Type type1 = getExpType(ctx.constInitVal().constExp().exp()), type2 = map.get(name);
            if(!type1.equals(type2)){
                if(!(type1 instanceof Other)){
                    errorAlert(5, ctx.getStart().getLine());
                }
            }
        }
        return visitChildren(ctx); 
    }
	
	
	@Override 
    public Void visitVarDef(SysYParser.VarDefContext ctx) { 
        String name = ctx.IDENT().getText();
        if(ctx.IDENT() != null){
            if(map.containsKeyLocal(name)){
                errorAlert(3, ctx.getStart().getLine());
            }else{
                if(ctx.L_BRACKT().size() != 0){
                    Array array = new Array();
                    array.size = ctx.L_BRACKT().size();
                    map.put(name, array);
                }else{
                    map.put(name, new Int());
                }
            }
        }if(ctx.ASSIGN() != null && ctx.initVal().exp() != null){
            Type type1 = getExpType(ctx.initVal().exp()), type2 = map.get(name);
            if(!type1.equals(type2)){
                if(!(type1 instanceof Other)){
                    errorAlert(5, ctx.getStart().getLine());
                }
            }
        }
        return visitChildren(ctx); 
    }
	
	
	@Override 
    public Void visitFuncDef(SysYParser.FuncDefContext ctx) { 
        while(map.depth >= depth) map = map.parent;

        if(map.containsKeyLocal(ctx.IDENT().getText())){
            errorAlert(4, ctx.getStart().getLine());
            stop  =  true;
            return null;
        }
        Function function = new Function();
        function.retType = ctx.funcType().getText();
        Frame frame = new Frame(map, depth, ctx.funcType().getText());
        map = frame;
        
        function.eleType = new ArrayList<Type>();
        if(ctx.funcFParams() != null){
            for(SysYParser.FuncFParamContext i: ctx.funcFParams().funcFParam()){

                if(i.IDENT() != null){
                    if(map.containsKeyLocal(i.IDENT().getText())){
                        errorAlert(3, i.getStart().getLine());
                    }else{
                        if(i.L_BRACKT().size() != 0){
                            Array array = new Array();
                            array.size = i.L_BRACKT().size();
                            map.put(i.IDENT().getText(), array);
                            function.eleType.add(array);
                        }else{
                            Int int1 = new Int();
                            function.eleType.add(int1);
                            map.put(i.IDENT().getText(), new Int());
                        }
                    }
                } 
                
            }
        }
        map.parent.put(ctx.IDENT().getText(), function);

        return visitChildren(ctx); 
    }
	
	@Override 
    public Void visitStmt(SysYParser.StmtContext ctx) { 
        while(map.depth >= depth) map = map.parent;
        if(ctx.block() != null){ // block          
            Frame frame = new Frame(map, depth, map.retType);
            map = frame;
        }
        if(ctx.ASSIGN() != null){ // lVal ASSIGN exp SEMICOLON
            if(map.containsKey(ctx.lVal().IDENT().getText()) &&
            ((map.get(ctx.lVal().IDENT().getText())) instanceof Function)){
                if(ctx.lVal().L_BRACKT().size() == 0){
                    errorAlert(11, ctx.lVal().getStart().getLine());
                }
            }
            else{
                if(map.get(ctx.lVal().IDENT().getText()) != null){
                    Type type1 = getExpType(ctx.exp()), type2 = map.get(ctx.lVal().IDENT().getText());
                    int dimension = (type2 instanceof Array) ? ((Array)type2).size - ctx.lVal().L_BRACKT().size(): 0 - ctx.lVal().L_BRACKT().size();
                    if(dimension > 0)
                        type2 = new Array(dimension);
                    else if(dimension==0)
                        type2 = new Int();
                    else
                        type2 = new Other(0);
                    if(!(type1 instanceof Other) && !(type2 instanceof Other)&& !type1.equals(type2)){
                        errorAlert(5, ctx.exp().getStart().getLine());
                    }
                }
            
            }
        }
        if(ctx.RETURN() != null){
            String retType = map.retType;
            if(ctx.exp() == null){
                if(!retType.equals("void")){
                    errorAlert(7, ctx.getStart().getLine());
                }
            }else{
                if((!(getExpType(ctx.exp()) instanceof Other)) 
                && (!(getExpType(ctx.exp()) instanceof Int  ))){
                    errorAlert(7, ctx.getStart().getLine());
                }else{
                    if(!retType.equals("int")){
                        errorAlert(7, ctx.getStart().getLine());
                    } 
                }
            }
        }
        return visitChildren(ctx); 
    }
	
	@Override 
    public Void visitExp(SysYParser.ExpContext ctx) { 
        if(ctx.IDENT() != null){
            String name = ctx.IDENT().getText();
            if(!map.containsKey(name)){ // IDENT L_PAREN funcRParams? R_PAREN 
                errorAlert(2, ctx.getStart().getLine());
            }else{
                if(map.get(name) instanceof Int || map.get(name) instanceof Array){
                    errorAlert(10, ctx.getStart().getLine());
                }else{
                    Boolean ErrorFlag = true;
                    Function tmp = new Function();
                    ArrayList<Type> tmpType = new ArrayList<Type>();
                    tmp.retType = ((Function)map.get(name)).retType;
                    if(ctx.funcRParams() != null){
                        for(SysYParser.ParamContext i: ctx.funcRParams().param()){
                            tmpType.add(getExpType(i.exp()));
                        }
                    }
                    tmp.eleType = tmpType;
                    for(Type i : tmpType){
                        if(i instanceof Other){
                            ErrorFlag = false;
                        }
                    }
                    if(!tmp.equals((Function)map.get(name)) && ErrorFlag){
                        errorAlert(8, ctx.getStart().getLine());
                    }
                }
            } 
        }else if(ctx.DIV() != null || ctx.MUL() != null ||
            ctx.MOD() != null || ctx.PLUS() != null || ctx.MINUS() != null){
                Type type1 = getExpType(ctx.exp(0));
                Type type2 = getExpType(ctx.exp(1));
                if(!(type1 instanceof Int) && !(type1 instanceof Other)){
                    errorAlert(6, ctx.getStart().getLine());
                }
                else if(!(type2 instanceof Int) && !(type2 instanceof Other)){
                    errorAlert(6, ctx.getStart().getLine());
                }

        }else if(ctx.unaryOp() != null){
            if(!(getExpType(ctx.exp(0)) instanceof Int) && !(getExpType(ctx.exp(0)) instanceof Other)){
                errorAlert(6, ctx.getStart().getLine());
            }
        }
        return visitChildren(ctx); 
    }

    public Type getExpType(SysYParser.ExpContext ctx){
        if(ctx.IDENT() != null){
            if( map.get(ctx.IDENT().getText()) != null &&
                map.get(ctx.IDENT().getText()) instanceof Function){
                    Function func1 = (Function) map.get(ctx.IDENT().getText());
                if(func1.retType.equals("int")){
                    return new Int();
                }
                else{
                    return new Function("void");
                }
            }
            else{
                if(map.get(ctx.IDENT().getText()) == null){
                    return new Other(0); // 未定义
                }else{
                    return new Other(0);  // 对变量使用函数调用
                }
            }
        }else if(ctx.L_PAREN() !=null){
            return getExpType(ctx.exp(0));
        }else if(ctx.lVal() != null){
            if(map.get(ctx.lVal().IDENT().getText()) == null) {
                return new Other(0); // 未定义
            }else if(map.get(ctx.lVal().IDENT().getText()) instanceof Array){
                int size = ((Array)map.get(ctx.lVal().IDENT().getText())).size - ctx.lVal().L_BRACKT().size();
                if(size < 0){
                    return new Other(0);
                }else if(size == 0){
                    return new Int();
                }else{
                    return new Array(size);
                }
            }
            else if(map.get(ctx.lVal().IDENT().getText()) instanceof Function && ctx.lVal().L_BRACKT().size() > 0){
                return new Other(0);
            }
            return map.get(ctx.lVal().IDENT().getText());
        }
        return new Int();
    }
	
	@Override 
    public Void visitCond(SysYParser.CondContext ctx) { 
        // if(ctx.EQ() != null || ctx.NEQ() != null || ctx.LT() != null ||
        //    ctx.GT() != null || ctx.LE()  != null || ctx.GE() != null){
        if(ctx.exp() == null){
            Type type1 = getCondType(ctx.cond(0));
            Type type2 = getCondType(ctx.cond(1));
            if(!(type1 instanceof Int) && !(type1 instanceof Other))
                errorAlert(6, ctx.getStart().getLine());
            else if(!(type2 instanceof Int)&& !(type2 instanceof Other))
                errorAlert(6, ctx.getStart().getLine());
        }
        return visitChildren(ctx); 
    }

    public Type getCondType(SysYParser.CondContext ctx){
        if(ctx.exp() != null){
            return getExpType(ctx.exp());
        }
        return new Int();
    }
	
	@Override 
    public Void visitLVal(SysYParser.LValContext ctx) { 
        String name = ctx.IDENT().getText();
        if(!map.containsKey(name)){
            errorAlert(1, ctx.getStart().getLine());
        }else{
            if(ctx.L_BRACKT().size() != 0){
                if(map.get(name) instanceof Int || map.get(name) instanceof Function || 
                (map.get(name) instanceof Array && (((Array)map.get(name)).size < ctx.L_BRACKT().size()))){
                    errorAlert(9, ctx.getStart().getLine());
                }
            }
        }
        return visitChildren(ctx); 
    }
	
}
