package io.iconator.rates;

import io.iconator.rates.service.OutlierCalculation;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class OutlierCalculationTest {

    private final static Logger LOG = LoggerFactory.getLogger(OutlierCalculationTest.class);

    @Test
    public void test1() {

        double[] data = new double[]{500, 501, 498, 500, 500, 510};

        OutlierCalculation oc = new OutlierCalculation(data, 1.0, 1.0);

        LOG.info("LowerBound Threshold: " + oc.getLowerBoundThreshold());
        LOG.info("UpperBound Threshold: " + oc.getUpperBoundThreshold());
        LOG.info("LowerBound Outliers: " + Arrays.toString(oc.findLowerBoundOutlierValues()));
        LOG.info("UpperBound Outliers: " + Arrays.toString(oc.findUpperBoundOutlierValues()));

        assertTrue(oc.findUpperBoundOutlierValues().length == 1);
        assertTrue(oc.findLowerBoundOutlierValues().length == 0);
        assertTrue(Arrays.stream(oc.findUpperBoundOutlierValues()).allMatch((value) -> value == 510.0));
    }

    @Test
    public void test2() {

        double[] data = new double[]{500, 501, 498, 500, 500, 500, 500, 500, 500, 501, 501, 498, 510};

        OutlierCalculation oc = new OutlierCalculation(data, 1.0, 1.0);

        LOG.info("LowerBound Threshold: " + oc.getLowerBoundThreshold());
        LOG.info("UpperBound Threshold: " + oc.getUpperBoundThreshold());
        LOG.info("LowerBound Outliers: " + Arrays.toString(oc.findLowerBoundOutlierValues()));
        LOG.info("UpperBound Outliers: " + Arrays.toString(oc.findUpperBoundOutlierValues()));

        assertTrue(oc.findUpperBoundOutlierValues().length == 1);
        assertTrue(oc.findLowerBoundOutlierValues().length == 0);
        assertTrue(Arrays.stream(oc.findUpperBoundOutlierValues()).allMatch((value) -> value == 510.0));
    }

    @Test
    public void test3() {

        double[] data = new double[]{500, 510, 505};

        OutlierCalculation oc = new OutlierCalculation(data, 2.0, 2.0);

        LOG.info("LowerBound Threshold: " + oc.getLowerBoundThreshold());
        LOG.info("UpperBound Threshold: " + oc.getUpperBoundThreshold());
        LOG.info("LowerBound Outliers: " + Arrays.toString(oc.findLowerBoundOutlierValues()));
        LOG.info("UpperBound Outliers: " + Arrays.toString(oc.findUpperBoundOutlierValues()));

        assertTrue(oc.findUpperBoundOutlierValues().length == 0);
        assertTrue(oc.findLowerBoundOutlierValues().length == 0);
    }

    @Test
    public void test4() {

        double[] data = new double[]{};

        OutlierCalculation oc = new OutlierCalculation(data, 2.0, 2.0);

        LOG.info("LowerBound Threshold: " + oc.getLowerBoundThreshold());
        LOG.info("UpperBound Threshold: " + oc.getUpperBoundThreshold());
        LOG.info("LowerBound Outliers: " + Arrays.toString(oc.findLowerBoundOutlierValues()));
        LOG.info("UpperBound Outliers: " + Arrays.toString(oc.findUpperBoundOutlierValues()));

        assertTrue(oc.findUpperBoundOutlierValues().length == 0);
        assertTrue(oc.findLowerBoundOutlierValues().length == 0);
    }

}
