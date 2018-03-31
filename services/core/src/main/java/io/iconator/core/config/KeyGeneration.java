package io.iconator.core.config;

import io.iconator.commons.bitcoin.BitcoinKeyGenerator;
import io.iconator.commons.ethereum.EthereumKeyGenerator;
import io.iconator.commons.model.Keys;
import io.iconator.commons.model.db.KeyPairs;
import io.iconator.commons.sql.dao.KeyPairsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;

@Configuration
@Profile("dev")
public class KeyGeneration {

    @Value("${io.iconator.services.core.keypairs.generation.file-output.path}")
    private String keyPairsGenerationFileOutput;

    @Value("${io.iconator.services.core.keypairs.generation.amount}")
    private Integer keyPairsGenerationAmount;

    private final KeyPairsRepository keyPairsRepository;
    private final BitcoinKeyGenerator bitcoinKeyGenerator;
    private final EthereumKeyGenerator ethereumKeyGenerator;

    @Autowired
    public KeyGeneration(KeyPairsRepository keyPairsRepository, BitcoinKeyGenerator bitcoinKeyGenerator, EthereumKeyGenerator ethereumKeyGenerator) {
        this.keyPairsRepository = keyPairsRepository;
        this.bitcoinKeyGenerator = bitcoinKeyGenerator;
        this.ethereumKeyGenerator = ethereumKeyGenerator;
    }

    @PostConstruct
    public void generateFreshKeys() {
        for (int i = 0; i < this.keyPairsGenerationAmount; i++) {
            Keys bitcoinKeys = this.bitcoinKeyGenerator.getKeys();
            Keys ethereumKeys = this.ethereumKeyGenerator.getKeys();
            KeyPairs keyPair = new KeyPairs(bitcoinKeys.getPublicKeyAsHexString(), ethereumKeys.getPublicKeyAsHexString());
            keyPairsRepository.saveAndFlush(keyPair);
        }
        keyPairsRepository.createFreshKeySequence();
    }
}
