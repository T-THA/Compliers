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
    public LLVMFrame<ArrayList<Integer>> arrayMap; // 模仿map
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
        arrayMap = new LLVMFrame<ArrayList<Integer>>();
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
        arrayMap = new LLVMFrame<ArrayList<Integer>>();
        funcMap = new HashMap<String, LLVMFunction>();
        zero = LLVMConstInt(i32Type, 0, 0);
    }

    @Override 
    public LLVMValueRef visitDecl(SysYParser.DeclContext ctx){
        // TODO
        if(ctx.parent instanceof SysYParser.CompUnitContext){ // 全局变量
            if(ctx.constDecl() != null){
                for(SysYParser.ConstDefContext i : ctx.constDecl().constDef()){
                    if(i.L_BRACKT() != null && i.L_BRACKT().size() != 0){
                        int size = getExpValue(i.constExp(0).exp());
                        LLVMValueRef globalVar = LLVMAddGlobal(module, LLVMArrayType(i32Type, size), i.IDENT().getText());
                        ArrayList<Integer> tmpArr = new ArrayList<Integer>();
                        for(SysYParser.ConstInitValContext j : i.constInitVal().constInitVal()){
                            tmpArr.add(getExpValue(j.constExp().exp()));
                        }
                        for(int k = 0; k < size - i.constInitVal().constInitVal().size(); k++) tmpArr.add(0);
                        // LLVMSetInitializer(globalVar, LLVMConstArray(i32Type, globalVar, size));
                        arrayMap.put(i.IDENT().getText(), tmpArr);
                    }
                    else{

                    LLVMValueRef globalVar = LLVMAddGlobal(module, i32Type, i.IDENT().getText());
                    LLVMSetInitializer(globalVar, LLVMConstInt(i32Type, getExpValue(i.constInitVal().constExp().exp()), 0));
                    map.put(i.IDENT().getText(), getExpValue(i.constInitVal().constExp().exp()));
                    
                    }
                }
            }else{
                for(SysYParser.VarDefContext i : ctx.varDecl().varDef()){
                    if(i.L_BRACKT() != null && i.L_BRACKT().size() != 0){
                        int size = getExpValue(i.constExp(0).exp());
                        LLVMValueRef globalVar = LLVMAddGlobal(module, LLVMArrayType(i32Type, size), i.IDENT().getText());
                        
                        ArrayList<Integer> tmpArr = new ArrayList<Integer>();
                        if(i.ASSIGN() != null){
                            if(i.initVal().exp() != null){
                                //TODO
                            }else{
                                for(SysYParser.InitValContext j: i.initVal().initVal()){
                                    tmpArr.add(getExpValue(j.exp()));
                                }
                                for(int k = 0; k < size - i.initVal().initVal().size(); k++) tmpArr.add(0);
                            }
                        }else{
                            for(int j = 0; j < size; j++) tmpArr.add(0);
                        }
                        arrayMap.put(i.IDENT().getText(), tmpArr);
                    }
                    else{

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
            } 
        }else{ // 局部
            if(ctx.constDecl() != null){
                for(SysYParser.ConstDefContext i : ctx.constDecl().constDef()){
                    if(i.L_BRACKT() != null && i.L_BRACKT().size() != 0){
                        int size = getExpValue(i.constExp(0).exp());
                        ArrayList<Integer> tmpArr = new ArrayList<Integer>();
                        for(SysYParser.ConstInitValContext j : i.constInitVal().constInitVal()){
                            tmpArr.add(getExpValue(j.constExp().exp()));
                        }
                        for(int k = 0; k < size - i.constInitVal().constInitVal().size(); k++) tmpArr.add(0);
                        arrayMap.put(i.IDENT().getText(), tmpArr);
                    }
                    
                    else{

                    LLVMValueRef pointer = LLVMBuildAlloca(builder, i32Type, i.IDENT().getText());
                    LLVMBuildStore(builder, LLVMConstInt(i32Type, getExpValue(i.constInitVal().constExp().exp()), 0), pointer);
                    map.put(i.IDENT().getText(), getExpValue(i.constInitVal().constExp().exp()));
                    
                    }
                }
            }else{
                for(SysYParser.VarDefContext i : ctx.varDecl().varDef()){
                    if(i.L_BRACKT() != null && i.L_BRACKT().size() != 0){
                        int size = getExpValue(i.constExp(0).exp());
                        
                        ArrayList<Integer> tmpArr = new ArrayList<Integer>();
                        if(i.ASSIGN() != null){
                            if(i.initVal().exp() != null){
                                //TODO
                            }else{
                                for(SysYParser.InitValContext j: i.initVal().initVal()){
                                    tmpArr.add(getExpValue(j.exp()));
                                }
                                for(int k = 0; k < size - i.initVal().initVal().size(); k++) tmpArr.add(0);
                            }
                        }else{
                            for(int j = 0; j < size; j++) tmpArr.add(0);
                        }
                        arrayMap.put(i.IDENT().getText(), tmpArr);
                    }

                    else{

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
            LLVMFrame<ArrayList<Integer>> localArrMap = new LLVMFrame<ArrayList<Integer>>(arrayMap);
            map = localMap;
            arrayMap = localArrMap;
            String tmpname = funcName;
            funcName = "main";
            super.visitChildren(ctx);
            funcName = tmpname;
            map = map.parent;
            arrayMap = arrayMap.parent;
            return null;
        }else{
            int size = ctx.funcFParams() == null ? 0 : ctx.funcFParams().funcFParam().size();
            // TODO
            LLVMFrame<Integer> localMap = new LLVMFrame<Integer>(map);
            LLVMFrame<ArrayList<Integer>> localArrMap = new LLVMFrame<ArrayList<Integer>>(arrayMap);
            ArrayList<String> funcParams = new ArrayList<String>();
            ArrayList<String> funcArrParams = new ArrayList<String>();
            for(int i = 0; i < size; i++){
                String paramName = ctx.funcFParams().funcFParam(i).IDENT().getText();
                if(ctx.funcFParams().funcFParam(i).L_BRACKT().size() != 0){
                    localArrMap.put(paramName, new ArrayList<Integer>());
                    funcArrParams.add(paramName);
                }
                else{
                    localMap.put(paramName, 0);
                    funcParams.add(paramName);
                }
            }
            LLVMFunction func = new LLVMFunction(ctx.block(), localMap, localArrMap, 
                                        ctx.IDENT().getText(), funcParams, funcArrParams);
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
            if(ctx.lVal().L_BRACKT() != null && ctx.lVal().L_BRACKT().size() != 0){
                String arrayName = ctx.lVal().IDENT().getText();
                ArrayList<Integer> tmpArrayList = arrayMap.get(arrayName);
                int val = getExpValue(ctx.exp());
                tmpArrayList.set(getExpValue(ctx.lVal().exp(0)), val);
                arrayMap.replace(arrayName, tmpArrayList);
                super.visitChildren(ctx);
            }
            else{
                // TODO 数组直接赋值
                map.replace(ctx.lVal().IDENT().getText(), getExpValue(ctx.exp()));
                super.visitChildren(ctx);
            }
            
        }
        else if(ctx.block() != null){
            LLVMFrame<Integer> localMap = new LLVMFrame<Integer>(map);
            LLVMFrame<ArrayList<Integer>> localArrMap = new LLVMFrame<ArrayList<Integer>>(arrayMap);
            map = localMap;
            arrayMap = localArrMap;
            super.visitChildren(ctx);
            map = map.parent;
            arrayMap = arrayMap.parent;
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
        else if(ctx.exp() != null){
            if(ctx.exp().IDENT() != null){
                getExpValue(ctx.exp());
            }
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
            return 1;
        }else 

        if(ctx.IDENT() !=null){
            int size = ctx.funcRParams() == null ? 0 : ctx.funcRParams().param().size();
            LLVMFunction func = funcMap.get(ctx.IDENT().getText());
            // TODO 函数传参修改
            ArrayList<Integer> params = new ArrayList<Integer>();
            ArrayList<ArrayList<Integer>> arrParams = new ArrayList<ArrayList<Integer>>();
            for(int i = 0; i < size; i++){
                if(ctx.funcRParams().param(i).exp().lVal() != null
                && arrayMap.containsKey(ctx.funcRParams().param(i).exp().lVal().IDENT().getText())
                && ctx.funcRParams().param(i).exp().lVal().L_BRACKT().size() == 0){
                    arrParams.add(arrayMap.get(ctx.funcRParams().param(i).exp().lVal().IDENT().getText()));
                }
                else{
                    params.add(getExpValue(ctx.funcRParams().param(i).exp()));
                }
                
            }
            func.Assign(params, arrParams);
            String tmpname = funcName;
            funcName = ctx.IDENT().getText();
            LLVMFrame<Integer> tmpMap = map;
            LLVMFrame<ArrayList<Integer>> tmpArrMap = arrayMap;
            map = func.map;
            arrayMap = func.arrayMap;
            super.visitBlock(func.ctx);
            map = tmpMap;
            arrayMap = tmpArrMap;
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
            if(ctx.lVal().L_BRACKT() != null && ctx.lVal().L_BRACKT().size() != 0){
                int loca = getExpValue(ctx.lVal().exp(0));
                return arrayMap.get(ctx.lVal().IDENT().getText()).get(loca);
            }
            else{
                
                return map.get(ctx.lVal().IDENT().getText());
            }
            
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
