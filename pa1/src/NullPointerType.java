//---------
//
//---------

class NullPointerType extends PointerType {
    public NullPointerType( String strNum, int size, Type elemt, int count){
        super(strNum, size,count,elemt);
    }

    public boolean isNullPointer() { return true; }
    public boolean isEquivalent(Type t) {
        if(t instanceof PointerType)
            return true;
        return false;
    }
    public boolean isAssignable(Type t) {
        return this.isEquivalent(t);
    }
}

