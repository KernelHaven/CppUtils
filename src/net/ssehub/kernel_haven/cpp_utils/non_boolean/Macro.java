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

import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.IFormulaVisitor;
import net.ssehub.kernel_haven.util.logic.IVoidFormulaVisitor;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.util.null_checks.Nullable;

/**
 * Extension of the default Boolean Formulas to support non-Boolean macros (function calls).<p>
 * 
 * <b><font color="red">Warning:</font></b> Please use this class with care and as seldom as possible.
 * KernelHaven and its analyses are designed to handle only Boolean conditions.
 * @author El-Sharkawy
 *
 */
public class Macro extends Formula {

    private static final long serialVersionUID = 449836254832473290L;
    private @NonNull String function;
    private @Nullable Formula argument;
    
    /**
     * Creates a non-Boolean {@link Macro}.
     * @param function The macro / function call
     * @param argument The argument of the function call.
     */
    public Macro(String function, @Nullable Formula argument) {
        this.function = function;
        this.argument = argument;
    }
    
    /**
     * Returns the argument of the function call.
     * @return The argument of the function call, which may be <tt>null</tt>.
     */
    public @Nullable Formula getArgument() {
        return argument;
    }
    
    /**
     * Returns the name of the function.
     * @return The name of the called function.
     */
    public @NonNull String getFunction() {
        return function;
    }

    @Override
    protected int getPrecedence() {
        return 4;
    }

    @Override
    public @NonNull String toString() {
        return (null != argument) ? function + "(" + argument.toString() + ")" : function + "()";
    }

    @Override
    public void toString(@NonNull StringBuilder result) {
        result.append(function);
        result.append("(");
        if (null != argument) {
            argument.toString(result);
        }
        result.append(")");
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        boolean equal = false;
        if (obj instanceof Macro) {
            equal = ((Macro) obj).function.equals(function);
            if (equal && null != argument) {
                equal = argument.equals(((Macro) obj).argument);
            } else {
                equal = ((Macro) obj).argument == null;
            }
        } 
        
        return equal;
    }

    @Override
    public int hashCode() {
        return function.hashCode() + argument.hashCode();
    }

    @Override
    public <T> T accept(@NonNull IFormulaVisitor<T> visitor) {
        if (visitor instanceof INonBooleanFormulaVisitor) {
            return ((INonBooleanFormulaVisitor<T>) visitor).visitMacro(this);
        } else {
            throw new RuntimeException(this.getClass().getCanonicalName() + " was used with "
                + visitor.getClass().getCanonicalName() + ", but supports only sub-classes of "
                + INonBooleanFormulaVisitor.class.getCanonicalName() + ".");
        }
    }

    @Override
    public void accept(@NonNull IVoidFormulaVisitor visitor) {
        if (visitor instanceof INonBooleanFormulaVisitor) {
            ((INonBooleanFormulaVisitor<?>) visitor).visitMacro(this);
        } else {
            throw new RuntimeException(this.getClass().getCanonicalName() + " was used with "
                + visitor.getClass().getCanonicalName() + ", but supports only sub-classes of "
                + INonBooleanFormulaVisitor.class.getCanonicalName() + ".");
        }
    }
}
