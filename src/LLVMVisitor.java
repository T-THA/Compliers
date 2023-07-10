import org.bytedeco.llvm.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;


public class LLVMVisitor extends SysYParserBaseVisitor<LLVMValueRef>{
    
    public LLVMModuleRef module;
    public LLVMBuilderRef builder;
    public LLVMTypeRef i32Type;
    public LLVMFrame<Integer> map;
    public HashMap<String, LLVMFunction> funcMap;
    public LLVMValueRef zero;
    public LLVMTypeRef voidType = LLVMVoidType();

    public String funcName = null; // 目前block的函数名字
    public boolean continueFlag = false;
    public boolean breakFlag = false;
    public LLVMVisitor(){
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget();

        module = LLVMModuleCreateWithName("module");
        builder = LLVMCreateBuilder();
        i32Type = LLVMInt32Type();
        map = new LLVMFrame<Integer>();
        funcMap = new HashMap<String, LLVMFunction>();
        zero = LLVMConstInt(i32Type, 0, 0);
    }

    public LLVMVisitor(LLVMModuleRef module_, LLVMBuilderRef builder_, LLVMTypeRef i32Type_){
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget();

        module = module_;
        builder = builder_;
        i32Type = i32Type_;
        map = new LLVMFrame<Integer>();
        funcMap = new HashMap<String, LLVMFunction>();
        zero = LLVMConstInt(i32Type, 0, 0);
    }

    @Override 
    public LLVMValueRef visitDecl(SysYParser.DeclContext ctx){
        // TODO
        if(ctx.parent instanceof SysYParser.CompUnitContext){ // 全局变量
            if(ctx.constDecl() != null){
                for(SysYParser.ConstDefContext i : ctx.constDecl().constDef()){
                    LLVMValueRef globalVar = LLVMAddGlobal(module, i32Type, i.IDENT().getText());
                    LLVMSetInitializer(globalVar, LLVMConstInt(i32Type, getExpValue(i.constInitVal().constExp().exp()), 0));
                    map.put(i.IDENT().getText(), getExpValue(i.constInitVal().constExp().exp()));
                }
            }else{
                for(SysYParser.VarDefContext i : ctx.varDecl().varDef()){
                    LLVMValueRef globalVar = LLVMAddGlobal(module, i32Type, i.IDENT().getText());
                    if(i.ASSIGN() != null){
                        LLVMSetInitializer(globalVar, LLVMConstInt(i32Type, getExpValue(i.initVal().exp()), 0));
                        map.put(i.IDENT().getText(), getExpValue(i.initVal().exp()));
                    }else{
                        LLVMSetInitializer(globalVar, zero);
                        map.put(i.IDENT().getText(), 0);
                    }
                    
                    
                }
            } 
        }else{ // 局部
            if(ctx.constDecl() != null){
                for(SysYParser.ConstDefContext i : ctx.constDecl().constDef()){
                    LLVMValueRef pointer = LLVMBuildAlloca(builder, i32Type, i.IDENT().getText());
                    LLVMBuildStore(builder, LLVMConstInt(i32Type, getExpValue(i.constInitVal().constExp().exp()), 0), pointer);
                    map.put(i.IDENT().getText(), getExpValue(i.constInitVal().constExp().exp()));
                }
            }else{
                for(SysYParser.VarDefContext i : ctx.varDecl().varDef()){
                    LLVMValueRef pointer = LLVMBuildAlloca(builder, i32Type, i.IDENT().getText());
                    if(i.ASSIGN() != null){
                        LLVMBuildStore(builder, LLVMConstInt(i32Type, getExpValue(i.initVal().exp()), 0), pointer);
                        map.put(i.IDENT().getText(), getExpValue(i.initVal().exp()));
                    }else{
                        LLVMBuildStore(builder, zero, pointer);
                        map.put(i.IDENT().getText(), 0);
                    }
                    
                    
                }
            }
        }
        super.visitChildren(ctx);
        return null;
    }

