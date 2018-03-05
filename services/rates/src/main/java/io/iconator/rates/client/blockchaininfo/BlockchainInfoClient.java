package io.iconator.rates.client.blockchaininfo;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import info.blockchain.api.blockexplorer.BlockExplorer;
import info.blockchain.api.blockexplorer.LatestBlock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class BlockchainInfoClient {

    @Autowired
    private BlockExplorer blockExplorer;

    @Autowired
    private Retryer retryer;

    public long getCurrentBlockNr() throws ExecutionException, RetryException {
        return (long) retryer.call(() -> {
            LatestBlock latestBlock = blockExplorer.getLatestBlock();
            return latestBlock.getIndex();
        });
    }

}
