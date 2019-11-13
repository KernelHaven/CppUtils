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

import net.ssehub.kernel_haven.util.logic.IFormulaVisitor;

/**
 * Extension of the {@link IFormulaVisitor} to support visitation of non-Boolean elements.<p>
 * 
 * <b><font color="red">Warning:</font></b> Please use this class with care and as seldom as possible.
 * KernelHaven and its analyses are designed to handle only Boolean conditions.
 * @author El-Sharkawy
 *
 */
public interface INonBooleanFormulaVisitor<T> extends IFormulaVisitor<T> {

    /**
     * Visits a non-Boolean operator.
     * @param operator The operator to visit.
     * @return @return Some return value.
     */
    public T visitNonBooleanOperator(NonBooleanOperator operator);
    
    /**
     * Visits a constant literal.
     * @param operator The literal to visit.
     * @return @return Some return value.
     */
    public T visitLiteral(Literal literal);
    
    /**
     * Visits a macro function call.
     * @param operator The function to visit.
     * @return @return Some return value.
     */
    public T visitMacro(Macro macro);
}
