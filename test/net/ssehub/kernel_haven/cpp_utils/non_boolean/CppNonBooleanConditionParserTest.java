package net.ssehub.kernel_haven.cpp_utils.non_boolean;

import static net.ssehub.kernel_haven.cpp_utils.InvalidConditionHandling.EXCEPTION;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.and;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.not;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.or;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.ssehub.kernel_haven.cpp_utils.CppConditionParser;
import net.ssehub.kernel_haven.cpp_utils.InvalidConditionHandling;
import net.ssehub.kernel_haven.util.cpp.parser.CppOperator;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.Variable;
import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;

/**
 * Tests the {@link CppNonBooleanConditionParser}.
 *
 * @author Adam
 * @author El-Sharkawy
 */
public class CppNonBooleanConditionParserTest {

    /**
     * Tests a variable with underscore characters.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testUnderscore() throws ExpressionFormatException {
        CppConditionParser parser = new CppNonBooleanConditionParser(false, EXCEPTION);
        
        assertThat(parser.parse("defined(__a_b__)"), is(new Variable("__a_b__")));
    }
    
    /**
     * Tests a more complex condition that uses all boolean operators.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testComplexCondition() throws ExpressionFormatException {
        CppConditionParser parser = new CppNonBooleanConditionParser(false, EXCEPTION);
        
        assertThat(parser.parse("(defined(A) && (!defined(B) || defined(C)))"), is(and("A", or(not("B"), "C"))));
    }
    
    /**
     * Tests parsing of constant literals.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testLiterals() throws ExpressionFormatException {
        CppConditionParser parser = new CppNonBooleanConditionParser(false, EXCEPTION);
        
        assertThat(parser.parse("0"), is(new Literal("0")));
        assertThat(parser.parse("-0"), is(new Literal("-0")));
        assertThat(parser.parse("1"), is(new Literal("1")));
        assertThat(parser.parse("2"), is(new Literal("2")));
        assertThat(parser.parse("-2"), is(new Literal("-2")));
    }

    /**
     * Tests an defined (VAR) with a space before the bracket.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testDefinedWithSpace() throws ExpressionFormatException {
        CppConditionParser parser = new CppNonBooleanConditionParser(false, EXCEPTION);
        
        assertThat(parser.parse("defined (A)"), is(new Variable("A")));
    }
    
    /**
     * Tests an defined VAR without the brackets.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testDefinedWithoutBrackets() throws ExpressionFormatException {
        CppConditionParser parser = new CppNonBooleanConditionParser(false, EXCEPTION);
        
        assertThat(parser.parse("defined A"), is(new Variable("A")));
    }
    
    /**
     * Tests the Linux macro handling.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testLinuxMacros() throws ExpressionFormatException {
        CppConditionParser parser = new CppNonBooleanConditionParser(true, EXCEPTION);

        assertThat(parser.parse("IS_ENABLED(A)"), is(or("A", "A_MODULE")));
        
        assertThat(parser.parse("IS_BUILTIN(A)"), is(new Variable("A")));
        assertThat(parser.parse("IS_MODULE(A)"), is(new Variable("A_MODULE")));
    }
        
    /**
     * Tests that IS_ENABLED() is translated to a {@link Macro} if Linux macros are disabled. 
     */
    @Test
    public void testIsEnabledWithoutLinuxEnabled() throws ExpressionFormatException {
        CppConditionParser parser = new CppNonBooleanConditionParser(false, EXCEPTION);
        
        parser.parse("IS_ENABLED(A)");
        assertThat(parser.parse("IS_ENABLED(A)"), is(new Macro("IS_ENABLED", new Variable("A"))));
    }
    
