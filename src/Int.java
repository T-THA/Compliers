public class Int extends Type{

    public int value;

    @Override
    public boolean equals(Type obj) {
        return obj instanceof Int;
    }
}
