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
 * Populator.java
 * 
 * The Resolve Software Composition Workbench Project
 * 
 * Copyright (c) 1999-2005
 * Reusable Software Research Group
 * Department of Computer Science
 * Clemson University
 */

package edu.clemson.cs.r2jt.population;

import edu.clemson.cs.r2jt.absyn.*;
import edu.clemson.cs.r2jt.collections.*;
import edu.clemson.cs.r2jt.data.*;
import edu.clemson.cs.r2jt.entry.*;
import edu.clemson.cs.r2jt.errors.*;
import edu.clemson.cs.r2jt.init.Environment;
import edu.clemson.cs.r2jt.init.CompileEnvironment;
import edu.clemson.cs.r2jt.location.TypeLocator;
import edu.clemson.cs.r2jt.scope.Binding;
import edu.clemson.cs.r2jt.scope.ModuleScope;
import edu.clemson.cs.r2jt.scope.SymbolTable;
import edu.clemson.cs.r2jt.scope.TypeID;
import edu.clemson.cs.r2jt.type.*;
import edu.clemson.cs.r2jt.treewalk.*;

public class NewPopulator extends TreeWalkerVisitor {

    // ===========================================================
    // Variables 
    // ===========================================================

    private SymbolTable table;

    //private Environment env = Environment.getInstance();
    private CompileEnvironment myInstanceEnvironment;

    private ErrorHandler err;

    private PosSymbol currentConcept = null;

    // ===========================================================
    // Constructors
    // ===========================================================

    public NewPopulator(SymbolTable table,
            CompileEnvironment instanceEnvironment) {
        myInstanceEnvironment = instanceEnvironment;
        this.table = table;
        this.err = instanceEnvironment.getErrorHandler();
    }

    // -----------------------------------------------------------
    // Type Translation
    // -----------------------------------------------------------

    private Type getConceptualType(Ty ty, PosSymbol name) {
        TypeConverter tc = new TypeConverter(table);
        return tc.getConceptualType(ty, name);
    }

    private List<Type> getProgramTypes(List<Ty> tys) {
        List<Type> retval = new List<Type>();
        for (Ty t : tys) {
            retval.add(getProgramType(t));
        }
        return retval;
    }

    private Type getProgramType(Ty ty) {
        TypeConverter tc = new TypeConverter(table);
        return tc.getProgramType(ty);
    }

    private Type getProgramType(Ty ty, PosSymbol name) {
        TypeConverter tc = new TypeConverter(table);
        return tc.getProgramType(ty, name);
    }

    private Type getMathType(Ty ty) {
        TypeConverter tc = new TypeConverter(table);
        return tc.getMathType(ty);
    }

    // -----------------------------------------------------------
    // Math Declarations
    // -----------------------------------------------------------

    private boolean isDecAVar(DefinitionDec dec) {
        List<MathVarDec> params1 = dec.getParameters();
        if (params1 == null)
            return false;
        Iterator<MathVarDec> i = params1.iterator();
        if (!i.hasNext()
                && !(getMathType(dec.getReturnTy()) instanceof FunctionType)) {
            if (dec.getDefinition() == null && dec.getBase() == null
                    && dec.getHypothesis() == null) {
                return true;
            }
        }
        return false;
    }

    private Type getTypeOfSetType(Type t) {
        if (t instanceof ConstructedType) {
            ConstructedType c = (ConstructedType) t;
            if (c.getName().getName().equalsIgnoreCase("set")) {
                List<Type> args = c.getArgs();
                if (args.size() == 1) {
                    return args.get(0);
                }
            }
        }
        return null;
    }

    private void addDefinitionAsType(DefinitionDec dec, Type type) {
        //TypeConverter tc = new TypeConverter(table);
        //Type t = tc.getMathType(dec.getReturnTy());
        Type typeOfSet = getTypeOfSetType(type);
        if (typeOfSet != null) {
            MathVarDec vd = null;
            Exp w = null;
            Exp o = null;
            if (dec.getDefinition() instanceof SetExp) {
                SetExp se = (SetExp) (dec.getDefinition());
                vd = se.getVar();
                w = se.getWhere();
                o = se.getBody();
            }
            TypeEntry te =
                    new TypeEntry(table.getCurrentScope(), dec.getName(),
                            typeOfSet, vd, w, o);
            table.addDefinitionTypeToScope(te);
        }
    }

    private void makeTypeCorrespondence(TypeID base, TypeID subtype) {
        TypeLocator tl = new TypeLocator(table, myInstanceEnvironment);

        try {
            TypeEntry baseEntry = tl.locateMathType(base);
            TypeEntry subtypeEntry = tl.locateMathType(subtype);
            table.addTypeCorrespondence(baseEntry.getType(), subtypeEntry
                    .getType());
        }
        catch (Exception e) {
            ;
        }
    }