    /**
     * Tests that IS_BUILTIN() is translated to a {@link Macro} if Linux macros are disabled.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testIsBuiltinWithoutLinuxEnabled() throws ExpressionFormatException {
        CppConditionParser parser = new CppNonBooleanConditionParser(false, EXCEPTION);

        parser.parse("IS_BUILTIN(A)");
        assertThat(parser.parse("IS_BUILTIN(A)"), is(new Macro("IS_BUILTIN", new Variable("A"))));
    }
    
    /**
     * Tests that IS_MODULE() is translated to a {@link Macro} if Linux macros are disabled.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testIsModuleWithoutLinuxEnabled() throws ExpressionFormatException {
        CppConditionParser parser = new CppNonBooleanConditionParser(false, EXCEPTION);

        assertThat(parser.parse("IS_MODULE(A)"), is(new Macro("IS_MODULE", new Variable("A"))));
    }
    
    /**
     * Tests parsing of an unknown function.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testUnknownFunctionWithLinuxEnabled() throws ExpressionFormatException {
        CppConditionParser parser = new CppNonBooleanConditionParser(true, EXCEPTION);

        assertThat(parser.parse("func(A)"), is(new Macro("func", new Variable("A"))));
    }
    
    /**
     * Tests that an unknown function throws an exception.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testUnknownFunction() throws ExpressionFormatException {
        CppConditionParser parser = new CppNonBooleanConditionParser(false, EXCEPTION);

        assertThat(parser.parse("func(A)"), is(new Macro("func", new Variable("A"))));
    }
    
    /**
     * Tests parsing with variables and literals.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testParsingVarAndLiteral() throws ExpressionFormatException {
        CppConditionParser parser = new CppNonBooleanConditionParser(false, EXCEPTION);
        
        assertThat(parser.parse("A == 2"),
            is(new NonBooleanOperator(new Variable("A"), CppOperator.CMP_EQ, new Literal("2"))));
        assertThat(parser.parse("A != 2"),
            is(new NonBooleanOperator(new Variable("A"), CppOperator.CMP_NE, new Literal("2"))));
        assertThat(parser.parse("A >= 2"),
            is(new NonBooleanOperator(new Variable("A"), CppOperator.CMP_GE, new Literal("2"))));
        assertThat(parser.parse("A > 2"),
            is(new NonBooleanOperator(new Variable("A"), CppOperator.CMP_GT, new Literal("2"))));
        assertThat(parser.parse("A < 2"),
            is(new NonBooleanOperator(new Variable("A"), CppOperator.CMP_LT, new Literal("2"))));
        assertThat(parser.parse("A <= 2"),
            is(new NonBooleanOperator(new Variable("A"), CppOperator.CMP_LE, new Literal("2"))));
        assertThat(parser.parse("A ^ 1"),
            is(new NonBooleanOperator(new Variable("A"), CppOperator.BIN_XOR, new Literal("1"))));
    }
    
    /**
     * Tests fuzzy parsing with literal on the left.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testFuzzyParsingVarAndLiteralReversed() throws ExpressionFormatException {
        CppConditionParser parser = new CppNonBooleanConditionParser(false, EXCEPTION);
        
        assertThat(parser.parse("2 == A"),
            is(new NonBooleanOperator(new Literal("2"), CppOperator.CMP_EQ, new Variable("A"))));
        assertThat(parser.parse("2 != A"),
            is(new NonBooleanOperator(new Literal("2"), CppOperator.CMP_NE, new Variable("A"))));
        assertThat(parser.parse("2 <= A"),
            is(new NonBooleanOperator(new Literal("2"), CppOperator.CMP_GE, new Variable("A"))));
        assertThat(parser.parse("2 < A"),
            is(new NonBooleanOperator(new Literal("2"), CppOperator.CMP_GT, new Variable("A"))));
        assertThat(parser.parse("2 > A"),
            is(new NonBooleanOperator(new Literal("2"), CppOperator.CMP_LT, new Variable("A"))));
        assertThat(parser.parse("2 >= A"),
            is(new NonBooleanOperator(new Literal("2"), CppOperator.CMP_LE, new Variable("A"))));
        assertThat(parser.parse("1 ^ A"),
            is(new NonBooleanOperator(new Literal("1"), CppOperator.BIN_XOR, new Variable("A"))));
    }
    
    /**
     * Tests parsing with two variables.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testComparisionsOfVariables() throws ExpressionFormatException {
        CppConditionParser parser = new CppNonBooleanConditionParser(false, EXCEPTION);
        
        assertThat(parser.parse("A == B"),
            is(new NonBooleanOperator(new Variable("A"), CppOperator.CMP_EQ, new Variable("B"))));
        assertThat(parser.parse("A != B"),
            is(new NonBooleanOperator(new Variable("A"), CppOperator.CMP_NE, new Variable("B"))));
        assertThat(parser.parse("A >= B"),
            is(new NonBooleanOperator(new Variable("A"), CppOperator.CMP_GE, new Variable("B"))));
        assertThat(parser.parse("A > B"),
            is(new NonBooleanOperator(new Variable("A"), CppOperator.CMP_GT, new Variable("B"))));
        assertThat(parser.parse("A < B"),
            is(new NonBooleanOperator(new Variable("A"), CppOperator.CMP_LE, new Variable("B"))));
        assertThat(parser.parse("A <= B"),
            is(new NonBooleanOperator(new Variable("A"), CppOperator.CMP_LT, new Variable("B"))));
        assertThat(parser.parse("A ^ B"),
            is(new NonBooleanOperator(new Variable("A"), CppOperator.BIN_XOR, new Variable("B"))));
    }
    
    /**
     * Tests small formulas which contain a comparison with a addition.
     * 
     * @throws ExpressionFormatException wanted.
     */
    @Test
    public void testAdditionAndComparision() throws ExpressionFormatException {
        CppConditionParser parser = new CppNonBooleanConditionParser(false, EXCEPTION);

        assertThat(parser.parse("5 > (A + 1)"),
            is(new NonBooleanOperator(new Literal("5"), CppOperator.CMP_GT,
                new NonBooleanOperator(new Variable("A"), CppOperator.INT_ADD, new Literal("1")))));
        assertThat(parser.parse("B > (A + 1)"),
            is(new NonBooleanOperator(new Variable("B"), CppOperator.CMP_GT,
                new NonBooleanOperator(new Variable("A"), CppOperator.INT_ADD, new Literal("1")))));
    }
    
    
    /**
     * Tests that an unary - throws an exception if it is not applied to an integer literal.
     * 
     * @throws ExpressionFormatException wanted.
     */
    @Test(expected = ExpressionFormatException.class)
    public void testUnsupportedUnarySub() throws ExpressionFormatException {
        CppConditionParser parser = new CppNonBooleanConditionParser(false, EXCEPTION);

        parser.parse("-A");
    }
    
   
    /**
     * Tests that a variable without a defined is translated correctly.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testVariableWithoutDefined() throws ExpressionFormatException {
        CppConditionParser parser = new CppNonBooleanConditionParser(false, EXCEPTION);

        assertThat(parser.parse("A"), is(new Variable("A")));
    }
    
    /**
     * Tests that a defined() call without a parameter throws an exception.
     * 
     * @throws ExpressionFormatException wanted.
     */
    @Test(expected = ExpressionFormatException.class)
    public void testDefinedWithoutArgument() throws ExpressionFormatException {
        CppConditionParser parser = new CppNonBooleanConditionParser(false, EXCEPTION);

        parser.parse("defined()");
    }
    
