package edu.clemson.cs.r2jt.proving;

import java.util.Map;

import edu.clemson.cs.r2jt.absyn.Exp;
import edu.clemson.cs.r2jt.analysis.MathExpTypeResolver;
import edu.clemson.cs.r2jt.proving.absyn.PExp;

public class Consequent extends ImmutableConjuncts {

    public Consequent(Exp e, MathExpTypeResolver typer) {
        super(e, typer);
    }

    public Consequent(PExp e) {
        super(e);
    }

    public Consequent(Iterable<PExp> i) {
        super(i);
    }

    @Override
    public Consequent substitute(Map<PExp, PExp> mapping) {
        ImmutableConjuncts genericRetval = super.substitute(mapping);
        return new Consequent(genericRetval);
    }

    @Override
    public Consequent appended(Iterable<PExp> i) {
        ImmutableConjuncts genericRetval = super.appended(i);
        return new Consequent(genericRetval);
    }

    @Override
    public Consequent eliminateObviousConjuncts() {
        ImmutableConjuncts genericRetval = super.eliminateObviousConjuncts();
        return new Consequent(genericRetval);
    }

    @Override
    public Consequent removed(int index) {
        ImmutableConjuncts genericRetval = super.removed(index);
        return new Consequent(genericRetval);
    }

    @Override
    public Consequent eliminateRedundantConjuncts() {
        ImmutableConjuncts genericRetval = super.eliminateRedundantConjuncts();
        return new Consequent(genericRetval);
    }

    public Antecedent assumed() {
        return new Antecedent(this);
    }
}
