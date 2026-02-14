package marketplace.entities;

/**
 * Rental status enumeration matching database enum
 */
public enum RentalStatus {
    EN_ATTENTE("en_attente"),
    CONFIRMEE("confirmee"),
    EN_COURS("en_cours"),
    TERMINEE("terminee"),
    ANNULEE("annulee");

    private final String value;

    RentalStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RentalStatus fromString(String value) {
        for (RentalStatus status : RentalStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown rental status: " + value);
    }
}
