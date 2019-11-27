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
package net.ssehub.kernel_haven.cpp_utils.non_boolean;

import net.ssehub.kernel_haven.cpp_utils.parser.CppOperator;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.IFormulaVisitor;
import net.ssehub.kernel_haven.util.logic.IVoidFormulaVisitor;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.util.null_checks.Nullable;

/**
 * Extension of the default Boolean Formulas to support non-Boolean operators.<p>
 * <b><font color="red">Warning:</font></b> Please use this class with care and as seldom as possible.
 * KernelHaven and its analyses are designed to handle only Boolean conditions.
 * @author El-Sharkawy
 *
 */
public class NonBooleanOperator extends Formula {
    
    private static final long serialVersionUID = -3318742597724247806L;
    private @NonNull Formula left;    
    private @NonNull CppOperator operation;
    private @NonNull Formula right;
    
    /**
     * Creates a {@link NonBooleanOperator}.
     * @param left Left operand
     * @param operation The operation
     * @param right Right operand
     */
    public NonBooleanOperator(@NonNull Formula left, @NonNull CppOperator operation, @NonNull Formula right) {
        this.left = left;
        this.operation = operation;
        this.right = right;
    }

    @Override
    protected int getPrecedence() {
        return 4;
    }

    @Override
    public @NonNull String toString() {
        return "(" + left.toString() + ") " + operation.getSymbol() + " (" + right.toString() + ")";
    }

    @Override
    public void toString(@NonNull StringBuilder result) {
        // TODO SE: Consider precedence (requires modification of Formula interface
        left.toString(result);
        result.append(" ");
        result.append(operation.getSymbol());
        result.append(" ");
        right.toString(result);
    }

    @Override
    public <T> T accept(@NonNull IFormulaVisitor<T> visitor) {
        if (visitor instanceof INonBooleanFormulaVisitor) {
            return ((INonBooleanFormulaVisitor<T>) visitor).visitNonBooleanOperator(this);
        } else {
            throw new RuntimeException(this.getClass().getCanonicalName() + " was used with "
                + visitor.getClass().getCanonicalName() + ", but supports only sub-classes of "
                + INonBooleanFormulaVisitor.class.getCanonicalName() + ".");
        }
    }

    @Override
    public void accept(@NonNull IVoidFormulaVisitor visitor) {
        if (visitor instanceof INonBooleanFormulaVisitor) {
            ((INonBooleanFormulaVisitor<?>) visitor).visitNonBooleanOperator(this);
        } else {
            throw new RuntimeException(this.getClass().getCanonicalName() + " was used with "
                + visitor.getClass().getCanonicalName() + ", but supports only sub-classes of "
                + INonBooleanFormulaVisitor.class.getCanonicalName() + ".");
        }
    }
    
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof NonBooleanOperator) {
            NonBooleanOperator other = (NonBooleanOperator) obj;
            return left.equals(other.getLeft()) && right.equals(other.getRight());
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return (left.hashCode() + right.hashCode()) * operation.hashCode() * 5591;
    }
    
    /**
     * Returns the formula that is nested on the left side of this operation.
     * 
     * @return The left operand.
     */
    public @NonNull Formula getLeft() {
        return left;
    }
    
    /**
     * Returns the formula that is nested on the right side of this operation.
     * 
     * @return The right operand.
     */
    public @NonNull Formula getRight() {
        return right;
    }

}
