//----------
//
//----------


class StructType extends CompositeType {
    public StructType(String strName){
        super(strName,0);
    }

    public boolean isStruct(){ return true; }
    public boolean isEquivalent(Type t){
        if(t instanceof StructType){
            if( t.getName() == this.getName()){
                return true;
            }

        }
        return false;
    }

    public boolean isAssignable(Type t){
        return this.isEquivalent(t);
    }
 
}
