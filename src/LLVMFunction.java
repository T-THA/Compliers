import java.util.ArrayList;

public class LLVMFunction {
    public SysYParser.BlockContext ctx;
    public LLVMFrame<Integer> map;
    public LLVMFrame<ArrayList<Integer>> arrayMap;
    public String name;
    public ArrayList<String> params; // 函数参数列表名字
    public ArrayList<String> arrParams;
    // TODO
    public int retValue;

    public LLVMFunction(SysYParser.BlockContext ctx_, LLVMFrame<Integer> map_,
                        LLVMFrame<ArrayList<Integer>> arrayMap_,
                        String name_, ArrayList<String> params_,
                        ArrayList<String> arrParams_){
        name = name_;
        ctx = ctx_;
        map = map_;
        arrayMap =  arrayMap_;
        params = params_;
        arrParams = arrParams_;
    }
    public LLVMFunction(){}

    public void Assign(ArrayList<Integer> values, ArrayList<ArrayList<Integer>> arrValues){
        // TODO
        for(int i = 0; i < values.size(); i++){
            map.replace(params.get(i), values.get(i));
        }
        for(int i = 0; i < arrValues.size(); i++){
            arrayMap.replace(arrParams.get(i), arrValues.get(i));
        }
    }
}
