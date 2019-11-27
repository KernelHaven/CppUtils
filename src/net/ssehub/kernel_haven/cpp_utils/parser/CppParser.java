/*
 * Copyright 2017-2019 University of Hildesheim, Software Systems Engineering
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
package net.ssehub.kernel_haven.cpp_utils.parser;

import static net.ssehub.kernel_haven.cpp_utils.parser.CppOperator.BIN_AND;
import static net.ssehub.kernel_haven.cpp_utils.parser.CppOperator.BIN_INV;
import static net.ssehub.kernel_haven.cpp_utils.parser.CppOperator.BIN_OR;
import static net.ssehub.kernel_haven.cpp_utils.parser.CppOperator.BIN_SHL;
import static net.ssehub.kernel_haven.cpp_utils.parser.CppOperator.BIN_SHR;
import static net.ssehub.kernel_haven.cpp_utils.parser.CppOperator.BIN_XOR;
import static net.ssehub.kernel_haven.cpp_utils.parser.CppOperator.BOOL_AND;
import static net.ssehub.kernel_haven.cpp_utils.parser.CppOperator.BOOL_NOT;
import static net.ssehub.kernel_haven.cpp_utils.parser.CppOperator.BOOL_OR;
import static net.ssehub.kernel_haven.cpp_utils.parser.CppOperator.CMP_EQ;
import static net.ssehub.kernel_haven.cpp_utils.parser.CppOperator.CMP_GE;
import static net.ssehub.kernel_haven.cpp_utils.parser.CppOperator.CMP_GT;
import static net.ssehub.kernel_haven.cpp_utils.parser.CppOperator.CMP_LE;
import static net.ssehub.kernel_haven.cpp_utils.parser.CppOperator.CMP_LT;
import static net.ssehub.kernel_haven.cpp_utils.parser.CppOperator.CMP_NE;
import static net.ssehub.kernel_haven.cpp_utils.parser.CppOperator.INT_ADD;
import static net.ssehub.kernel_haven.cpp_utils.parser.CppOperator.INT_ADD_UNARY;
import static net.ssehub.kernel_haven.cpp_utils.parser.CppOperator.INT_DEC;
import static net.ssehub.kernel_haven.cpp_utils.parser.CppOperator.INT_DIV;
import static net.ssehub.kernel_haven.cpp_utils.parser.CppOperator.INT_INC;
import static net.ssehub.kernel_haven.cpp_utils.parser.CppOperator.INT_MOD;
import static net.ssehub.kernel_haven.cpp_utils.parser.CppOperator.INT_MUL;
import static net.ssehub.kernel_haven.cpp_utils.parser.CppOperator.INT_SUB;
import static net.ssehub.kernel_haven.cpp_utils.parser.CppOperator.INT_SUB_UNARY;
import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import net.ssehub.kernel_haven.cpp_utils.NumberUtils;
import net.ssehub.kernel_haven.cpp_utils.parser.ast.CppExpression;
import net.ssehub.kernel_haven.cpp_utils.parser.ast.ExpressionList;
import net.ssehub.kernel_haven.cpp_utils.parser.ast.FunctionCall;
import net.ssehub.kernel_haven.cpp_utils.parser.ast.ICppExressionVisitor;
import net.ssehub.kernel_haven.cpp_utils.parser.ast.NumberLiteral;
import net.ssehub.kernel_haven.cpp_utils.parser.ast.Operator;
import net.ssehub.kernel_haven.cpp_utils.parser.ast.Variable;
import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.util.null_checks.Nullable;

/**
 * A parser for expressions in #if, #elif, etc. of the C preprocessor (CPP).
 * <p>
 * This parser works in the following steps:
 * <ol>
 *      <li>Lex the string; this produces {@link CppToken}s (see {@link #lex(String)})</li>
 *      <li>Parse the bracket hierarchy; this produces {@link CppExpression}s with {@link ExpressionList}s for each
 *      bracket hierarchy (see {@link #parse(String)})</li>
 *      <li>Detect {@link FunctionCall}s (variables in front of brackets) (see {@link FunctionCallTranslator})</li>
 *      <li>Resolve the flat {@link ExpressionList}s into operator hierarchies with the correct precedence;
 *      after this, there are no more {@link ExpressionList}s left (see {@link OperatorResolver})</li>
 * </ol>
 * 
 * @author Adam
 */
public class CppParser {
    
