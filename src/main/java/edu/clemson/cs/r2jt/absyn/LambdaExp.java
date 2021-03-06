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
 * LambdaExp.java
 * 
 * The Resolve Software Composition Workbench Project
 * 
 * Copyright (c) 1999-2005
 * Reusable Software Research Group
 * Department of Computer Science
 * Clemson University
 */

package edu.clemson.cs.r2jt.absyn;

import edu.clemson.cs.r2jt.collections.List;
import edu.clemson.cs.r2jt.data.Location;
import edu.clemson.cs.r2jt.data.PosSymbol;
import edu.clemson.cs.r2jt.type.Type;
import edu.clemson.cs.r2jt.analysis.TypeResolutionException;

public class LambdaExp extends Exp {

    // ===========================================================
    // Variables
    // ===========================================================

    /** The location member. */
    private Location location;

    /** The name member. */
    private PosSymbol name;

    /** The ty member. */
    private Ty ty;

    /** The body member. */
    private Exp body;

    // ===========================================================
    // Constructors
    // ===========================================================

    public LambdaExp() {};

    public LambdaExp(Location location, PosSymbol name, Ty ty, Exp body) {
        this.location = location;
        this.name = name;
        this.ty = ty;
        this.body = body;
    }

    // ===========================================================
    // Accessor Methods
    // ===========================================================

    // -----------------------------------------------------------
    // Get Methods
    // -----------------------------------------------------------

    /** Returns the value of the location variable. */
    public Location getLocation() {
        return location;
    }

    /** Returns the value of the name variable. */
    public PosSymbol getName() {
        return name;
    }

    /** Returns the value of the ty variable. */
    public Ty getTy() {
        return ty;
    }

    /** Returns the value of the body variable. */
    public Exp getBody() {
        return body;
    }

    // -----------------------------------------------------------
    // Set Methods
    // -----------------------------------------------------------

    /** Sets the location variable to the specified value. */
    public void setLocation(Location location) {
        this.location = location;
    }

    /** Sets the name variable to the specified value. */
    public void setName(PosSymbol name) {
        this.name = name;
    }

    /** Sets the ty variable to the specified value. */
    public void setTy(Ty ty) {
        this.ty = ty;
    }

    /** Sets the body variable to the specified value. */
    public void setBody(Exp body) {
        this.body = body;
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    public Exp substituteChildren(java.util.Map<Exp, Exp> substitutions) {
        return new LambdaExp(location, name, ty,
                substitute(body, substitutions));
    }

    /** Accepts a ResolveConceptualVisitor. */
    public void accept(ResolveConceptualVisitor v) {
        v.visitLambdaExp(this);
    }

    /** Accepts a TypeResolutionVisitor. */
    public Type accept(TypeResolutionVisitor v) throws TypeResolutionException {
        return v.getLambdaExpType(this);
    }

    /** Returns a formatted text string of this class. */
    public String asString(int indent, int increment) {

        StringBuffer sb = new StringBuffer();

        printSpace(indent, sb);
        sb.append("LambdaExp\n");

        if (name != null) {
            sb.append(name.asString(indent + increment, increment));
        }

        if (ty != null) {
            sb.append(ty.asString(indent + increment, increment));
        }

        if (body != null) {
            sb.append(body.asString(indent + increment, increment));
        }

        return sb.toString();
    }

    public boolean equivalent(Exp e) {
        boolean result = e instanceof LambdaExp;

        if (result) {
            LambdaExp eAsLambdaExp = (LambdaExp) e;

            result = eAsLambdaExp.getName().equals(name);
            result &= eAsLambdaExp.getBody().equivalent(body);
        }

        return result;
    }

    /** Returns true if the variable is found in any sub expression   
        of this one. **/
    public boolean containsVar(String varName, boolean IsOldExp) {
        if (name.toString().equals(varName)) {
            return true;
        }

        if (body != null) {
            return body.containsVar(varName, IsOldExp);
        }

        return false;
    }

    public List<Exp> getSubExpressions() {
        List<Exp> list = new List<Exp>();
        list.add(body);
        return list;
    }

    public void setSubExpression(int index, Exp e) {
        body = e;
    }

    public boolean shallowCompare(Exp e2) {
        if (!(e2 instanceof LambdaExp)) {
            return false;
        }
        if (!(name.equals(((LambdaExp) e2).getName().getName()))) {
            return false;
        }
        return true;
    }

    public Exp replace(Exp old, Exp replace) {
        if (!(old instanceof LambdaExp)) {
            LambdaExp result = (LambdaExp) this.copy();
            result.body = result.body.replace(old, replace);
            if (name != null) {
                if (old instanceof VarExp && replace instanceof VarExp) {
                    if (((VarExp) old).getName().toString().equals(
                            name.toString())) {
                        this.name = ((VarExp) replace).getName();
                        return this;
                    }
                }
            }
            return result;
        }
        return this;
    }

    public void prettyPrint() {
        System.out.print("lambda " + name.getName() + ": ");
        ty.prettyPrint();
        System.out.print(" (");
        body.prettyPrint();
        System.out.print(")");
    }

    public String toString(int indent) {
        StringBuffer sb = new StringBuffer();
        sb.append("lambda " + name.getName() + ": ");
        if (ty != null)
            sb.append(ty.toString(0));
        sb.append(" (");
        sb.append(body.toString(0));
        sb.append(")");
        return sb.toString();
    }

    public Exp copy() {
        PosSymbol newName = name.copy();
        Exp newBody = body.copy();
        Exp result = new LambdaExp(null, newName, ty, newBody);
        result.setType(type);

        return result;
    }

    public Object clone() {
        PosSymbol newName = name.copy();
        Exp newBody = (Exp) body.clone();
        Exp result = new LambdaExp(null, newName, ty, newBody);
        result.setType(type);

        return result;
    }

    public Exp remember() {

        if (body instanceof OldExp)
            this.setBody(((OldExp) (body)).getExp());

        if (body != null)
            body = body.remember();

        return this;
    }

}