    /**
     * Tests that a defined() call with too many parameters throws an exception.
     * 
     * @throws ExpressionFormatException wanted.
     */
    @Test(expected = ExpressionFormatException.class)
    public void testDefinedWithTooManyArguments() throws ExpressionFormatException {
        CppConditionParser parser = new CppNonBooleanConditionParser(false, EXCEPTION);

        parser.parse("defined(a, b)");
    }
    
    /**
     * Tests that a macro call with more than 1 parameter throws an exception.
     * 
     * @throws ExpressionFormatException wanted.
     */
    @Test(expected = ExpressionFormatException.class)
    public void testMacroTwoArguments() throws ExpressionFormatException {
        CppConditionParser parser = new CppNonBooleanConditionParser(false, EXCEPTION);

        parser.parse("myMacro(a, b)");
    }
    
    /**
     * Tests that a defined() call on a literal throws an exception.
     * 
     * @throws ExpressionFormatException wanted.
     */
    @Test(expected = ExpressionFormatException.class)
    public void testDefinedOnLiteral() throws ExpressionFormatException {
        CppConditionParser parser = new CppNonBooleanConditionParser(false, EXCEPTION);

        parser.parse("defined(1)");
    }
    
    
    /**
     * Tests that an invalid expression throws an exception if {@link InvalidConditionHandling#EXCEPTION} is used.
     * 
     * @throws ExpressionFormatException wanted.
     */
    @Test(expected = ExpressionFormatException.class)
    public void testMalformedException() throws ExpressionFormatException {
        CppConditionParser parser = new CppNonBooleanConditionParser(false, EXCEPTION);

        parser.parse("defined(A) || ");
    }
    
