package tv.codealong.tutorials.springboot.thenewboston.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import tv.codealong.tutorials.springboot.thenewboston.model.Bank

class JsonStringTest{

    @Test
    fun `should convert object to json string`() {
        // given
        val bank = Bank("123456",  1.2, 1)

        // when
        val jsonString = JsonString.serialize(bank).value

        // then
        println(jsonString) // {"account_number":"123456","trust":1.2,"default_transaction_fee":1}
    }

    @Test
    fun `should convert json string to object`() {
        val jsonString = "{\"account_number\":\"123456\",\"trust\":1.2,\"default_transaction_fee\":1}"

        val deserialize = JsonString(jsonString).deserialize<Bank>()
        println(deserialize)// Bank(accountNumber=123456, trust=1.2, transactionFee=1)
    }
}