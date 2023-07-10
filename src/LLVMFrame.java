import java.util.HashMap;


public class LLVMFrame<T> {
    public LLVMFrame<T> parent;
    public HashMap<String, T> map;

    public LLVMFrame(){
        parent = null;
        map = new HashMap<String, T>();
    }

    public LLVMFrame(LLVMFrame<T> parent_){
        parent = parent_;
        map = new HashMap<String, T>();
    }

    public void put(String key, T i){
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

    public T get(String key){
        if(map.get(key) != null) return map.get(key);
        if(parent != null) return parent.get(key);
        return null;
    }

    public T replace(String key, T val){
        if(!map.containsKey(key)){
            return parent.replace(key, val);
        }
        return map.replace(key, val);
    }
}
