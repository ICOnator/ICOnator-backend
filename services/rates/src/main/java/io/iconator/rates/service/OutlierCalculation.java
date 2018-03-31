package io.iconator.rates.service;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.math.BigDecimal;
import java.util.Arrays;

public class OutlierCalculation {

    private double[] data;
    private double outlierStdDevThresholdLowerBound;
    private double outlierStdDevThresholdUpperBound;
    private double mean;
    private double standardDeviation;

    public OutlierCalculation(BigDecimal[] data,
                              Double outlierStdDevThresholdLowerBound,
                              Double outlierStdDevThresholdUpperBound) {
        this(
                Arrays.stream(data).mapToDouble((bigDecimal) -> bigDecimal.doubleValue()).toArray(),
                outlierStdDevThresholdLowerBound,
                outlierStdDevThresholdUpperBound
        );
    }

    public OutlierCalculation(double[] data,
                              Double outlierStdDevThresholdLowerBound,
                              Double outlierStdDevThresholdUpperBound) {
        this.data = data;
        this.outlierStdDevThresholdLowerBound = outlierStdDevThresholdLowerBound;
        this.outlierStdDevThresholdUpperBound = outlierStdDevThresholdUpperBound;
        this.mean = getMean();
        this.standardDeviation = getStdDev();
    }

    public double[] findUpperBoundOutlierValues() {
        double upperBoundThreshold = getUpperBoundThreshold();
        return Arrays.stream(this.data).filter((value) -> value > upperBoundThreshold).toArray();
    }

    public double[] findLowerBoundOutlierValues() {
        double lowerBoundThreshold = getLowerBoundThreshold();
        return Arrays.stream(this.data).filter((value) -> value < lowerBoundThreshold).toArray();
    }

    public double getLowerBoundThreshold() {
        return this.mean - (this.outlierStdDevThresholdLowerBound * this.standardDeviation);
    }

    public double getUpperBoundThreshold() {
        return this.mean + (this.outlierStdDevThresholdUpperBound * this.standardDeviation);
    }

    public double getStdDev() {
        // population standard deviation is applied, and not a sample standard deviation
        StandardDeviation stdDev = new StandardDeviation(false);
        return stdDev.evaluate(this.data);
    }

    public double getMean() {
        Mean mean = new Mean();
        return mean.evaluate(this.data, 0, this.data.length);
    }

}
