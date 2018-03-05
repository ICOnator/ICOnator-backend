package io.iconator.rates.retrier;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RetryAction {

    Retryer retryer;

    public RetryAction(long minTimeWait, long maxTimeWait, int stopAfterAttempt) {
        retryer = RetryerBuilder.newBuilder()
                .retryIfExceptionOfType(IOException.class)
                .retryIfRuntimeException()
                .withWaitStrategy(WaitStrategies.randomWait(minTimeWait, TimeUnit.MILLISECONDS, maxTimeWait, TimeUnit.MILLISECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(stopAfterAttempt))
                .build();
    }

    public static void execute() {

    }

}
