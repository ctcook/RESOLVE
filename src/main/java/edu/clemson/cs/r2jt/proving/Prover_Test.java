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
 * Hampton Smith
 */
package edu.clemson.cs.r2jt.proving;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import edu.clemson.cs.r2jt.Main;
import edu.clemson.cs.r2jt.ResolveCompiler;
import edu.clemson.cs.r2jt.absyn.Dec;
import edu.clemson.cs.r2jt.absyn.EqualsExp;
import edu.clemson.cs.r2jt.absyn.Exp;
import edu.clemson.cs.r2jt.absyn.InfixExp;
import edu.clemson.cs.r2jt.absyn.MathAssertionDec;
import edu.clemson.cs.r2jt.absyn.MathModuleDec;
import edu.clemson.cs.r2jt.absyn.ModuleDec;
import edu.clemson.cs.r2jt.analysis.MathExpTypeResolver;
import edu.clemson.cs.r2jt.collections.List;
import edu.clemson.cs.r2jt.compilereport.CompileReport;
import edu.clemson.cs.r2jt.data.ModuleID;
import edu.clemson.cs.r2jt.data.Symbol;
import edu.clemson.cs.r2jt.errors.ErrorHandler;
import edu.clemson.cs.r2jt.init.CompileEnvironment;
import edu.clemson.cs.r2jt.init.Environment;
import edu.clemson.cs.r2jt.proving.absyn.PExp;
import edu.clemson.cs.r2jt.scope.ModuleScope;
import edu.clemson.cs.r2jt.scope.SymbolTable;
import edu.clemson.cs.r2jt.utilities.Flag;
import edu.clemson.cs.r2jt.utilities.FlagDependencies;
import edu.clemson.cs.r2jt.utilities.FlagManager;
import edu.clemson.cs.r2jt.verification.Verifier;

/**
 * <p>The <code>Prover</code> accepts as its input 
 * <em>verification conditions</em> (VCs) and attempts to resolve each VC to
 * <code>true</code> using various logical steps.</p>
 * 
 * TODO : Currently this will only properly prove VCs originating from the 
 * "target file", which is the file named at the command line rather than any
 * files imported from that file.  This is because the Environment only gives us
 * a handle on the target file and not the currently compiled file.  This is
 * ok for the moment because it seems the Verifier works this way as well (see
 * getMainFile()) inside it.  But eventually we want to prove VCs for imported
 * files as well.  So for right now, we consider exactly those theories availale
 * to us from the target file, even if we're trying to import a different file
 * on the WAY to proving the target file. -HwS
 * 
 * TODO : Theorems are drawn only from Theories named in the target file and not
 * from those theories included from the theories named.  That is, if I include
 * String_Theory and String_Theory "uses" Boolean_Theory, the thoerems from
 * Boolean_Theory will not be imported. -HwS
 *  
 * @author H. Smith
 */
public final class Prover_Test {

