package com.apu.db.product.requests

import com.apu.callback.ExecutionResult
import java.util.regex.Pattern

enum class CPT(val pattern: Pattern) {
    BOOL(Pattern.compile("[true|false]")),
    STRING(Pattern.compile(".*")),
    INTEGER(Pattern.compile("[+-]?[0-9]*")),
    DOUBLE(Pattern.compile("[+-]?([0-9]*[.])?[0-9]+")),
    FILEPATH(Pattern.compile("^([a-zA-Z]:)?(\\\\[^<>:\"/\\\\|?*]+)+\\\\?\n")),
    JSON(Pattern.compile("""^\s*(\{.*\}|\[.*\])\s*$"""))
}

data class Argument(
    val name: String,
    val type: CPT,
    val description: String
)

data class CommandReference(
    val description: String? = "Command is not implemented yet",
    val arguments: List<Argument>? = null, // argument, pattern
    val preCompile: (List<String>) -> List<String> = { it },
    val function: suspend (
        List<String>,
        (ExecutionResult) -> Unit
    ) -> Unit
)
