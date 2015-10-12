//----------------
//
//----------------


class ModOp extends ArithmetricOp {

    public ModOp(STO a,STO b, String s) {
        super(a,b,s);
    }

    public STO checkOperands(STO a, STO b) {
        typeA = a.getType();
        typeB = b.getType();

        if (!(typeA instanceof IntType)){
            return new ErrorSTO(a.getType().getName());
        }
        else if(!(typeB instanceof IntType)) {
            return new ErrorSTO(b.getType().getName());
        }
        else if((typeA instanceof IntType) && (typeB instanceof IntType)){
             if(a instanceof ConstSTO && b instanceof ConstSTO) {
                if (Integer.parseInt(b.getName()) == 0){

                    return new ErrorSTO("Mod-by-zero");
                    
                }
                int result = Integer.parseInt(a.getName()) % Integer.parseInt(b.getName());
                return new ConstSTO(Integer.toString(result), new IntType("int"),result);

            }
            return new ExprSTO(a.getName(), new IntType("int")); 
        }
        return new ErrorSTO(a.getType().getName());
        
    }
 
}
