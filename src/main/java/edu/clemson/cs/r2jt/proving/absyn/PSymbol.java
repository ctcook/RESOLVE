package edu.clemson.cs.r2jt.proving.absyn;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.clemson.cs.r2jt.analysis.MathExpTypeResolver;
import edu.clemson.cs.r2jt.analysis.TypeResolutionException;
import edu.clemson.cs.r2jt.proving.immutableadts.ImmutableList;
import edu.clemson.cs.r2jt.proving.immutableadts.SimpleImmutableList;
import edu.clemson.cs.r2jt.type.Type;

/**
 * <p>A <code>PSymbol</code> represents a reference to a named element such as
 * a variable, constant, or function.  More specifically, all three are 
 * represented as function calls, with the former two represented as functions 
 * with no arguments.</p>
 */
public class PSymbol extends PExp {

    public static enum Quantification {

        NONE {

            protected Quantification flipped() {
                return NONE;
            }
        },
        FOR_ALL {

            protected Quantification flipped() {
                return THERE_EXISTS;
            }
        },
        THERE_EXISTS {

            protected Quantification flipped() {
                return FOR_ALL;
            }
        };

        protected abstract Quantification flipped();
    }

    public static enum DisplayType {

        PREFIX {

            protected String toString(PSymbol s) {
                String argumentsAsString;

                if (s.arguments.size() == 0) {
                    argumentsAsString = "";
                }
                else {
                    argumentsAsString =
                            "(" + delimit(s.arguments.iterator(), ", ") + ")";
                }

                return s.name + argumentsAsString;
            }

            protected void beginAccept(PExpVisitor v, PSymbol s) {
                v.beginPrefixPSymbol(s);
            }

            protected void fencepostAccept(PExpVisitor v, PSymbol s) {
                v.fencepostPrefixPSymbol(s);
            }

            protected void endAccept(PExpVisitor v, PSymbol s) {
                v.endPrefixPSymbol(s);
            }
        },
        INFIX {

            protected String toString(PSymbol s) {
                return "("
                        + delimit(s.arguments.iterator(), " " + s.name + " ")
                        + ")";
            }

            protected void beginAccept(PExpVisitor v, PSymbol s) {
                v.beginInfixPSymbol(s);
            }

            protected void fencepostAccept(PExpVisitor v, PSymbol s) {
                v.fencepostInfixPSymbol(s);
            }

            protected void endAccept(PExpVisitor v, PSymbol s) {
                v.endInfixPSymbol(s);
            }
        },
        POSTFIX {

            protected String toString(PSymbol s) {
                String retval = delimit(s.arguments.iterator(), ", ");

                if (s.arguments.size() > 1) {
                    retval = "(" + retval + ")";
                }

                return retval + s.name;
            }

            protected void beginAccept(PExpVisitor v, PSymbol s) {
                v.beginPostfixPSymbol(s);
            }

            protected void fencepostAccept(PExpVisitor v, PSymbol s) {
                v.fencepostPostfixPSymbol(s);
            }

            protected void endAccept(PExpVisitor v, PSymbol s) {
                v.endPostfixPSymbol(s);
            }
        },
        OUTFIX {

            protected String toString(PSymbol s) {
                return s.leftPrint + delimit(s.arguments.iterator(), ", ")
                        + s.rightPrint;
            }

            protected void beginAccept(PExpVisitor v, PSymbol s) {
                v.beginOutfixPSymbol(s);
            }

            protected void fencepostAccept(PExpVisitor v, PSymbol s) {
                v.fencepostOutfixPSymbol(s);
            }

            protected void endAccept(PExpVisitor v, PSymbol s) {
                v.endOutfixPSymbol(s);
            }
        };

        protected abstract String toString(PSymbol s);

        protected abstract void beginAccept(PExpVisitor v, PSymbol s);

        protected abstract void fencepostAccept(PExpVisitor v, PSymbol s);

        protected abstract void endAccept(PExpVisitor v, PSymbol s);
    }

    public final String name;
    public final SimpleImmutableList<PExp> arguments;
    public final Quantification quantification;

    final DisplayType displayType;
    final String leftPrint, rightPrint;

    private int myArgumentsSize;
    private final PExp[] myScratchSpace;

