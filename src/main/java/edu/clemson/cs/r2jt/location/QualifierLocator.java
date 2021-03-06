/*
 * This software is released under the new BSD 2006 license.
 * 
 * Note the new BSD license is equivalent to the MIT License, except for the
 * no-endorsement final clause.
 * 
 * Copyright (c) 2007, Clemson University
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the Clemson University nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This sofware has been developed by past and present members of the
 * Reusable Sofware Research Group (RSRG) in the School of Computing at
 * Clemson University. Contributors to the initial version are:
 * 
 * Steven Atkinson
 * Greg Kulczycki
 * Kunal Chopra
 * John Hunt
 * Heather Keown
 * Ben Markle
 * Kim Roche
 * Murali Sitaraman
 */
/*
 * QualifierLocator.java
 * 
 * The Resolve Software Composition Workbench Project
 * 
 * Copyright (c) 1999-2005
 * Reusable Software Research Group
 * Department of Computer Science
 * Clemson University
 */

package edu.clemson.cs.r2jt.location;

import edu.clemson.cs.r2jt.collections.Iterator;
import edu.clemson.cs.r2jt.collections.List;
import edu.clemson.cs.r2jt.collections.Stack;
import edu.clemson.cs.r2jt.data.Location;
import edu.clemson.cs.r2jt.data.ModuleID;
import edu.clemson.cs.r2jt.data.PosSymbol;
import edu.clemson.cs.r2jt.data.Symbol;
import edu.clemson.cs.r2jt.entry.*;
import edu.clemson.cs.r2jt.errors.ErrorHandler;
import edu.clemson.cs.r2jt.init.Environment;
import edu.clemson.cs.r2jt.scope.*;
import edu.clemson.cs.r2jt.type.*;

public class QualifierLocator {

    // ===========================================================
    // Variables
    // ===========================================================

    private ErrorHandler err;

    //private Environment env = Environment.getInstance();

    private SymbolTable table;

    // ===========================================================
    // Constructors
    // ===========================================================

    public QualifierLocator(SymbolTable table, ErrorHandler err) {
        this.table = table;
        this.err = err;
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    public boolean isProgramQualifier(PosSymbol qual) {
        ModuleScope mainscope = table.getModuleScope();
        ModuleScope module = locateModuleInStack(qual);
        if (module == null) {
            if (mainscope.isProgramVisible(qual.getSymbol())) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return true;
        }
    }

    public boolean isMathQualifier(PosSymbol qual) {
        ModuleScope mainscope = table.getModuleScope();
        ModuleScope module = locateModuleInStack(qual);
        if (module == null) {
            if (mainscope.isMathVisible(qual.getSymbol())) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    public ModuleScope locateMathModule(PosSymbol qual)
            throws SymbolSearchException {
        ModuleScope mainscope = table.getModuleScope();
        ModuleScope module = locateModuleInStack(qual);
        if (module == null) {
            if (mainscope.isMathVisible(qual.getSymbol())) {
                module = mainscope.getMathVisibleModule(qual.getSymbol());
            }
            else {
                String msg = cantFindMathModMessage(qual.toString());
                err.error(qual.getLocation(), msg);
                throw new SymbolSearchException();
            }
        }
        return module;
    }

    public ModuleScope locateProgramModule(PosSymbol qual)
            throws SymbolSearchException {
        assert qual != null : "qual is null";
        ModuleScope mainscope = table.getModuleScope();
        ModuleScope module = locateModuleInStack(qual);
        if (module == null) {
            if (mainscope.isProgramVisible(qual.getSymbol())) {
                module = mainscope.getProgramVisibleModule(qual.getSymbol());
            }
            else {
                String msg = cantFindProgModMessage(qual.toString());
                err.error(qual.getLocation(), msg);
                throw new SymbolSearchException();
            }
        }
        return module;
    }

    // ===========================================================
    // Private Methods
    // ===========================================================

    private ModuleScope locateModuleInStack(PosSymbol qual) {
        ModuleScope module = null;
        Stack<Scope> stack = table.getStack();
        Stack<Scope> hold = new Stack<Scope>();
        while (!stack.isEmpty()) {
            Scope scope = stack.pop();
            hold.push(scope);
            if (scope instanceof ProcedureScope) {
                module = locateModule(qual, (ProcedureScope) scope);
            }
            if (module != null) {
                break;
            }
            if (scope instanceof ProofScope) {
                module = locateModule(qual, (ProofScope) scope);
            }
            if (module != null) {
                break;
            }
        }
        while (!hold.isEmpty()) {
            stack.push(hold.pop());
        }
        return module;
    }

    private ModuleScope locateModule(PosSymbol name, ProcedureScope scope) {
        if (scope.containsVisibleModule(name.getSymbol())) {
            return scope.getVisibleModule(name.getSymbol());
        }
        else {
            return null;
        }
    }

    private ModuleScope locateModule(PosSymbol name, ProofScope scope) {
        if (scope.containsVisibleModule(name.getSymbol())) {
            return scope.getVisibleModule(name.getSymbol());
        }
        else {
            return null;
        }
    }

    // -----------------------------------------------------------
    // Error Related Methods
    // -----------------------------------------------------------

    private String cantFindProgModMessage(String qual) {
        return "The qualifier " + qual + " does not correspond to any "
                + "program modules visible from this scope.";
    }

    private String cantFindMathModMessage(String qual) {
        return "The qualifier " + qual + " does not correspond to any "
                + "modules visible from this scope.";
    }
}
