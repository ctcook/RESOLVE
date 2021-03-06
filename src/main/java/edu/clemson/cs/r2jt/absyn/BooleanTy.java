package edu.clemson.cs.r2jt.absyn;

import edu.clemson.cs.r2jt.analysis.TypeResolutionException;
import edu.clemson.cs.r2jt.data.PosSymbol;
import edu.clemson.cs.r2jt.type.BooleanType;
import edu.clemson.cs.r2jt.type.Type;

public class BooleanTy extends Ty {

    // ===========================================================
    // Variables
    // ===========================================================

    /** The qualifier member. */
    private PosSymbol qualifier;

    /** The name member. */
    private PosSymbol name;

    private Type retType = BooleanType.INSTANCE;

    // ===========================================================
    // Constructors
    // ===========================================================

    public BooleanTy() {};

    public BooleanTy(PosSymbol qualifier, PosSymbol name) {
        this.qualifier = qualifier;
        this.name = name;
    }

    // ===========================================================
    // Accessor Methods
    // ===========================================================

    // -----------------------------------------------------------
    // Get Methods
    // -----------------------------------------------------------

    /** Returns the value of the qualifier variable. */
    public PosSymbol getQualifier() {
        return qualifier;
    }

    /** Returns the value of the name variable. */
    public PosSymbol getName() {
        return name;
    }

    // -----------------------------------------------------------
    // Set Methods
    // -----------------------------------------------------------

    /** Sets the qualifier variable to the specified value. */
    public void setQualifier(PosSymbol qualifier) {
        this.qualifier = qualifier;
    }

    /** Sets the name variable to the specified value. */
    public void setName(PosSymbol name) {
        this.name = name;
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    /** Accepts a ResolveConceptualVisitor. */
    public void accept(ResolveConceptualVisitor v) {
        v.visitBooleanTy(this);
    }

    /** Accepts a TypeResolutionVisitor. */
    public Type accept(TypeResolutionVisitor v) throws TypeResolutionException {
        return retType;
        //return v.getNameTyType(this);
    }

    /** Returns a formatted text string of this class. */
    public String asString(int indent, int increment) {

        StringBuffer sb = new StringBuffer();

        printSpace(indent, sb);
        sb.append("BooleanTy\n");

        if (qualifier != null) {
            sb.append(qualifier.asString(indent + increment, increment));
        }

        if (name != null) {
            sb.append(name.asString(indent + increment, increment));
        }

        return sb.toString();
    }

    public void prettyPrint() {
        if (qualifier != null)
            System.out.print(qualifier.getName() + ".");
        System.out.print(name.getName());
    }

    public String toString(int indent) {
        StringBuffer sb = new StringBuffer();
        // 	if(qualifier != null) sb.append(qualifier.getName() + ".");
        sb.append(name.getName());
        return sb.toString();
    }

    public Ty copy() {
        PosSymbol newQualifier = null;
        if (qualifier != null)
            newQualifier = qualifier.copy();
        PosSymbol newName = name.copy();
        return new NameTy(newQualifier, newName);
    }

}
