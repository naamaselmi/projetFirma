package marketplace.entities;

/**
 * Product type enumeration matching database enum
 */
public enum ProductType {
    EQUIPEMENT("equipement"),
    VEHICULE("vehicule"),
    TERRAIN("terrain");

    private final String value;

    ProductType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ProductType fromString(String value) {
        for (ProductType type : ProductType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown product type: " + value);
    }
}
