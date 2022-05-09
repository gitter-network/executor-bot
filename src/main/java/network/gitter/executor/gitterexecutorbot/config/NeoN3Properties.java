package network.gitter.executor.gitterexecutorbot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "neo.n3")
public class NeoN3Properties {
    @NotBlank
    private String nodeUrl;
    @NotBlank
    private String gitterCoreScriptHash;
    @NotBlank
    private String wif;
}