    /**
     * A visitor that finds functions calls (variables followed by expression list (brackets)).
     */
    private static class FunctionCallTranslator implements ICppExressionVisitor<@NonNull CppExpression> {
        
        @Override
        public @NonNull CppExpression visitExpressionList(@NonNull ExpressionList expressionList)
                throws ExpressionFormatException {
            
            for (int i = 0; i < expressionList.getExpressionSize(); i++) {
                CppExpression currentExpr = expressionList.getExpression(i);
                boolean foundFunction = false;
                
                if (currentExpr instanceof Variable && i + 1 < expressionList.getExpressionSize()) {
                    Variable currentVar = (Variable) currentExpr;
                    CppExpression nextExpr = expressionList.getExpression(i + 1);
                    
                    if (nextExpr instanceof ExpressionList) {
                        foundFunction = true;
                        nextExpr = nextExpr.accept(this);
                        String name = currentVar.getName();
                        
                        ExpressionList nextExprList = (ExpressionList) nextExpr;
                        // unpack argument list if we only have one element
                        if (nextExprList.getExpressionSize() == 1) {
                            nextExpr = nextExprList.getExpression(0);
                            
                        } else if (nextExprList.getExpressionSize() == 0) {
                            // function with no arguments
                            nextExpr = null;
                        }
                        
                        expressionList.setExpression(i, new FunctionCall(name, nextExpr));
                        expressionList.removeExpression(i + 1);
                        i++; // increment, since we already handled nextExpr
                        
                    } else if (currentVar.getName().equals("defined") && nextExpr instanceof Variable) {
                        // special case: defined(VAR) without brackets ("defined VAR") is allowed
                        foundFunction = true;
                        
                        expressionList.setExpression(i, new FunctionCall("defined", nextExpr));
                        expressionList.removeExpression(i + 1);
                    }
                }
                
                if (!foundFunction) {
                    expressionList.setExpression(i, currentExpr.accept(this));
                }
                
            }
            
            return expressionList;
        }

        @Override
        public @NonNull CppExpression visitFunctionCall(@NonNull FunctionCall call) throws ExpressionFormatException {
            throw new ExpressionFormatException("This code cannot be reached");
        }

        @Override
        public @NonNull CppExpression visitVariable(@NonNull Variable variable) throws ExpressionFormatException {
            return variable;
        }

        @Override
        public @NonNull CppExpression visitOperator(@NonNull Operator operator) throws ExpressionFormatException {
            return operator;
        }

        @Override
        public @NonNull CppExpression visitLiteral(@NonNull NumberLiteral literal) throws ExpressionFormatException {
            return literal;
        }
        
    }
    
    /**
     * Finds and sets the left and right side for the operators.
     */
    private static class OperatorResolver implements ICppExressionVisitor<@NonNull CppExpression> {

        private @NonNull String expression = ""; // this will be set before this visitor is called
        
        @Override
        public @NonNull CppExpression visitExpressionList(@NonNull ExpressionList expressionList)
                throws ExpressionFormatException {
            
            CppExpression result;
            
            if (expressionList.getExpressionSize() == 0) {
                throw makeException(expression, "Expected variable");
                
            } else if (expressionList.getExpressionSize() == 1) {
                result = expressionList.getExpression(0).accept(this); 
                
            } else {
                int highestOpPos = -1;
                int highestOpPrecedence = Integer.MAX_VALUE;
                Operator highestOpNode = null;
                
                for (int i = 0; i < expressionList.getExpressionSize(); i++) {
                    CppExpression currentExpr = expressionList.getExpression(i);
                    
                    if (currentExpr instanceof Operator) {
                        CppOperator op = ((Operator) currentExpr).getOperator();
                        
                        // equal, because we want to find the right-most lowest precedence
                        // this is needed for proper left-to-right evaluation of equal precedence operators
                        if (op.getPrecedence() <= highestOpPrecedence) {
                            highestOpPos = i;
                            highestOpPrecedence = op.getPrecedence();
                            highestOpNode = (Operator) currentExpr;
                        }
                    }
                }
                
                if (highestOpNode == null) {
                    throw makeException(expression, "Couldn't find operator");
                    
                } else {
                    parseParameters(expressionList, highestOpNode, highestOpPos);
                }
                
                
                result = highestOpNode;
            }
            
            return result;
        }
        
