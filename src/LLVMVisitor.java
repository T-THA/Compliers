import org.bytedeco.llvm.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.*;

import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;


public class LLVMVisitor extends SysYParserBaseVisitor<LLVMValueRef>{
    
    public LLVMModuleRef module;
    public LLVMBuilderRef builder;
    public LLVMTypeRef i32Type;

    public LLVMVisitor(){
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget();

        module = null;
        builder = null;
        i32Type = null;
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
    }

    @Override
    public LLVMValueRef visitFuncDef(SysYParser.FuncDefContext ctx){
        LLVMTypeRef returnType = i32Type;
        PointerPointer<Pointer> argumentTypes = new PointerPointer<>(0);
        LLVMTypeRef ft = LLVMFunctionType(returnType, argumentTypes, 0, 0);
        LLVMValueRef function = LLVMAddFunction(module, ctx.IDENT().getText(), ft);
        LLVMBasicBlockRef block1 = LLVMAppendBasicBlock(function, ctx.IDENT().getText() + "Entry");
        LLVMPositionBuilderAtEnd(builder, block1);

        super.visitChildren(ctx);
        return null;
    }

    @Override 
    public LLVMValueRef visitStmt(SysYParser.StmtContext ctx){
        if(ctx.RETURN() != null){
            if(ctx.exp() != null){
                int retValue = getExpValue(ctx.exp());
                // System.out.println(retValue);
                LLVMValueRef result = LLVMConstInt(i32Type, retValue, 0);
                LLVMBuildRet(builder, result);
            }
        }
        return null;
    }

    public int getExpValue(SysYParser.ExpContext ctx){
        if(ctx.L_PAREN() != null){
            return getExpValue(ctx.exp(0));
        }
        else if(ctx.number() != null){
            return Integer.parseInt(toInt(ctx.number().getText()));
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