    private static final double FITNESS_THRESHOLD = 0.8;
    private static final String FLAG_SECTION_NAME = "Proving";
    private static final String FLAG_DESC_PROVE =
            "Verify program with RESOLVE's integrated prover.";
    private static final String FLAG_DESC_LEGACY_PROVE =
            "Verify program with RESOLVE's legacy integrated prover.";
    private static final String FLAG_DESC_LEGACY_PROVE_ALIAS =
            "An alias for -prove.";
    private static final String FLAG_DESC_DEBUG =
            "Prompt user to guide the prover step-by-step.  May be used with "
                    + "either the -prove or -altprove options.";
    private static final String FLAG_DESC_VERBOSE =
            "Prints prover debugging information.  May be used with either the "
                    + "-prove or -altprove options.";
    private static final String FLAG_DESC_NOGUI =
            "Supresses any graphical interfaces so that the compiler can be run "
                    + "headlessly.";
    /**
     * <p>The main prover flag.  Causes the integrated prover to attempt to
     * dispatch generated VCs.</p>
     */
    public static final Flag FLAG_PROVE =
            new Flag(FLAG_SECTION_NAME, "altprove", FLAG_DESC_PROVE);
    /**
     * <p>The legacy prover flag.  In place for backwards compatibility--causes
     * the integrated prover to attempt to dispatch generated VCs using the
     * original prover circa January 2010.</p>
     */
    public static final Flag FLAG_LEGACY_PROVE =
            new Flag(FLAG_SECTION_NAME, "prove", FLAG_DESC_LEGACY_PROVE);
    /**
     * <p>An alias for FLAG_LEGACY_PROVE.  Also for backward compatibility.</p>
     */
    private static final Flag FLAG_LEGACY_PROVE_ALIAS =
            new Flag(FLAG_SECTION_NAME, "quickprove",
                    FLAG_DESC_LEGACY_PROVE_ALIAS, Flag.Type.HIDDEN);
    /**
     * <p>Causes the prover to prompt the user to choose a theorem at each step,
     * rather than choosing one on its own.</p>
     */
    public static final Flag FLAG_DEBUG =
            new Flag(FLAG_SECTION_NAME, "debugprove", FLAG_DESC_DEBUG,
                    Flag.Type.HIDDEN);
    /**
     * <p>Prints additional debugging information.</p>
     */
    public static final Flag FLAG_VERBOSE =
            new Flag(FLAG_SECTION_NAME, "verboseprove", FLAG_DESC_VERBOSE,
                    Flag.Type.HIDDEN);
    public static final Flag FLAG_NOGUI =
            new Flag(Main.FLAG_SECTION_GENERAL, "noGUI", FLAG_DESC_NOGUI);

    /**
     * <p>An auxiliary flag implied by any flag that attempts to do some
     * proving.  This is a single flag that can be checked to find out if
     * either the old or new prover is active.</p>
     */
    public static final Flag FLAG_SOME_PROVER =
            new Flag(Main.FLAG_SECTION_GENERAL, "someprover", "aux",
                    Flag.Type.AUXILIARY);

    /**
     * <p>This is a quick hack for the web demo to allow us to write
     * a big flashy CORRECT/INCORRECT line at the top of the output.</p>
     *
     * <p>It was added Sep 3, 2009 and should be replaced with a better method
     * post haste.  NOTHING SHOULD DEPEND ON THIS FUNCTIONALITY.</p>
     *
     * <p>Changed this to use the CompileReport class to report to the
     * webcompiler, so the static var was no longer needed. (Chuck)</p>
     */
    public boolean allProved;
    public StringBuffer output = new StringBuffer();
    private static DebugOptionsWindow myDebugOptions = null;
    /**
     * <p>A window displaying progress if OPTION_DISPLAY_PROGRES_WINDOW is on.
     * </p>
     */
    private ProofProgressWindow myProgressWindow;
    /**
     * <p>The current RESOLVE environment, from which we can get information on
     * the file structure and available modules.  This is particularly useful
     * in this class for tracking down mathematical theories associated with
     * various types.</p>
     *
     * <p>INVARIANT: <code>myResolveEnvironment != null</code></p>
     */
    //private final Environment myResolveEnvironment =
    //Environment.getInstance();
    /**
     * <p>An object to help determine the types of expressions based on the
     * current symbol table.  Only necessary because the Analyzer is currently
     * incomplete.  Eventually, each expression should contain its own type,
     * retrievable with the getType() method.</p>
     *
     * <p>INVARIANT: <code>myTyper != null</code>.</p>
     */
    private final MathExpTypeResolver myTyper;
    /**
     * <p>A list of theorems available in the current scope.  This will be built
     * up once at the very beginning of execution and then never touched.</p>
     */
    private final List<Exp> myTheorems = new List<Exp>();
    private final List<PExp> myPExpTheorems = new List<PExp>();
    private final List<Implication> myImplications = new List<Implication>();
    /**
     * <p>A list of the names of the theorems in <code>myTheorems</code> such
     * that the <code>i</code>th entry in <code>myTheorems</code> is named as
     * indicated by the <code>i</code>th entry in <code>myTheoremNames</code>.
     * </p>
     */
    private final List<String> myTheoremNames = new List<String>();
    private final CompileEnvironment myInstanceEnvironment;

