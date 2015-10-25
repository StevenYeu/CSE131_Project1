//---------------------------------------------------------------------
// CSE 131 Reduced-C Compiler Project
// Copyright (C) 2008-2015 Garo Bournoutian and Rick Ord
// University of California, San Diego
//---------------------------------------------------------------------

import java_cup.runtime.*;
import java.util.Vector;
import java.util.List;

class MyParser extends parser
{
	private Lexer m_lexer;
	private ErrorPrinter m_errors;
	private boolean m_debugMode;
	private int m_nNumErrors;
	private String m_strLastLexeme;
	private boolean m_bSyntaxError = true;
	private int m_nSavedLineNum;
    private boolean paramAmp = false;
    private int isInLoop = 0;
    private boolean isInStruct = false;
    private boolean isMultiError = false;
    private Scope scope;
    private String StructName;
    private String name;


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
	// for auto
	//----------------------------------------------------------------
	void DoVarDecl(String id, Type t)
	{
		if (m_symtab.accessLocal(id) != null)
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
		}

		VarSTO sto = new VarSTO(id,t);
        if(t instanceof ArrayType){
            sto.setIsAddressable(true);
            sto.setIsModifiable(false);
        }
        else{
            sto.setIsAddressable(false);
            sto.setIsModifiable(false);
        }

