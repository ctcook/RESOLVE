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
 * EnhancementBodyModuleDec.java
 * 
 * The Resolve Software Composition Workbench Project
 * 
 * Copyright (c) 1999-2005
 * Reusable Software Research Group
 * Department of Computer Science
 * Clemson University
 */

package edu.clemson.cs.r2jt.absyn;

import edu.clemson.cs.r2jt.collections.Iterator;
import edu.clemson.cs.r2jt.collections.List;
import edu.clemson.cs.r2jt.data.PosSymbol;
import edu.clemson.cs.r2jt.data.Symbol;

public class EnhancementBodyModuleDec extends ModuleDec {

    // ===========================================================
    // Variables
    // ===========================================================

    /** The name member. */
    private PosSymbol name;

    /** The performance profile name member. */
    private PosSymbol profileName;

    /** The parameters member. */
    private List<ModuleParameter> parameters;

    /** The enhancementName member. */
    private PosSymbol enhancementName;

    /** The conceptName member. */
    private PosSymbol conceptName;

    /** The enhancementBodies member. */
    private List<EnhancementBodyItem> enhancementBodies;

    /** The usesItems member. */
    private List<UsesItem> usesItems;

    /** The requires member. */
    private Exp requires;

    /** The conventions member. */
    private List<Exp> conventions;

    /** The corrs member. */
    private List<Exp> corrs;

    /** The facilityInit member. */
    private InitItem facilityInit;

    /** The facilityFinal member. */
    private FinalItem facilityFinal;

    /** The decs member. */
    private List<Dec> decs;

    // ===========================================================
    // Constructors
    // ===========================================================

    public EnhancementBodyModuleDec() {};

    public EnhancementBodyModuleDec(PosSymbol name, PosSymbol profileName,
            List<ModuleParameter> parameters, PosSymbol enhancementName,
            PosSymbol conceptName, List<EnhancementBodyItem> enhancementBodies,
            List<UsesItem> usesItems, Exp requires, List<Exp> conventions,
            List<Exp> corrs, InitItem facilityInit, FinalItem facilityFinal,
            List<Dec> decs) {
        this.name = name;
        this.profileName = profileName;
        this.parameters = parameters;
        this.enhancementName = enhancementName;
        this.conceptName = conceptName;
        this.enhancementBodies = enhancementBodies;
        this.usesItems = usesItems;
        this.requires = requires;
        this.conventions = conventions;
        this.corrs = corrs;
        this.facilityInit = facilityInit;
        this.facilityFinal = facilityFinal;
        this.decs = decs;
    }

    // ===========================================================
    // Accessor Methods
    // ===========================================================

    // -----------------------------------------------------------
    // Get Methods
    // -----------------------------------------------------------

    /** Returns the value of the name variable. */
    public PosSymbol getName() {
        return name;
    }

    /** Returns the value of the profileName variable. */
    public PosSymbol getProfileName() {
        return profileName;
    }

    /** Returns the value of the parameters variable. */
    public List<ModuleParameter> getParameters() {
        return parameters;
    }

    /** Returns the value of the enhancementName variable. */
    public PosSymbol getEnhancementName() {
        return enhancementName;
    }

    /** Returns the value of the conceptName variable. */
    public PosSymbol getConceptName() {
        return conceptName;
    }

    /** Returns the value of the enhancementBodies variable. */
    public List<EnhancementBodyItem> getEnhancementBodies() {
        return enhancementBodies;
    }

    /** Returns the value of the usesItems variable. */
    public List<UsesItem> getUsesItems() {
        return usesItems;
    }

    /** Returns the value of the requires variable. */
    public Exp getRequires() {
        return requires;
    }

    /** Returns the value of the conventions variable. */
    public List<Exp> getConventions() {
        return conventions;
    }

    /** Returns the value of the corrs variable. */
    public List<Exp> getCorrs() {
        return corrs;
    }

    /** Returns the value of the facilityInit variable. */
    public InitItem getFacilityInit() {
        return facilityInit;
    }

    /** Returns the value of the facilityFinal variable. */
    public FinalItem getFacilityFinal() {
        return facilityFinal;
    }

