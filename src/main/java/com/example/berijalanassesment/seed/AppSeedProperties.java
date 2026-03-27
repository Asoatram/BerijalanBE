package com.example.berijalanassesment.seed;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.seed")
public class AppSeedProperties {

    private boolean enabled = false;
    private boolean wipe = true;
    private int bulkVisitors = 200;
    private int bulkOpenAlerts = 30;
    private int bulkReports = 14;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isWipe() {
        return wipe;
    }

    public void setWipe(boolean wipe) {
        this.wipe = wipe;
    }

    public int getBulkVisitors() {
        return bulkVisitors;
    }

    public void setBulkVisitors(int bulkVisitors) {
        this.bulkVisitors = bulkVisitors;
    }

    public int getBulkOpenAlerts() {
        return bulkOpenAlerts;
    }

    public void setBulkOpenAlerts(int bulkOpenAlerts) {
        this.bulkOpenAlerts = bulkOpenAlerts;
    }

    public int getBulkReports() {
        return bulkReports;
    }

    public void setBulkReports(int bulkReports) {
        this.bulkReports = bulkReports;
    }
}
