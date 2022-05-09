package network.gitter.executor.gitterexecutorbot.service;

import io.neow3j.contract.SmartContract;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.response.NeoBlock;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.stackitem.ByteStringStackItem;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.transaction.Signer;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class GitterCoreService {

    private final Neow3j neow3j;
    private final Account account;
    private final Hash160 gitterCoreScriptHash;

    public GitterCoreService(Neow3j neow3j, Account account, Hash160 gitterCoreScriptHash) {
        this.neow3j = neow3j;
        this.account = account;
        this.gitterCoreScriptHash = gitterCoreScriptHash;

        try {
            this.neow3j.subscribeToNewBlocksObservable(false)
                    .subscribe(v -> {
                        log.info("new block...fetching jobs");
                        fetchJobs().stream().forEach(job -> {
                            try {
                                fetchJob(job);
                                executeJob(job);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                        });
                    }, throwable -> {
                        log.error("something went wrong");
                    });
        } catch (IOException e) {
        }
    }
    private void executeJob(byte[] job) {
        log.info("executing job: {}", job);
        try {
            new SmartContract(gitterCoreScriptHash, neow3j)
                    .invokeFunction("executeJob", ContractParameter.byteArray(job), ContractParameter.hash160(account))
                    .signers(AccountSigner.calledByEntry(account)).sign().send();
        } catch (Throwable t) {
            log.error("Something went wrong", t);
        }
    }
    private List<byte[]> fetchJobs() throws IOException {
        List<byte[]> result = new SmartContract(gitterCoreScriptHash, neow3j)
                .callInvokeFunction("jobs").getInvocationResult().getStack()
                .get(0).getList().stream().map(v -> v.getByteArray())
                .collect(Collectors.toList());
        return result;
    }

    private StackItem fetchJob(byte[] job) throws IOException {
        return new SmartContract(gitterCoreScriptHash, neow3j).callInvokeFunction("getJob", Arrays.asList(ContractParameter.byteArray(job)))
                .getInvocationResult().getStack().get(0);
    }
}
