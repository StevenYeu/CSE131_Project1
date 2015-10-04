//-----------------------
//
//-----------------------


class ArrayType extends CompositeType{
    //int dimension;
    Type element;
    public ArrayType(String strName, int size, Type elmt){
        super(strName, size);
        //dimension = dim;
        element = elmt;
    }

    public boolean isArray() { return true; }

    public boolean isEquivalent(Type t) {     
        if(t instanceof ArrayType) {
            if (t.getSize() == super.getSize()) {
                return t.isEquivalent(element);
            }
        }
        return false;

    }
    public boolean isAssignable(Type t){
        return this.isEquivalent(t);     
    }
}
