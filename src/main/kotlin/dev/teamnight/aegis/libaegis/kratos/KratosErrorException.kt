package dev.teamnight.aegis.libaegis.kratos

import dev.teamnight.aegis.libaegis.kratos.http.KratosError

class KratosErrorException(
    val error: KratosError
) : Exception("Kratos returned an error: ${error.message}")