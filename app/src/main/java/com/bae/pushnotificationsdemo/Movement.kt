package com.bae.pushnotificationsdemo

data class Movement(
    val id: String,
    val type: String,
    val amount: String,
    val status: String
)

object FakeMovementRepository {

    private val movements = mapOf(
        "78421" to Movement(
            id = "78421",
            type = "Transferencia recibida",
            amount = "S/ 250.00",
            status = "Completada"
        ),
        "90135" to Movement(
            id = "90135",
            type = "Pago realizado",
            amount = "S/ 89.90",
            status = "Procesado"
        )
    )

    fun getMovement(id: String): Movement? = movements[id]
}