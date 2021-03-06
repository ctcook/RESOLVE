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
 * Exp.java
 * 
 * The Resolve Software Composition Workbench Project
 * 
 * Copyright (c) 1999-2005
 * Reusable Software Research Group
 * Department of Computer Science
 * Clemson University
 */

package edu.clemson.cs.r2jt.absyn;

import java.util.Iterator;
import java.util.Set;

import edu.clemson.cs.r2jt.collections.List;
import edu.clemson.cs.r2jt.data.Location;
import edu.clemson.cs.r2jt.data.PosSymbol;
import edu.clemson.cs.r2jt.data.Symbol;
import edu.clemson.cs.r2jt.type.BooleanType;
import edu.clemson.cs.r2jt.type.Type;
import edu.clemson.cs.r2jt.analysis.TypeResolutionException;

public abstract class Exp extends ResolveConceptualElement implements Cloneable {

    /*
     * These variables are useful to the proof checking classes and
     * will only be set if the -proofcheck flag in the environment is ON --
     *  Addendum HwS: But type should ultimately be set always!  And it is now
     *  set if you turn on -prove as well!
     */
    protected Type type = null;

    /** If the type can be determined in the builder we set it here.  */
    protected Type bType = null;

    //private boolean isLocal = false;
    private int marker = 0;

    public abstract void accept(ResolveConceptualVisitor v);

    public abstract Type accept(TypeResolutionVisitor v)
            throws TypeResolutionException;

    public abstract String asString(int indent, int increment);

    public abstract Location getLocation();

    public abstract boolean containsVar(String varName, boolean IsOldExp);

    public abstract List<Exp> getSubExpressions();

    public abstract void setSubExpression(int index, Exp e);

    //    public abstract boolean  equals(Exp exp, TypeMatcher tm);
    public String toString(int indent, int increment) {

        return new String();
    }

    public String toString(int indent) {
        String exp = "";
        //return this.toString();
        if (type != null)
            exp.concat(exp);
        return exp;
    }

    public Exp replace(Exp old, Exp replacement) {

        throw new UnsupportedOperationException("Replace not implemented for "
                + this.getClass() + ".");
        //return new VarExp();
    }

    public String toString() {
        return toString(0);
    }

    /**
     * <p>Returns a DEEP COPY of this expression, with all instances of 
     * <code>Exp</code>s that occur as keys in <code>substitutions</code> 
     * replaced with their corresponding values.</p>
     * 
     * <p>In general, a key <code>Exp</code> "occurs" in this <code>Exp</code>
     * if either this <code>Exp</code> or some subexpression is 
     * <code>equivalent()</code>.  However, if the key is a <code>VarExp</code>
     * function names are additionally matched, even though they would not
     * ordinarily match via <code>equivalent()</code>, so function names can
     * be substituted without affecting their arguments.</p>
     *   
     * @param substitutions A mapping from <code>Exp</code>s that should be
     *                      substituted out to the <code>Exp</code> that should
     *                      replace them.
     * @return A new <code>Exp</code> that is a deep copy of the original with
     *         the provided substitutions made.
     */
    public final Exp substitute(java.util.Map<Exp, Exp> substitutions) {
        Exp retval;

        boolean match = false;

        java.util.Map.Entry<Exp, Exp> curEntry = null;
        if (substitutions.size() > 0) {
            Set<java.util.Map.Entry<Exp, Exp>> entries =
                    substitutions.entrySet();
            Iterator<java.util.Map.Entry<Exp, Exp>> entryIter =
                    entries.iterator();
            //System.out.println("Recursing: " + this.toString(0) + " : " + this.getClass());
            while (entryIter.hasNext() && !match) {
                curEntry = entryIter.next();
                //System.out.print(curEntry.getKey().toString(0) + " --?-> " + curEntry.getValue().toString(0));
                match = curEntry.getKey().equivalent(this);

                /*if (match) {
                	System.out.println(" [Yes] ");
                }
                else {
                	System.out.println(" [ No] ");
                }*/
            }

            if (match) {
                //System.out.println(curEntry.getKey().toString(0) + " --> " + curEntry.getValue().toString(0));
                retval = curEntry.getValue();
            }
            else {
                retval = substituteChildren(substitutions);
            }
        }
        else {
            retval = this.copy();
        }

        return retval;
    }

