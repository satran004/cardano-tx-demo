package com.bloxbean.examples;

import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.cip.cip20.MessageMetadata;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.governance.GovId;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.client.quicktx.Tx;
import com.bloxbean.cardano.client.transaction.spec.governance.DRep;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StakingAndGovTransactions extends BaseTest {

    @Test
    @Order(1)
    void stakeAddressRegistration_drepRegistrations() {
        Tx tx = new Tx()
                .registerStakeAddress(sender1Addr)
                .registerDRep(sender1)
                .attachMetadata(MessageMetadata.create().add("This is a stake registration tx"))
                .from(sender1Addr);

        QuickTxBuilder quickTxBuilder = new QuickTxBuilder(backendService);

        Result<String> result = quickTxBuilder.compose(tx)
                .withSigner(SignerProviders.signerFrom(sender1))
                .withSigner(SignerProviders.drepKeySignerFrom(sender1))
                .completeAndWait(msg -> System.out.println(msg));

        System.out.println(result);
        assertTrue(result.isSuccessful());
    }

    @Test
    @Order(2)
    void voteDelegation() {

        DRep dRep = GovId.toDrep(sender1.drepId());

        Tx tx = new Tx()
                .registerStakeAddress(sender2Addr)
                .delegateVotingPowerTo(sender2Addr, dRep)
                .attachMetadata(MessageMetadata
                        .create()
                        .add("Register Stake Key + Vote Delegation"))
                .from(sender1Addr);

        QuickTxBuilder quickTxBuilder = new QuickTxBuilder(backendService);

        Result<String> result = quickTxBuilder.compose(tx)
                .withSigner(SignerProviders.signerFrom(sender1))
                .withSigner(SignerProviders.stakeKeySignerFrom(sender2))
                .completeAndWait(msg -> System.out.println(msg));

        System.out.println(result);
        assertTrue(result.isSuccessful());
    }
}
