/*
 * Copyright 2018-2019 University of Hildesheim, Software Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ssehub.kernel_haven.cpp_utils;

import static net.ssehub.kernel_haven.cpp_utils.InvalidConditionHandling.EXCEPTION;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.and;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.not;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.or;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.Variable;
import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;

/**
 * Tests the {@link CppConditionParser}.
 *
 * @author Adam
 */
public class CppConditionParserTest {
    
    /**
     * Tests a variable with underscore characters.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testUnderscore() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, false, EXCEPTION);
        
        assertThat(parser.parse("defined(__a_b__)"), is(new Variable("__a_b__")));
    }
    
    /**
     * Tests a more complex condition that uses all boolean operators.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testComplexCondition() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, false, EXCEPTION);
        
        assertThat(parser.parse("(defined(A) && (!defined(B) || defined(C)))"), is(and("A", or(not("B"), "C"))));
    }
    
    /**
     * Tests parsing a literal true.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testConditionLiteralTrue() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, false, EXCEPTION);
        
        assertThat(parser.parse("1"), is(True.INSTANCE));
        assertThat(parser.parse("2"), is(True.INSTANCE));
        assertThat(parser.parse("-2"), is(True.INSTANCE));
    }
    
    /**
     * Tests parsing a literal false.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testConditionLiteralFalse() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, false, EXCEPTION);
        
        assertThat(parser.parse("0"), is(False.INSTANCE));
        assertThat(parser.parse("-0"), is(False.INSTANCE));
    }

    /**
     * Tests an defined (VAR) with a space before the bracket.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testDefinedWithSpace() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, false, EXCEPTION);
        
        assertThat(parser.parse("defined (A)"), is(new Variable("A")));
    }
    
    /**
     * Tests an defined VAR without the brackets.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testDefinedWithoutBrackets() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, false, EXCEPTION);
        
        assertThat(parser.parse("defined A"), is(new Variable("A")));
    }
    
    /**
     * Tests the Linux macro handling.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testLinuxMacros() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(true, false, EXCEPTION);

        assertThat(parser.parse("IS_ENABLED(A)"), is(or("A", "A_MODULE")));
        
        assertThat(parser.parse("IS_BUILTIN(A)"), is(new Variable("A")));
        assertThat(parser.parse("IS_MODULE(A)"), is(new Variable("A_MODULE")));
    }
    

    /**
     * Tests that IS_ENABLED() throws an exception if Linux handling is disabled.
     * 
     * @throws ExpressionFormatException wanted.
     */
    @Test(expected = ExpressionFormatException.class)
    public void testIsEnabledWithoutLinuxEnabled() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, false, EXCEPTION);

        parser.parse("IS_ENABLED(A)");
    }
    
    /**
     * Tests that IS_BUILTIN() throws an exception if Linux handling is disabled.
     * 
     * @throws ExpressionFormatException wanted.
     */
    @Test(expected = ExpressionFormatException.class)
    public void testIsBuiltinWithoutLinuxEnabled() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, false, EXCEPTION);

        parser.parse("IS_BUILTIN(A)");
    }
    
    /**
     * Tests that IS_MODULE() throws an exception if Linux handling is disabled.
     * 
     * @throws ExpressionFormatException wanted.
     */
    @Test(expected = ExpressionFormatException.class)
    public void testIsModuleWithoutLinuxEnabled() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, false, EXCEPTION);

        parser.parse("IS_MODULE(A)");
    }
    
    /**
     * Tests that an unknown function throws an exception.
     * 
     * @throws ExpressionFormatException wanted.
     */
    @Test(expected = ExpressionFormatException.class)
    public void testUnknownFunctionWithLinuxEnabled() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(true, false, EXCEPTION);

        parser.parse("func(A)");
    }
    
    /**
     * Tests that an unknown function throws an exception.
     * 
     * @throws ExpressionFormatException wanted.
     */
    @Test(expected = ExpressionFormatException.class)
    public void testUnknownFunction() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, false, EXCEPTION);

        parser.parse("func(A)");
    }
    
    /**
     * Tests fuzzy parsing with variables and literals.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testFuzzyParsingVarAndLiteral() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, true, EXCEPTION);
        
        assertThat(parser.parse("A == 2"), is(new Variable("A_eq_2")));
        assertThat(parser.parse("A != 2"), is(new Variable("A_ne_2")));
        assertThat(parser.parse("A >= 2"), is(new Variable("A_ge_2")));
        assertThat(parser.parse("A > 2"), is(new Variable("A_gt_2")));
        assertThat(parser.parse("A < 2"), is(new Variable("A_lt_2")));
        assertThat(parser.parse("A <= 2"), is(new Variable("A_le_2")));
    }
    
    /**
     * Tests fuzzy parsing with literal on the left.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testFuzzyParsingVarAndLiteralReversed() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, true, EXCEPTION);
        
        assertThat(parser.parse("2 == A"), is(new Variable("A_eq_2")));
        assertThat(parser.parse("2 != A"), is(new Variable("A_ne_2")));
        assertThat(parser.parse("2 <= A"), is(new Variable("A_ge_2")));
        assertThat(parser.parse("2 < A"), is(new Variable("A_gt_2")));
        assertThat(parser.parse("2 > A"), is(new Variable("A_lt_2")));
        assertThat(parser.parse("2 >= A"), is(new Variable("A_le_2")));
    }
    
    /**
     * Tests fuzzy parsing with two variables.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testFuzzyParsingVarAndVar() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, true, EXCEPTION);
        
        assertThat(parser.parse("A == B"), is(new Variable("A_eq_B")));
        assertThat(parser.parse("A != B"), is(new Variable("A_ne_B")));
        assertThat(parser.parse("A >= B"), is(new Variable("A_ge_B")));
        assertThat(parser.parse("A > B"), is(new Variable("A_gt_B")));
        assertThat(parser.parse("A < B"), is(new Variable("A_lt_B")));
        assertThat(parser.parse("A <= B"), is(new Variable("A_le_B")));
    }
    
    /**
     * Tests a case where fuzzy parsing still fails.
     * 
     * @throws ExpressionFormatException wanted.
     */
    @Test(expected = ExpressionFormatException.class)
    public void testFuzzyParsingWithNonVarOnLeft() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, true, EXCEPTION);

        parser.parse("(A + 1) > 5");
    }
    
    /**
     * Tests a case where fuzzy parsing still fails.
     * 
     * @throws ExpressionFormatException wanted.
     */
    @Test(expected = ExpressionFormatException.class)
    public void testFuzzyParsingWithNonVarOnRight() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, true, EXCEPTION);

        parser.parse("5 > (A + 1)");
    }
    
    /**
     * Tests a case where fuzzy parsing still fails.
     * 
     * @throws ExpressionFormatException wanted.
     */
    @Test(expected = ExpressionFormatException.class)
    public void testFuzzyParsingWithOneNonVar() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, true, EXCEPTION);

        parser.parse("B > (A + 1)");
    }
    
    /**
     * Tests that unsupported operators throw an exception.
     * 
     * @throws ExpressionFormatException wanted.
     */
    @Test(expected = ExpressionFormatException.class)
    public void testUnsupportedOperators() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, false, EXCEPTION);

        parser.parse("A ^ 1");
    }
    
    /**
     * Tests that an unary - throws an exception if it is not applied to an integer literal.
     * 
     * @throws ExpressionFormatException wanted.
     */
    @Test(expected = ExpressionFormatException.class)
    public void testUnsupportedUnarySub() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, false, EXCEPTION);

        parser.parse("-A");
    }
    
    /**
     * Tests that a variable without a defined throws an exception.
     * 
     * @throws ExpressionFormatException wanted.
     */
    @Test(expected = ExpressionFormatException.class)
    public void testVariableWithoutDefined() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, false, EXCEPTION);

        parser.parse("A");
    }
    
    /**
     * Tests that a variable without a defined is translated correctly with fuzzy parsing.
     * 
     * @throws ExpressionFormatException unwanted..
     */
    @Test
    public void testVariableWithoutDefinedFuzzyParsing() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, true, EXCEPTION);

        assertThat(parser.parse("A"), is(new Variable("A_ne_0")));
    }
    
    /**
     * Tests that a defined() call without a parameter throws an exception.
     * 
     * @throws ExpressionFormatException wanted.
     */
    @Test(expected = ExpressionFormatException.class)
    public void testDefinedWithoutArgument() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, false, EXCEPTION);

        parser.parse("defined()");
    }
    
    /**
     * Tests that a defined() call with too many parameters throws an exception.
     * 
     * @throws ExpressionFormatException wanted.
     */
    @Test(expected = ExpressionFormatException.class)
    public void testDefinedWithTooManyArguments() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, false, EXCEPTION);

        parser.parse("defined(a, b)");
    }
    
    /**
     * Tests that a macro call with more than 1 parameter throws an exception.
     * 
     * @throws ExpressionFormatException wanted.
     */
    @Test(expected = ExpressionFormatException.class)
    public void testMacroTwoArguments() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, false, EXCEPTION);

        parser.parse("myMacro(a, b)");
    }
    
    /**
     * Tests that a defined() call on a literal throws an exception.
     * 
     * @throws ExpressionFormatException wanted.
     */
    @Test(expected = ExpressionFormatException.class)
    public void testDefinedOnLiteral() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, false, EXCEPTION);

        parser.parse("defined(1)");
    }
    
    /**
     * Tests that a comparator operator throws an exception if fuzzy parsing is disabled.
     * 
     * @throws ExpressionFormatException wanted.
     */
    @Test(expected = ExpressionFormatException.class)
    public void testComparatorWithoutFuzzyParsing() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, false, EXCEPTION);

        parser.parse("A == 2");
    }
    
    /**
     * Tests that an invalid expression throws an exception if {@link InvalidConditionHandling#EXCEPTION} is used.
     * 
     * @throws ExpressionFormatException wanted.
     */
    @Test(expected = ExpressionFormatException.class)
    public void testMalformedException() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, false, EXCEPTION);

        parser.parse("defined(A) || ");
    }
    
    /**
     * Tests that an invalid expression is replaced by True if {@link InvalidConditionHandling#TRUE} is used.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testMalformedTrue() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, false, InvalidConditionHandling.TRUE);

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
        CppConditionParser parser = new CppConditionParser(false, false, InvalidConditionHandling.ERROR_VARIABLE);

        assertThat(parser.parse("defined(A) || "), is(new Variable("PARSING_ERROR")));
    }
    
    /**
     * Tests that floating point zero is detected as false.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testVisitLiteralFloat() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, false, EXCEPTION);
        
        assertThat(parser.parse("0.0"), is(False.INSTANCE));
        assertThat(parser.parse("-0.0"), is(False.INSTANCE));
        assertThat(parser.parse("-4.2"), is(True.INSTANCE));
        assertThat(parser.parse("5.2"), is(True.INSTANCE));
    }
   
    /**
     * Tests that fuzzy parsing with floating point works.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testFuzzyParsingFloat() throws ExpressionFormatException {
        CppConditionParser parser = new CppConditionParser(false, true, EXCEPTION);
        
        assertThat(parser.parse("A == 2.26"), is(new Variable("A_eq_2_26")));
        assertThat(parser.parse("A != 0.0"), is(new Variable("A_ne_0")));
        assertThat(parser.parse("A >= 2.214"), is(new Variable("A_ge_2_214")));
        assertThat(parser.parse("2.26 == A"), is(new Variable("A_eq_2_26")));
        assertThat(parser.parse("0.0 != A"), is(new Variable("A_ne_0")));
        assertThat(parser.parse("2.214 <= A"), is(new Variable("A_ge_2_214")));
    }

}
