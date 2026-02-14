package marketplace.entities;

/**
 * Payment status enumeration matching database enum
 */
public enum PaymentStatus {
    EN_ATTENTE("en_attente"),
    PAYE("paye"),
    ECHOUE("echoue"),
    PARTIEL("partiel");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PaymentStatus fromString(String value) {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown payment status: " + value);
    }
}
