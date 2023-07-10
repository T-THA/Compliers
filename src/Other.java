public class Other extends Type{
    
    public String typeName; // error or bool
    public String[] msgTable={
        "error", "bool", "other"
    };

    public Other(int i){
        typeName = msgTable[i];
    }

    public Other(){
        typeName = msgTable[0];
    }
    @Override
    public boolean equals(Type obj){
        return obj instanceof Other;
    }
}