    /**
     * <p>Constructs a new prover with the given <code>SymbolTable</code> and
     * sets it immediately to work on the verification conditions represented in
     * the provided <code>Collection</code> of <code>AssertiveCode</code>.  If
     * this constructor returns without throwing an exception, the VCs have been
     * proved.  Otherwise, an exception is thrown indicating which VC could not
     * be proved (or was proved inconsistent).</p>
     *
     * @param symbolTable The current symbol table.  May not be
     *                    <code>null</code>.
     * @param vCs A list of verification conditions in the form of
     *            <code>AssertiveCode</code>.  May not be <code>null</code>.
     * @param maxDepth The maximum length of proof the prover should consider.
     *
     * @throws UnableToProveException If a given VC cannot be proved in a
     *                                reasonable amount of time.
     * @throws VCInconsistentException If a given VC can be proved inconsistent.
     * @throes NullPointerException If <code>symbolTable</code> or
     *                              <code>vC</code> is <code>null</code>.
     */
    public Prover_Test(final MathExpTypeResolver typer,
            final Iterable<VerificationCondition> vCs,
            final CompileEnvironment instanceEnvironment)
            throws ProverException {

        myInstanceEnvironment = instanceEnvironment;

        allProved = true;

        if (!myInstanceEnvironment.flags.isFlagSet(FLAG_NOGUI)) {
            myProgressWindow = new ProofProgressWindow("VC", null);
        }

        myTyper = typer;
        buildTheories();

        try {
            proveVCs(vCs);

            CompileReport myReport = myInstanceEnvironment.getCompileReport();
            if (!myReport.hasError() && allProved) {
                myReport.setProveSuccess();
            }
        }
        catch (UnsupportedOperationException e) {
            //Exp.equivalent() is not consistently implemented throughout the
            //absyn package.  By default, it will just throw an
            //UnsupportedOperationException if it is called and has not been
            //overridden.  This will catch this case and produce a useful
            //error message.

            //On the other hand, it's sometimes useful for debugging to see a
            //full stack trace.  Uncomment this next line to see that instead:
            //if (true) throw new RuntimeException(e);

            ErrorHandler handler = myInstanceEnvironment.getErrorHandler();
            handler.error(e.getMessage()
                    + "\n\nTry disabling the -prove option.");
        }

        if (!myInstanceEnvironment.flags.isFlagSet(FLAG_NOGUI)) {
            myProgressWindow.dispose();
        }
    }

    /**
     * <p>Builds a list of <code>TheoremEntry</code>s representing all available
     * theorems for Theories currently in scope of the "target file".</p>
     *
     * <p>TODO : Currently this will not include theorems included in theories
     * referenced from included theories.  That is, if the target file lists
     * Set_Theory in its "uses" clause, and Set_Theory lists Boolean_Theory in
     * its "uses" clause, only the theorems from Set_Theory will be included.
     * </p>
     *
     * @return The list of Theorems.
     */
    private void buildTheories() {
        myTheorems.clear();
        myPExpTheorems.clear();

        File targetFileName = myInstanceEnvironment.getTargetFile();
        ModuleID targetFileID =
                myInstanceEnvironment.getModuleID(targetFileName);
        List<ModuleID> availableTheories =
                myInstanceEnvironment.getTheories(targetFileID);
        Exp curTheorem;

        //Add local axioms to the library
        ModuleDec targetDec = myInstanceEnvironment.getModuleDec(targetFileID);
        addLocalAxioms(targetDec);

        //Add any kind of mathematical assertions from any included library
        SymbolTable curSymbolTable;
        ModuleScope bindingsInScope;
        List<Symbol> symbolsInScope;
        for (ModuleID curModule : availableTheories) {
            curSymbolTable = myInstanceEnvironment.getSymbolTable(curModule);
            bindingsInScope = curSymbolTable.getModuleScope();
            symbolsInScope = bindingsInScope.getLocalTheoremNames();

            for (Symbol s : symbolsInScope) {

                curTheorem = bindingsInScope.getLocalTheorem(s).getValue();
                addTheoremToLibrary(s.getName(), curTheorem);

            }
        }
    }

