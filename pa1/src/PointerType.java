//----------------
//
//----------------


class PointerType extends CompositeType{
    int cnt;
    Type elmt;
    public PointerType(String strNum, int size, int count, Type element){
        super(strNum, size);
        cnt = count;
        elmt = element;
    }
    
    // override getSize() to access the number of dereference
    public int getSize(){
        return cnt;
    }
    
    public boolean isPointer() { return true; }

    public boolean isEquivalent(Type t) {
        if( t instanceof PointerType ) {
            if(t.getSize() == this.getSize())
                return t.isEquivalent(elmt);
                            
        }
        return false;
    }

    public boolean isAssignable(Type t){
        return this.isEquivalent(t);
    }
}