        /**
         * Parses the arguments (left and right for binary, only one side for unary operators) and adds them to the
         * given operator.
         * 
         * @param expressionList The {@link ExpressionList} containing all nodes of the current nesting depth.
         * @param operator The operator to parse the parameters for.
         * @param operatorIndex The index of the operator in <code>expressionList</code>.
         * 
         * @throws ExpressionFormatException If finding the parameters fails.
         */
        private void parseParameters(@NonNull ExpressionList expressionList, @NonNull Operator operator,
                int operatorIndex) throws ExpressionFormatException {
            
            if (operator.getOperator().isUnary()) {
                if (operatorIndex != 0
                        // special case: ++ and -- may be on right side
                        && !(
                                (operator.getOperator() == INT_DEC || operator.getOperator() == INT_INC)
                                && operatorIndex == expressionList.getExpressionSize() - 1
                            )) {
                        
                    throw makeException(expression, "Found elements on wrong side of unary operator");
                        
                } else {
                    expressionList.removeExpression(operatorIndex);
                    operator.setLeftSide(expressionList.accept(this));
                }
                
            } else {
                if (operatorIndex == 0 || operatorIndex == expressionList.getExpressionSize() - 1) {
                    throw makeException(expression, "Didn't find elements on both sides of binary operator");
                    
                } else {
                    ExpressionList leftSide = new ExpressionList();
                    ExpressionList rightSide = new ExpressionList();
                    for (int i = 0; i < expressionList.getExpressionSize(); i++) {
                        if (i < operatorIndex) {
                            leftSide.addExpression(expressionList.getExpression(i));
                            
                        } else if (i > operatorIndex) {
                            rightSide.addExpression(expressionList.getExpression(i));
                        }
                    }
                    
                    operator.setLeftSide(leftSide.accept(this));
                    operator.setRightSide(rightSide.accept(this));

                }
            }
        }

        @Override
        public @NonNull CppExpression visitFunctionCall(@NonNull FunctionCall call) throws ExpressionFormatException {
            CppExpression arg = call.getArgument();
            if (arg != null) {
                arg = arg.accept(this);
            }
            call.setArgument(arg);
            return call;
        }

        @Override
        public @NonNull CppExpression visitVariable(@NonNull Variable variable) throws ExpressionFormatException {
            return variable;
        }

        @Override
        public @NonNull CppExpression visitOperator(@NonNull Operator operator) throws ExpressionFormatException {
            return operator;
        }

        @Override
        public @NonNull CppExpression visitLiteral(@NonNull NumberLiteral literal) throws ExpressionFormatException {
            return literal;
        }
        
    }
    
    private @NonNull FunctionCallTranslator functionCallTranslator = new FunctionCallTranslator();
    private @NonNull OperatorResolver operatorResolver = new OperatorResolver();
    
    /**
     * Partially parses the given CPP expression. The resulting AST only has bracket hierarchies and function calls
     * resolved. This method is not thread-safe (don't call it from multiple threads).
     * 
     * @param expression The expression to parse.
     * @return A (partially) parsed AST for the given expression.
     * 
     * @throws ExpressionFormatException If the expression is malformed.
     */
    public @NonNull CppExpression parse(@NonNull String expression) throws ExpressionFormatException {
        @NonNull CppToken @NonNull [] tokens = lex(expression);
        
        Deque<ExpressionList> expressionListStack = new LinkedList<>();
        expressionListStack.push(new ExpressionList());
        
        for (int tokenIndex = 0; tokenIndex < tokens.length; tokenIndex++) {
            
            CppToken currentToken = tokens[tokenIndex];
            
            if (currentToken instanceof Bracket) {
                Bracket currentBracket = (Bracket) currentToken;
                
                if (currentBracket.isOpening()) {
                    ExpressionList newList = new ExpressionList();
                    expressionListStack.peek().addExpression(newList);
                    expressionListStack.push(newList);
                    
                } else {
                    expressionListStack.pop();
                    if (expressionListStack.size() < 1) {
                        throw makeException(expression, "Unbalanced brackets (too many closing)",
                                currentToken.getPos());
                    }
                }
                
            } else if (currentToken instanceof IdentifierToken) {
                expressionListStack.peek().addExpression(new Variable(((IdentifierToken) currentToken).getName()));
                
            } else if (currentToken instanceof OperatorToken) {
                expressionListStack.peek().addExpression(new Operator(((OperatorToken) currentToken).getOperator()));
                
            } else if (currentToken instanceof LiteralToken) {
                expressionListStack.peek().addExpression(new NumberLiteral(((LiteralToken) currentToken).getValue()));
                
            } else {
                throw makeException(expression, "Unexpected token: " + currentToken, currentToken.getPos());
            }
            
        }
        
        if (expressionListStack.size() != 1) {
            CppToken lastToken = tokens[tokens.length - 1];
            throw makeException(expression, "Unbalanced brackets (missing closing)",
                    lastToken.getPos() + lastToken.getLength());
        }
        
        ExpressionList resultList = expressionListStack.pop();
        CppExpression result = resultList;
        // unpack surrounding list if we only have one element
        if (resultList.getExpressionSize() == 1) {
            result = resultList.getExpression(0);
        }
        
        
        result = result.accept(functionCallTranslator);
        operatorResolver.expression = expression;
        result = result.accept(operatorResolver);
        
        return result;
    }
    
