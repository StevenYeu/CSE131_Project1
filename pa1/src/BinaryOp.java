//---------------------------
//
//
//---------------------------


class BinaryOp extends Operator {
	STO op1;
	STO op2;
	
	public BinaryOp(){
	}

	public BinaryOp(STO a, STO b){
		op1 = a;
		op2 = b;
	}

}	