    @Override
    public LLVMValueRef visitFuncDef(SysYParser.FuncDefContext ctx){
        if(ctx.IDENT().getText().equals("main")){
            LLVMTypeRef returnType = i32Type;
            PointerPointer<Pointer> argumentTypes = new PointerPointer<>(0);
            LLVMTypeRef ft = LLVMFunctionType(returnType, argumentTypes, 0, 0);
            LLVMValueRef function = LLVMAddFunction(module, ctx.IDENT().getText(), ft);
            LLVMBasicBlockRef block1 = LLVMAppendBasicBlock(function, ctx.IDENT().getText() + "Entry");
            LLVMPositionBuilderAtEnd(builder, block1);
            LLVMFrame<Integer> localMap = new LLVMFrame<Integer>(map);
            map = localMap;
            String tmpname = funcName;
            funcName = "main";
            super.visitChildren(ctx);
            funcName = tmpname;
            map = map.parent;
            return null;
        }else{
            // LLVMTypeRef returnType = ctx.funcType().INT() == null ? voidType : i32Type;
            int size = ctx.funcFParams() == null ? 0 : ctx.funcFParams().funcFParam().size();
            // PointerPointer<Pointer> argumentTypes = new PointerPointer<>(size);
            // LLVMTypeRef ft = LLVMFunctionType(returnType, argumentTypes, 0, 0);
            // LLVMValueRef function = LLVMAddFunction(module, ctx.IDENT().getText(), ft);
            // LLVMBasicBlockRef block1 = LLVMAppendBasicBlock(function, ctx.IDENT().getText() + "Entry");
            // LLVMPositionBuilderAtEnd(builder, block1);

            // TODO
            LLVMFrame<Integer> localMap = new LLVMFrame<Integer>(map);
            ArrayList<String> funcParams = new ArrayList<String>();
            for(int i = 0; i < size; i++){
                String paramName = ctx.funcFParams().funcFParam(i).IDENT().getText();
                localMap.put(paramName, 0);
                funcParams.add(paramName);
            }
            LLVMFunction func = new LLVMFunction(ctx.block(), localMap, ctx.IDENT().getText(), funcParams);
            funcMap.put(ctx.IDENT().getText(), func);
            return null;
        }
        
    }

    @Override 
    public LLVMValueRef visitStmt(SysYParser.StmtContext ctx){
        if(!continueFlag){


        if(ctx.RETURN() != null){
            if(ctx.exp() != null){
                int retValue = getExpValue(ctx.exp());
                LLVMValueRef result = LLVMConstInt(i32Type, retValue, 0);
                
                if(funcName.equals("main")){
                    LLVMBuildRet(builder, result);
                }
                else{
                    funcMap.get(funcName).retValue = retValue;
                }
            }
        }
        else if(ctx.lVal() != null){
            // TODO
            map.replace(ctx.lVal().IDENT().getText(), getExpValue(ctx.exp()));
            super.visitChildren(ctx);
        }
        else if(ctx.block() != null){
            LLVMFrame<Integer> localMap = new LLVMFrame<Integer>(map);
            map = localMap;
            super.visitChildren(ctx);
            map = map.parent;
        }
        else if(ctx.IF() != null){
            int flag = getCondValue(ctx.cond());
            if(flag != 0){
                visitStmt(ctx.stmt(0));
            }
            if(flag == 0 && ctx.stmt().size() == 2){
                visitStmt(ctx.stmt(1));
            }
        }
        else if(ctx.WHILE() != null){
            while(getCondValue(ctx.cond()) != 0 && (!breakFlag)){
                continueFlag = false;
                visitStmt(ctx.stmt(0));
            }
            continueFlag = false;
            breakFlag = false;
        }
        else if(ctx.BREAK() != null){
            breakFlag = true;
            continueFlag = true;
        }
        else if(ctx.CONTINUE() != null){
            continueFlag = true;
        }


        }
        return null;
    }