    private void addLocalAxioms(ModuleDec module) {

        // TODO : Eventually axioms in any type of module should be supported

        if (module instanceof MathModuleDec) {
            MathModuleDec moduleAsMathModuleDec = (MathModuleDec) module;
            List<Dec> decs = moduleAsMathModuleDec.getDecs();
            addLocalAxioms(decs);
        }
    }

    private void addLocalAxioms(List<Dec> decs) {

        for (Dec d : decs) {
            if (d instanceof MathAssertionDec) {
                MathAssertionDec dAsMathAssertionDec = (MathAssertionDec) d;

                if (dAsMathAssertionDec.getKind() == MathAssertionDec.AXIOM) {
                    Exp theorem = dAsMathAssertionDec.getAssertion();

                    addTheoremToLibrary(d.getName().getName(), theorem);
                }
            }
        }
    }

    private void addTheoremToLibrary(String name, Exp theorem) {
        try {

            Exp quantifiersAppliedTheorem =
                    Utilities.applyQuantification(theorem);

            if (quantifiersAppliedTheorem instanceof EqualsExp) {
                myTheorems.add(quantifiersAppliedTheorem);
                myPExpTheorems.add(PExp.buildPExp(quantifiersAppliedTheorem,
                        myTyper));
                myTheoremNames.add(name);
            }
            else if (quantifiersAppliedTheorem instanceof InfixExp) {
                InfixExp theoremAsInfixExp =
                        (InfixExp) quantifiersAppliedTheorem;
                if (theoremAsInfixExp.getOpName().getName().equals("implies")) {
                    myImplications.add(new Implication(theoremAsInfixExp
                            .getLeft(), theoremAsInfixExp.getRight()));
                }
            }

        }
        catch (IllegalArgumentException e) {
            //This theorem contains a "where" clause and just shouldn't
            //be added.
        }
    }

    /**
     * <p>Attempts to prove a collection of VCs.  If this method returns without
     * throwing an exception, then all VCs were proved.</p>
     *
     * @param vCs A list of verification conditions in the form of
     *            <code>AssertiveCode</code>.  May not be <code>null</code>.
     * @param theorems A list of theorems which may be applied.  May not be
     *                 <code>null</code>.
     * @param maxDepth The maximum number of substitutions the prover should
     *                 attempt before giving up on a proof.
     *
     * @throws UnableToProveException If a given VC cannot be proved in a
     *                                reasonable amount of time.
     * @throws VCInconsistentException If a given VC can be proved inconsistent.
     * @throws NullPointerException If <code>vCs</code> or <code>theorems</code>
     *                              is <code>null</code>.
     */
    private void proveVCs(final Iterable<VerificationCondition> vcs)
            throws ProverException {

        Metrics metrics = new Metrics();

        FileWriter proofFile;
        try {
            proofFile = new FileWriter(getProofFileName());
        }
        catch (IOException e) {
            proofFile = null;
        }

        for (VerificationCondition vc : vcs) {
            proveVC(vc, metrics, proofFile);
        }

        if (!myInstanceEnvironment.flags.isFlagSet(ResolveCompiler.FLAG_WEB)) {
            if (allProved) {
                System.out.println("\n\n       Result: CORRECT\n\n");
            }
            else {
                System.out.println("\n\n       Result: Not correct\n\n");
            }
        }

        if (proofFile != null) {
            try {
                proofFile.flush();
                proofFile.close();
            }
            catch (Exception e) {}
        }

        System.out.println(output);
    }

