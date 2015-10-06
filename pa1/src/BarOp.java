class BarOp extends BitwiseOp {
    public BarOp(STO a, STO b) {
        super(a,b,"|");
    }

    public STO checkOperands(STO a, STO b) {
        typeA = a.getType();
        typeB = b.getType();


        if(!(typeA.isEquivalent(new IntType("int")))) {
            return new ErrorSTO(typeA.getName());
        }
        else if (!(typeB.isEquivalent(new IntType("int")))) {
            return new ErrorSTO(typeB.getName());
        }
        else if ( (typeA.isEquivalent(new IntType("int"))) && (typeB.isEquivalent(new BoolType("int")))) {
            return new ExprSTO("result", new IntType("int"));
        }
        return new ErrorSTO("Error");
        
    }
}