    public int getCondValue(SysYParser.CondContext ctx){
        if(ctx.exp() != null){
            return getExpValue(ctx.exp());
        }else{
            if(ctx.OR() != null){
                int tmp = getCondValue(ctx.cond(0));
                if(tmp != 0) return 1;
                return getCondValue(ctx.cond(1));
            }
            else if(ctx.AND() != null){
                int tmp = getCondValue(ctx.cond(0));
                if(tmp == 0) return 0;
                return getCondValue(ctx.cond(1));
            }
            else{
                if(ctx.EQ() != null){
                    return getCondValue(ctx.cond(0)) == getCondValue(ctx.cond(1)) ? 1 : 0;
                }
                else if(ctx.NEQ() != null){
                    return getCondValue(ctx.cond(0)) != getCondValue(ctx.cond(1)) ? 1 : 0;
                }
                else if(ctx.LT() != null){
                    return getCondValue(ctx.cond(0)) < getCondValue(ctx.cond(1)) ? 1 : 0;
                }
                else if(ctx.GT() != null){
                    return getCondValue(ctx.cond(0)) > getCondValue(ctx.cond(1)) ? 1 : 0;
                }
                else if(ctx.LE() != null){
                    return getCondValue(ctx.cond(0)) <= getCondValue(ctx.cond(1)) ? 1 : 0;
                }
                else if(ctx.GE() != null){
                    return getCondValue(ctx.cond(0)) >= getCondValue(ctx.cond(1)) ? 1 : 0;
                }
            }
        }
        return 0;
    }

    public int getExpValue(SysYParser.ExpContext ctx){
        if(ctx == null){
            // NUll POINTER
            return 0;
        }else 

        if(ctx.IDENT() !=null){
            int size = ctx.funcRParams() == null ? 0 : ctx.funcRParams().param().size();
            LLVMFunction func = funcMap.get(ctx.IDENT().getText());
            // TODO 函数传参修改
            ArrayList<Integer> params = new ArrayList<Integer>();
            for(int i = 0; i < size; i++){
                params.add(getExpValue(ctx.funcRParams().param(i).exp()));
            }
            func.Assign(params);
            String tmpname = funcName;
            funcName = ctx.IDENT().getText();
            LLVMFrame<Integer> tmpMap = map;
            map = func.map;
            super.visitBlock(func.ctx);
            map = tmpMap;
            funcName = tmpname;
            return func.retValue;
        }
        else if(ctx.L_PAREN() != null){
            return getExpValue(ctx.exp(0));
        }
        else if(ctx.number() != null){
            return Integer.parseInt(toInt(ctx.number().INTEGER_CONST().getText()));
        }
        else if(ctx.unaryOp() != null){
            if(ctx.unaryOp().getText().equals("+")){
                return getExpValue(ctx.exp(0));
            }
            else if(ctx.unaryOp().getText().equals("-")){
                return - getExpValue(ctx.exp(0));
            }
            else if(ctx.unaryOp().getText().equals("!")){
                return getExpValue(ctx.exp(0)) == 0 ? 1 : 0;
            }
        }
        else if(ctx.lVal() != null){
            // TODO
            return map.get(ctx.lVal().IDENT().getText());
        }else{
            if(ctx.DIV() != null){
                return getExpValue(ctx.exp(0)) / getExpValue(ctx.exp(1));
            }
            else if(ctx.MUL() != null){
                return getExpValue(ctx.exp(0)) * getExpValue(ctx.exp(1));
            }
            else if(ctx.MOD() != null){
                return getExpValue(ctx.exp(0)) % getExpValue(ctx.exp(1));
            }
            else if(ctx.MINUS() != null){
                return getExpValue(ctx.exp(0)) - getExpValue(ctx.exp(1));
            }
            else if(ctx.PLUS() != null){
                return getExpValue(ctx.exp(0)) + getExpValue(ctx.exp(1));
            }
        }
        return 0;
    }

    public String toInt(String ret){
        if(ret.charAt(0) == '0' && ret.length() > 1 &&
            ret.charAt(1) != 'x' && ret.charAt(1) != 'X'){
            return String.valueOf(Long.parseLong(ret.substring(1), 8));
        }
        else if(ret.length() > 2 && 
        (ret.substring(0,2).equals("0x") || ret.substring(0,2).equals("0X"))){
            return  String.valueOf(Long.parseLong(ret.substring(2), 16));
        }else{
            return ret;
        }
    }

}
