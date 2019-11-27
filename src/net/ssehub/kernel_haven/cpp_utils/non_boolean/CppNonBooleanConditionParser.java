/*
 * Copyright 2019 University of Hildesheim, Software Systems Engineering
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
package net.ssehub.kernel_haven.cpp_utils.non_boolean;

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import net.ssehub.kernel_haven.cpp_utils.CppConditionParser;
import net.ssehub.kernel_haven.cpp_utils.InvalidConditionHandling;
import net.ssehub.kernel_haven.util.cpp.parser.ast.CppExpression;
import net.ssehub.kernel_haven.util.cpp.parser.ast.FunctionCall;
import net.ssehub.kernel_haven.util.cpp.parser.ast.NumberLiteral;
import net.ssehub.kernel_haven.util.cpp.parser.ast.Operator;
import net.ssehub.kernel_haven.util.cpp.parser.ast.Variable;
import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Disjunction;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.util.null_checks.Nullable;

/**
 * A {@link CppConditionParser} that supports non-Boolean conditions.<p>
 * 
 * <b><font color="red">Warning:</font></b> Please use this class with care and as seldom as possible.
 * KernelHaven and its analyses are designed to handle only Boolean conditions.
 * @author El-Sharkawy
 *
 */
public class CppNonBooleanConditionParser extends CppConditionParser {

    /**
     * Creates a new {@link CppNonBooleanConditionParser}.
     * 
     * @param handleLinuxMacros Whether to handle preprocessor macros found in the Linux Kernel (i.e.
     *      IS_ENABLED, IS_BUILTIN, IS_MODULE).
     * @param invalidConditionHandling How to handle unparseable conditions.
     */
    public CppNonBooleanConditionParser(boolean handleLinuxMacros, InvalidConditionHandling invalidConditionHandling) {
        // Fuzzy parsing is not required by this parser
        super(handleLinuxMacros, false, invalidConditionHandling);
    }
    
    
    @Override
    public @NonNull Formula visitOperator(@NonNull Operator operator) throws ExpressionFormatException {
        Formula result;
        
        switch (operator.getOperator()) {
        case BOOL_AND:
            result = new Conjunction(operator.getLeftSide().accept(this),
                notNull(operator.getRightSide()).accept(this));
            break;
        case BOOL_OR:
            result = new Disjunction(operator.getLeftSide().accept(this),
                notNull(operator.getRightSide()).accept(this));
            break;
        case BOOL_NOT:
            result = new Negation(operator.getLeftSide().accept(this));
            break;
            
        case CMP_EQ: // falls through
        case CMP_NE: // falls through
        case CMP_LT: // falls through
        case CMP_LE: // falls through
        case CMP_GT: // falls through
        case CMP_GE:
            result = new NonBooleanOperator(operator.getLeftSide().accept(this), operator.getOperator(),
                notNull(operator.getRightSide()).accept(this));
            break;
            
        case INT_SUB_UNARY:
            if (operator.getLeftSide() instanceof NumberLiteral) {
                result = new Literal("-" + ((NumberLiteral) operator.getLeftSide()).getValue().toString());
            } else {
                throw new ExpressionFormatException("Unsupported operator: " + operator.getOperator());
            }
            break;
            
        default:
            CppExpression rightSide = operator.getRightSide();
            if (null != rightSide) {
                result = new NonBooleanOperator(operator.getLeftSide().accept(this), operator.getOperator(),
                    rightSide.accept(this));
            } else {
                throw new ExpressionFormatException("Unsupported operator: " + operator.getOperator());
            }
            break;
        }
        
        return result;
    }
    
    @Override
    public @NonNull Formula visitVariable(@NonNull Variable variable) throws ExpressionFormatException {
        return new net.ssehub.kernel_haven.util.logic.Variable(variable.getName());
    }

    @Override
    public @NonNull Formula visitLiteral(@NonNull NumberLiteral literal) throws ExpressionFormatException {
        return new Literal(literal.getValue().toString());
    }
    
    @Override
    public @NonNull Formula visitFunctionCall(@NonNull FunctionCall call) throws ExpressionFormatException {
        
        String function = call.getFunctionName();
        CppExpression arg = call.getArgument();
        @Nullable Formula argument = null;
        if (null != arg) {
            argument = arg.accept(this);
        }
        
        
        Formula result;
        if (function.equals("defined")) {
            if (argument == null) {
                throw new ExpressionFormatException("Can't handle defined()-function without argument");
            } else if (!(argument instanceof net.ssehub.kernel_haven.util.logic.Variable)) {
                throw new ExpressionFormatException("Can't handle defined()-function one other elements than "
                    + "variables");
            }
            result = argument;
            
        } else if (getHandleLinuxMacros() && function.equals("IS_ENABLED")) {
            net.ssehub.kernel_haven.util.logic.Variable variable
                = (net.ssehub.kernel_haven.util.logic.Variable) argument;
            result = new Disjunction(variable,
                    new net.ssehub.kernel_haven.util.logic.Variable(variable.getName() + "_MODULE"));
            
        } else if (getHandleLinuxMacros() && function.equals("IS_MODULE")) {
            net.ssehub.kernel_haven.util.logic.Variable variable
                = (net.ssehub.kernel_haven.util.logic.Variable) argument;
            result = new net.ssehub.kernel_haven.util.logic.Variable(variable.getName() + "_MODULE");
            
        } else if (getHandleLinuxMacros() && function.equals("IS_BUILTIN")) {
            result = argument;
            
        } else {
            result = new Macro(function, argument);
        }
        
        return result;
    }
}
