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

import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.IFormulaVisitor;
import net.ssehub.kernel_haven.util.logic.IVoidFormulaVisitor;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.util.null_checks.Nullable;

/**
 * Extension of the default Boolean Formulas to support non-Boolean literals (strings, numbers).<p>
 * 
 * <b><font color="red">Warning:</font></b> Please use this class with care and as seldom as possible.
 * KernelHaven and its analyses are designed to handle only Boolean conditions.
 * @author El-Sharkawy
 *
 */
public class Literal extends Formula {

    private static final long serialVersionUID = 449836254832473290L;
    private @NonNull String literal;
    
    /**
     * Creates a non-Boolean {@link Literal}.
     * @param literal The literal (string or number constant)
     */
    public Literal(@NonNull String literal) {
        this.literal = literal;
    }

    @Override
    protected int getPrecedence() {
        return 3;
    }

    @Override
    public @NonNull String toString() {
        return literal;
    }

    @Override
    public void toString(@NonNull StringBuilder result) {
        result.append(literal);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        boolean equal = false;
        if (obj instanceof Literal) {
            equal = ((Literal) obj).literal.equals(literal);
        } 
        
        return equal;
    }

    @Override
    public int hashCode() {
        return literal.hashCode();
    }

    @Override
    public <T> T accept(@NonNull IFormulaVisitor<T> visitor) {
        if (visitor instanceof INonBooleanFormulaVisitor) {
            return ((INonBooleanFormulaVisitor<T>) visitor).visitLiteral(this);
        } else {
            throw new RuntimeException(this.getClass().getCanonicalName() + " was used with "
                + visitor.getClass().getCanonicalName() + ", but supports only sub-classes of "
                + INonBooleanFormulaVisitor.class.getCanonicalName() + ".");
        }
    }

    @Override
    public void accept(@NonNull IVoidFormulaVisitor visitor) {
        if (visitor instanceof INonBooleanFormulaVisitor) {
            ((INonBooleanFormulaVisitor<?>) visitor).visitLiteral(this);
        } else {
            throw new RuntimeException(this.getClass().getCanonicalName() + " was used with "
                + visitor.getClass().getCanonicalName() + ", but supports only sub-classes of "
                + INonBooleanFormulaVisitor.class.getCanonicalName() + ".");
        }
    }
}
