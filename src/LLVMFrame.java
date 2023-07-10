import java.util.HashMap;


public class LLVMFrame {
    public LLVMFrame parent;
    public HashMap<String, Integer> map;

    public LLVMFrame(){
        parent = null;
        map = new HashMap<String, Integer>();
    }

    public LLVMFrame(LLVMFrame parent_){
        parent = parent_;
        map = new HashMap<String, Integer>();
    }

    public void put(String key, int i){
        map.put(key, i);
    }

    public boolean containsKey(String key){
        boolean ret = false;
        if(parent != null){
            ret = ret || parent.containsKey(key);
        }
        return map.containsKey(key) || ret;
    }

    public boolean containsKeyLocal(String key){
        return map.containsKey(key);
    }

    public Integer get(String key){
        if(map.get(key) != null) return map.get(key);
        if(parent != null) return parent.get(key);
        return null;
    }

    public Integer replace(String key, int val){
        if(!map.containsKey(key)){
            return parent.replace(key, val);
        }
        return map.replace(key, val);
    }
}