    // -----------------------------------------------------------
    // Visitor Methods
    // -----------------------------------------------------------

    @Override
    public void preModuleDec(ModuleDec data) {
        table.createModuleScope();

        //Implicitly import Std_Boolean_Fac, but only if we're not in one of the
        //boolean modules required to compiled Std_Boolean_Face
        Symbol stdBooleanFac, booleanTemplate, booleanTheory;
        stdBooleanFac = Symbol.symbol("Std_Boolean_Fac");
        booleanTemplate = Symbol.symbol("Boolean_Template");
        booleanTheory = Symbol.symbol("Boolean_Theory");

        Symbol moduleDecName = data.getName().getSymbol();
        if (!(moduleDecName.equals(stdBooleanFac)
                || moduleDecName.equals(booleanTemplate) || moduleDecName
                .equals(booleanTheory))) {
            PosSymbol stdBooleanFacPosSymbol =
                    new PosSymbol(null, stdBooleanFac);
            ModuleID stdBooleanFacModuleID =
                    ModuleID.createFacilityID(stdBooleanFac);
            ModuleEntry entry =
                    new ModuleEntry(stdBooleanFacPosSymbol,
                            myInstanceEnvironment.getSymbolTable(
                                    stdBooleanFacModuleID).getModuleScope());
            table.addFacilityToScope(entry);
        }
    }

    @Override
    public void postModuleDec(ModuleDec data) {
        table.completeModuleScope();
    }

    @Override
    public void preEnhancementModuleDec(EnhancementModuleDec data) {
        table.addAssocConcept(data.getConceptName());
        table.addAssocVisibleModules();
    }

    @Override
    public void preConceptBodyModuleDec(ConceptBodyModuleDec data) {
        table.addConceptSpec(data.getConceptName());
        Iterator<PosSymbol> i = data.getEnhancementNames().iterator();
        while (i.hasNext()) {
            table.addEnhancementSpec(i.next(), data.getConceptName());
        }
        table.addAssocVisibleModules();
    }

    @Override
    public void preEnhancementBodyModuleDec(EnhancementBodyModuleDec data) {
        table.addEnhancementSpec(data.getEnhancementName(), data
                .getConceptName());
        table.addAssocConcept(data.getConceptName());
        table.addAssocVisibleModules();
        currentConcept = data.getConceptName();
    }

    @Override
    public void postEnhancementBodyItem(EnhancementBodyItem data) {
        table.addAssocEnhancement(data.getName(), currentConcept);
    }

    @Override
    public void postShortFacilityModuleDec(ShortFacilityModuleDec data) {
        table.createShortFacility(data.getName());
    }

    @Override
    public void postParameterVarDec(ParameterVarDec data) {
        VarEntry var =
                new VarEntry(table.getCurrentScope(), data.getMode(), data
                        .getName(), getProgramType(data.getTy()));
        table.addVariableToScope(var);
    }

    @Override
    public void preDefinitionDec(DefinitionDec dec) {
        //TODO : Can we factor out the type conversion so that we get
        //       the types when we visit them? -BD

        // store the math type
        Type mathType = getMathType(dec.getReturnTy());

        // If the defn. has no params, treat it as a VarEntry
        if (isDecAVar(dec)) {
            VarEntry vEntry =
                    new VarEntry(table.getCurrentScope(), Mode.MATH, dec
                            .getName(), mathType);
            table.addVariableToScope(vEntry);
        }
        // "Definition suc: N -> N;", etc.
        else if (dec.getReturnTy() instanceof FunctionTy) {
            // Add as a definition
            DefinitionEntry defEntry = null;
            FunctionType ftype = (FunctionType) (mathType);
            Type newParam = ftype.getDomain();
            Type retValue = ftype.getRange();
            PosSymbol ps = new PosSymbol(null, Symbol.symbol(""));
            // "Definition conj: B x B -> B;"
            if (newParam instanceof TupleType) {
                List<FieldItem> oldParams = ((TupleType) newParam).getFields();
                Iterator<FieldItem> i = oldParams.iterator();
                List<VarEntry> newParamList = new List<VarEntry>();
                while (i.hasNext()) {
                    VarEntry ve =
                            new VarEntry(table.getCurrentScope(), Mode.MATH,
                                    ps, i.next().getType());
                    newParamList.add(ve);
                }
                defEntry =
                        new DefinitionEntry(table.getCurrentScope(), dec
                                .getName(), newParamList, retValue);
                table.addDefinitionToScope(defEntry);
            }
            else {
                VarEntry vEntry =
                        new VarEntry(table.getCurrentScope(), Mode.MATH, ps,
                                newParam);
                List<VarEntry> params = new List<VarEntry>();
                params.add(vEntry);
                defEntry =
                        new DefinitionEntry(table.getCurrentScope(), dec
                                .getName(), params, retValue);
                table.addDefinitionToScope(defEntry);
            }
        }
        // "Definition suc(x: N): N = ...;"
        else {
            DefinitionEntry defEntry = null;
            List<VarEntry> params = new List<VarEntry>();

            if (dec.getParameters() != null) {
                Iterator<MathVarDec> paramsIt = dec.getParameters().iterator();
                while (paramsIt.hasNext()) {
                    MathVarDec mvDec = paramsIt.next();
                    VarEntry vEntry =
                            new VarEntry(table.getCurrentScope(), Mode.MATH,
                                    mvDec.getName(), getMathType(mvDec.getTy()));
                    params.add(vEntry);
                }
            }

            defEntry =
                    new DefinitionEntry(table.getCurrentScope(), dec.getName(),
                            params, mathType);
            table.addDefinitionToScope(defEntry);
        }
        addDefinitionAsType(dec, mathType);
        table.createDefinitionScope(dec.getName());
    }

