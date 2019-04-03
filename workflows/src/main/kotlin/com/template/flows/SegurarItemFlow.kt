package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.ItemSeguradoContract
import com.template.model.ItemSegurado
import com.template.states.ItemSeguradoState
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.unwrap
import java.time.Instant

object SegurarItemFlow {

    @InitiatingFlow
    @StartableByRPC
    class Initiator(val itemSegurado: ItemSegurado,
                    val segurador: Party) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val notary = serviceHub.networkMapCache.notaryIdentities.first()
            val comando = Command(ItemSeguradoContract.Commands.SegurarItem(),
                    listOf(ourIdentity, segurador).map { it.owningKey })

            val sessionSegurador = initiateFlow(segurador)

            val horaTransacao = sessionSegurador.receive<Instant>().unwrap {
                it
            }

            val itemSeguradoComTimestamp = itemSegurado.copy(timeStamp = horaTransacao)

            val output = ItemSeguradoState(itemSeguradoComTimestamp, segurador, ourIdentity)

            val txBuilder = TransactionBuilder(notary)
                    .addCommand(comando)
                    .addOutputState(output, ItemSeguradoContract::class.java.canonicalName)

            txBuilder.verify(serviceHub)

            val transacaoParcialmenteAssinada = serviceHub.signInitialTransaction(txBuilder)

            val transacaoAssinada = subFlow(CollectSignaturesFlow(transacaoParcialmenteAssinada,
                    listOf(sessionSegurador)))

            return subFlow(FinalityFlow(transacaoAssinada))
        }
    }
}