  		m_symtab.insert(sto);
	}

    //-----------------------------------------------------------------
    // For Structs
    // ----------------------------------------------------------------
	void DoCtorStructs(String id, Type t, Vector<STO> params)
	{
        if(t instanceof ErrorType){
            return;
        }


		if (m_symtab.accessLocal(id) != null)
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
		}

        STO str = m_symtab.accessGlobal(t.getName());

       
        Vector<STO> functions = ((StructdefSTO)str).OverloadCheck(t.getName()); 
        

        
        if(functions.size() < 2) {
            STO fsto = functions.get(0); // in sym tab
            if( params.size() != fsto.getParams().size()){
               m_nNumErrors++;
               m_errors.print(Formatter.toString(ErrorMsg.error5n_Call, params.size(), fsto.getParams().size()));
               return;
            }
            else if( params.size() == fsto.getParams().size()){
                for(int i = 0; i < fsto.getParams().size(); i++){ 
                    if(params.size() != 0) {if(params.get(i) instanceof ErrorSTO) {return;} }//Nasty Error check for indexoutofbound  
                    if( fsto.getParams().get(i).flag == true || params.get(i).getType() instanceof ArrayType){ // added Array Type check
                        if(!(params.get(i).getType().isEquivalent(fsto.getParams().get(i).getType()))){
                            m_nNumErrors++;
                            m_errors.print(Formatter.toString(ErrorMsg.error5r_Call, params.get(i).getType().getName(), fsto.getParams().get(i).getName(), fsto.getParams().get(i).getType().getName()));
                            return;
                        }
                        else if(!(params.get(i).isModLValue() && !(params.get(i).getType() instanceof ArrayType))){
                            m_nNumErrors++;
                            m_errors.print(Formatter.toString(ErrorMsg.error5c_Call, fsto.getParams().get(i).getName(), fsto.getParams().get(i).getType().getName()));
                            return;
                        }
                        //paramAmp = false;

                   }
                   else {
                        if(!(params.get(i).getType().isAssignable(fsto.getParams().get(i).getType()))){
                           m_nNumErrors++;
                           m_errors.print(Formatter.toString(ErrorMsg.error5a_Call, params.get(i).getType().getName(), fsto.getParams().get(i).getName(), fsto.getParams().get(i).getType().getName()));
                           return;
                        }
                            
                   }
                                                   
                }
                        
           }

        }

        else{ // overload check
          int OverloadCnt = 0;
          for(int i = 0; i < functions.size(); i++){ // thru all ctors
              Vector<STO> curPar = functions.elementAt(i).getParams();
              if(curPar.size() == params.size()){
                 int ParamCnt = 0; // param counter
                 for(int j = 0; j < params.size(); j++){
                    if(params.size() != 0) {if(params.get(j) instanceof ErrorSTO) {return;} }//Nasty Error check for indexoutofbound 
                     Type parType = params.elementAt(j).getType();
                     Type curParType = curPar.elementAt(j).getType();
                     if(!parType.isEquivalent(curParType)){
                            OverloadCnt++;
                            break;
                     }
                     else{
                        if( functions.get(i).getParams().get(j).flag == true || params.get(j).getType() instanceof ArrayType){
                            if(!(params.get(j).isModLValue())) {
                               m_nNumErrors++;
                               m_errors.print(Formatter.toString(ErrorMsg.error9_Illegal, t.getName()));
                               return;
                            }
                        }
        
                        ParamCnt++;
                        
                     }
                     
                 }
                 if(ParamCnt == (params.size())) {
                  break;
                 }
                }
                else{
                    OverloadCnt++;
                }
                        
          }
          if(OverloadCnt == (functions.size())){
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error9_Illegal, t.getName()));
            return;
          }
        }          
           
		StructdefSTO sto = new StructdefSTO(id,t);
        sto.setFuncs(((StructdefSTO)str).getFuncs());
        sto.setVars(((StructdefSTO)str).getVars());
        
        //set to Lval for struct
        sto.setIsAddressable(true);
        sto.setIsModifiable(true);
        
		m_symtab.insert(sto);
	}




    // decl of var in struct
    void DoStructVarDecl(Type t, String id, Vector<STO> arraylist)
	{
		
        VarSTO sto;
        if (m_symtab.accessLocal(id) != null)
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.error13a_Struct, id));
		}


        if(arraylist.size() > 0) {
            int numDim = arraylist.size();
            STO sizeStoTop = arraylist.elementAt(0);
            ArrayType aTopType = new ArrayType(t.getName()+"["+ ((ConstSTO)sizeStoTop).getIntValue() +"]", ((ConstSTO)sizeStoTop).getIntValue(), numDim);

            
            for(int i = 1; i <=numDim; i++){
            
                if(i == numDim){
                    aTopType.addNext(t);
                }
                else{  
                  STO sizeSto = arraylist.elementAt(i);
                  ArrayType typ = new ArrayType(t.getName()+"["+ ((ConstSTO)sizeSto).getIntValue() +"]", ((ConstSTO)sizeSto).getIntValue(),numDim-i);
                  aTopType.addNext(typ);

                }                        
            }
		    sto = new VarSTO(id,aTopType);
            sto.setIsAddressable(true);
            sto.setIsModifiable(false);
        }
        else {
		   sto = new VarSTO(id,t);
            sto.setIsAddressable(false);
            sto.setIsModifiable(false);

        
        }
        // set var in struct to mod-lval
		m_symtab.insert(sto);
	}


    //----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoVarDecl2(String id, Type t, Vector<STO> arraylist, STO expr)
	{
        int numDim = arraylist.size();
        VarSTO sto;
        if( expr instanceof ErrorSTO)
            return;

        if (m_symtab.accessLocal(id) != null)
		{
		    m_nNumErrors++;
		    m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
            return;
		}


        if(numDim > 0)
        {
            for(int i = 0; i < numDim; i++){
                STO arrayDim = arraylist.elementAt(i);
                
                if(arrayDim instanceof ErrorSTO || arrayDim.getType() instanceof ErrorType) {
                   return;
                }

 
                if(! arrayDim.getType().isEquivalent(new IntType("int"))) {
                    m_nNumErrors++;
			        m_errors.print(Formatter.toString(ErrorMsg.error10i_Array, arrayDim.getType().getName()));
                    return;
                }
                else if(!(arrayDim instanceof ConstSTO)){
                    m_nNumErrors++;
                    m_errors.print(ErrorMsg.error10c_Array);
                    return;
                }
                else if(((ConstSTO)arrayDim).getIntValue() <=0){
                    m_nNumErrors++;
			        m_errors.print(Formatter.toString(ErrorMsg.error10z_Array, (((ConstSTO)arrayDim).getIntValue())));
                    return;
                }

            }

            STO sizeStoTop = arraylist.elementAt(0);
            String arr = this.CreateArray(arraylist);
            ArrayType aTopType = new ArrayType(t.getName() + arr, ((ConstSTO)sizeStoTop).getIntValue(), numDim);


            
            for(int i = 1; i <=numDim; i++){
            
                if(i == numDim){
                    aTopType.addNext(t);
                }
                else{  
                  STO sizeSto = arraylist.elementAt(i);
                  if((arr.indexOf("]")+1) != -1){
                     arr = arr.substring(arr.indexOf("]")+1);
                  }
                 // else {
                    // arr = arr;
                 // }
                  ArrayType typ = new ArrayType(t.getName()+arr, ((ConstSTO)sizeSto).getIntValue(),numDim-i);
                  aTopType.addNext(typ);
                }
                        
            }
 

            
            sto = new VarSTO(id, aTopType);
            // non-mod l-val for array
            sto.setIsAddressable(true);
            sto.setIsModifiable(false);
        }
        else {
            if(t instanceof PointerType){
                if(expr == null) {
              	    sto = new VarSTO(id,t);
		            m_symtab.insert(sto);
                    return;
                }
                
                else if(!(expr.getType() instanceof PointerType)){
                    m_nNumErrors++;
                    m_errors.print(Formatter.toString(ErrorMsg.error8_Assign, expr.getType().getName(),t.getName()));
                    return;
                }
 

    
                sto = new VarSTO(id,t);
                //lval for pointer
                sto.setIsAddressable(true);
                sto.setIsModifiable(true);
  
            }
            else{            

                if(expr == null) {
              	    sto = new VarSTO(id,t);
		            m_symtab.insert(sto);
                    return;
                }
		   
                else if(!expr.getType().isAssignable(t)){
                    m_nNumErrors++;
                    m_errors.print(Formatter.toString(ErrorMsg.error8_Assign, expr.getType().getName(),t.getName()));
                    return;
                }
                sto = new VarSTO(id,t);
                
                // r-val for var why
                sto.setIsAddressable(true);
                sto.setIsModifiable(true);
                
            }
        }
            
       
		m_symtab.insert(sto);
            
        
	}

    String CreateArray(Vector<STO> list) {
        String s = "";
        for(int i = 0; i < list.size(); i++){
            s =s.concat("[" + ((ConstSTO)list.get(i)).getIntValue() + "]");
        }
        return s;
        
    }

    //----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoForEachDecl(Type iterType, Object opRef, String id, STO expr)
	{ 


        String s = opRef.toString();
        
		if (m_symtab.accessLocal(id) != null)
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
            return;
		}
        VarSTO sto = new VarSTO(id,iterType);        
		m_symtab.insert(sto);

        if (!(expr.getType() instanceof ArrayType)){
	        m_nNumErrors++;
			m_errors.print(ErrorMsg.error12a_Foreach);
            return;
        }

        if ( s == "&" ){

            if(!(((ArrayType)expr.getType()).getNext().isEquivalent(iterType))){

                
                m_nNumErrors++;
			    m_errors.print(Formatter.toString(ErrorMsg.error12r_Foreach, ( (ArrayType) expr.getType()).getNext().getName(), id, iterType.getName()));
                return;

            }

        }
        else{
            if(!(((ArrayType)expr.getType()).getNext().isAssignable(iterType))){
                
                m_nNumErrors++;
			    m_errors.print(Formatter.toString(ErrorMsg.error12v_Foreach, ( (ArrayType) expr.getType()).getNext().getName(), id, iterType.getName()));
                return;

            }
        }
		//VarSTO sto = new VarSTO(id,iterType);        
		//m_symtab.insert(sto);
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
	void DoConstDecl2(String id, Type t, STO constexpr)
	{
        if(constexpr instanceof ErrorSTO){
            return;
        }

		if (m_symtab.accessLocal(id) != null)
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
            return;
		}
       				
        if( !(constexpr instanceof ConstSTO)){
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error8_CompileTime, id));
            return;
            
        }
        if( !(constexpr.getType().isAssignable(t)))
        {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error8_Assign, constexpr.getType().getName(),t.getName()));
            return;
        }
        else{
            ConstSTO sto;
            if (t instanceof IntType) {
                sto = new ConstSTO(id, t, ((ConstSTO)constexpr).getIntValue());   // fix me Done
            }
            else if (t instanceof FloatType) {
                sto = new ConstSTO(id, t, ((ConstSTO)constexpr).getFloatValue());   // fix me Done

            }
            else {
                if(((ConstSTO)constexpr).getBoolValue())
                    sto = new ConstSTO(id,t,1);
                else
                    sto = new ConstSTO(id,t,0);
            }
          

            sto.setIsModifiable(false);
            sto.setIsAddressable(true);
            

           	m_symtab.insert(sto);
        }
	}


    // auto for const decl
    void DoAutoDecl(String id, STO expr)
	{
        if(expr instanceof ErrorSTO)
            return;

		if (m_symtab.accessLocal(id) != null)
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
		}
       		
		ConstSTO sto;
        if (expr.getType() instanceof IntType) {
            sto = new ConstSTO(id, expr.getType(), Integer.parseInt(expr.getName()));   // fix me Done
        }
        else if (expr.getType() instanceof FloatType) {
            sto = new ConstSTO(id, expr.getType(), Float.parseFloat(expr.getName()));   // fix me Done
        }
        else {
            if(((ConstSTO)expr).getBoolValue())
                sto = new ConstSTO(id,expr.getType(),1);
            else
                sto = new ConstSTO(id,expr.getType(),0);   
        }

        sto.setIsModifiable(false);
        sto.setIsAddressable(true);

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

       	StructdefSTO sto = new StructdefSTO(id, new StructType(id));
        Vector<STO> locals = scope.getLocals();
        int size = 0;
        
        for(int i = 0; i < locals.size(); i++){
            
            STO elm = locals.get(i);
            if(elm instanceof FuncSTO) {
                sto.getFuncs().addElement(elm);
            }
            else {
                sto.getVars().addElement(elm);
                if(elm.getType() instanceof ArrayType) {
                   size = size + ((ArrayType)elm.getType()).getTotalSize();
                }
                else {
                  size = size + elm.getType().getSize();
                } 
            }

        }
        sto.getType().setSize(size);

		m_symtab.insert(sto);
	}

    //----------------------------------------------------------------
    // Crafty way to get struct name
    //----------------------------------------------------------------
    void DoStructName(String name){
        StructName = name;
    }

    //----------------------------------------------------------------
    //
    //----------------------------------------------------------------
    void DoDefaultConstructor(){
        if (m_symtab.accessLocal(StructName) == null)
        {
            FuncSTO sto = new FuncSTO(StructName, new StructType(StructName));
            m_symtab.insert(sto);

		    m_symtab.openScope();
		    m_symtab.setFunc(sto);
            this.DoFormalParams(new Vector<String>());
            this.DoFuncDecl_2();

            
        }
        else {
            return;
        }
    }
	//----------------------------------------------------------------
	// Struct Constructor Dtor
	//----------------------------------------------------------------
	void DoStructorDecl(String id)
	{
        // destructor
        if (id.charAt(0) == '~'){
            String s = id.substring(1);
            if( !s.equals(StructName)){
                m_nNumErrors++;
			    m_errors.print(Formatter.toString(ErrorMsg.error13b_Dtor, id, StructName));
                
            }
            if (m_symtab.accessLocal(id) != null)
		    {
            	m_nNumErrors++;
			    m_errors.print(Formatter.toString(ErrorMsg.error9_Decl, id));
                
		    }

        }
        // constructor
        else {
            if (!id.equals(StructName) ){
                m_nNumErrors++;
			    m_errors.print(Formatter.toString(ErrorMsg.error13b_Ctor, id, StructName));
                
            }
        }
			
		FuncSTO sto = new FuncSTO(id,new StructType(id));
		m_symtab.insert(sto);

		m_symtab.openScope();
		m_symtab.setFunc(sto);
    
	}


    //----------------------------------------------------------------
	// Original
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



    //----------------------------------------------------------------
    // helper method to check if it's in struct
    //----------------------------------------------------------------
    void IsInStruct(){ isInStruct = !isInStruct;}

    //---------------------------------------------------------------
    // helper method that resolves multi-errors in same line for struct
    //---------------------------------------------------------------

    void DoFuncDecl_3(String id, Type t, Object o)
	{
        String s = o.toString();
		if (m_symtab.accessLocal(id) != null)
		{
            if(isInStruct){
                if (!(m_symtab.accessLocal(id) instanceof FuncSTO)){
                    m_nNumErrors++;
                    m_errors.print(Formatter.toString(ErrorMsg.error13a_Struct, id));
                    isMultiError = true;
                }
                
            }
            else {
                if (!(m_symtab.accessLocal(id) instanceof FuncSTO)) {
                    m_nNumErrors++;
			        m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
                }

            }
            
		}


      
	
		FuncSTO sto = new FuncSTO(id, t);
        
        if(s == "&"){
            sto.flag = true;
        }
        sto.setReturnType(t);
        m_symtab.insert(sto); 

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
        Vector<STO> over = new Vector<STO>();
		if (m_symtab.getFunc() == null)
		{
			m_nNumErrors++;
			m_errors.print ("internal: DoFormalParams says no proc!");
		}

		// insert parameters here
        FuncSTO sto = m_symtab.getFunc();
        for(int i = 0; i < params.size(); i++){
        
            sto.addParam(this.ProcessParams(params.get(i)));
        }
        sto.setTag(true);

        boolean overloadErr = false;

        //int comp;

        //struct case
        if(isInStruct){

            Scope localScope = m_symtab.getAboveScope();
            over = overloadFuncs(localScope.getLocals(), sto.getName());

            sto.setTag(false);
          // comp = 1;

        }
        //general case
        else{
         
            over = m_symtab.OverloadCheck(sto.getName());
            sto.setTag(false);
        }


        if (over.size() > 0) {

            for (int i = 0; i < over.size(); i++) {
                           


                Vector<STO> par = ((FuncSTO)over.elementAt(i)).getParams();
                if (par.size() == sto.getParams().size()) {


                    for(int curParam = 0; curParam < par.size(); curParam++) {
                        Type parType = par.elementAt(curParam).getType();
                        Type curType = sto.getParams().elementAt(curParam).getType();
                        if(!parType.isEquivalent(curType)) {
                            if(!isInStruct) {
                              m_symtab.addFunc(sto);
                            }
                            overloadErr = true;
                            break;
                        }
                    }

                    if(!overloadErr && !isMultiError) {
                        m_nNumErrors++;
                        m_errors.print(Formatter.toString(ErrorMsg.error9_Decl, sto.getName()));
                        return;
                    }
                }
                else {
                   if(!isInStruct) {
                      m_symtab.addFunc(sto);
                   }
                }
               
            }

        
        }
        else {
             if(!isInStruct) {
               m_symtab.addFunc(sto);
             }
        }

        isMultiError = false;
        
        m_symtab.setFunc(sto);
	}


    public Vector<STO> overloadFuncs(Vector<STO> functions,String funcName) {
        Vector<STO> overloaded = new Vector<STO>();
        for (int i =0; i < functions.size(); i++) {
            
            if (funcName.equals(functions.elementAt(i).getName()) && functions.get(i) instanceof FuncSTO   ) {
                if(((FuncSTO)functions.get(i)).isTag() == false) {
                  overloaded.add(functions.elementAt(i));
                }
            }
        }
        return overloaded;    
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

    //---------------------------------
    // get current scope in struct
    //---------------------------------
    void getStructScope(){
        scope = m_symtab.getCurrScope();
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

		
		return stoDes;
	}

    STO DoAssignTypeCheck(STO a, STO b) {
      

        
        if(a instanceof ErrorSTO) {
            return a;
        }
        else if (b instanceof ErrorSTO) {
            return b;
        }

        
        
        if ((!a.isModLValue()) && !( a instanceof StructdefSTO )) 
		{
			// Good place to do the assign checks
            m_errors.print(ErrorMsg.error3a_Assign);
            m_nNumErrors++;
            return new ErrorSTO(a.getName());
		}


        STO result;
        if (!b.getType().isAssignable(a.getType())) {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error3b_Assign,b.getType().getName(),a.getType().getName())); 
            return new ErrorSTO(a.getName()); 
           
                   
        }


        result = new ExprSTO(a.getName() + "=" + b.getName(), a.getType());
        result.setIsAddressable(false);
        result.setIsModifiable(false);

        return result;
    
    }


    //----------------------------------------------------------------
	// 
	//----------------------------------------------------------------

    void getName(String s)
    {
        name = s;
    }
	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	STO DoFuncCall(STO sto, Vector<STO> params)
	{
       

        boolean isStruct = false;
        if(sto instanceof ErrorSTO){
            return sto;
        }

     	if ((!sto.isFunc()) && (!sto.isStructdef()))
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.not_function, sto.getName()));
			return new ErrorSTO(sto.getName());
		}
        else {


             if(((FuncSTO)sto).getIsStruct() == true) {
                isStruct = true;
             }
          
            if (m_symtab.access(sto.getName()) == null) { // checks if functions exists
                m_nNumErrors++;
                m_errors.print(Formatter.toString(ErrorMsg.undeclared_id, sto.getName()));
                return new ErrorSTO("Error");
            }
            else{
                Vector<STO> fsto;
                Vector<STO> ovldList;
                if(isStruct) {

                  STO str = m_symtab.getStruct();  
                  Vector<STO> funone = ((StructdefSTO)str).OverloadCheck(sto.getName());
                  fsto = funone.get(0).getParams();
                  ovldList = ((StructdefSTO)str).OverloadCheck(sto.getName());
                }
                else {
                  fsto = m_symtab.access(sto.getName()).getParams(); // get function from sym tab
                  ovldList = m_symtab.OverloadCheck(sto.getName()); // get overload

                
                }
                if(ovldList.size() < 2){

                   
                    STO function;
                    if( params.size() != fsto.size()){
                        m_nNumErrors++;
                        m_errors.print(Formatter.toString(ErrorMsg.error5n_Call, params.size(), fsto.size()));
                        return new ErrorSTO(sto.getName());
                    }
                    else if( params.size() == fsto.size()){

                        if(params.size()==0 ){
                            function = ovldList.elementAt(0);
                           if(function.flag == true){
                              function.setIsAddressable(true);
                              function.setIsModifiable(true);
                           }
                             // pass by value
                           else {
                              function.setIsAddressable(false);
                              function.setIsModifiable(false);
                           }

                           return function;
                        } 
   
                         
                        for(int i = 0; i < fsto.size(); i++){

                          if(params.size() != 0) {if(params.get(i) instanceof ErrorSTO) {return params.get(i);} }//Nasty Error check for indexoutofbound 


                            if(fsto.get(i).flag == true || fsto.get(i).getType() instanceof ArrayType){ // added Array Type check 10/17 changed from flag to old way
                                if(!(params.get(i).getType().isEquivalent(fsto.get(i).getType()))){
                                    m_nNumErrors++;
                                    m_errors.print(Formatter.toString(ErrorMsg.error5r_Call, params.get(i).getType().getName(), fsto.get(i).getName(), fsto.get(i).getType().getName()));
                                    sto = new ErrorSTO("Error");
                                }
                                else if(!(params.get(i).isModLValue()) && !(params.get(i).getType() instanceof ArrayType)){
                                    m_nNumErrors++;
                                    m_errors.print(Formatter.toString(ErrorMsg.error5c_Call, fsto.get(i).getName(), fsto.get(i).getType().getName()));
                                    sto = new ErrorSTO("Error");
                                }

                            }

                            else {
                                 if(!(params.get(i).getType().isAssignable(fsto.get(i).getType()))){
                                    m_nNumErrors++;
                                    m_errors.print(Formatter.toString(ErrorMsg.error5a_Call, params.get(i).getType().getName(), fsto.get(i).getName(), fsto.get(i).getType().getName()));
                                    sto = new ErrorSTO("Error");
                                }
                            
                            }
                                                   
                        }
                        if(sto instanceof ErrorSTO) {
                           return sto;
                        }

                        function = ovldList.elementAt(0);
                        if(function.flag == true){
                           function.setIsAddressable(true);
                           function.setIsModifiable(true);
                        }
                          // pass by value
                        else {
                           function.setIsAddressable(false);
                           function.setIsModifiable(false);
                        }
                        return function;

                    }
                }
                else{ // overload check
                    int OverloadCnt = 0;
                    STO function;
                    for(int i = 0; i < ovldList.size(); i++){
                        Vector<STO> curPar = ovldList.elementAt(i).getParams();
                        if(curPar.size() == params.size()){
                            if(curPar.size() == 0 ) {
                               function = ovldList.elementAt(i);
                               if(function.flag == true){
                                  function.setIsAddressable(true);
                                  function.setIsModifiable(true);
                               }
                               // pass by value
                               else {
                                  function.setIsAddressable(false);
                                  function.setIsModifiable(false);
                               }
                               return function;
                            }


                            int ParamCnt = 0;
                            for(int j = 0; j < params.size(); j++){

                                //if(params.size() != 0) {
                                    if(params.get(j) instanceof ErrorSTO) {
                                        m_nNumErrors++;
                                        m_errors.print(Formatter.toString(ErrorMsg.error9_Illegal, sto.getName()));
                                        return new ErrorSTO("Error");
                                        //return params.get(j);
                                    } 
                                //}//Nasty Error check for indexoutofbound


                                Type parType = params.elementAt(j).getType();
                                Type curParType = curPar.elementAt(j).getType();
                                if(!parType.isEquivalent(curParType)){
                                    OverloadCnt++;
                                    break;
                                }
                                else{
                                    ParamCnt++;
                                }
                            }
                            if(ParamCnt == (params.size())) { // found match
                               function = ovldList.elementAt(i);
                               if(function.flag == true){
                                  function.setIsAddressable(true);
                                  function.setIsModifiable(true);
                               }
                               // pass by value
                               else {
                                  function.setIsAddressable(false);
                                  function.setIsModifiable(false);
                               }
                               return function;

                            }
                        }
                        else{
                            OverloadCnt++;
                        }

                        
                    }
                    if(OverloadCnt == (ovldList.size())){
                        m_nNumErrors++;
                        m_errors.print(Formatter.toString(ErrorMsg.error9_Illegal, sto.getName()));
                        return new ErrorSTO("Error");
                    }
                }

                 //return by reference calls to function values setting
                // true if pass by reference
                if(m_symtab.access(sto.getName()).flag == true){
                    sto.setIsAddressable(true);
                    sto.setIsModifiable(true);
                }
                // pass by value
                else {
                    sto.setIsAddressable(false);
                    sto.setIsModifiable(false);
                }
            
            }

        }

		return sto;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	STO DoDesignator2_Dot(STO sto, String strID)
	{
        if( sto instanceof ErrorSTO){
            return sto;
        }
        

		// Good place to do the struct checks
        if(!(sto.getType() instanceof StructType) ){
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error14t_StructExp, sto.getType().getName()));
            return new ErrorSTO("error");
   
        }

        if((sto.getName().equals("this"))){
            Scope scope = m_symtab.getAboveScope();
            Vector<STO> locals = scope.getLocals();
            for(int j = 0; j < locals.size(); j++){
                if(locals.get(j).getName().equals(strID)){
                    locals.get(j).setIsModifiable(true);
                    locals.get(j).setIsAddressable(true);
                    return locals.get(j);
                }

            }
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error14c_StructExpThis ,strID));
            return new ErrorSTO("error"); 

        }
       
       
        if((sto.getType() instanceof StructType) && (!sto.getName().equals("this"))) {

                Vector<STO> fun = ((StructdefSTO)sto).getFuncs();
                Vector<STO> var = ((StructdefSTO)sto).getVars();
        
                for(int i = 0; i < fun.size(); i++){
                   if(fun.get(i).getName().equals(strID)){
                       fun.get(i).setIsModifiable(true);
                       fun.get(i).setIsAddressable(true);
                       ((FuncSTO)fun.get(i)).setIsStruct(true);
                       m_symtab.setStruct((StructdefSTO)sto); // add
                       return fun.get(i);
                   }
                }
                for(int i = 0; i < var.size(); i++){
                   if(var.get(i).getName().equals(strID)){
                       var.get(i).setIsModifiable(true);
                       var.get(i).setIsAddressable(true);

                       return var.get(i);

                   }
                }



                m_nNumErrors++;
                m_errors.print(Formatter.toString(ErrorMsg.error14f_StructExp,strID, sto.getType().getName()));
                return new ErrorSTO("error");
        }
	    return sto;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	STO DoDesignator2_Array(STO sto, STO expr)
	{
		// Good place to do the array checks
        if (sto instanceof ErrorSTO){
            return sto;
        }
        else if(expr instanceof ErrorSTO){
            return expr;
        }

        STO ptr = this.DoNullPointerCheck(sto);
        if(ptr instanceof ErrorSTO){
            return ptr;
        }

        
        if (!(sto.getType() instanceof ArrayType) && !(sto.getType() instanceof PointerType)){
            
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error11t_ArrExp, sto.getType().getName()));
            return new ErrorSTO("error");
            
        }
        else if(!(expr.getType().isEquivalent(new IntType("int")))){
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error11i_ArrExp, expr.getType().getName()));
            return new ErrorSTO("error");
        }

        else if(expr instanceof ConstSTO && !(sto.getType() instanceof PointerType)){
            Type temp = sto.getType();
            if(temp instanceof ArrayType){
                if (temp.getSize()-1 < ((ConstSTO)expr).getIntValue()  || ( (ConstSTO )expr).getIntValue() < 0  ){
                    m_nNumErrors++;
                    m_errors.print(Formatter.toString(ErrorMsg.error11b_ArrExp, ((ConstSTO) expr).getIntValue(),temp.getSize()));
                    return new ErrorSTO("Error");
                }

            }
        }
        if(sto.getType() instanceof ArrayType) {
            VarSTO v = new VarSTO(sto.getName(), ((ArrayType)sto.getType()).getNext());
            v.setIsModifiable(true);
            v.setIsAddressable(true);
            return v;
        }
        else if(sto.getType() instanceof PointerType) {
            VarSTO v = new VarSTO(sto.getName(), ((PointerType)sto.getType()).getNext());
            v.setIsModifiable(true);
            v.setIsAddressable(true);
            return v;


        }
        
        return sto;
        
        
    }

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	STO DoDesignator3_ID(String strID)
	{
		STO sto;

        //change accesslocal to access might break things
		if ((sto = m_symtab.access(strID)) == null )
		{	
           // if((sto = m_symtab.accessGlobal(strID)) == null){
                m_nNumErrors++;
		        m_errors.print(Formatter.toString(ErrorMsg.undeclared_id, strID));
		        sto = new ErrorSTO(strID);
                return sto;
           // }
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

    // change access to accessGlobal 
		if (m_symtab.accessGlobal(strID) == null)
		{
			m_nNumErrors++;
		 	m_errors.print(Formatter.toString(ErrorMsg.undeclared_id, strID));
			return new ErrorType();
		}
        else{
            sto = m_symtab.access(strID); 

		    if (!sto.isStructdef())
		    {
			   m_nNumErrors++;
			   m_errors.print(Formatter.toString(ErrorMsg.not_type, sto.getName()));
			   return new ErrorType();
		    }


		return sto.getType();
        }
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
                if(result.getName() == "Mod-by-zero"){
                     m_nNumErrors++;
                     m_errors.print(ErrorMsg.error8_Arithmetic);
                     return result;
                     
                 }

                 m_errors.print(Formatter.toString(ErrorMsg.error1w_Expr,result.getName(),o.getOp(),"int"));                
            }

            else if(o.getOp().equals("+") || o.getOp().equals("-") || o.getOp().equals("/") || o.getOp().equals("*")) {
                 
                if((o.getOp().equals("/")) && (result.getName() == "Divide-by-zero")){
                     m_nNumErrors++;
                     m_errors.print(ErrorMsg.error8_Arithmetic);
                     return result;
                     
                }
                m_errors.print(Formatter.toString(ErrorMsg.error1n_Expr,result.getName(),o.getOp()));                
            }

            else if(o.getOp().equals("<") || o.getOp().equals("<=") || o.getOp().equals(">") || o.getOp().equals(">=")) {
                 m_errors.print(Formatter.toString(ErrorMsg.error1n_Expr,result.getName(),o.getOp())); 
            }
            else if(o.getOp().equals("==") || o.getOp().equals("!=")) {
                
                if(a.getType() instanceof PointerType && !(b.getType() instanceof PointerType)) {
                   m_errors.print(Formatter.toString(ErrorMsg.error17_Expr,o.getOp(),a.getType().getName(),b.getType().getName())); 
                }
                else if (!(a.getType() instanceof PointerType) && (b.getType() instanceof PointerType)) {
                    m_errors.print(Formatter.toString(ErrorMsg.error17_Expr,o.getOp(),a.getType().getName(),b.getType().getName()));                 
                }
                else {
                   m_errors.print(Formatter.toString(ErrorMsg.error1b_Expr,a.getType().getName(),o.getOp(),b.getType().getName())); 
                }

            }
            else if(o.getOp().equals("&&") || o.getOp().equals("||")) {
                m_errors.print(Formatter.toString(ErrorMsg.error1w_Expr,result.getName(),o.getOp(),"bool")); 
            }
            else if(o.getOp().equals("&") || o.getOp().equals("^") || o.getOp().equals("|")) {
                m_errors.print(Formatter.toString(ErrorMsg.error1w_Expr,result.getName(),o.getOp(),"int"));
            }

            result = new ErrorSTO("Error");
            return result;
            
        }
        result.setIsModifiable(false);
        result.setIsAddressable(false);

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


        if (!(a.getType() instanceof NumericType) && !(a.getType() instanceof PointerType)) {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error2_Type,a.getType().getName(), s1));
            return result = new ErrorSTO("Error");
        }
        else if (!(a.isModLValue())){
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error2_Lval,s1));
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
        String[] splitStr;
        String type;
        String id;
        String arr = "";
        String numArr = "";
        int dim = 0;
        Boolean isArray = false;
        Vector<Integer> sizes = new Vector<Integer>();

        
        if(s.contains("&")) {
            splitStr = s.split("&");
            type = splitStr[0].trim();
            id = "&" +splitStr[1].trim();
  
             
             if (id.contains("[") || id.contains("]")) {

                 
                 
               isArray = true;
               String[] splits = id.split(" ");;
               String newId = splits[0];
               arr = splits[1];
               numArr = splits[1];
               arr = splits[1].replaceAll("[^-?0-9]+", " ");
               String[] splitStrs;
               splitStrs = arr.trim().split(" ");
               for(int index = 0 ; index<splitStrs.length ; index++) {
                 sizes.add(Integer.parseInt(splitStrs[index]));
               }
               dim = sizes.size();
               id = newId;
 
             }
        
        }

    
        else if (s.contains("[") || s.contains("]")) {
               isArray = true;
               String[] split = s.split(" ");
               type = split[0];
               id = split[1];
               numArr = split[2];
               arr = split[2].replaceAll("[^-?0-9]+", " ");
               splitStr = arr.trim().split(" ");
         
               for(int index = 0 ; index<splitStr.length ; index++) {
                 sizes.add(Integer.parseInt(splitStr[index]));
               }
               dim = sizes.size();
 
        }

        else {
            splitStr = s.split("\\s+");
            type = splitStr[0].trim();
            id = splitStr[1].trim();

        }



        Type t;
        if(type.contains("*")) { // check for pointer
          int numPtr; 
          numPtr = this.CountChar(type,'*');
          String ptrStr = type.replace("*","");
          Vector<STO> ptrs = new Vector<STO>();
          for (int i =0; i <numPtr; i++) {
             ptrs.addElement(new ExprSTO("star"));
          }
          Type ptrType;
          switch (ptrStr) 
            {
               case "int" :  ptrType = new IntType("int");
                             break;
               case "float": ptrType = new FloatType("float");
                             break;
               case "bool":  ptrType = new BoolType("bool");
                             break;
               default:      ptrType = new StructType(type);
            }
            t = this.DoPointer(ptrType,ptrs);
        }
        else { // non pointers
           switch (type) 
            {
               case "int" :  t = new IntType("int");
                             break;
               case "float": t = new FloatType("float");
                             break;
               case "bool":  t = new BoolType("bool");
                             break;
               default:      t = new StructType(type);
        
            }
        
        }

        STO result;
        if(isArray == false){
            result = new VarSTO(id,t);
        }
        else{

            String name = type.concat("" + numArr);
            ArrayType aTopType = new ArrayType(name,sizes.get(0), dim);

            
            for(int i = 1; i <=dim; i++){
            
                if(i == dim){
                    aTopType.addNext(t);
                }
                else{  
                  int size = sizes.get(i);
                  String n = name.substring(0,name.lastIndexOf("["));
                  ArrayType typ = new ArrayType(n, size,dim-i);
                  aTopType.addNext(typ);
                }
                
                
                
            }

            result = new VarSTO(id,aTopType);
        }

        if (result.getName().contains("&")) {
            result.setName(result.getName().substring(1));
            result.flag = true;
        }

        m_symtab.insert(result);
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
        else if (m_symtab.getFunc().getReturnType() instanceof VoidType){
                    m_nNumErrors++;
                    m_errors.print(Formatter.toString(ErrorMsg.error6a_Return_type, expr.getType().getName(), m_symtab.getFunc().getReturnType().getName()));
                    return new ErrorSTO("Error");
        }

        return new ExprSTO("return", m_symtab.getFunc().getReturnType() );
        
    }


    STO DoReturnStmt(){
        if(!(m_symtab.getFunc().getReturnType() instanceof VoidType)){
                m_nNumErrors++;
                m_errors.print(ErrorMsg.error6a_Return_expr);
                return new ErrorSTO("Error");
        }

        return new ExprSTO("return", m_symtab.getFunc().getReturnType() );

    }

    STO MissingReturnStmt(Type typ, Vector<STO> stmtlist){
        int returncnt = 0;

        //if(stmtlist.isEmpty()) {
          //System.out.println(stmtlist.size());
        //}

        for(int i = 0; i < stmtlist.size(); i++){
           
            if(stmtlist.get(i) == null) {
                returncnt++;
                continue;
            }
            else if(stmtlist.get(i).getName().equals("return")){
                return stmtlist.get(i);
            }
           // else{
              //  returncnt++;
            //}
        }
        if(returncnt == stmtlist.size()){
            if (!(typ instanceof VoidType) ) {

               m_nNumErrors++;
               m_errors.print(ErrorMsg.error6c_Return_missing);
               return new ErrorSTO("Error");

            }
    
        }
        return new ExprSTO("return", typ);
    }



    STO DoExit(STO expr) {

        if(expr instanceof ErrorSTO) {
            return expr;
        }


        if(!(expr.getType().isAssignable(new IntType("int") ))) {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error7_Exit,expr.getType().getName()));
            return new ErrorSTO("error check 7");
        }
        else {
            return expr;
        }
    
    }

    STO DoUnarySign(String s, STO des){
        if(des instanceof ErrorSTO)
            return des;
        if(s == "-"){
            if( des instanceof ConstSTO){
                if(des.getType() instanceof IntType){
                    return new ConstSTO("-" + des.getName(), des.getType(), -1*((ConstSTO)des).getIntValue());
                }
                else if(des.getType() instanceof FloatType){
                    return new ConstSTO("-" + des.getName(), des.getType(), -1.0*((ConstSTO)des).getFloatValue());
                }
            }
            
        }
        return des;
    }

    public int CountChar(String s, char c) {
        int count = 0;
        for(char ch : s.toCharArray()) {
            if (ch == c ) {
                count++;
            } 
        }
        return count;
    }

    
    void DoInLoop(){
        isInLoop += 1;
    }

    void DoExitLoop(){
        isInLoop -= 1;
    }

    void DoBreak(){
        if(isInLoop <= 0){
            m_nNumErrors++;
            m_errors.print(ErrorMsg.error12_Break);
            return;
        }
    }

    void DoContinue(){
         if(isInLoop <= 0){
            m_nNumErrors++;
            m_errors.print(ErrorMsg.error12_Continue);
            return;
        }
    }

    StructdefSTO getStructSTO() {
        return m_symtab.getStruct();
    }


    STO DoNullPointerCheck(STO sto){
        if(sto.getType() instanceof NullPointerType){
            m_nNumErrors++;
            m_errors.print(ErrorMsg.error15_Nullptr);
            return new ErrorSTO("Error");

        }
        return sto;
    }
    // this is only for * 
    STO DoPointerCheck(STO sto){
        if (sto instanceof ErrorSTO){
            return sto;
        }
        if(!(sto.getType() instanceof PointerType)){ // Pointer
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error15_Receiver, sto.getType().getName()));
            return new ErrorSTO("Error");

            
        }

        return sto;
    }
    STO DoPointerArrowCheck(STO sto, String strID){
        if (sto instanceof ErrorSTO)
            return sto; 


        if(!(sto.getType() instanceof PointerType)){ // Arrow
           m_nNumErrors++;
           m_errors.print(Formatter.toString(ErrorMsg.error15_ReceiverArrow, sto.getType().getName()));
           return new ErrorSTO("Error");

        }
        else if(!(((PointerType)sto.getType()).getBaseType() instanceof StructType)){
           m_nNumErrors++;
           m_errors.print(Formatter.toString(ErrorMsg.error15_ReceiverArrow, sto.getType().getName()));
           return new ErrorSTO("Error");

        }
        else {
        
           STO struct = m_symtab.accessGlobal(((PointerType)sto.getType()).getBaseType().getName());
            
             
           Vector<STO> fun = ((StructdefSTO)struct).getFuncs();
           Vector<STO> var = ((StructdefSTO)struct).getVars();
        
           for(int i = 0; i < fun.size(); i++){
              if(fun.get(i).getName().equals(strID)){
                  fun.get(i).setIsModifiable(true);
                  fun.get(i).setIsAddressable(true);
                  return fun.get(i);
              }
           }
           for(int i = 0; i < var.size(); i++){
              if(var.get(i).getName().equals(strID)){
                   var.get(i).setIsModifiable(true);
                   var.get(i).setIsAddressable(true);
                   return var.get(i);

              }
           }

           m_nNumErrors++;
           m_errors.print(Formatter.toString(ErrorMsg.error14f_StructExp,strID, struct.getName()));
           return new ErrorSTO("error");

        }
        
    }

    // decl a pointer
    Type DoPointer(Type t, Vector<STO> ptrlist){
    
        PointerType TopType = new PointerType(t.getName() + this.PrintStar(ptrlist.size()));
        TopType.setNumPointers(ptrlist.size());
        if(ptrlist.isEmpty()){
            return t;
        }
        for (int i = ptrlist.size()-1; i >=0 ; i--){
            if(i == 0){
                TopType.addNext(t);
            }
            else{
                PointerType typ = new PointerType(t.getName()+ this.PrintStar(i));
                typ.setNumPointers(i);
                TopType.addNext(typ);
            }
        }

        
        return TopType;
    }


    STO DoDereference(STO sto){
        if(sto instanceof ErrorSTO){
            return sto;
        }
        VarSTO result = new VarSTO(sto.getName(),((PointerType)sto.getType()).getNext());
        if(!(((PointerType)sto.getType()).getNext() instanceof ArrayType)){
            result.setIsModifiable(true);
            result.setIsAddressable(true);
        }
        else{
            result.setIsModifiable(false);
            result.setIsAddressable(true);

        }
        return result;
    }



    String PrintStar(int i){
        return new String(new char[i]).replace("\0", "*");
    }

    STO DoNew(STO sto, Vector<STO> params){
        if(sto instanceof ErrorSTO)
            return sto;


        if(!sto.isModLValue()){
           m_nNumErrors++;
           m_errors.print(ErrorMsg.error16_New_var);
           return new ErrorSTO("error");
        }
        else if(!(sto.getType() instanceof PointerType)){
           m_nNumErrors++;
           m_errors.print(Formatter.toString(ErrorMsg.error16_New, sto.getType().getName()));
           return new ErrorSTO("error");           
        }

        if(sto.getType() instanceof PointerType) {
            if(params.size() == 0){
                if(!(((PointerType)sto.getType()).getNext() instanceof StructType)) {
                    return sto;
                }
                else if(((PointerType)sto.getType()).getNext() instanceof StructType){
                   this.DoCtorStructs(null, ((PointerType)sto.getType()).getNext()  ,params);
        
                }
            }
            else{
                if(((PointerType)sto.getType()).getNext() instanceof StructType){
                   this.DoCtorStructs(null, ((PointerType)sto.getType()).getNext()  ,params);
        
                }
                else if(!(((PointerType)sto.getType()).getNext() instanceof StructType)) {
                   m_nNumErrors++;
                   m_errors.print(Formatter.toString(ErrorMsg.error16b_NonStructCtorCall, sto.getType().getName()));
                   return new ErrorSTO("error"); 
                }



            }


        }
        return sto;

    }

    STO DoDelete(STO sto){
        if(sto instanceof ErrorSTO)
            return sto;


        if(!sto.isModLValue()){
           m_nNumErrors++;
           m_errors.print(ErrorMsg.error16_Delete_var);
           return new ErrorSTO("error");
        }
        else if(!(sto.getType() instanceof PointerType)){
           m_nNumErrors++;
           m_errors.print(Formatter.toString(ErrorMsg.error16_Delete, sto.getType().getName()));
           return new ErrorSTO("error");           
        }
        return sto;
    }

    STO DoAddressOf(STO sto){
        if(sto instanceof ErrorSTO){
            return sto;
        }

        if(!sto.getIsAddressable()){
           m_nNumErrors++;
           m_errors.print(Formatter.toString(ErrorMsg.error18_AddressOf, sto.getType().getName()));
           return new ErrorSTO("error"); 
        }

        PointerType ptr =  new PointerType(sto.getType().getName()+ this.PrintStar(1),1);
        ptr.addNext(sto.getType());

        ExprSTO expr = new ExprSTO(sto.getName(),ptr);
        expr.setIsAddressable(false);
        expr.setIsAddressable(false);
        return expr;
         
    }

    STO DoSizeOf(STO sto){
        if(sto instanceof ErrorSTO){
            return sto;
        }
        
        if(!(sto.getType() instanceof Type)){
           m_nNumErrors++;
           m_errors.print(ErrorMsg.error19_Sizeof);
           return new ErrorSTO("error"); 

        }
        if(!(sto.getIsAddressable())) {
           m_nNumErrors++;
           m_errors.print(ErrorMsg.error19_Sizeof);
           return new ErrorSTO("error");          
        }
        ConstSTO result;
 
        if(sto.getType() instanceof ArrayType){
          result = new ConstSTO(sto.getName(), new IntType("int"), ((ArrayType)sto.getType()).getTotalSize());

        }
        else {
          result = new ConstSTO(sto.getName(), new IntType("int"), sto.getType().getSize());
        
        }


        result.setIsAddressable(false);
        result.setIsModifiable(false);
        return result;       
    }





    STO DoSizeOf(Type t, Vector<STO> arraylist){
        if(!(t instanceof Type)){
           m_nNumErrors++;
           m_errors.print(ErrorMsg.error19_Sizeof);
           return new ErrorSTO("error"); 

        }
        ConstSTO result;

        if(!(arraylist.isEmpty())) {
            int numDim = arraylist.size();
            STO sizeStoTop = arraylist.elementAt(0);
            ArrayType aTopType = new ArrayType(t.getName(), ((ConstSTO)sizeStoTop).getIntValue(), numDim);

            
            for(int i = 1; i <=numDim; i++){
            
                if(i == numDim){
                    aTopType.addNext(t);
                }
                else{  
                  STO sizeSto = arraylist.elementAt(i);
                  ArrayType typ = new ArrayType(t.getName()+"["+ ((ConstSTO)sizeSto).getIntValue() +"]", ((ConstSTO)sizeSto).getIntValue(),numDim-i);
                  aTopType.addNext(typ);

                }                        
            }
		    result = new ConstSTO(t.getName(), new IntType("int"),aTopType.getTotalSize());
        }
        else {
           result = new ConstSTO(t.getName(), new IntType("int"), t.getSize());

        }
      

        result.setIsAddressable(false);
        result.setIsModifiable(false);
        return result;       
    }




    String processArray(Vector<STO> v) {
        String s = "";
      for(int i = 0; i < v.size();i++) {
         s = s.concat("["+v.get(i).getName() + "]");
      }
      return s; 
    
    }

    STO DoTypeCast(Type t, STO sto) {
        if(sto instanceof ErrorSTO){
            return sto;
        }


        if(sto.getType() instanceof BasicType || sto.getType() instanceof PointerType){

            STO res = new ConstSTO("temp");
            if(sto instanceof ConstSTO){

                Type typ = sto.getType();
                if( typ instanceof BoolType && t instanceof NumericType){
                    int val=0;
                    if(((ConstSTO)sto).getBoolValue()){val = 1;}
                    else if (!(((ConstSTO)sto).getBoolValue())) {val = 0;}
                    res = new ConstSTO(sto.getName(), t, val);
                }
               else if(typ instanceof IntType && t instanceof BoolType){
                    
                    res = new ConstSTO(sto.getName(), t, ((ConstSTO)sto).getIntValue());
               }
               else if(typ instanceof FloatType && t instanceof BoolType){
                    if(((ConstSTO)sto).getFloatValue() == 0.0)
                        res = new ConstSTO(sto.getName(), t, 0.0);
                    else if(((ConstSTO)sto).getFloatValue() != 0.0)
                        res = new ConstSTO(sto.getName(), t, 1.0);
   
               }
               else if(typ instanceof FloatType && t instanceof IntType){
                    float val = ((ConstSTO)sto).getFloatValue();
                    int intVal = (int)val;
                    res = new ConstSTO(sto.getName(), t,intVal);
               
               }
               else if(typ instanceof IntType && t instanceof FloatType){
                    float val = ((ConstSTO)sto).getFloatValue();
                    //float floatVal = (float)val;
                    res = new ConstSTO(sto.getName(), t, val);
               }
               else if(t instanceof PointerType && typ instanceof BasicType){
                    res = new ExprSTO(sto.getName(), t);
               }
            
            
            }
            else{
                res = new ExprSTO(sto.getName(), t);
                
            }
            res.setIsAddressable(false);
            res.setIsModifiable(false);
            return res;

        }
        else{
           m_nNumErrors++;
           m_errors.print(Formatter.toString(ErrorMsg.error20_Cast, sto.getType().getName(), t.getName()));
           return new ErrorSTO("error"); 

        }

        //return sto;
        

    }


}