    /** Returns the value of the decs variable. */
    public List<Dec> getDecs() {
        return decs;
    }

    /** Returns a list of procedures in this realization. */
    public List<Symbol> getLocalProcedureNames() {
        List<Symbol> retval = new List<Symbol>();
        Iterator<Dec> it = decs.iterator();
        while (it.hasNext()) {
            Dec d = it.next();
            if (d instanceof ProcedureDec) {
                retval.add(d.getName().getSymbol());
            }
        }
        return retval;
    }

    // -----------------------------------------------------------
    // Set Methods
    // -----------------------------------------------------------

    /** Sets the name variable to the specified value. */
    public void setName(PosSymbol name) {
        this.name = name;
    }

    /** Sets the profileName variable to the specified value. */
    public void setProfileName(PosSymbol name) {
        this.profileName = name;
    }

    /** Sets the parameters variable to the specified value. */
    public void setParameters(List<ModuleParameter> parameters) {
        this.parameters = parameters;
    }

    /** Sets the enhancementName variable to the specified value. */
    public void setEnhancementName(PosSymbol enhancementName) {
        this.enhancementName = enhancementName;
    }

    /** Sets the conceptName variable to the specified value. */
    public void setConceptName(PosSymbol conceptName) {
        this.conceptName = conceptName;
    }

    /** Sets the enhancementBodies variable to the specified value. */
    public void setEnhancementBodies(List<EnhancementBodyItem> enhancementBodies) {
        this.enhancementBodies = enhancementBodies;
    }

    /** Sets the usesItems variable to the specified value. */
    public void setUsesItems(List<UsesItem> usesItems) {
        this.usesItems = usesItems;
    }

    /** Sets the requires variable to the specified value. */
    public void setRequires(Exp requires) {
        this.requires = requires;
    }

    /** Sets the conventions variable to the specified value. */
    public void setConventions(List<Exp> conventions) {
        this.conventions = conventions;
    }

    /** Sets the corrs variable to the specified value. */
    public void setCorrs(List<Exp> corrs) {
        this.corrs = corrs;
    }

    /** Sets the facilityInit variable to the specified value. */
    public void setFacilityInit(InitItem facilityInit) {
        this.facilityInit = facilityInit;
    }

    /** Sets the facilityFinal variable to the specified value. */
    public void setFacilityFinal(FinalItem facilityFinal) {
        this.facilityFinal = facilityFinal;
    }

    /** Sets the decs variable to the specified value. */
    public void setDecs(List<Dec> decs) {
        this.decs = decs;
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    /** Accepts a ResolveConceptualVisitor. */
    public void accept(ResolveConceptualVisitor v) {
        v.visitEnhancementBodyModuleDec(this);
    }

    /** Returns a formatted text string of this class. */
    public String asString(int indent, int increment) {

        StringBuffer sb = new StringBuffer();

        printSpace(indent, sb);
        sb.append("EnhancementBodyModuleDec\n");

        if (name != null) {
            sb.append(name.asString(indent + increment, increment));
        }

        if (parameters != null) {
            sb.append(parameters.asString(indent + increment, increment));
        }

        if (enhancementName != null) {
            sb.append(enhancementName.asString(indent + increment, increment));
        }

        if (conceptName != null) {
            sb.append(conceptName.asString(indent + increment, increment));
        }

        if (enhancementBodies != null) {
            sb
                    .append(enhancementBodies.asString(indent + increment,
                            increment));
        }

        if (usesItems != null) {
            sb.append(usesItems.asString(indent + increment, increment));
        }

        if (requires != null) {
            sb.append(requires.asString(indent + increment, increment));
        }

        if (conventions != null) {
            sb.append(conventions.asString(indent + increment, increment));
        }

        if (corrs != null) {
            sb.append(corrs.asString(indent + increment, increment));
        }

        if (facilityInit != null) {
            sb.append(facilityInit.asString(indent + increment, increment));
        }

        if (facilityFinal != null) {
            sb.append(facilityFinal.asString(indent + increment, increment));
        }

        if (decs != null) {
            sb.append(decs.asString(indent + increment, increment));
        }

        return sb.toString();
    }
}
