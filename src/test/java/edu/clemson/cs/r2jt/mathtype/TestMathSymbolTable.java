package edu.clemson.cs.r2jt.mathtype;

import java.io.File;

import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.clemson.cs.r2jt.absyn.CallStmt;
import edu.clemson.cs.r2jt.absyn.MathModuleDec;
import edu.clemson.cs.r2jt.absyn.OutfixExp;
import edu.clemson.cs.r2jt.absyn.ResolveConceptualElement;
import edu.clemson.cs.r2jt.absyn.VariableNameExp;
import edu.clemson.cs.r2jt.data.Location;
import edu.clemson.cs.r2jt.data.Pos;
import edu.clemson.cs.r2jt.data.PosSymbol;
import edu.clemson.cs.r2jt.data.Symbol;
import edu.clemson.cs.r2jt.mathtype.DuplicateSymbolException;
import edu.clemson.cs.r2jt.mathtype.MTProper;
import edu.clemson.cs.r2jt.mathtype.MTType;
import edu.clemson.cs.r2jt.mathtype.MathSymbolTable;
import edu.clemson.cs.r2jt.mathtype.MathSymbolTableBuilder;
import edu.clemson.cs.r2jt.mathtype.MathSymbolTableEntry;
import edu.clemson.cs.r2jt.mathtype.ModuleIdentifier;
import edu.clemson.cs.r2jt.mathtype.ModuleScope;
import edu.clemson.cs.r2jt.mathtype.NoSuchModuleException;
import edu.clemson.cs.r2jt.mathtype.NoSuchScopeException;
import edu.clemson.cs.r2jt.mathtype.NoSuchSymbolException;
import edu.clemson.cs.r2jt.mathtype.Scope;
import edu.clemson.cs.r2jt.mathtype.ScopeBuilder;

public class TestMathSymbolTable {

    private PosSymbol myPosSymbol1 =
            new PosSymbol(new Location(new File("/some/file"), new Pos(1, 1)),
                    Symbol.symbol("x"));
    private PosSymbol myPosSymbol2 =
            new PosSymbol(new Location(new File("/some/file"), new Pos(1, 1)),
                    Symbol.symbol("y"));
    private PosSymbol myPosSymbol3 =
            new PosSymbol(new Location(new File("/some/file"), new Pos(1, 1)),
                    Symbol.symbol("z"));
    private PosSymbol myPosSymbol4 =
            new PosSymbol(new Location(new File("/some/file"), new Pos(1, 1)),
                    Symbol.symbol("w"));
    private ResolveConceptualElement myConceptualElement1 =
            new VariableNameExp();
    private ResolveConceptualElement myConceptualElement2 = new OutfixExp();
    private ResolveConceptualElement myConceptualElement3 = new CallStmt();
    private MTType myType1 = new MTProper();
    private MTType myType2 = new MTProper();
    private MTType myType3 = new MTProper();

    @Test(expected = NoSuchSymbolException.class)
    public void testFreshMathSymbolTable()
            throws NoSuchSymbolException,
                NoSuchModuleException {
        MathSymbolTableBuilder b = new MathSymbolTableBuilder();
        MathSymbolTable t = b.seal();

        t.getModuleScope(new ModuleIdentifier("NonExistent"));
    }

    @Test(expected = NoSuchScopeException.class)
    public void testFreshMathSymbolTableBuilder2() throws NoSuchModuleException {
        MathSymbolTableBuilder b = new MathSymbolTableBuilder();
        MathSymbolTable t = b.seal();

        t.getScope(myConceptualElement1);
    }

    @Test(expected = IllegalStateException.class)
    public void testSeal() throws NoSuchModuleException {
        MathSymbolTableBuilder b = new MathSymbolTableBuilder();
        MathModuleDec m = new MathModuleDec(myPosSymbol1, null, null, null);
        b.startModuleScope(m);
        b.seal();
    }

