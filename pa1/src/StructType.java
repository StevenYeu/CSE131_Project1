//----------
//
//----------

import java.util.ArrayList;

class StructType extends CompositeType {
    ArrayList<Type> Member;
    public StructType(String strName, int size, ArrayList<Type> Mem){
        super(strName,size);
        Member = Mem;
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
