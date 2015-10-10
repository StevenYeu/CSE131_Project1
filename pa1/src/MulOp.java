//----------------
//
//----------------


class MulOp extends ArithmetricOp {

    public MulOp(STO a,STO b, String s) {
        super(a,b,s);
    }

    public STO checkOperands(STO a, STO b) {
        typeA = a.getType();
        typeB = b.getType();

        if (!(typeA instanceof NumericType)){
            return new ErrorSTO(a.getType().getName());
        }
        else if(!(typeB instanceof NumericType)) {
            return new ErrorSTO(b.getType().getName());
        }
        else if((typeA instanceof IntType) && (typeB instanceof IntType)){
            return new ExprSTO(a.getName(), new IntType("int")); 
        }
        else {
            return new ExprSTO(a.getName(), new FloatType("float"));
        }
    }
 
}