    @Test
    public void testStartModuleScope1()
            throws NoSuchSymbolException,
                NoSuchModuleException {
        MathSymbolTableBuilder b = new MathSymbolTableBuilder();

        MathModuleDec m = new MathModuleDec(myPosSymbol1, null, null, null);
        b.startModuleScope(m);
        b.endScope();

        MathSymbolTable t = b.seal();

        ModuleScope s = t.getModuleScope(new ModuleIdentifier("x"));
        assertTrue(s.getDefiningElement() == m);
    }

    @Test(expected = NoSuchSymbolException.class)
    public void testScopeGetInnermostBinding1()
            throws NoSuchSymbolException,
                NoSuchModuleException {
        MathSymbolTableBuilder b = new MathSymbolTableBuilder();

        MathModuleDec m = new MathModuleDec(myPosSymbol1, null, null, null);
        b.startModuleScope(m);
        b.endScope();

        MathSymbolTable t = b.seal();
        ModuleScope s = t.getModuleScope(new ModuleIdentifier("x"));

        s.getInnermostBinding("NonExistent");
    }

    @Test
    public void testScopeGetInnermostBinding2()
            throws NoSuchSymbolException,
                DuplicateSymbolException,
                NoSuchModuleException {
        MathSymbolTableBuilder b = new MathSymbolTableBuilder();

        MathModuleDec m = new MathModuleDec(myPosSymbol1, null, null, null);
        ScopeBuilder s = b.startModuleScope(m);
        s.addBinding("E", myConceptualElement1, myType1);
        b.endScope();

        MathSymbolTable t = b.seal();

        ModuleScope ms = t.getModuleScope(new ModuleIdentifier("x"));
        MathSymbolTableEntry e = ms.getInnermostBinding("E");

        assertEquals(e.getDefiningElement(), myConceptualElement1);
        assertEquals(e.getName(), "E");
        assertEquals(e.getType(), myType1);
    }

    @Test
    public void testScopeGetInnermostBinding4()
            throws NoSuchSymbolException,
                DuplicateSymbolException,
                NoSuchModuleException {
        MathSymbolTableBuilder b = new MathSymbolTableBuilder();

        MathModuleDec m = new MathModuleDec(myPosSymbol1, null, null, null);
        ScopeBuilder s = b.startModuleScope(m);

        s.addBinding("E", myConceptualElement1, myType1);

        s = b.startScope(myConceptualElement1);
        s.addBinding("E", myConceptualElement2, myType2);

        s = b.startScope(myConceptualElement3);
        s.addBinding("G", myConceptualElement3, myType3);

        b.endScope();
        b.endScope();
        b.endScope();

        MathSymbolTable t = b.seal();
        Scope ss = t.getScope(myConceptualElement3);

        MathSymbolTableEntry e = ss.getInnermostBinding("E");
        assertEquals(e.getDefiningElement(), myConceptualElement2);
        assertEquals(e.getName(), "E");
        assertEquals(e.getType(), myType2);
    }

    @Test
    public void testScopeAllBindings1() throws NoSuchModuleException {
        MathSymbolTableBuilder b = new MathSymbolTableBuilder();
        MathModuleDec m = new MathModuleDec(myPosSymbol1, null, null, null);

        b.startModuleScope(m);
        b.endScope();

        MathSymbolTable t = b.seal();
        Scope s = t.getScope(m);

        List<MathSymbolTableEntry> bindings = s.getAllBindings("NonExistent");
        assertEquals(bindings.size(), 0);

        s.buildAllBindingsList("NonExistent", bindings);
        assertEquals(bindings.size(), 0);
    }