    public PSymbol(Type type, String leftPrint, String rightPrint,
            Collection<PExp> arguments, Quantification quantification,
            DisplayType display, MathExpTypeResolver typer) {

        this(type, leftPrint, rightPrint, new ImmutableList<PExp>(arguments),
                quantification, display, typer);
    }

    public PSymbol(Type type, String leftPrint, String rightPrint,
            SimpleImmutableList<PExp> arguments, Quantification quantification,
            DisplayType display, MathExpTypeResolver typer) {
        super(calculateHashes(leftPrint, rightPrint, arguments.iterator()),
                type, typer);

        if (rightPrint == null || leftPrint.equals(rightPrint)) {
            rightPrint = leftPrint;
            this.name = leftPrint;
        }
        else {
            this.name = leftPrint + rightPrint;
        }

        this.arguments = arguments;
        myArgumentsSize = arguments.size();
        myScratchSpace = new PExp[myArgumentsSize];

        this.quantification = quantification;
        this.leftPrint = leftPrint;
        this.rightPrint = rightPrint;

        displayType = display;
    }

    public PSymbol(Type type, String leftPrint, String rightPrint,
            Collection<PExp> arguments, DisplayType display,
            MathExpTypeResolver typer) {
        this(type, leftPrint, rightPrint, arguments, Quantification.NONE,
                display, typer);
    }

    public PSymbol(Type type, String name, Collection<PExp> arguments,
            Quantification quantification, DisplayType display,
            MathExpTypeResolver typer) {
        this(type, name, null, arguments, quantification, display, typer);
    }

    public PSymbol(Type type, String name, Collection<PExp> arguments,
            Quantification quantification, MathExpTypeResolver typer) {
        this(type, name, null, arguments, quantification, DisplayType.PREFIX,
                typer);
    }

    public PSymbol(Type type, String name, Collection<PExp> arguments,
            DisplayType display, MathExpTypeResolver typer) {
        this(type, name, arguments, Quantification.NONE, display, typer);
    }

    public PSymbol(Type type, String name, Collection<PExp> arguments,
            MathExpTypeResolver typer) {
        this(type, name, arguments, Quantification.NONE, DisplayType.PREFIX,
                typer);
    }

    public PSymbol(Type type, String name, Quantification quantification,
            MathExpTypeResolver typer) {
        this(type, name, new LinkedList<PExp>(), quantification,
                DisplayType.PREFIX, typer);
    }

    public PSymbol(Type type, String name, MathExpTypeResolver typer) {
        this(type, name, new LinkedList<PExp>(), Quantification.NONE,
                DisplayType.PREFIX, typer);
    }

    private static PExp.HashDuple calculateHashes(String left, String right,
            Iterator<PExp> args) {

        int structureHash;

        int leftHashCode = left.hashCode();
        int valueHash = leftHashCode;

        valueHash *= 59;
        if (right == null) {
            valueHash += leftHashCode;
        }
        else {
            valueHash += right.hashCode();
        }

        if (args.hasNext()) {
            structureHash = 17;

            int argMod = 2;
            PExp arg;
            while (args.hasNext()) {
                arg = args.next();
                structureHash += arg.structureHash * argMod;
                valueHash += arg.valueHash * argMod;
                argMod++;
            }
        }
        else {
            structureHash = 0;
        }

        return new PExp.HashDuple(structureHash, valueHash);
    }

    public void accept(PExpVisitor v) {
        v.beginPExp(this);
        v.beginPSymbol(this);
        displayType.beginAccept(v, this);

        boolean first = true;
        for (PExp arg : arguments) {
            if (!first) {
                displayType.fencepostAccept(v, this);
                v.fencepostPSymbol(this);
            }
            first = false;

            arg.accept(v);
        }

        displayType.endAccept(v, this);
        v.endPSymbol(this);
        v.endPExp(this);
    }

    public boolean isFunction() {
        return myArgumentsSize > 0;
    }

    @Override
    public boolean isVariable() {
        return !isFunction();
    }

    public SimpleImmutableList<PExp> getSubExpressions() {
        return arguments;
    }

    public boolean equals(Object o) {
        boolean retval = (o instanceof PSymbol);

        if (retval) {
            PSymbol oAsPSymbol = (PSymbol) o;

            retval =
                    (oAsPSymbol.valueHash == valueHash)
                            && name.equals(oAsPSymbol.name);

            if (retval) {
                Iterator<PExp> localArgs = arguments.iterator();
                Iterator<PExp> oArgs = oAsPSymbol.arguments.iterator();

                while (retval && localArgs.hasNext() && oArgs.hasNext()) {
                    retval = localArgs.next().equals(oArgs.next());
                }

                if (retval) {
                    retval = !(localArgs.hasNext() || oArgs.hasNext());
                }
            }
        }

        return retval;
    }

