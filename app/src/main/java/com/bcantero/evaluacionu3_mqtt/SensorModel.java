package com.bcantero.evaluacionu3_mqtt;

public class SensorModel {

    private String id_Sensor;
    private String sensorName;
    private String typeSensor;
    private String valueSensor;
    private String locationSensor;
    private String dateSensor;
    private String observationSensor;

    public SensorModel() {
    }

    public SensorModel(String id_Sensor, String sensorName, String typeSensor, String valueSensor, String locationSensor, String dateSensor, String observationSensor) {
        this.id_Sensor = id_Sensor;
        this.sensorName = sensorName;
        this.typeSensor = typeSensor;
        this.valueSensor = valueSensor;
        this.locationSensor = locationSensor;
        this.dateSensor = dateSensor;
        this.observationSensor = observationSensor;
    }

    public String getId_Sensor() {
        return id_Sensor;
    }

    public void setId_Sensor(String id_Sensor) {
        this.id_Sensor = id_Sensor;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public String getTypeSensor() {
        return typeSensor;
    }

    public void setTypeSensor(String typeSensor) {
        this.typeSensor = typeSensor;
    }

    public String getValueSensor() {
        return valueSensor;
    }

    public void setValueSensor(String valueSensor) {
        this.valueSensor = valueSensor;
    }

    public String getLocationSensor() {
        return locationSensor;
    }

    public void setLocationSensor(String locationSensor) {
        this.locationSensor = locationSensor;
    }

    public String getDateSensor() {
        return dateSensor;
    }

    public void setDateSensor(String dateSensor) {
        this.dateSensor = dateSensor;
    }

    public String getObservationSensor() {
        return observationSensor;
    }

    public void setObservationSensor(String observationSensor) {
        this.observationSensor = observationSensor;
    }
}
