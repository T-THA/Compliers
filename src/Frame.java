import java.util.HashMap;

public class Frame {

    public Frame parent = null;
    public HashMap<String, Type> map = new HashMap<String, Type>();
    public int depth = 0;
    public String retType = "null";

    public Frame(Frame frame, int depth_, String retType_){
        depth = depth_;
        parent = frame;
        retType = retType_;
    }
    public Frame(){

    }

    public void put(String key, Type type){
        map.put(key, type);
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

    public Type get(String key){
        if(map.get(key) != null) return map.get(key);
        if(parent != null) return parent.get(key);
        return null;
    }
}