    /**
     * Lex (tokenize) the given expression. Package visibility for test cases.
     * 
     * @param expression The expression to turn into tokens.
     * @return The tokens.
     * 
     * @throws ExpressionFormatException If invalid characters appear in the expression.
     */
    @NonNull CppToken @NonNull [] lex(@NonNull String expression) throws ExpressionFormatException {
        List<@NonNull CppToken> tokens = new ArrayList<>(50);
        int tokenIndex = 0;
        
        IdentifierToken currentIdentifier = null;
        
        char[] expr = expression.toCharArray();
        // iterate over the string; i is incremented based on which token was identified
        for (int exprPos = 0; exprPos < expr.length;) {
            
            CppOperator op = getOperator(expr, exprPos, tokenIndex == 0 ? null : tokens.get(tokenIndex - 1));
            
            if (isWhitespace(expr, exprPos)) {
                identifierFinished(currentIdentifier, tokens, tokenIndex - 1, expression);
                currentIdentifier = null;
                
                exprPos++;
                
            } else if (isOpeningBracket(expr, exprPos)) {
                identifierFinished(currentIdentifier, tokens, tokenIndex - 1, expression);
                currentIdentifier = null;
                
                tokens.add(new Bracket(exprPos, false));
                tokenIndex++;
                exprPos++;
                
            } else if (isClosingBracket(expr, exprPos)) {
                identifierFinished(currentIdentifier, tokens, tokenIndex - 1, expression);
                currentIdentifier = null;
                
                tokens.add(new Bracket(exprPos, true));
                tokenIndex++;
                exprPos++;
                
            } else if (op != null) {
                identifierFinished(currentIdentifier, tokens, tokenIndex - 1, expression);
                currentIdentifier = null;
                
                tokens.add(new OperatorToken(exprPos, op));
                tokenIndex++;
                exprPos += op.getSymbol().length();
                
            } else if (isIdentifierChar(expr, exprPos)) {
                if (currentIdentifier == null) {
                    currentIdentifier = new IdentifierToken(exprPos, "");
                    tokens.add(currentIdentifier);
                    tokenIndex++;
                }
                currentIdentifier.setName(currentIdentifier.getName() + expr[exprPos]);
                exprPos++;
                
            } else {
                throw makeException(expression, "Invalid character in expression: '" + expr[exprPos] + "'", exprPos);
            }
        }

        identifierFinished(currentIdentifier, tokens, tokenIndex - 1, expression);
        
        return notNull(tokens.toArray(new @NonNull CppToken[0]));
    }
    
