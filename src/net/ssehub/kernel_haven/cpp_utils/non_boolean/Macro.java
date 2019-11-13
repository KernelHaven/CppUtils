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
     */
    public Macro(String function, @Nullable Formula argument) {
        this.function = function;
        this.argument = argument;
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
            equal = ((Macro)obj).function.equals(function);
            if (equal && null != argument) {
                equal = argument.equals(((Macro)obj).argument);
            } else {
                equal = ((Macro)obj).argument == null;
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
            throw new RuntimeException(this.getClass().getCanonicalName() + " was used with " +
                visitor.getClass().getCanonicalName() + ", but supports only sub-classes of " +
                INonBooleanFormulaVisitor.class.getCanonicalName() + ".");
        }
    }

    @Override
    public void accept(@NonNull IVoidFormulaVisitor visitor) {
        if (visitor instanceof INonBooleanFormulaVisitor) {
            ((INonBooleanFormulaVisitor<?>) visitor).visitMacro(this);
        } else {
            throw new RuntimeException(this.getClass().getCanonicalName() + " was used with " +
                visitor.getClass().getCanonicalName() + ", but supports only sub-classes of " +
                INonBooleanFormulaVisitor.class.getCanonicalName() + ".");
        }
    }
}
