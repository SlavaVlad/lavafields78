package app.common.server

import com.apu.db.product.ProductRepository
import com.apu.db.product.requests.CPT
import com.apu.db.product.requests.CommandReference
import com.apu.db.product.requests.ConsoleError
import com.apu.takeAfter
import kotlinx.serialization.Serializable

class CommandCompilationException(message: String) : Exception(message)

@Serializable
class Command(
    var name: String? = null,
    var args: Array<String>? = null,
)