    /**
     * Called when an identifier is finished. This method detects whether the identifier was a literal.
     *  
     * @param identifier The identifier that was finished. May be <code>null</code>, in which case this method does
     *      nothing.
     * @param tokens The list of tokens to replace the identifier in.
     * @param tokenIndex The index of the identifier in the token list.
     * @param expression The currently parsed expression. Used for exception messages.
     * 
     * @throws ExpressionFormatException If the identifier is supposed to be a literal, but not parseable as one.
     */
    private void identifierFinished(@Nullable IdentifierToken identifier, @NonNull List<@NonNull CppToken> tokens,
            int tokenIndex, @NonNull  String expression) throws ExpressionFormatException {
        
        if (identifier != null) {
            if (Character.isDigit(identifier.getName().charAt(0))) {
                StringBuilder literal = new StringBuilder(identifier.getName().toLowerCase());
                
                // remove any trailing 'l's
                while (literal.charAt(literal.length() - 1) == 'l') {
                    literal.replace(literal.length() - 1, literal.length(), "");
                }
                // remove one trailing 'u'
                if (literal.charAt(literal.length() - 1) == 'u') {
                    literal.replace(literal.length() - 1, literal.length(), "");
                }
                
                try {
                    // parse number
                    Number numberValue = NumberUtils.convertToNumber(notNull(literal.toString()));
                    if (numberValue == null) {
                        throw new NumberFormatException();
                    }
                    
                    // replace IdentifierToken with LiteralToken
                    tokens.set(tokenIndex,
                            new LiteralToken(identifier.getPos(), identifier.getLength(), numberValue));
                    
                } catch (NumberFormatException e) {
                    throw makeException(expression, "Cannot parse literal " + identifier.getName(),
                            identifier.getPos());
                }
                
            } else {
                int dotIndex = identifier.getName().indexOf('.');
                if (dotIndex != -1) {
                    throw makeException(expression, "Literal contains invalid character: '.'",
                            identifier.getPos() + dotIndex);
                }
                
            }
        }
        
    }
    
    /**
     * Whether the given character is a whitespace.
     * 
     * @param expr An array containing characters.
     * @param exprPos Which character in the array should be checked.
     * 
     * @return Whether the given character is a whitespace.
     */
    private boolean isWhitespace(char[] expr, int exprPos) {
        return Character.isWhitespace(expr[exprPos]);
    }
    
    /**
     * Whether the given character is an opening bracket.
     * 
     * @param expr An array containing characters.
     * @param exprPos Which character in the array should be checked.
     * 
     * @return Whether the given character is an opening bracket.
     */
    private boolean isOpeningBracket(char[] expr, int exprPos) {
        return safeCheckChar(expr, exprPos, '(');
    }
    
    /**
     * Whether the given character is an closing bracket.
     * 
     * @param expr An array containing characters.
     * @param exprPos Which character in the array should be checked.
     * 
     * @return Whether the given character is an closing bracket.
     */
    private boolean isClosingBracket(char[] expr, int exprPos) {
        return safeCheckChar(expr, exprPos, ')');
    }
    
    /**
     * Returns the operator that is found the given position in the given array.
     * 
     * @param expr The expression that the operator is searched in.
     * @param exprPos The position in the expression where the operator is at.
     * @param previousToken The previous token that is to the left of this potential operator. <code>null</code> if this
     *      would be the first token for the expression. This is used to determine if + and - are binary or unary.
     * 
     * @return The operator at the given position, or <code>null</code> if there is no operator.
     */
    private CppOperator getOperator(char[] expr, int exprPos, @Nullable CppToken previousToken) {
        CppOperator result = null;
        
        /*
         * Double char operators
         */
        if (safeCheckChar(expr, exprPos, '&') && safeCheckChar(expr, exprPos + 1, '&')) {
            result = BOOL_AND;
        } else if (safeCheckChar(expr, exprPos, '|') && safeCheckChar(expr, exprPos + 1, '|')) {
            result = BOOL_OR;
            
        } else if (safeCheckChar(expr, exprPos, '+') && safeCheckChar(expr, exprPos + 1, '+')) {
            result = INT_INC;
        } else if (safeCheckChar(expr, exprPos, '-') && safeCheckChar(expr, exprPos + 1, '-')) {
            result = INT_DEC;
            
        } else if (safeCheckChar(expr, exprPos, '=') && safeCheckChar(expr, exprPos + 1, '=')) {
            result = CMP_EQ;
        } else if (safeCheckChar(expr, exprPos, '!') && safeCheckChar(expr, exprPos + 1, '=')) {
            result = CMP_NE;
        } else if (safeCheckChar(expr, exprPos, '<') && safeCheckChar(expr, exprPos + 1, '=')) {
            result = CMP_LE;
        } else if (safeCheckChar(expr, exprPos, '>') && safeCheckChar(expr, exprPos + 1, '=')) {
            result = CMP_GE;
            
        } else if (safeCheckChar(expr, exprPos, '>') && safeCheckChar(expr, exprPos + 1, '>')) {
            result = BIN_SHR;
        } else if (safeCheckChar(expr, exprPos, '<') && safeCheckChar(expr, exprPos + 1, '<')) {
            result = BIN_SHL;
            
        /*
         *  single char operators
         */
        } else if (safeCheckChar(expr, exprPos, '!')) {
            result = BOOL_NOT;
            
        } else if (safeCheckChar(expr, exprPos, '+')) {
            result = isUnary(previousToken) ? INT_ADD_UNARY : INT_ADD;
        } else if (safeCheckChar(expr, exprPos, '-')) {
            result = isUnary(previousToken) ? INT_SUB_UNARY : INT_SUB;
        } else if (safeCheckChar(expr, exprPos, '*')) {
            result = INT_MUL;
        } else if (safeCheckChar(expr, exprPos, '/')) {
            result = INT_DIV;
        } else if (safeCheckChar(expr, exprPos, '%')) {
            result = INT_MOD;
            
        } else if (safeCheckChar(expr, exprPos, '<')) {
            result = CMP_LT;
        } else if (safeCheckChar(expr, exprPos, '>')) {
            result = CMP_GT;
            
        } else if (safeCheckChar(expr, exprPos, '&')) {
            result = BIN_AND;
        } else if (safeCheckChar(expr, exprPos, '|')) {
            result = BIN_OR;
        } else if (safeCheckChar(expr, exprPos, '^')) {
            result = BIN_XOR;
        } else if (safeCheckChar(expr, exprPos, '~')) {
            result = BIN_INV;
        }
        
        return result;
    }
    