    /**
     * <p>Prints various metrics out at the conclusion of a proof.</p>
     *
     * @param startTime The time at which the proof was begun, as returned from
     *                  <code>System.currentTimeMillis()</code>.
     * @param exitInformation A prover exception containing the metric
     *                        information to print.
     */
    private void printExitReport(long startTime,
            final ProverException exitInformation) {

        Metrics metrics = exitInformation.getMetrics();
        long endTime = System.currentTimeMillis();

        output.append((endTime - startTime) + " milliseconds.");
        /*System.out.print((endTime - startTime) +
        " milliseconds.");*/

        if (!myInstanceEnvironment.flags.isFlagSet(ResolveCompiler.FLAG_WEB)) {
            output.append("  Overall, " + metrics.getNumProofsConsidered()
                    + " proofs were directly considered and "
                    + metrics.numTimesBacktracked + " useful backtracks were "
                    + "performed.");
        }

        output.append("\n");

        /*
        System.out.println("  Overall, " + metrics.getNumProofsConsidered() +
        " proofs were directly considered and " +
        metrics.numTimesBacktracked + " useful backtracks were " +
        "performed.");
         */

        if (myInstanceEnvironment.flags.isFlagSet(FLAG_VERBOSE)) {
            output.append("PROOF:\n" + exitInformation);
        }
    }

    /**
     * <p>Attempts to prove a single VC.  If this method returns without
     * throwing an exception, then the VC was proved.</p>
     *
     * @param vC The verification condition to be proved.  May not be
     *           <code>null</code>.
     * @param theorems A list of theorems that may be applied as part of the
     *                 proof.  May not be <code>null</code>.
     * @param maxDepth The maximum number of steps the prover should attempt
     *                 before giving up on a proof.
     * @param metrics A reference to the metrics the prover should keep on the
     *                proof in progress.  May not be <code>null</code>.
     *
     * @throws UnableToProveException If the VC cannot be proved in a
     *                                reasonable amount of time.
     * @throws VCInconsistentException If the VC can be proved inconsistent.
     * @throws NullPointerException If <code>vC</code>, <code>theorems</code>,
     *                              or <code>metrics</code> is
     *                              <code>null</code>.
     */
    private void proveVC(final VerificationCondition vC, final Metrics metrics,
            FileWriter proofFile) throws VCInconsistentException {

        if (myInstanceEnvironment.flags.isFlagSet(FLAG_VERBOSE)) {
            System.out.println("\n\n############################# VC "
                    + "#############################");

            System.out.println(vC);
        }

        long startTime = System.currentTimeMillis();
        vC.propagateExpansionsInPlace();

        ProverException exitInformation = null;

        ActionCanceller c = new ActionCanceller();

        if (!myInstanceEnvironment.flags.isFlagSet(FLAG_NOGUI)) {
            myProgressWindow.setTitle("VC " + vC.getName());
            myProgressWindow.setActionCanceller(c);
        }

        VCProver p;

        if (myInstanceEnvironment.flags.isFlagSet(FLAG_PROVE)) {
            if (myInstanceEnvironment.flags.isFlagSet(FLAG_DEBUG)) {
                p = setUpProverDebug(vC);
            }
            else {
                p = setUpProver(vC);
            }
        }
        else {
            if (myInstanceEnvironment.flags.isFlagSet(FLAG_DEBUG)) {
                p = setUpOldProverDebug(vC);
            }
            else {
                p = setUpOldProver(vC);
            }
        }
        if (myInstanceEnvironment.flags.isFlagSet(ResolveCompiler.FLAG_WEB)) {
            output.append("<vcProve id=\"" + vC.getName() + "\">");
        }
        else {
            output.append(vC.getName() + " ");
        }
        //System.out.print(vC.getName() + " ");

        try {
            p.prove(vC.copy(), myProgressWindow, c, Long.MAX_VALUE);
        }
        catch (UnableToProveException e) {
            exitInformation = e;
            output.append("Skipped after ");
            //System.out.print("Skipped after ");
            allProved = false;

            if (proofFile != null) {
                try {
                    proofFile.append(vC.getName() + " failed.\n\n");
                }
                catch (IOException ex) {}
            }
        }
        catch (VCProvedException e) {
            exitInformation = e;
            output.append("Proved in ");
            //System.out.print("Proved in ");

            if (proofFile != null) {
                try {
                    proofFile.append(vC.getName() + " succeeded.\n\n");
                    proofFile.append(e.toString());
                }
                catch (IOException ex) {}
            }
        }

        printExitReport(startTime, exitInformation);
        if (myInstanceEnvironment.flags.isFlagSet(ResolveCompiler.FLAG_WEB)) {
            output.append("</vcProve>");
            myInstanceEnvironment.getCompileReport().setProveVCs(
                    output.toString());
        }
    }

