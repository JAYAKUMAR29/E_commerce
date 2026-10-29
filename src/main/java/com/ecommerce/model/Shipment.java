package com.ecommerce.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Shipment {

    private final String shipmentId;
    private final String orderId;
    private final String carrier;
    private final String trackingNumber;
    private final LocalDate estimatedDeliveryDate;

    private boolean delivered;
    private LocalDateTime deliveredDate;

    public Shipment(String shipmentId, String orderId, String carrier, String trackingNumber, int deliveryDaysEstimate) {
        if (shipmentId == null || shipmentId.isBlank()) {
            throw new IllegalArgumentException("Shipment ID cannot be empty");
        }
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID cannot be empty");
        }
        if (carrier == null || carrier.isBlank()) {
            throw new IllegalArgumentException("Carrier cannot be empty");
        }
        if (trackingNumber == null || trackingNumber.isBlank()) {
            throw new IllegalArgumentException("Tracking number cannot be empty");
        }
        if (deliveryDaysEstimate <= 0) {
            throw new IllegalArgumentException("Estimated delivery days must be greater than zero");
        }

        this.shipmentId = shipmentId.trim();
        this.orderId = orderId.trim();
        this.carrier = carrier.trim();
        this.trackingNumber = trackingNumber.trim();
        this.estimatedDeliveryDate = LocalDate.now().plusDays(deliveryDaysEstimate);
        this.delivered = false;
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCarrier() {
        return carrier;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public LocalDate getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public LocalDateTime getDeliveredDate() {
        return deliveredDate;
    }

    // Business action when the delivery partner marks package handed over
    public void markDelivered() {
        if (this.delivered) {
            throw new IllegalStateException("Shipment is already delivered.");
        }
        this.delivered = true;
        this.deliveredDate = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("Shipment[ID=%s, Order=%s, Carrier=%s, Tracking=%s, EstDelivery=%s, Delivered=%b]",
                shipmentId, orderId, carrier, trackingNumber, estimatedDeliveryDate, delivered);
    }
}
