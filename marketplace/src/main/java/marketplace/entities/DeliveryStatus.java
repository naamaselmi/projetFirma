package marketplace.entities;

/**
 * Delivery status enumeration matching database enum
 */
public enum DeliveryStatus {
    EN_ATTENTE("en_attente"),
    EN_PREPARATION("en_preparation"),
    EXPEDIE("expedie"),
    LIVRE("livre"),
    ANNULE("annule");

    private final String value;

    DeliveryStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DeliveryStatus fromString(String value) {
        for (DeliveryStatus status : DeliveryStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown delivery status: " + value);
    }
}
