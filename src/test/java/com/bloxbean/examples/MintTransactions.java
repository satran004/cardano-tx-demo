package com.bloxbean.examples;

import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.api.util.PolicyUtil;
import com.bloxbean.cardano.client.cip.cip20.MessageMetadata;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.client.quicktx.Tx;
import com.bloxbean.cardano.client.transaction.spec.Asset;
import com.bloxbean.cardano.client.transaction.spec.Policy;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static com.bloxbean.cardano.client.common.ADAConversionUtil.adaToLovelace;
import static com.bloxbean.cardano.client.common.CardanoConstants.LOVELACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MintTransactions extends BaseTest {

    @Test
    void simpleMinting() throws CborSerializationException {
        Policy policy = PolicyUtil.createMultiSigScriptAllPolicy("test_policy",1);

        Tx tx = new Tx()
                .mintAssets(policy.getPolicyScript(),
                        new Asset("MyAsset", BigInteger.valueOf(1000)),
                        sender1Addr)
                .attachMetadata(MessageMetadata.create().add("Minting tx"))
                .from(sender1Addr);

        QuickTxBuilder quickTxBuilder = new QuickTxBuilder(backendService);

        Result<String> result = quickTxBuilder.compose(tx)
                .withSigner(SignerProviders.signerFrom(sender1))
                .withSigner(SignerProviders.signerFrom(policy))
                .completeAndWait();

        assertTrue(result.isSuccessful());
    }

    @Test
    void minting_withTransfer() throws CborSerializationException {
        Policy policy = PolicyUtil.createMultiSigScriptAtLeastPolicy("test_policy", 1, 1);

        Tx tx1 = new Tx()
                .payToAddress(receiver, Amount.ada(1.5))
                .mintAssets(policy.getPolicyScript(),
                        new Asset("AnotherAsset", BigInteger.valueOf(50)),
                        receiver)
                .attachMetadata(MessageMetadata.create().add("Minting tx"))
                .from(sender1Addr);

        Tx tx2 = new Tx()
                .payToAddress(receiver, new Amount(LOVELACE, adaToLovelace(100)))
                .from(sender2Addr);

        QuickTxBuilder quickTxBuilder = new QuickTxBuilder(backendService);

        Result<String> result = quickTxBuilder.compose(tx1, tx2)
                .feePayer(sender1.baseAddress())
                .withSigner(SignerProviders.signerFrom(sender1))
                .withSigner(SignerProviders.signerFrom(sender2))
                .withSigner(SignerProviders.signerFrom(policy))
                .completeAndWait(System.out::println);

        System.out.println(result);
        assertTrue(result.isSuccessful());
    }
}