    private static String delimit(Iterator<?> i, String delimiter) {
        String retval = "";

        boolean first = true;
        while (i.hasNext()) {
            if (!first) {
                retval += delimiter;
            }
            else {
                first = false;
            }

            retval += i.next().toString();
        }

        return retval;
    }

    @Override
    public boolean isObviouslyTrue() {
        return (myArgumentsSize == 0 && name.equalsIgnoreCase("true"))
                || (myArgumentsSize == 2 && name.equals("=") && arguments
                        .get(0).equals(arguments.get(1)));
    }

    @Override
    public PExp substitute(Map<PExp, PExp> substitutions) {
        PExp retval = substitutions.get(this);

        if (retval == null) {
            boolean argumentChanged = false;
            int argIndex = 0;
            Iterator<PExp> argumentsIter = arguments.iterator();

            PExp argument;
            while (argumentsIter.hasNext()) {
                argument = argumentsIter.next();

                myScratchSpace[argIndex] = argument.substitute(substitutions);

                argumentChanged |= (myScratchSpace[argIndex] != argument);
                argIndex++;
            }

            if (argumentChanged) {
                retval =
                        new PSymbol(myType, leftPrint, rightPrint,
                                new ImmutableList<PExp>(myScratchSpace),
                                quantification, displayType, myTyper);
            }
            else {
                retval = this;
            }
        }

        return retval;
    }

    @Override
    protected void splitIntoConjuncts(List<PExp> accumulator) {
        if (myArgumentsSize == 2 && name.equals("and")) {
            arguments.get(0).splitIntoConjuncts(accumulator);
            arguments.get(1).splitIntoConjuncts(accumulator);
        }
        else {
            accumulator.add(this);
        }
    }

    public PSymbol setArgument(int index, PExp newArgument) {
        SimpleImmutableList<PExp> newArguments =
                arguments.set(index, newArgument);

        return new PSymbol(myType, leftPrint, rightPrint, newArguments,
                quantification, displayType, myTyper);
    }

    public PSymbol setArguments(Collection<PExp> newArguments) {
        return new PSymbol(myType, leftPrint, rightPrint, newArguments,
                quantification, displayType, myTyper);
    }

    public PSymbol setName(String newName) {
        return new PSymbol(myType, newName, rightPrint, arguments,
                quantification, displayType, myTyper);
    }

    @Override
    public PExp flipQuantifiers() {
        PExp retval;

        boolean argumentChanged = false;
        int argIndex = 0;
        Iterator<PExp> argumentsIter = arguments.iterator();

        PExp argument;
        while (argumentsIter.hasNext()) {
            argument = argumentsIter.next();

            myScratchSpace[argIndex] = argument.flipQuantifiers();

            argumentChanged |= (myScratchSpace[argIndex] != argument);
            argIndex++;
        }

        if (argumentChanged) {
            retval =
                    new PSymbol(myType, leftPrint, rightPrint, Arrays
                            .asList(myScratchSpace), quantification.flipped(),
                            displayType, myTyper);
        }
        else {
            Quantification flipped = quantification.flipped();

            if (flipped == quantification) {
                retval = this;
            }
            else {
                retval =
                        new PSymbol(myType, leftPrint, rightPrint, arguments,
                                flipped, displayType, myTyper);
            }
        }

        return retval;
    }

