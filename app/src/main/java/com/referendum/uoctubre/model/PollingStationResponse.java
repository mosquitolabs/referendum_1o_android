package com.referendum.uoctubre.model;

public class PollingStationResponse {
    private String status;
    private ColegiElectoral pollingStation;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ColegiElectoral getPollingStation() {
        return pollingStation;
    }

    public void setPollingStation(ColegiElectoral pollingStation) {
        this.pollingStation = pollingStation;
    }
}