    protected static Exp substitute(Exp e, java.util.Map<Exp, Exp> substitutions) {
        Exp retval;

        if (e == null) {
            retval = null;
        }
        else {
            retval = e.substitute(substitutions);
        }

        return retval;
    }

    /**
     * <p>Implemented by concrete subclasses of <code>Exp</code> to manufacture
     * a copy of themselves where all subexpressions have been appropriately
     * substituted.  The concrete subclass may assume that <code>this</code>
     * does not match any key in <code>substitutions</code> and thus need only
     * concern itself with performing substitutions in its children.</p>
     * 
     * @param substitutions A mapping from <code>Exp</code>s that should be
     *                      substituted out to the <code>Exp</code> that should
     *                      replace them.
     * @return A new <code>Exp</code> that is a deep copy of the original with
     *         the provided substitutions made.
     */
    protected abstract Exp substituteChildren(
            java.util.Map<Exp, Exp> substitutions);

    public Exp simplify() {
        return this;
    }

    public boolean equals(Exp exp) {
        return exp.toString(1).equals(this.toString(1));
    }

    public List<InfixExp> split(Exp assumpts, boolean single) {
        if (this instanceof InfixExp) {
            if (((InfixExp) this).getOpName().toString().equals("implies"))
                return this.split(null, false);
            else
                return this.split(null, single);
        }
        else if (single) {
            List<InfixExp> lst = new List<InfixExp>();
            if (assumpts == null) {
                lst.add(new InfixExp(null, null, createPosSymbol("implies"),
                        this));
            }
            else {
                lst.add(new InfixExp(null, assumpts,
                        createPosSymbol("implies"), this));
            }
            return lst;
        }
        else
            return new List<InfixExp>();
    }

    public List<InfixExp> split() {
        return this.split(null, true);
    }

    Exp getAssumptions() {
        return this;
    }

