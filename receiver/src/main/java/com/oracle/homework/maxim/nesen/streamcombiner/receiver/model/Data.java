package com.oracle.homework.maxim.nesen.streamcombiner.receiver.model;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * main data structure for holding two values from parsed XML streams
 */
@XmlRootElement
public class Data {
    @XmlElement
    private String timestamp;
    @XmlElement
    private BigDecimal amount;

    public Data() {   //required by JAXB
    }

    public Data(String timestamp, BigDecimal amount) {
        this.timestamp = timestamp;
        this.amount = amount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Data copy() {
        return new Data(timestamp, amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Data data = (Data) o;

        if (timestamp != null ? !timestamp.equals(data.timestamp) : data.timestamp != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return timestamp != null ? timestamp.hashCode() : 0;
    }
}
