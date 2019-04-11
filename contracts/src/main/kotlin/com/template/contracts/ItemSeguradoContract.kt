package com.template.contracts

import com.template.model.SeguroContratado
import com.template.states.ItemSeguradoState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

class ItemSeguradoContract: Contract {

    override fun verify(tx: LedgerTransaction) {
        val comando = tx.commandsOfType<Commands>().single()
        when (comando.value) {
            is Commands.SegurarItem -> verifySegurarItem(tx)
        }
    }

    fun verifySegurarItem(tx: LedgerTransaction){
        requireThat {
            "Não deveria haver nenhum input." using tx.inputsOfType<ItemSeguradoState>().isEmpty()
            "Deve haver apenas um output." using (tx.outputsOfType<ItemSeguradoState>().size == 1)
            val outputs = tx.outputsOfType<ItemSeguradoState>()
            "O Segurador deve ser diferente do Vendedor." using outputs.all{ it.segurador != it.vendedor}
            "A marca precisa ser informada." using outputs.all { it.itemSegurado.marca.isNotBlank() }
            "O nome do Produto precisa ser informado." using outputs.all { it.itemSegurado.produto.isNotBlank() }
            "O Tipo do Produto precisa ser informado." using outputs.all { it.itemSegurado.tipo.isNotBlank() }
            "O Valor do Produto precisa ser maior que zero." using outputs.all { it.itemSegurado.valor > 0 }
            "Ao menos um seguro deve ser cadastrado." using outputs.all { it.itemSegurado.segurosContratados.isNotEmpty() }
            outputs.forEach { it.itemSegurado.segurosContratados.forEach { verifySeguroContratado(it) } }
        }
    }

    fun verifySeguroContratado(seguroContratado: SeguroContratado){
        requireThat {
            "A descrição do Seguro Contratado precisa ser preenchida" using seguroContratado.descricao.isNotBlank()
            "O ID do Seguro Contratado precisa ser informado." using seguroContratado.idExterno.isNotBlank()
            "O Tipo do Seguro Contratado precisa ser informado." using seguroContratado.tipo.isNotBlank()
            "O Valor do Seguro Contratado deve ser maior que zero." using (seguroContratado.valor > 0 )
        }
    }

    interface Commands: CommandData {
        class SegurarItem: Commands
    }
}