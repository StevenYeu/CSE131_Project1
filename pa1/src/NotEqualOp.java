class NotEqualOp extends ComparisonOp {
    public NotEqualOp(STO a, STO b, String s) {
        super(a,b,s);
    }

    public STO checkOperands(STO a, STO b) {
        typeA = a.getType();
        typeB = b.getType();


        if((typeA instanceof NumericType) && (typeB instanceof NumericType)){
            return new ExprSTO(a.getName(), new BoolType("bool")); 
        }
        else if ( (typeA.isEquivalent(new BoolType("bool"))) && (typeB.isEquivalent(new BoolType("bool")))) {
            return new ExprSTO(a.getName(), new BoolType("bool"));
        }
        else {
            return new ErrorSTO("Error");
        } 
        
    }
}