    private VCProver setUpProverDebug(VerificationCondition vc) {
        ChainingIterable<VCTransformer> steps =
                new ChainingIterable<VCTransformer>();

        steps.add(new ChooserEncapsulationStep("Reduce",
                setUpReductionTransformer()));

        steps.add(new ExistentialInstantiationStep(myPExpTheorems));

        SubstitutionRuleNormalizer normalizer =
                new SubstitutionRuleNormalizer(myTyper, false);
        for (PExp e : myPExpTheorems) {
            steps.add(normalizer.normalize(e));
        }

        Antecedent antecedent;
        Consequent consequent;
        for (Implication i : myImplications) {

            antecedent = new Antecedent(i.getAntecedent(), myTyper);
            consequent = new Consequent(i.getConsequent(), myTyper);

            steps.add(new ConsequentWeakeningStep(antecedent, consequent));
            steps.add(new TheoryDevelopingStep(antecedent, consequent,
                    myPExpTheorems));
        }

        VCTransformer batchDeveloper = buildBatchTheoryDeveloper(5);

        return new AlternativeProver(
                myInstanceEnvironment,
                new FirstStepGivenTransformationChooser(
                        new SimplifyingTransformationChooser(
                                new GuidedTransformationChooser(steps, myTyper),
                                0), batchDeveloper), myTyper);
    }

    private TransformationChooser buildConsequentSubstitutions(
            TransformerFitnessFunction f) {

        return new UpfrontFitnessTransformationChooser(f,
                AbstractEqualityRuleNormalizer
                        .normalizeAll(new ConsequentSubstitutionRuleNormalizer(
                                myTyper, false), myPExpTheorems),
                FITNESS_THRESHOLD, myTyper, myInstanceEnvironment);
    }

    private TransformationChooser buildEquivalentAntecedentDevelopments(
            TransformerFitnessFunction f) {

        return new UpfrontFitnessTransformationChooser(f,
                AbstractEqualityRuleNormalizer.normalizeAll(
                        new AntecedentExtenderRuleNormalizer(myTyper, false),
                        myPExpTheorems), 0, myTyper, myInstanceEnvironment);
    }

    private TransformationChooser buildImplicationAntecedentDevelopments(
            TransformerFitnessFunction f) {

        Antecedent an;
        Consequent co;
        java.util.List<VCTransformer> l = new LinkedList<VCTransformer>();
        for (Implication i : myImplications) {

            an = new Antecedent(i.getAntecedent(), myTyper);
            co = new Consequent(i.getConsequent(), myTyper);

            l.add(new TheoryDevelopingStep(an, co, myPExpTheorems));
        }

        return new UpfrontFitnessTransformationChooser(f, l, 0, myTyper,
                myInstanceEnvironment);
    }

    private TransformationChooser buildConsequentStrengthenings(
            TransformerFitnessFunction f) {

        Antecedent an;
        Consequent co;
        java.util.List<VCTransformer> l = new LinkedList<VCTransformer>();
        for (Implication i : myImplications) {

            an = new Antecedent(i.getAntecedent(), myTyper);
            co = new Consequent(i.getConsequent(), myTyper);

            l.add(new ConsequentWeakeningStep(an, co));
        }

        return new UpfrontFitnessTransformationChooser(f, l, 0, myTyper,
                myInstanceEnvironment);
    }