    @Test
    public void testScopeAllBindings2()
            throws DuplicateSymbolException,
                NoSuchModuleException {
        MathSymbolTableBuilder b = new MathSymbolTableBuilder();

        MathModuleDec m = new MathModuleDec(myPosSymbol1, null, null, null);
        b.startModuleScope(m);

        ScopeBuilder s = b.getInnermostActiveScope();
        s.addBinding("E", myConceptualElement1, myType1);

        s = b.startScope(myConceptualElement1);
        s.addBinding("E", myConceptualElement2, myType2);

        s = b.startScope(myConceptualElement2);
        s.addBinding("G", myConceptualElement3, myType3);

        s = b.startScope(myConceptualElement3);
        s.addBinding("H", myConceptualElement1, myType1);
        s.addBinding("E", myConceptualElement3, myType3);
        s.addBinding("J", myConceptualElement2, myType2);

        b.endScope();
        b.endScope();
        b.endScope();
        b.endScope();

        MathSymbolTable t = b.seal();
        Scope ss = t.getScope(myConceptualElement3);

        List<MathSymbolTableEntry> bindings = ss.getAllBindings("E");
        assertEquals(bindings.size(), 3);

        bindings.clear();
        ss.buildAllBindingsList("E", bindings);
        assertEquals(bindings.size(), 3);
    }

    @Test
    public void testImportBehavior1()
            throws DuplicateSymbolException,
                NoSuchSymbolException,
                NoSuchModuleException {
        MathSymbolTableBuilder b = new MathSymbolTableBuilder();
        MathModuleDec m = new MathModuleDec(myPosSymbol1, null, null, null);
        ScopeBuilder s = b.startModuleScope(m);

        s.addBinding("E", myConceptualElement1, myType1);

        s = b.startScope(myConceptualElement1);
        s.addBinding("E", myConceptualElement2, myType2);

        b.endScope();
        b.endScope();
        //There is now a module "x" with two "E"s, one at the top level
        m = new MathModuleDec(myPosSymbol2, null, null, null);
        s = b.startModuleScope(m);
        s.addBinding("E", myConceptualElement1, myType1);
        b.endScope();
        //There is now a module "y" with a single "E" (at the top level)
        m = new MathModuleDec(myPosSymbol3, null, null, null);
        s = b.startModuleScope(m);
        b.addModuleImport(new ModuleIdentifier(myPosSymbol1.getName()));
        b.addModuleImport(new ModuleIdentifier(myPosSymbol2.getName()));
        s.addBinding("E", myConceptualElement1, myType1);
        b.endScope();
        //There is now a module "z" that imports both "x" and "y" and has a 
        //single "E", which is at the top level
        m = new MathModuleDec(myPosSymbol4, null, null, null);
        s = b.startModuleScope(m);
        b.addModuleImport(new ModuleIdentifier(myPosSymbol3.getName()));
        s.addBinding("E", myConceptualElement3, myType3);
        b.endScope();

        //There is now a module "w" that imports "z" and has a single "E", which
        //is at the top level.
        MathSymbolTable t = b.seal();
        Scope ss = t.getScope(m);

        MathSymbolTableEntry e =
                ss.getInnermostBinding("E",
                        MathSymbolTable.ImportStrategy.IMPORT_NONE);
        assertEquals(e.getDefiningElement(), myConceptualElement3);
        assertEquals(e.getName(), "E");
        assertEquals(e.getType(), myType3);

        e =
                ss.getInnermostBinding("E",
                        MathSymbolTable.ImportStrategy.IMPORT_NAMED);
        assertEquals(e.getDefiningElement(), myConceptualElement3);
        assertEquals(e.getName(), "E");
        assertEquals(e.getType(), myType3);

        e =
                ss.getInnermostBinding("E",
                        MathSymbolTable.ImportStrategy.IMPORT_RECURSIVE);
        assertEquals(e.getDefiningElement(), myConceptualElement3);
        assertEquals(e.getName(), "E");
        assertEquals(e.getType(), myType3);

        List<MathSymbolTableEntry> bindings =
                ss.getAllBindings("E",
                        MathSymbolTable.ImportStrategy.IMPORT_NONE);
        assertEquals(1, bindings.size());

        bindings =
                ss.getAllBindings("E",
                        MathSymbolTable.ImportStrategy.IMPORT_NAMED);
        assertEquals(2, bindings.size());

        bindings =
                ss.getAllBindings("E",
                        MathSymbolTable.ImportStrategy.IMPORT_RECURSIVE);
        assertEquals(bindings.size(), 4);

        bindings.clear();
        ss.buildAllBindingsList("E", bindings,
                MathSymbolTable.ImportStrategy.IMPORT_NONE);
        assertEquals(bindings.size(), 1);

        bindings.clear();
        ss.buildAllBindingsList("E", bindings,
                MathSymbolTable.ImportStrategy.IMPORT_NAMED);
        assertEquals(bindings.size(), 2);

        bindings.clear();
        ss.buildAllBindingsList("E", bindings,
                MathSymbolTable.ImportStrategy.IMPORT_RECURSIVE);
        assertEquals(bindings.size(), 4);
    }

