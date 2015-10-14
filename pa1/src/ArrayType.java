//-----------------------
//
//-----------------------
import java.util.Vector;

class ArrayType extends CompositeType{

    Type element;
    int dimension;
    //Vector<int> indices = new Vector<int>();

    public ArrayType(String strName, int size, int dim, Type elmt){
        super(strName, size);
        element = elmt;
        dimension = dim;

    }

    public boolean isArray() { return true; }

    public boolean isEquivalent(Type t) {     
        //if(t instanceof element) {
          //  return t.isEquivalent(element);
        //}
        return false;

    }
    public boolean isAssignable(Type t){
        return element.isEquivalent(t);     
    }

    public Type getBaseType() {
        return element;
    }
    public void setIndices(int d){
       // indices.add(d);
    }
}
