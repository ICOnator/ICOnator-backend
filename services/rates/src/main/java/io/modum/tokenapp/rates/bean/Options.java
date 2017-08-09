package io.modum.tokenapp.rates.bean;

import org.kohsuke.args4j.Option;

public class Options {
    @Option(name="-r",usage="rate to query APIs")
    private int rate;

    @Option(name="-u",usage="rate to query APIs")
    private String userAgent = "a -- user -- agent -- beep";

    @Option(name="-i",usage="csv to import to the database")
    private String importFile;

    @Option(name="-e",usage="csv to export to the database")
    private String exportFile;

    public int getRate() {
        return rate;
    }

    public Options setRate(int rate) {
        this.rate = rate;
        return this;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public Options setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public String getImportFile() {
        return importFile;
    }

    public Options setImportFile(String importFile) {
        this.importFile = importFile;
        return this;
    }

    public String getExportFile() {
        return exportFile;
    }

    public Options setExportFile(String exportFile) {
        this.exportFile = exportFile;
        return this;
    }
}