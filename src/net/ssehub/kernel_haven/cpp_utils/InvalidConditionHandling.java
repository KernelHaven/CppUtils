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

import net.ssehub.kernel_haven.util.logic.True;

/**
 * Different options how to handle invalid / unparseable expressions in the {@link BlockParser}.
 *
 * @author Adam
 */
public enum InvalidConditionHandling {

    /**
     * Throw an exception. This causes the whole file to not be parseable.
     */
    EXCEPTION,
    
    /**
     * Replace the invalid condition with {@link True}.
     */
    TRUE,
    
    /**
     * Replace the invalid condition with a variable called "PARSING_ERROR".
     */
    ERROR_VARIABLE,
    
}
