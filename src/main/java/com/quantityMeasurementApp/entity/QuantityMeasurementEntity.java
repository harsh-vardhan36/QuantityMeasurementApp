package com.quantityMeasurementApp.entity;

import java.io.Serializable;

public class QuantityMeasurementEntity implements Serializable {

    private Object operand1;
    private Object operand2;
    private String operation;
    private Object result;
    private boolean error;

    public QuantityMeasurementEntity(Object op1, Object op2, String operation, Object result) {
        this.operand1 = op1;
        this.operand2 = op2;
        this.operation = operation;
        this.result = result;
        this.error = false;
    }

    public QuantityMeasurementEntity(String errorMessage) {
        this.result = errorMessage;
        this.error = true;
    }

    public boolean hasError() {
        return error;
    }

}