    @Test(expected = NoSuchSymbolException.class)
    public void testImportBehavior2()
            throws DuplicateSymbolException,
                NoSuchSymbolException,
                NoSuchModuleException {
        MathSymbolTableBuilder b = new MathSymbolTableBuilder();
        MathModuleDec m = new MathModuleDec(myPosSymbol1, null, null, null);
        ScopeBuilder s = b.startModuleScope(m);

        s.addBinding("E", myConceptualElement1, myType1);

        s = b.startScope(myConceptualElement1);
        s.addBinding("E", myConceptualElement2, myType2);

        b.endScope();
        b.endScope();
        //There is now a module "x" with two "E"s, one at the top level
        m = new MathModuleDec(myPosSymbol2, null, null, null);
        s = b.startModuleScope(m);
        s.addBinding("E", myConceptualElement1, myType1);
        b.endScope();
        //There is now a module "y" with a single "E" (at the top level)
        m = new MathModuleDec(myPosSymbol3, null, null, null);
        s = b.startModuleScope(m);
        b.addModuleImport(new ModuleIdentifier(myPosSymbol1.getName()));
        b.addModuleImport(new ModuleIdentifier(myPosSymbol2.getName()));
        s.addBinding("E", myConceptualElement1, myType1);
        b.endScope();
        //There is now a module "z" that imports both "x" and "y" and has a 
        //single "E", which is at the top level
        m = new MathModuleDec(myPosSymbol4, null, null, null);
        s = b.startModuleScope(m);
        b.addModuleImport(new ModuleIdentifier(myPosSymbol3.getName()));
        b.endScope();

        //There is now a module "w" that imports "z" and has a no "E".
        MathSymbolTable t = b.seal();
        Scope ss = t.getScope(m);

        MathSymbolTableEntry e =
                ss.getInnermostBinding("E",
                        MathSymbolTable.ImportStrategy.IMPORT_NONE);
        assertEquals(e.getDefiningElement(), myConceptualElement3);
        assertEquals(e.getName(), "E");
        assertEquals(e.getType(), myType3);
    }

