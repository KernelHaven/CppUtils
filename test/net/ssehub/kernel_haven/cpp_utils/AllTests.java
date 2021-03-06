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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import net.ssehub.kernel_haven.cpp_utils.non_boolean.AllNonBooleanTests;
import net.ssehub.kernel_haven.cpp_utils.parser.AllParserTests;

/**
 * All tests for this project.
 *
 * @author El-Sharkawy
 */
@RunWith(Suite.class)
@SuiteClasses({
    AllParserTests.class,
    AllNonBooleanTests.class,
    
    CppConditionParserTest.class,
    NumberUtilsTest.class,
    })
public class AllTests {

}
