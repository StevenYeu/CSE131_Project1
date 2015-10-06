class NotOp extends UnaryOp {
    Type typeA;
    public NotOp(STO a) {
        super(a,"!");
    }

    public STO checkOperands(STO a) {
        typeA = a.getType();

        if(!(typeA.isEquivalent(new BoolType("bool")))) {
            return new ErrorSTO(typeA.getName());
        }
        else {
            return new ExprSTO(a.getType().getName(), new BoolType("bool"));
        
        }
        
    }
}
