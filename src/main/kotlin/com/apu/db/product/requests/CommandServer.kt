package app.common.server

import com.apu.ConsoleColors
import com.apu.callback.ExecutionResult
import com.apu.db.product.ProductDao
import com.apu.db.product.ProductRepository
import com.apu.db.product.requests.Argument
import com.apu.db.product.requests.CPT
import com.apu.db.product.requests.CommandReference
import com.apu.db.utils.ProductCollectionInfo
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import persistence.database.product.Product

class CommandServer(private val dao: ProductDao) {

    fun String.toProduct(): Product {
        val mapper = ObjectMapper().apply {
            this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
        return mapper.readValue(this, Product::class.java)
    }

    private var commands = hashMapOf(
        "info" to CommandReference(description = "Command that shows information about collection") { args, onCompleted ->
            onCompleted(ExecutionResult(message = ProductCollectionInfo(dao).toString()))
        },
        "show" to CommandReference(description = "Command that shows all elements of collection") { args, onCompleted ->
            var msg = ""
            dao.getProducts().forEach {
                msg += it.toString()
            }
            onCompleted(ExecutionResult(message = msg))
        },
        "add" to CommandReference(description = "Command that adds new element to collection", arguments = listOf(
            Argument(
                "product", CPT.JSON, "Product to add"
            )
        )) { args, onCompleted ->
            dao.addProduct(args[0].toProduct()) {
                onCompleted(it)
            }
        },
        "update" to CommandReference(
            description = "Command that updates element with specified id",
            arguments = listOf(
                Argument("id", CPT.INTEGER, "Id of element to update"),
                Argument("id", CPT.JSON, "element with updated parameters")
            )
        ) { args, onCompleted ->
            try {
                dao.removeProductById(args[0].toInt().toLong()) { statusRemoved ->
                    if (statusRemoved.error == null) {
                        runBlocking {
                            dao.addProduct(args[1].toProduct()) { statusAdded ->
                                onCompleted(statusAdded)
                            }
                        }
                    } else {
                        onCompleted(statusRemoved)
                    }
                }
            } catch (e: Exception) {
                onCompleted(ExecutionResult(error = e.message))
            }
            onCompleted(ExecutionResult(message = "Element updated"))
        },
        "remove_by_id" to CommandReference(
            description = "Command that removes element with specified id",
            listOf(
                Argument("id", CPT.INTEGER, "Id of element to delete")
            )
        ) { args, onCompleted ->
            dao.removeProductById(args[0].toLong()) {
                onCompleted(it)
            }
        },
        "clear" to CommandReference(description = "Command that clears collection") { args, onCompleted ->
            dao.clear()
            onCompleted(ExecutionResult(message = "Collection cleared"))
        },
        "add_if_max" to CommandReference(
            description = "Command that adds new element to collection if it's price is greater than max price in collection",
            arguments = listOf(
                Argument("product", CPT.JSON, "Product to add if max")
            )
        ) { args, onCompleted ->
            try {
                args[0].toProduct().let {
                    if (dao.compareMax(it)) {
                        dao.addProduct(it)
                    }
                }
            } catch (e: Exception) {
                onCompleted(ExecutionResult(error = e.message))
            }
            onCompleted(ExecutionResult(message = "Element added"))
        },
        "add_if_min" to CommandReference(
            description = "Command that adds new element to collection if it's price is less than max price in collection",
            arguments = listOf(
                Argument("product", CPT.JSON, "Product to add if max")
            )
        ) { args, onCompleted ->
            try {
                args[0].toProduct().let {
                    if (dao.compareMin(it)) {
                        dao.addProduct(it)
                    }
                }
            } catch (e: Exception) {
                onCompleted(ExecutionResult(error = e.message))
            }
            onCompleted(ExecutionResult(message = "Element added"))
        },
        "remove_greater" to CommandReference(
            description = "Command that removes all elements that are greater than specified",
            arguments = listOf(
                Argument("product", CPT.JSON, "Product to add if max")
            )
        ) { args, onCompleted ->
            var prod: Product? = null
            try {
                prod = args[0].toProduct()
            } catch (e: Exception) {
                onCompleted(ExecutionResult(error = e.message))
            }
            onCompleted(ExecutionResult(message = "Removed ${prod?.let { dao.removeAllGreaterThan(it) }} elements"))
        },
        "group_counting_by_price" to CommandReference(
            description = "Command that groups elements by price and shows count of elements in each group",
            arguments = listOf(
                Argument("price", CPT.INTEGER, "Price to group by")
            )
        ) { args, onCompleted ->
            var msg = ""
            dao.getProducts().groupBy { args[0] }.forEach {
                msg += "Price: ${it.key}, count: ${it.value.size}\n"
            }
            onCompleted(ExecutionResult(message = msg))
        },
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
        "filter_greater_than_price" to CommandReference(
            description = "Command that shows elements that have price greater than specified",
            arguments = listOf(
                Argument("price", CPT.INTEGER, "Price to check")
            )
        ) { args, onCompleted ->
            var msg = ""
            dao.getProducts().filter { (it.price ?: -1) > args[0].toInt() }.forEach {
                msg += it.toString()
            }
            onCompleted(ExecutionResult(message = msg))
        }
    )

    private val runtimeCommands = hashMapOf(
        "add_command" to CommandReference(
            description = "Command that adds new command", arguments = listOf(
                Argument("name", CPT.STRING, description = "Name of command"),
                Argument("description", CPT.STRING, description = "Description of command"),
            )
        ) { args, onCompleted ->
            try {
                commands[args[0]] = CommandReference(description = "Command ad") { args, onCompleted ->
                    onCompleted(ExecutionResult(message = "Command added at runtime"))
                }
            } catch (e: Exception) {
                onCompleted(ExecutionResult(error = e.message))
            }
            onCompleted(ExecutionResult(message = "Command added"))
        },
        "help" to CommandReference(
            description = "Manual for all commands"
        ) { args, onCompleted ->
            var msg = "Available commands:\n"
            commands.forEach { (name, command) ->
                var argumentsString = "\n"
                command.arguments?.forEach {
                    argumentsString += "    ${ConsoleColors.ANSI_CYAN}(${it.name}: ${it.type.name})${ConsoleColors.ANSI_RESET} - ${ConsoleColors.ANSI_GREEN}${it.description}${ConsoleColors.ANSI_RESET}\n"
                }
                // list of all commands in format: commandName - description and list of arguments from argumentsString
                msg += if ((command.arguments?.size ?: 0) == 0) {
                    "${ConsoleColors.ANSI_BLUE}$name${ConsoleColors.ANSI_RESET}() - ${ConsoleColors.ANSI_LIGHT_GREEN}${command.description}${ConsoleColors.ANSI_RESET}\n"
                } else {
                    "${ConsoleColors.ANSI_BLUE}$name${ConsoleColors.ANSI_RESET}($argumentsString) - ${ConsoleColors.ANSI_LIGHT_GREEN}${command.description}${ConsoleColors.ANSI_RESET}\n"
                }
            }

            onCompleted(ExecutionResult(message = msg))
        }
    )

    init {
        commands += runtimeCommands
    }

    fun findReferenceOrNull(commandName: String): CommandReference? {
        return commands[commandName]
    }

    companion object {
        suspend fun handleCommand(
            command: Command,
            repo: ProductRepository,
            callback: (ExecutionResult) -> Unit = { _ -> },
        ) {
            val ref = CommandServer(repo)
                .findReferenceOrNull(command.name!!)
            ref?.function
                ?.invoke(command.args!!.toList(), callback)
        }
    }

}