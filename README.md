Лабораторная работа по проге номер 7 (Нейротех).
HTTP Ktor server
UI Compose for Desktop

На мой взгляд, CommandReference и всё что связано с обработкой команд является нагляднейшим пособием по функционалке на Kotlin, а также здесь самые хитровыебанные лямбды в мире)
Пример: 
```kotlin
data class CommandReference(
    val description: String? = "Command is not implemented yet",
    val arguments: List<Argument>? = null, // argument, pattern
    val preCompile: (List<String>) -> List<String> = { it },
    val function: suspend (
        List<String>,
        (ExecutionResult) -> Unit
    ) -> Unit
)
```
И вот пример применения:
```kotlin
"filter_starts_with_part_number" to CommandReference(
            description = "Command that shows elements that have partNumber that starts with specified substring",
            arguments = listOf(
                Argument("substring", CPT.STRING, "Substring to check")
            )
        ) { args, onCompleted ->
            var msg = ""
            dao.getProducts().filter { it.partNumber?.startsWith(args[0]) == true }.forEach {
                msg += it.toString()
            }
            onCompleted(ExecutionResult(message = msg))
        },
```
Учитесь и да пребудет с вами бог и душевное спокойствие!
Удачи, перваши)

> [!IMPORTANT]
> very public and loud constant val thanks = "Петренко Никита боженька)"
