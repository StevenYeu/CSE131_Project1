//---------------------------------------------------------------------
// CSE 131 Reduced-C Compiler Project
// Copyright (C) 2008-2015 Garo Bournoutian and Rick Ord
// University of California, San Diego
//---------------------------------------------------------------------

import java_cup.runtime.*;
import java.util.Vector;

class MyParser extends parser
{
	private Lexer m_lexer;
	private ErrorPrinter m_errors;
	private boolean m_debugMode;
	private int m_nNumErrors;
	private String m_strLastLexeme;
	private boolean m_bSyntaxError = true;
	private int m_nSavedLineNum;
    private int errCount = 0; // add for check 3a Phase 1

	private SymbolTable m_symtab;

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public MyParser(Lexer lexer, ErrorPrinter errors, boolean debugMode)
	{
		m_lexer = lexer;
		m_symtab = new SymbolTable();
		m_errors = errors;
		m_debugMode = debugMode;
		m_nNumErrors = 0;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public boolean Ok()
	{
		return m_nNumErrors == 0;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public Symbol scan()
	{
		Token t = m_lexer.GetToken();

		//	We'll save the last token read for error messages.
		//	Sometimes, the token is lost reading for the next
		//	token which can be null.
		m_strLastLexeme = t.GetLexeme();

		switch (t.GetCode())
		{
			case sym.T_ID:
			case sym.T_ID_U:
			case sym.T_STR_LITERAL:
			case sym.T_FLOAT_LITERAL:
			case sym.T_INT_LITERAL:
				return new Symbol(t.GetCode(), t.GetLexeme());
			default:
				return new Symbol(t.GetCode());
		}
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public void syntax_error(Symbol s)
	{
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public void report_fatal_error(Symbol s)
	{
		m_nNumErrors++;
		if (m_bSyntaxError)
		{
			m_nNumErrors++;

			//	It is possible that the error was detected
			//	at the end of a line - in which case, s will
			//	be null.  Instead, we saved the last token
			//	read in to give a more meaningful error 
			//	message.
			m_errors.print(Formatter.toString(ErrorMsg.syntax_error, m_strLastLexeme));
		}
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public void unrecovered_syntax_error(Symbol s)
	{
		report_fatal_error(s);
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public void DisableSyntaxError()
	{
		m_bSyntaxError = false;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public void EnableSyntaxError()
	{
		m_bSyntaxError = true;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public String GetFile()
	{
		return m_lexer.getEPFilename();
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public int GetLineNum()
	{
		return m_lexer.getLineNumber();
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public void SaveLineNum()
	{
		m_nSavedLineNum = m_lexer.getLineNumber();
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public int GetSavedLineNum()
	{
		return m_nSavedLineNum;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoProgramStart()
	{
		// Opens the global scope.
		m_symtab.openScope();
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoProgramEnd()
	{
		m_symtab.closeScope();
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoVarDecl(String id, Type t)
	{
		if (m_symtab.accessLocal(id) != null)
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
		}

		VarSTO sto = new VarSTO(id,t);
		m_symtab.insert(sto);
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoExternDecl(String id, Type t)
	{
		if (m_symtab.accessLocal(id) != null)
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
		}

		VarSTO sto = new VarSTO(id, t);
		m_symtab.insert(sto);
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoConstDecl(String id, Type t)
	{
		if (m_symtab.accessLocal(id) != null)
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
		}
		
		ConstSTO sto = new ConstSTO(id, t, t.getSize());   // fix me ask tutor about size of type
		m_symtab.insert(sto);
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoStructdefDecl(String id)
	{
		if (m_symtab.accessLocal(id) != null)
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
		}
		
		StructdefSTO sto = new StructdefSTO(id);
		m_symtab.insert(sto);
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoFuncDecl_1(String id)
	{
		if (m_symtab.accessLocal(id) != null)
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
		}
	
		FuncSTO sto = new FuncSTO(id);
		m_symtab.insert(sto);

		m_symtab.openScope();
		m_symtab.setFunc(sto);
	}

    void DoFuncDecl_3(String id, Type t, Object o)
	{
        String s = o.toString();
		if (m_symtab.accessLocal(id) != null)
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
		}
	
		FuncSTO sto = new FuncSTO(id);
        if(s == "&"){
            sto.flag = true;
        }
        sto.setReturnType(t);
        

		m_symtab.openScope();
		m_symtab.setFunc(sto);
	}




	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoFuncDecl_2()
	{
		m_symtab.closeScope();
		m_symtab.setFunc(null);
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoFormalParams(Vector<String> params)
	{
		if (m_symtab.getFunc() == null)
		{
			m_nNumErrors++;
			m_errors.print ("internal: DoFormalParams says no proc!");
		}

		// insert parameters here
        FuncSTO sto = m_symtab.getFunc();
        for(int i = 0; i < params.size(); i++){
            STO s = this.ProcessParams(params.get(i));
            sto.addParam(this.ProcessParams(params.get(i)));
        }
        m_symtab.setFunc(sto);
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoBlockOpen()
	{
		// Open a scope.
		m_symtab.openScope();
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoBlockClose()
	{
		m_symtab.closeScope();
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	STO DoAssignExpr(STO stoDes)
	{
        
        if(stoDes instanceof ErrorSTO) {
            return stoDes;
        }

		if (!stoDes.isModLValue()) 
		{
			// Good place to do the assign checks
            m_errors.print(ErrorMsg.error3a_Assign);
            m_nNumErrors++;
            return new ErrorSTO(stoDes.getName());
		}
        stoDes.setIsAddressable(false);
        stoDes.setIsModifiable(false);
		
		return stoDes;
	}

    STO DoAssignTypeCheck(STO a, STO b) {
        
        if(a instanceof ErrorSTO) {
            return a;
        }
        else if (b instanceof ErrorSTO) {
            return b;
        }


        STO result;
        System.out.println(b.getType().getName());
        if (!b.getType().isAssignable(a.getType())) {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error3b_Assign,b.getType().getName(),a.getType().getName())); 
            return new ErrorSTO(a.getName());
                   
        }
        result = a;
        return result;
    
    }

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	STO DoFuncCall(STO sto, Vector<STO> params)
	{
        if(sto instanceof ErrorSTO){
            return sto;
        }
		if (!sto.isFunc())
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.not_function, sto.getName()));
			return new ErrorSTO(sto.getName());
		}
        else {            
          
            if (m_symtab.access(sto.getName()) == null) {
                m_nNumErrors++;
                m_errors.print(Formatter.toString(ErrorMsg.undeclared_id, sto.getName()));
                return new ErrorSTO("Error");
            }
            else{

                STO fsto = m_symtab.access(sto.getName());
                if( params.size() != fsto.getParams().size()){
                   m_nNumErrors++;
                   m_errors.print(Formatter.toString(ErrorMsg.error5n_Call, params.size(), fsto.getParams().size()));
                   return new ErrorSTO(sto.getName());
                }
                else if( params.size() == fsto.getParams().size()){
                    for(int i = 0; i < fsto.getParams().size(); i++){
                        if((fsto.getParams().get(i).getName().charAt(0)) == '&') {
                            if(!(params.get(i).getType().isEquivalent(fsto.getParams().get(i).getType()))){
                                m_nNumErrors++;
                                m_errors.print(Formatter.toString(ErrorMsg.error5r_Call, params.get(i).getType().getName(), fsto.getParams().get(i).getName().substring(1), fsto.getParams().get(i).getType().getName()));
                            }
                            else if(!(params.get(i).isModLValue())){
                                m_nNumErrors++;
                                m_errors.print(Formatter.toString(ErrorMsg.error5c_Call, fsto.getParams().get(i).getName().substring(1), fsto.getParams().get(i).getType().getName()));
                            }

                        }
                        else if(!(params.get(i).getType().isAssignable(fsto.getParams().get(i).getType()))){
                            m_nNumErrors++;
                            m_errors.print(Formatter.toString(ErrorMsg.error5a_Call, params.get(i).getType().getName(), fsto.getParams().get(i).getName(), fsto.getParams().get(i).getType().getName()));
                        }
                        
                    }
                    return new ErrorSTO("Error");
                }

                 //return by reference calls to function values setting
                if(fsto.flag == true){
                    sto.setIsAddressable(true);
                    sto.setIsModifiable(true);
                }
                else {
                    sto.setIsAddressable(false);
                    sto.setIsModifiable(false);
                }
            
            }

        }
        STO result = new ExprSTO(m_symtab.getFunc().getName(),m_symtab.getFunc().getReturnType());
		return result;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	STO DoDesignator2_Dot(STO sto, String strID)
	{
		// Good place to do the struct checks

		return sto;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	STO DoDesignator2_Array(STO sto)
	{
		// Good place to do the array checks

		return sto;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	STO DoDesignator3_ID(String strID)
	{
		STO sto;
		if (((sto = m_symtab.access(strID)) == null )  )
		{	
                
            m_nNumErrors++;
		    m_errors.print(Formatter.toString(ErrorMsg.undeclared_id, strID));
		    sto = new ErrorSTO(strID);
            return sto;
        }

        return sto;
            
	}

    STO DoDes3_GlobalID(String strID)
    {
        STO sto;
        if (((sto = m_symtab.accessGlobal(strID)) == null)) 
		{
            
            m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.error0g_Scope, strID));
			sto = new ErrorSTO(strID);
            return sto;
                
		}
        return sto;

    }


	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	Type DoStructType_ID(String strID)
	{
		STO sto;

		if ((sto = m_symtab.access(strID)) == null)
		{
			m_nNumErrors++;
		 	m_errors.print(Formatter.toString(ErrorMsg.undeclared_id, strID));
			return new ErrorType();
		}

		if (!sto.isStructdef())
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.not_type, sto.getName()));
			return new ErrorType();
		}

		return sto.getType();
	}

    STO DoBinaryExpr(STO a, Operator o, STO b) {

        if (a instanceof ErrorSTO) {
            return a;
        }
        else if (b instanceof ErrorSTO) {
            return b;
        }
        

        STO result = o.checkOperands(a, b);
        if ((result instanceof ErrorSTO)) {

            m_nNumErrors++;
            if(o.getOp().equals("%")) {
                 m_errors.print(Formatter.toString(ErrorMsg.error1w_Expr,result.getName(),o.getOp(),"int"));                
            }

            else if(o.getOp().equals("+") || o.getOp().equals("-") || o.getOp().equals("/") || o.getOp().equals("*")) {
                 m_errors.print(Formatter.toString(ErrorMsg.error1n_Expr,result.getName(),o.getOp()));                
            }

            else if(o.getOp().equals("<") || o.getOp().equals("<=") || o.getOp().equals(">") || o.getOp().equals(">=")) {
                 m_errors.print(Formatter.toString(ErrorMsg.error1n_Expr,result.getName(),o.getOp())); 
            }
            else if(o.getOp().equals("==") || o.getOp().equals("!=")) {
                m_errors.print(Formatter.toString(ErrorMsg.error1b_Expr,a.getType().getName(),o.getOp(),b.getType().getName())); 
            }
            else if(o.getOp().equals("&&") || o.getOp().equals("||")) {
                m_errors.print(Formatter.toString(ErrorMsg.error1w_Expr,result.getName(),o.getOp(),"bool")); 
            }
            else if(o.getOp().equals("&") || o.getOp().equals("^") || o.getOp().equals("|")) {
                m_errors.print(Formatter.toString(ErrorMsg.error1w_Expr,result.getName(),o.getOp(),"int"));
            }

            //result = new ErrorSTO("Error");
            return result;
            
        }

        return result;
    }

    STO DoUnaryExpr(STO a, Operator o) {


        if(a instanceof ErrorSTO) {
            return a;
        }

        STO result = o.checkOperands(a);
        if (result instanceof ErrorSTO) {

            
            if(o.getOp().equals("!")) {
                m_errors.print(Formatter.toString(ErrorMsg.error1u_Expr,result.getName(),o.getOp(),"bool")); 
            }
            m_nNumErrors++;
            result = new ErrorSTO("Error");
            
        }

        return result;
    
    }

    STO DoIncDecCheck(String s1, STO a) {
        STO result;

        if (a instanceof ErrorSTO) {
            return a;
        }


        if (!(a.getType() instanceof NumericType)) {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error2_Type,a.getType().getName(), s1));
            return result = new ErrorSTO("Error");
        } 
        else if (!(a.isModLValue())){
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error2_Lval,a.getName()));
            result = new ErrorSTO("Error");
            return result;
        }
        
        result = new ExprSTO(a.getName(), a.getType());
        result.setIsAddressable(false);
        result.setIsModifiable(false);

       return result;

    }

    STO DoIfAndWhile(STO a){
        STO result;

        if(a instanceof ErrorSTO) {
            return a;
        }


        if(!(a.getType().isEquivalent(new BoolType("Bool")))){
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error4_Test, a.getType().getName()));
            return new ErrorSTO("Error");
        }
        result = a;
        return result;
    } 

    STO ProcessParams(String s) {
        String[] splitStr = s.split("\\s+");
        String type = splitStr[0];
        String id = splitStr[1];
        Type t;
        switch (type) 
        {
            case "int" :  t = new IntType("int");
                          break;
            case "float": t = new FloatType("float");
                          break;
            case "bool":  t = new BoolType("bool");
                          break;
            default:      t = new VoidType("void",0);
        
        }
        STO result = new VarSTO(id,t);
        return result;

    }

    STO DoReturnStmt(STO expr){
        if(expr instanceof ErrorSTO)
            return expr;
        if(!(m_symtab.getFunc().getReturnType() instanceof VoidType)){
    
            if(m_symtab.getFunc().flag == false)
            {
                if(!(expr.getType().isAssignable(m_symtab.getFunc().getReturnType()))){ 
                    m_nNumErrors++;
                    m_errors.print(Formatter.toString(ErrorMsg.error6a_Return_type, expr.getType().getName(), m_symtab.getFunc().getReturnType().getName()));
                    return new ErrorSTO("Error");
                }
            }
            else if(m_symtab.getFunc().flag == true){
                if(!(expr.getType().isEquivalent(m_symtab.getFunc().getReturnType()))){ 

                    m_nNumErrors++;
                    m_errors.print(Formatter.toString(ErrorMsg.error6b_Return_equiv, expr.getType().getName(), m_symtab.getFunc().getReturnType().getName()));
                    return new ErrorSTO("Error");
                }
                else if(!(expr.isModLValue())){
                    m_nNumErrors++;
                    m_errors.print(ErrorMsg.error6b_Return_modlval);
                    return new ErrorSTO("Error");
                }

                
            }
        }
        return expr;

    }


    STO DoReturnStmt(){
        if(!(m_symtab.getFunc().getReturnType() instanceof VoidType)){
                m_nNumErrors++;
                m_errors.print(ErrorMsg.error6a_Return_expr);
                return new ErrorSTO("Error");
        }
        return m_symtab.getFunc();

    }

  }
