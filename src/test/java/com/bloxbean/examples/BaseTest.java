package com.bloxbean.examples;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.api.DefaultUtxoSupplier;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.crypto.cip1852.DerivationPath;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.plutus.spec.BigIntPlutusData;
import com.bloxbean.cardano.client.plutus.spec.PlutusData;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.client.quicktx.Tx;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public class BaseTest {
    public static String MNEMONIC = "test test test test test test test test test test test test test test test test test test test test test test test sauce";

    public final static Account sender1 = new Account(Networks.testnet(), MNEMONIC);
    public final static String sender1Addr = sender1.baseAddress();

    public final static Account sender2 = new Account(Networks.testnet(), MNEMONIC, DerivationPath.createExternalAddressDerivationPathForAccount(2));
    public final static String sender2Addr = sender2.baseAddress();

    public String receiver = "addr_test1qq8phk43ndg0zf2l4xc5vd7gu4f85swkm3dy7fjmfkf6q249ygmm3ascevccsq5l5ym6khc3je5plx9t5vsa06jvlzls8el07z";
    public String receiver2 = "addr_test1qq7a8p6zaxzgcmcjcy7ak8u5vn7qec9mjggzw6qg096nzlj6n7rflnv3x43vnv8q7q0h0ef4n6ncp5mljd2ljupwl79s5mqneq";

    public static BackendService backendService = new BFBackendService("http://localhost:8080/api/v1/", "Dummy Key");

    protected void checkIfUtxoAvailable(String txHash, String address) {
        Optional<Utxo> utxo = Optional.empty();
        int count = 0;
        while (utxo.isEmpty()) {
            if (count++ >= 20)
                break;
            List<Utxo> utxos = new DefaultUtxoSupplier(backendService.getUtxoService()).getAll(address);
            utxo = utxos.stream().filter(u -> u.getTxHash().equals(txHash))
                    .findFirst();
            System.out.println("Try to get new output... txhash: " + txHash);
            try {
                Thread.sleep(1000);
            } catch (Exception e) {}
        }
    }

    protected void lockFund(String scriptAddress, BigInteger scriptAmt, PlutusData plutusData) {
        Tx tx = new Tx();
        tx.payToContract(scriptAddress, Amount.lovelace(scriptAmt), plutusData)
                .from(sender2Addr);

        QuickTxBuilder quickTxBuilder = new QuickTxBuilder(backendService);
        Result<String> result = quickTxBuilder.compose(tx)
                .withSigner(SignerProviders.signerFrom(sender2))
                .completeAndWait(System.out::println);

        System.out.println(result.getResponse());
        checkIfUtxoAvailable(result.getValue(), scriptAddress);
    }
}