    private VCTransformer buildBatchTheoryDeveloper(int iterations) {
        BatchTheoryDevelopmentStep developer =
                new BatchTheoryDevelopmentStep(myPExpTheorems, iterations);

        Antecedent an;
        Consequent co;
        for (Implication i : myImplications) {

            an = new Antecedent(i.getAntecedent(), myTyper);
            co = new Consequent(i.getConsequent(), myTyper);

            if (!co.containsQuantifiedVariableNotIn(an)) {
                developer.addImplicationTheorem(an, co);
            }
        }

        return developer;
    }

    private VCProver setUpProver(VerificationCondition vc) {

        TransformationChooser mainStrategy = setUpMainProofStrategy();
        VCTransformer batchDeveloper = buildBatchTheoryDeveloper(5);
        TransformationChooser reductionStep = setUpReductionTransformer();

        //First reduce, then develop
        /*TransformationChooser preprocessingStep =
         new FirstStepGivenTransformationChooser(
         reductionStep, batchDeveloper);*/

        //These are the different depth-first-searches of the proof
        //space we will try
        LinkedList<TransformationChooser> rounds =
                new LinkedList<TransformationChooser>();
        rounds.add(new LengthWindowTransformationChooser(mainStrategy, 0, 2));
        rounds.add(new LengthWindowTransformationChooser(mainStrategy, 3, 3));
        rounds.add(new LengthWindowTransformationChooser(mainStrategy, 4, 4));

        MultiStrategyProver retval = new MultiStrategyProver();
        VCProver curDepth;

        //Create a strategy for each defined depth-first-search
        String failoverNote = "--- End of reduction phase. ---";
        TransformationChooser developAndProve;
        for (TransformationChooser chooser : rounds) {

            //Right before we start proving, we need to develop theories
            developAndProve =
                    new FirstStepGivenTransformationChooser(chooser,
                            batchDeveloper);

            //And before everything, reduce
            curDepth =
                    new AlternativeProver(myInstanceEnvironment,
                            new FailoverChooser(reductionStep, developAndProve,
                                    failoverNote), myTyper);

            retval.addStrategy(curDepth);
        }

        return retval;
    }

    private TransformationChooser setUpReductionTransformer() {
        ChainingIterable<VCTransformer> steps =
                new ChainingIterable<VCTransformer>();

        SubstitutionRuleNormalizer normalizer =
                new SubstitutionRuleNormalizer(myTyper, false);
        for (PExp e : myPExpTheorems) {
            steps.add(normalizer.normalize(e));
        }

        return new NoBacktrackChooser(new ProductiveStepChooser(
                new UpfrontFitnessTransformationChooser(
                        new NormalizingTransformerFitnessFunction(), steps, 0,
                        myTyper, myInstanceEnvironment)));
    }

