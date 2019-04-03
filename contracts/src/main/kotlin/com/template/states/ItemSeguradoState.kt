package com.template.states

import com.template.model.ItemSegurado
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class ItemSeguradoState(val itemSegurado: ItemSegurado,
                             val segurador: Party,
                             val vendedor: Party,
                             override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {
    override val participants: List<AbstractParty> = listOf(vendedor, segurador)
}