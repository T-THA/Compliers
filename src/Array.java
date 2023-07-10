import java.util.ArrayList;

public class Array extends Type{

    public Type eleType;
    public int size;
    public ArrayList<Integer> array;

    public Array(){}
    public Array(int size_){
        size = size_;
    }
    @Override
    public boolean equals(Type obj) {
        return obj instanceof Array && ((Array)obj).size == size;
    }
}