    /**
     * Builds a sequence of numSpaces spaces and returns that
     * sequence.
     */
    protected void printSpace(int numSpaces, StringBuffer buffer) {
        for (int i = 0; i < numSpaces; ++i) {
            buffer.append(" ");
        }
    }

    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError("But we are Cloneable!!!");
        }
    }

    public Exp copy() {
        System.out.println("Shouldn't be calling Exp.copy() from type "
                + this.getClass() + ".");
        throw new RuntimeException();
        //return null;
    }

    public void prettyPrint() {
        System.out.println("Shouldn't be calling Exp.prettyPrint()!");
    }

    public Type getType() {
        return type;
    }

    public void setType(Type t) {
        type = t;
    }

    //    public boolean isLocal() { return isLocal; }

    //    public void setIsLocal(boolean i) { isLocal = i; }

    public int getMarker() {
        return marker;
    }

    public void setMarker(int i) {
        marker = i;
    }

    public String proofCheckInfoToString() {
        String s = "";
        if (type == null) {
            s += " >>> TYPE: null\n";
        }
        else {
            s += " >>> TYPE: " + type.toString() + "\n";
        }
        s += " >>> Marker: " + marker + "\n";
        return s;
    }

    /**
     * <p>Shallow compare is too weak for many things, and equals() is too
     * strict.  This method returns <code>true</code> <strong>iff</code> this
     * expression and the provided expression, <code>e</code>, are equivalent
     * with respect to structure and all function and variable names.</p>
     * 
     * @param e The expression to compare this one to.
     * @return True <strong>iff</strong> this expression and the provided
     *         expression are equivalent with respect to structure and all
     *         function and variable names.
     */
    public boolean equivalent(Exp e) {
        System.out.println(e.toString(1));
        throw new UnsupportedOperationException(
                "Equivalence for classes of type " + this.getClass()
                        + " is not currently supported.");
    }

    /**
     * <p>Helper method to deal with <code>Exps</code>s that need to be 
     * compared but might be null.  Returns true <strong>iff</strong> 
     * <code>e1</code> and <code>e2</code> are both <code>null</code> or both 
     * are not <code>null</code> and equivalent.</p>
     * 
     * @param e1 The first <code>Exp</code>.
     * @param e2 The second <code>Exp</code>.
     * @return <code>true</code> <strong>iff</strong> both 
     * 		   <code>Exps</code>s are null; or both are not null and are
     *         equivalent.
     */
    public static boolean equivalent(Exp e1, Exp e2) {
        return !((e1 == null ^ e2 == null))
                && ((e1 == null && e2 == null) || e1.equivalent(e2));
    }

    /**
     * <p>Helper method to deal with <code>PosSymbol</code>s that need to be 
     * compared but might be null.  Returns true <strong>iff</strong> 
     * <code>s1</code> and <code>s2</code> are both <code>null</code> or both 
     * are not <code>null</code> and have names that are equivalent strings (see
     * <code>stringEquivalent</code>())</p>
     * 
     * @param s1 The first <code>PosSymbol</code>.
     * @param s2 The second <code>PosSymbol</code>.
     * @return <code>true</code> <strong>iff</strong> both 
     * <code>PosSymbol</code>s are null; or both are not null and have names
     * that are equivalent strigns (see <code>stringEquivalent</code>()).
     */
    public static boolean posSymbolEquivalent(PosSymbol s1, PosSymbol s2) {
        //The first line makes sure that either both s1 and s2 are null or
        //neither is.  If not, we short circuit with "false".
        //The second line short circuits and returns "true" if both are null.
        //The third line performs the string comparison.
        return !((s1 == null) ^ (s2 == null))
                && ((s1 == null && s2 == null) || (stringEquivalent(s1
                        .getName(), s2.getName())));
    }

    /**
     * <p>Helper method to deal with strings that need to be compared but might
     * be null.  Returns true <strong>iff</strong> <code>s1</code> and 
     * <code>s2</code> are both <code>null</code> or both are not null and
     * represent the same string (case sensitive).</p>
     * 
     * @param s1 The first string.
     * @param s2 The second string.
     * @return <code>true</code> <strong>iff</strong> both string are null;
     * or both are not null and represent the same string.
     */
    public static boolean stringEquivalent(String s1, String s2) {
        //The first line makes sure that either both s1 and s2 are null or
        //neither is.  If not, we short circuit with "false".
        //The second line short circuits and returns "true" if both are null.
        //The third line performs the string comparison.
        return !((s1 == null) ^ (s2 == null))
                && ((s1 == null && s2 == null) || (s1.equals(s2)));
    }

    public boolean shallowCompare(Exp e2) {
        return false;
    }

    public Exp remember() {
        return this;
    }

    public boolean containsExp(Exp exp) {
        return false;
    }

    public boolean containsExistential() {
        boolean retval = false;

        for (Exp e : getSubExpressions()) {
            retval = e.containsExistential();
        }

        return retval;
    }

    public Exp compareWithAssumptions(Exp exp) {
        if (this.equals(exp))
            return getTrueVarExp();
        return this;
    }

    private PosSymbol createPosSymbol(String name) {
        PosSymbol posSym = new PosSymbol();
        posSym.setSymbol(Symbol.symbol(name));
        return posSym;
    }

    public static InfixExp buildImplication(Exp antecedent, Exp consequent) {
        return new InfixExp(antecedent.getLocation(), antecedent,
                new PosSymbol(antecedent.getLocation(), Symbol
                        .symbol("implies")), consequent, BooleanType.INSTANCE);
    }

    public static InfixExp buildConjunction(Exp left, Exp right) {
        return new InfixExp(left.getLocation(), left, new PosSymbol(left
                .getLocation(), Symbol.symbol("and")), right,
                BooleanType.INSTANCE);
    }

    public static VarExp getTrueVarExp() {
        Symbol trueSym = Symbol.symbol("true");
        PosSymbol truePosSym = new PosSymbol();
        truePosSym.setSymbol(trueSym);
        VarExp trueExp = new VarExp(null, null, truePosSym);
        trueExp.setType(BooleanType.INSTANCE);

        return trueExp;
    }

    public void setLocation(Location locatoin) {

    }

    public Type getBType() {
        return bType;
    }
}
