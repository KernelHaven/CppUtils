package net.ssehub.kernel_haven.cpp_utils;

import net.ssehub.kernel_haven.config.EnumSetting;
import net.ssehub.kernel_haven.config.Setting;
import net.ssehub.kernel_haven.config.Setting.Type;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * General settings  for parsing CPP expressions, which may be used by Code-Extractors.
 * @author Adam
 * @author El-Sharkawy
 *
 */
public class CppParsingSettings {
    
    public static final @NonNull EnumSetting<@NonNull InvalidConditionHandling> INVALID_CONDITION_SETTING
        = new EnumSetting<>("code.extractor.invalid_condition", InvalidConditionHandling.class, true,
            InvalidConditionHandling.EXCEPTION, "How to handle conditions of blocks that are invalid or not "
                + "parseable.\n\n- EXCEPTION: Throw an exception. This causes the whole file to not be "
                + "parseable.\n- TRUE: Replace the invalid condition with true.\n- ERROR_VARIABLE: Replace "
                + "the invalid condition with a variable called \"PARSING_ERROR\"");

    public static final @NonNull Setting<@NonNull Boolean> HANDLE_LINUX_MACROS = new Setting<>(
        "code.extractor.handle_linux_macros", Type.BOOLEAN, true, "false", "Whether to handle the preprocessor macros "
            + "IS_ENABLED, IS_BUILTIN and IS_MODULE in preprocessor block conditions.");

}
