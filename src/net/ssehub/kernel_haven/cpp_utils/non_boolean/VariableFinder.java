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
