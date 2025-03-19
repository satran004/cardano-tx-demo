package com.bloxbean.examples;

import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.cip.cip20.MessageMetadata;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.client.quicktx.Tx;
import org.junit.jupiter.api.Test;

public class SimpleTransfers extends BaseTest {

    @Test
    public void simpleTransfer() {
        Tx tx = new Tx()
                .payToAddress(receiver, Amount.ada(2.5))
                .attachMetadata(MessageMetadata.create().add("This is a test message 2"))
                .from(sender1Addr);

        QuickTxBuilder quickTxBuilder = new QuickTxBuilder(backendService);
        Result<String> result = quickTxBuilder
                .compose(tx)
                .withSigner(SignerProviders.signerFrom(sender1))
                .completeAndWait(System.out::println);
    }

    @Test
    public void transfer() {
        Tx tx1 = new Tx()
                .payToAddress(receiver, Amount.ada(1.5))
                .from(sender1Addr);

        Tx tx2 = new Tx()
                .payToAddress(receiver2, Amount.ada(4.5))
                .from(sender2Addr);

        QuickTxBuilder quickTxBuilder = new QuickTxBuilder(backendService);
        Result<String> result = quickTxBuilder
                .compose(tx1, tx2)
                .feePayer(sender1Addr)
                .withSigner(SignerProviders.signerFrom(sender1))
                .withSigner(SignerProviders.signerFrom(sender2))
                .completeAndWait(System.out::println);
    }

}
