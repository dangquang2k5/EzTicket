package huce.fit.myezticket.ui.screens

internal fun parseSelectedTicketsArg(selectedTicketsString: String?): Map<String, Int> {
    return selectedTicketsString
        ?.split(";")
        ?.mapNotNull { item ->
            val separatorIndex = item.lastIndexOf(":")
            if (separatorIndex <= 0) return@mapNotNull null

            val name = item.substring(0, separatorIndex)
            val quantity = item.substring(separatorIndex + 1).toIntOrNull() ?: 0
            name to quantity
        }
        ?.filter { (_, quantity) -> quantity > 0 }
        ?.toMap()
        ?: emptyMap()
}

internal fun formatVnd(amount: Long): String {
    return String.format("%,d", amount).replace(",", ".")
}
