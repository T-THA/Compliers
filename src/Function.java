import java.util.ArrayList;

public class Function extends Type{

    public String retType;
    public ArrayList<Type> eleType;

    public Function(){
        retType = "null";
    }
    public Function(String retType_){
        retType = retType_;
    }

    @Override
    public boolean equals(Type obj) {
        return (obj instanceof Function) 
        && ((Function)obj).retType.equals(retType)
        && eleTypeEquals((Function)obj);
    }

    public boolean eleTypeEquals(Function obj){
        if(obj.eleType.size() != eleType.size()) return false;
        if(obj.eleType.size() == 0) return true;
        for(int i = 0; i < eleType.size(); i++){
            if(!eleType.get(i).equals(obj.eleType.get(i))) return false;
        }
        return true;
    }
}