    @Test
    public void testImportBehavior3()
            throws DuplicateSymbolException,
                NoSuchSymbolException,
                NoSuchModuleException {
        MathSymbolTableBuilder b = new MathSymbolTableBuilder();
        MathModuleDec m = new MathModuleDec(myPosSymbol1, null, null, null);
        ScopeBuilder s = b.startModuleScope(m);

        s.addBinding("E", myConceptualElement1, myType1);

        s = b.startScope(myConceptualElement1);
        s.addBinding("E", myConceptualElement2, myType2);

        b.endScope();
        b.endScope();
        //There is now a module "x" with two "E"s, one at the top level
        m = new MathModuleDec(myPosSymbol2, null, null, null);
        s = b.startModuleScope(m);
        s.addBinding("E", myConceptualElement1, myType1);
        b.endScope();
        //There is now a module "y" with a single "E" (at the top level)
        m = new MathModuleDec(myPosSymbol3, null, null, null);
        s = b.startModuleScope(m);
        b.addModuleImport(new ModuleIdentifier(myPosSymbol1.getName()));
        b.addModuleImport(new ModuleIdentifier(myPosSymbol2.getName()));
        s.addBinding("E", myConceptualElement3, myType3);
        b.endScope();
        //There is now a module "z" that imports both "x" and "y" and has a 
        //single "E", which is at the top level
        m = new MathModuleDec(myPosSymbol4, null, null, null);
        s = b.startModuleScope(m);
        b.addModuleImport(new ModuleIdentifier(myPosSymbol3.getName()));
        b.endScope();

        //There is now a module "w" that imports "z" and has a no "E".
        MathSymbolTable t = b.seal();
        Scope ss = t.getScope(m);

        MathSymbolTableEntry e =
                ss.getInnermostBinding("E",
                        MathSymbolTable.ImportStrategy.IMPORT_NAMED);
        assertEquals(e.getDefiningElement(), myConceptualElement3);
        assertEquals(e.getName(), "E");
        assertEquals(e.getType(), myType3);
    }

    @Test(expected = DuplicateSymbolException.class)
    public void testImportBehavior4()
            throws DuplicateSymbolException,
                NoSuchSymbolException,
                NoSuchModuleException {
        MathSymbolTableBuilder b = new MathSymbolTableBuilder();
        MathModuleDec m = new MathModuleDec(myPosSymbol1, null, null, null);
        ScopeBuilder s = b.startModuleScope(m);

        s.addBinding("E", myConceptualElement1, myType1);

        s = b.startScope(myConceptualElement1);
        s.addBinding("E", myConceptualElement2, myType2);

        b.endScope();
        b.endScope();
        //There is now a module "x" with two "E"s, one at the top level
        m = new MathModuleDec(myPosSymbol2, null, null, null);
        s = b.startModuleScope(m);
        s.addBinding("E", myConceptualElement1, myType1);
        b.endScope();
        //There is now a module "y" with a single "E" (at the top level)
        m = new MathModuleDec(myPosSymbol3, null, null, null);
        s = b.startModuleScope(m);
        b.addModuleImport(new ModuleIdentifier(myPosSymbol1.getName()));
        b.addModuleImport(new ModuleIdentifier(myPosSymbol2.getName()));
        s.addBinding("E", myConceptualElement1, myType1);
        b.endScope();
        //There is now a module "z" that imports both "x" and "y" and has a 
        //single "E", which is at the top level
        m = new MathModuleDec(myPosSymbol4, null, null, null);
        s = b.startModuleScope(m);
        b.addModuleImport(new ModuleIdentifier(myPosSymbol3.getName()));
        b.endScope();

        //There is now a module "w" that imports "z" and has a no "E".
        MathSymbolTable t = b.seal();
        Scope ss = t.getScope(m);

        ss.getInnermostBinding("E",
                MathSymbolTable.ImportStrategy.IMPORT_RECURSIVE);
    }

    @Test(expected = NoSuchModuleException.class)
    public void testImportBehavior5()
            throws DuplicateSymbolException,
                NoSuchSymbolException,
                NoSuchModuleException {
        MathSymbolTableBuilder b = new MathSymbolTableBuilder();
        MathModuleDec m = new MathModuleDec(myPosSymbol1, null, null, null);
        b.startModuleScope(m);
        b.addModuleImport(new ModuleIdentifier("z"));
        b.endScope();
        //There is now a module "x" that imports non-existent module "z"
        m = new MathModuleDec(myPosSymbol2, null, null, null);
        b.startModuleScope(m);
        b.addModuleImport(new ModuleIdentifier("x"));
        b.endScope();
        //There is now a module "y" that imports module "x"
        b.seal();
    }
}
