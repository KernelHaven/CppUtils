package net.ssehub.kernel_haven.cpp_utils.non_boolean;

import java.util.Set;

import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Variable;

/**
 * Collects all used variables from a formula, which may contain non-Boolean extensions of the default Formula model.
 * @author El-Sharkawy
 *
 */
public class VariableFinder extends net.ssehub.kernel_haven.util.logic.VariableFinder
    implements INonBooleanFormulaVisitor<Set<Variable>> {

    @Override
    public Set<Variable> visitNonBooleanOperator(NonBooleanOperator operator) {
        operator.getLeft().accept(this);
        operator.getRight().accept(this);
        return getVariables();
    }

    @Override
    public Set<Variable> visitLiteral(Literal literal) {
        // Nothing to do, literal cannot hold any variables
        return getVariables();
    }

    @Override
    public Set<Variable> visitMacro(Macro macro) {
        Formula argument = macro.getArgument();
        if (null != argument) {
            argument.accept(this); 
        }
        return getVariables();
    }

}
