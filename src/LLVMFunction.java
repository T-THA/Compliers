import java.util.ArrayList;

public class LLVMFunction {
    public SysYParser.BlockContext ctx;
    public LLVMFrame<Integer> map;
    public String name;
    public ArrayList<String> params; // 函数参数列表名字
    // TODO
    public int retValue;

    public LLVMFunction(SysYParser.BlockContext ctx_, LLVMFrame<Integer> map_, 
                        String name_, ArrayList<String> params_){
        name = name_;
        ctx = ctx_;
        map = map_;
        params = params_;
    }
    public LLVMFunction(){}

    public void Assign(ArrayList<Integer> values){
        // TODO
        for(int i = 0; i < values.size(); i++){
            map.replace(params.get(i), values.get(i));
        }
    }
}
