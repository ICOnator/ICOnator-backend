package io.iconator.rates.task;

import io.iconator.rates.config.RatesAppConfigHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Determines the next execution time for fetching rates.
 * The interval is configurable in the the application properties.
 */
@Component
public class FetchCurrentRatesTrigger implements Trigger {

    @Autowired
    private RatesAppConfigHolder ratesAppConfig;

    @Override
    public Date nextExecutionTime(TriggerContext triggerContext) {
        Calendar nextExecutionTime = new GregorianCalendar();
        Date lastActualExecutionTime = triggerContext.lastActualExecutionTime();
        nextExecutionTime.setTime(lastActualExecutionTime != null ? lastActualExecutionTime : new Date());
        nextExecutionTime.add(Calendar.MILLISECOND, ratesAppConfig.getCurrentPeriodicInterval());
        return nextExecutionTime.getTime();
    }

}