    private TransformationChooser setUpMainProofStrategy() {
        //Basic fitness function for equivalence rules
        TransformerFitnessFunction fitness =
                new SimpleTransformerFitnessFunction();

        //OK, first we're going to build objects to perform each of the basic
        //kinds of steps.

        //(A ^ B ==> C ^ D) + (C = E) becomes A ^ B ==> E ^ D
        TransformationChooser consequentSubstitutions =
                buildConsequentSubstitutions(fitness);

        //(A ^ B ==> C ^ D) + (A = E) becomes A ^ B ^ E ==> C ^ D
        TransformationChooser equivalentDevelopments =
                buildEquivalentAntecedentDevelopments(fitness);

        //(A ^ B ==> C ^ D) + (A ^ B ==> E) becomes A ^ B ^ E ==> C ^ D
        TransformationChooser implicationDevelopments =
                buildImplicationAntecedentDevelopments(fitness);

        //(A ^ B ==> C ^ D) + (E ^ F ==> C) becomes A ^ B ==> E ^ F ^ D
        TransformationChooser consequentStrengthenings =
                buildConsequentStrengthenings(fitness);

        //(A:a ^ B:b ==> there exists C:a s.t. C ^ D) becomes A ^ B ==> A ^ D
        VCTransformer instantiateExistential =
                new ExistentialInstantiationStep(myPExpTheorems);

        //###################### Now, the main proofsearch algorithm
        TransformationChooser workingChooser = consequentSubstitutions;

        //Include consequent strengthenings
        workingChooser =
                new ConcatenatingTransformationChooser(workingChooser,
                        consequentStrengthenings);

        //An object for all kinds of antecedent development
        TransformationChooser antecedentDevelopment =
                new ConcatenatingTransformationChooser(equivalentDevelopments,
                        implicationDevelopments);

        //Only develop the antecedent at the beginning of proofs, and only after
        //we've tried everything else (i.e., only as a last resort)
        workingChooser =
                new OnlyBeforeChooser(antecedentDevelopment, workingChooser,
                        true);

        //Always try to instantiate first
        workingChooser =
                new FirstChoiceGivenTransformationChooser(workingChooser,
                        instantiateExistential);

        return workingChooser;
    }

    private VCProver setUpOldProverDebug(VerificationCondition vc) {
        vc.simplify();

        GuidedRuleChooser chooser = new GuidedRuleChooser(myTyper);
        chooser.addRules(myTheoremNames, myTheorems);
        SingleStrategyProver slaveProver =
                new SingleStrategyProver(chooser, true, 0, myImplications,
                        myTyper, myInstanceEnvironment);

        return slaveProver;
    }

    private VCProver setUpOldProver(VerificationCondition vc) {
        BlindIterativeRuleChooser baseChooser =
        //new BlindIterativeRuleChooser(myTyper);
                new UpfrontFitnessSortRuleChooser(myTyper,
                        new SimpleFitnessFunction(), 0);
        baseChooser.addRules(myTheoremNames, myTheorems);
        baseChooser.lock(vc);

        vc.simplify();

        MultiStrategyProver p = new MultiStrategyProver();

        SingleStrategyProver slaveProver =
                new SingleStrategyProver(new LengthLimitedProvider(baseChooser,
                        2), true, 0, myImplications, myTyper,
                        myInstanceEnvironment);
        p.addStrategy(slaveProver);

        slaveProver =
                new SingleStrategyProver(new LengthLimitedProvider(baseChooser,
                        3), true, 3, myImplications, myTyper,
                        myInstanceEnvironment);
        p.addStrategy(slaveProver);

        return p;
    }

    private String getProofFileName() {
        File file = myInstanceEnvironment.getTargetFile();
        ModuleID cid = myInstanceEnvironment.getModuleID(file);
        file = myInstanceEnvironment.getFile(cid);
        String filename = file.toString();
        int temp = filename.indexOf(".");
        String tempfile = filename.substring(0, temp);
        String mainFileName;

        mainFileName = tempfile + ".proof";

        return mainFileName;
    }

    public void createFlags(FlagManager m) {}

    public static void setUpFlags() {
        Flag[] someProveFlag = { FLAG_LEGACY_PROVE, FLAG_PROVE };
        FlagDependencies.addRequires(FLAG_DEBUG, someProveFlag);
        FlagDependencies.addRequires(FLAG_VERBOSE, someProveFlag);

        FlagDependencies.addImplies(FLAG_LEGACY_PROVE_ALIAS, FLAG_LEGACY_PROVE);

        FlagDependencies.addExcludes(FLAG_LEGACY_PROVE, FLAG_PROVE);

        FlagDependencies.addExcludes(FLAG_NOGUI, FLAG_DEBUG);

        FlagDependencies.addImplies(FLAG_PROVE, FLAG_SOME_PROVER);
        FlagDependencies.addImplies(FLAG_LEGACY_PROVE, FLAG_SOME_PROVER);

        FlagDependencies.addImplies(FLAG_SOME_PROVER, Verifier.FLAG_VERIFY_VC);
    }
}