    /**
     * Tests that an invalid expression is replaced by True if {@link InvalidConditionHandling#TRUE} is used.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testMalformedTrue() throws ExpressionFormatException {
        CppConditionParser parser = new CppNonBooleanConditionParser(false, InvalidConditionHandling.TRUE);

        assertThat(parser.parse("defined(A) || "), is(True.INSTANCE));
    }
    
    /**
     * Tests that an invalid expression is replaced by an error variable if {@link InvalidConditionHandling#TRUE} is
     * used.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testMalformedVariable() throws ExpressionFormatException {
        CppConditionParser parser = new CppNonBooleanConditionParser(false, InvalidConditionHandling.ERROR_VARIABLE);

        assertThat(parser.parse("defined(A) || "), is(new Variable("PARSING_ERROR")));
    }
    
    /**
     * Tests that floating point zero is detected as false.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testVisitLiteralFloat() throws ExpressionFormatException {
        CppConditionParser parser = new CppNonBooleanConditionParser(false, EXCEPTION);
        
        assertThat(parser.parse("0.0"), is(new Literal("0")));
        assertThat(parser.parse("-0.0"), is(new Literal("-0")));
        assertThat(parser.parse("-4.2"), is(new Literal("-4.2")));
        assertThat(parser.parse("5.2"), is(new Literal("5.2")));
    }
   
    /**
     * Tests that parsing with floating point works.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testParsingFloatComparisons() throws ExpressionFormatException {
        CppConditionParser parser = new CppNonBooleanConditionParser(false, EXCEPTION);
        
        assertThat(parser.parse("A == 2.26"),
            is(new NonBooleanOperator(new Variable("A"), CppOperator.CMP_EQ, new Literal("2.26"))));
        assertThat(parser.parse("A != 0.0"),
            is(new NonBooleanOperator(new Variable("A"), CppOperator.CMP_NE, new Literal("0"))));
        assertThat(parser.parse("A >= 2.214"),
            is(new NonBooleanOperator(new Variable("A"), CppOperator.CMP_GE, new Literal("2.214"))));
        assertThat(parser.parse("2.26 == A"),
            is(new NonBooleanOperator(new Literal("2.26"), CppOperator.CMP_EQ, new Variable("A"))));
        assertThat(parser.parse("0.0 != A"),
            is(new NonBooleanOperator(new Literal("0"), CppOperator.CMP_NE, new Variable("A"))));
        assertThat(parser.parse("2.214 <= A"),
            is(new NonBooleanOperator(new Literal("2.214"), CppOperator.CMP_LE, new Variable("A"))));
    }

}