    /**
     * Checks if a + or - operator is unary given the token to the left. The operator is unary, if there is no token
     * to the left (i.e. the expression starts with it), there is an opening bracket to the left, or there is another
     * operator to the left.
     * 
     * @param previousToken The token to the left of the operator token.
     * 
     * @return Whether the operator should be unary.
     */
    private boolean isUnary(@Nullable CppToken previousToken) {
        return previousToken == null
                || previousToken instanceof OperatorToken
                || (previousToken instanceof Bracket && ((Bracket) previousToken).isOpening());
    }
    
    /**
     * Whether the given character is a valid identifier character.
     * 
     * @param expr An array containing characters.
     * @param exprPos Which character in the array should be checked.
     * 
     * @return Whether the given character is a valid identifier character.
     */
    private boolean isIdentifierChar(char[] expr, int exprPos) {
        // CHECKSTYLE:OFF
        // checkstyle thinks that this boolean formula is too complex;
        // but we need it this way because every other option is not as
        // performant, and this
        // is a rather performance critical code path.
        return (expr[exprPos] >= 'a' && expr[exprPos] <= 'z') || (expr[exprPos] >= 'A' && expr[exprPos] <= 'Z')
                || (expr[exprPos] >= '0' && expr[exprPos] <= '9') || (expr[exprPos] == '_')  || expr[exprPos] == '.';
        // CHECKSTYLE:ON
    }
    
    /**
     * Safely checks if the character at the given position in the array is equal to the given character.
     * 
     * @param expr The array to check in.
     * @param exprPos The position to check in the array. May be out of bounds.
     * @param check The character that should be checked against.
     * 
     * @return <code>true</code> if <code>check</code> is at the array in the given position; <code>false</code> if not
     *      or <code>exprPos</code> is out of bounds.
     */
    private static boolean safeCheckChar(char[] expr, int exprPos, char check) {
        boolean result = false;
        if (exprPos >= 0 && exprPos < expr.length) {
            result = expr[exprPos] == check;
        }
        return result;
    }
    
    /**
     * Creates an {@link ExpressionFormatException}.
     * 
     * @param expression The expression that couldn't be parsed.
     * @param message The message describing the exception.
     * @param markers The indices for characters in the expression where markers should be displayed. Must be sorted
     *      ascending.
     * 
     * @return The exception with the proper error message.
     */
    private static @NonNull ExpressionFormatException makeException(@NonNull String expression, @NonNull String message,
            int ... markers) {
        
        StringBuilder fullMessage = new StringBuilder(message).append("\nIn formula: ").append(expression);
        if (markers.length > 0) {
            fullMessage.append("\n            ");
            int markersIndex = 0;
            for (int i = 0; markersIndex < markers.length; i++) {
                if (i == markers[markersIndex]) {
                    fullMessage.append('^');
                    markersIndex++;
                    
                } else {
                    fullMessage.append(' ');
                }
            }
        }
        return new ExpressionFormatException(fullMessage.toString());
    }
    
}