    @Override
    public void bindTo(PExp target, Map<PExp, PExp> accumulator)
            throws BindingException {

        PSymbol sTarget;
        try {
            sTarget = (PSymbol) target;
        }
        catch (ClassCastException e) {
            //We can only bind against other instances of PSymbol
            throw BINDING_EXCEPTION;
        }

        if (!typesMatch(target)) {
            //We can only bind against things with the same type
            throw BINDING_EXCEPTION;
        }

        //Note that at this point we're guaranteed that target is of the same
        //type as us
        if (quantification == Quantification.FOR_ALL) {
            if (myArgumentsSize == 0) {
                accumulator.put(this, target);
            }
            else {
                if (myArgumentsSize != sTarget.arguments.size()) {
                    //If we're a function, we can only bind against another
                    //function with the same number of arguments
                    throw BINDING_EXCEPTION;
                }

                accumulator.put(new PSymbol(myType, name, myTyper),
                        new PSymbol(sTarget.getType(), sTarget.name, myTyper));

                Iterator<PExp> thisArgumentsIter = arguments.iterator();
                Iterator<PExp> targetArgumentsIter =
                        sTarget.arguments.iterator();
                while (thisArgumentsIter.hasNext()) {
                    thisArgumentsIter.next().substitute(accumulator).bindTo(
                            targetArgumentsIter.next(), accumulator);
                }
            }
        }
        else {
            if (!name.equals(sTarget.name)) {
                throw BINDING_EXCEPTION;
            }

            if (myArgumentsSize != sTarget.arguments.size()) {
                //We aren't a "for all", so everything better be exact
                throw BINDING_EXCEPTION;
            }

            Iterator<PExp> thisArgumentsIter = arguments.iterator();
            Iterator<PExp> targetArgumentsIter = sTarget.arguments.iterator();
            while (thisArgumentsIter.hasNext()) {
                thisArgumentsIter.next().substitute(accumulator).bindTo(
                        targetArgumentsIter.next(), accumulator);
            }
        }
    }

    private boolean typesMatch(PExp target) {
        boolean retval;

        try {
            retval =
                    myTyper
                            .matchTypes(null, myType, target.myType, true,
                                    false);
        }
        catch (TypeResolutionException trex) {
            throw new RuntimeException("Couldn't match types.");
        }

        return retval;
    }

    @Override
    public PExpSubexpressionIterator getSubExpressionIterator() {
        return new PSymbolArgumentIterator(this);
    }

    @Override
    public boolean containsName(String name) {
        boolean retval = this.name.equals(name);

        Iterator<PExp> argumentIterator = arguments.iterator();
        while (!retval && argumentIterator.hasNext()) {
            retval = argumentIterator.next().containsName(name);
        }

        return retval;
    }

    @Override
    public Set<PSymbol> getQuantifiedVariablesNoCache() {
        Set<PSymbol> result = new HashSet<PSymbol>();

        if (myArgumentsSize == 0 && quantification != Quantification.NONE) {
            result.add(this);
        }

        Iterator<PExp> argumentIter = arguments.iterator();
        Set<PSymbol> argumentVariables;
        while (argumentIter.hasNext()) {
            argumentVariables = argumentIter.next().getQuantifiedVariables();
            result.addAll(argumentVariables);
        }

        return result;
    }

    @Override
    public boolean containsExistential() {
        boolean retval = (quantification == Quantification.THERE_EXISTS);

        Iterator<PExp> argumentIter = arguments.iterator();
        while (!retval && argumentIter.hasNext()) {
            retval = argumentIter.next().containsExistential();
        }

        return retval;
    }

    @Override
    public List<PExp> getFunctionApplicationsNoCache() {
        List<PExp> result = new LinkedList<PExp>();

        if (myArgumentsSize > 0) {
            result.add(this);
        }

        Iterator<PExp> argumentIter = arguments.iterator();
        List<PExp> argumentFunctions;
        while (argumentIter.hasNext()) {
            argumentFunctions = argumentIter.next().getFunctionApplications();
            result.addAll(argumentFunctions);
        }

        return result;
    }

    @Override
    public Set<String> getSymbolNamesNoCache() {

        Set<String> result = new HashSet<String>();

        if (quantification == Quantification.NONE) {
            result.add(name);
        }

        Iterator<PExp> argumentIter = arguments.iterator();
        Set<String> argumentSymbols;
        while (argumentIter.hasNext()) {
            argumentSymbols = argumentIter.next().getSymbolNames();
            result.addAll(argumentSymbols);
        }

        return result;
    }

    @Override
    public boolean isEquality() {
        return (myArgumentsSize == 2 && name.equals("="));
    }

    @Override
    public boolean isLiteral() {
        //XXX : All PExps originally come from Exps.  Currently there is no way
        //      to tell if an Exp is a literal.  I.e., in an expression like
        //      "S'' = empty_string", the left and right sides of the equality
        //      are indistinguishable except for their names.  Until this
        //      situation is resolved, literals should be hard coded here.
        return (name.equals("empty_string"));
    }
}
