//----------------
//
//----------------


class DivOp extends ArithmetricOp {

    public DivOp(STO a,STO b, String s) {
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
            if(a instanceof ConstSTO && b instanceof ConstSTO) {
                if (Integer.parseInt(b.getName()) == 0){
                    return new ErrorSTO("Divide-by-zero");    
                }
                int result = Integer.parseInt(a.getName()) / Integer.parseInt(b.getName());
                return new ConstSTO(Integer.toString(result), new IntType("int"),result);

            }
            return new ExprSTO(a.getName(), new IntType("result")); 
        }
        else {

            if(a instanceof ConstSTO && b instanceof ConstSTO) {
                if( Float.parseFloat(b.getName()) == 0.0){
                    return new ErrorSTO("Divide-by-zero");
                }
                float result = Float.parseFloat(a.getName()) + Float.parseFloat(b.getName());
                return new ConstSTO(Float.toString(result), new FloatType("float"),result);

            }
 
            return new ExprSTO(a.getName(), new FloatType("result"));
        }
    }
 
}
