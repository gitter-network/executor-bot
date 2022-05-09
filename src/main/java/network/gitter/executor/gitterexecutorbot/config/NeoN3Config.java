package network.gitter.executor.gitterexecutorbot.config;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Account;
import lombok.extern.slf4j.Slf4j;
import network.gitter.executor.gitterexecutorbot.service.GitterCoreService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(NeoN3Properties.class)
public class NeoN3Config {
    private final NeoN3Properties properties;

    public NeoN3Config(NeoN3Properties properties) {
        this.properties = properties;
    }

    @Bean
    public Neow3j neow3j() {
        String nodeUrl = this.properties.getNodeUrl();
        log.info("Using Neo N3 node: {}", nodeUrl);
        return Neow3j.build(new HttpService(nodeUrl));
    }

    @Bean
    public GitterCoreService gitterCoreService() {
        Hash160 scriptHash = new Hash160(this.properties.getGitterCoreScriptHash());
        log.info("GitterCoreService script hash: 0x{}", scriptHash);
        String walletWIF = this.properties.getWif();
        Account account = Account.fromWIF(walletWIF);
        log.info("Using wallet: {}", account.getAddress());
        return new GitterCoreService(this.neow3j(), account, scriptHash);
    }
}