    @Override
    public void postDefinitionDec(DefinitionDec data) {
        table.completeDefinitionScope();
    }

    @Override
    public void preMathAssertionDec(MathAssertionDec dec) {
        TheoremEntry entry = new TheoremEntry(dec.getName(), dec.getKind());
        entry.setValue(dec.getAssertion());
        table.addTheoremToScope(entry);
        assert dec.getAssertion() != null : "Assertion is null";
        table.createExpressionScope();
    }

    @Override
    public void postMathAssertionDec(MathAssertionDec data) {
        table.completeExpressionScope();
    }

    @Override
    public void preMathTypeDec(MathTypeDec data) {
        // TODO Auto-generated method stub
        super.preMathTypeDec(data);
    }

    @Override
    public void postMathTypeDec(MathTypeDec dec) {
        Type decType;

        if (dec.getTy() instanceof FunctionTy) {
            int paramCount = 0;
            FunctionTy ty = (FunctionTy) dec.getTy();
            if (ty.getDomain() instanceof TupleTy) {
                paramCount = ((TupleTy) ty.getDomain()).getFields().size();
            }
            else {
                paramCount = 1;
            }

            decType =
                    new PrimitiveType(table.getModuleID(), dec.getName(),
                            paramCount);
        }
        else {
            decType = new PrimitiveType(table.getModuleID(), dec.getName(), 0);
        }

        TypeEntry entry =
                new TypeEntry(table.getCurrentScope(), dec.getName(), decType);
        table.addTypeToScope(entry);
    }

    @Override
    public void postMathTypeFormalDec(MathTypeFormalDec dec) {
        Type type = new MathFormalType(table.getModuleID(), dec.getName());
        TypeEntry entry =
                new TypeEntry(table.getCurrentScope(), dec.getName(), type);
        table.addTypeToScope(entry);
    }

    @Override
    public void postSubtypeDec(SubtypeDec dec) {
        TypeID tid1 = new TypeID(dec.getQualifier1(), dec.getName1(), 0);
        TypeID tid2 = new TypeID(dec.getQualifier2(), dec.getName2(), 0);
        makeTypeCorrespondence(tid1, tid2);
    }

    @Override
    public void preFacilityTypeDec(FacilityTypeDec dec) {
        Type type = getProgramType(dec.getRepresentation(), dec.getName());
        TypeEntry entry =
                new TypeEntry(table.getCurrentScope(), dec.getName(), type);
        table.addTypeToScope(entry);
        table.createTypeScope();
    }

    @Override
    public void postFacilityTypeDec(FacilityTypeDec data) {
        table.completeTypeScope();
    }

    @Override
    public void preTypeDec(TypeDec dec) {
        Type type = getConceptualType(dec.getModel(), dec.getName());
        TypeEntry entry =
                new TypeEntry(table.getCurrentScope(), dec.getName(), type, dec
                        .getExemplar());
        table.addTypeToScope(entry);
        table.createTypeScope();
        VarEntry ex =
                new VarEntry(table.getCurrentScope(), Mode.EXEMPLAR, dec
                        .getExemplar(), type);
        table.addVariableToScope(ex);
    }

    @Override
    public void postTypeDec(TypeDec data) {
        table.completeTypeScope();
    }

    @Override
    public void preRepresentationDec(RepresentationDec data) {
        // TODO : what is going on here???
        super.preRepresentationDec(data);
    }

    @Override
    public void postRepresentationDec(RepresentationDec data) {
        // TODO what is going on here???
        super.postRepresentationDec(data);
    }

    @Override
    public void preFacilityModuleDec(FacilityModuleDec data) {
        // TODO Auto-generated method stub
        super.preFacilityModuleDec(data);
    }

    @Override
    public void postFacilityModuleDec(FacilityModuleDec data) {
        // TODO Auto-generated method stub
        super.postFacilityModuleDec(data);
    }
}
