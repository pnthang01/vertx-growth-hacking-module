package vn.panota.growth.enums;

public enum GrowthHackingType {
    NEWS_LETTER("NEWS_LETTER");

    private final String value;

    /**
     * @param value
     */
    GrowthHackingType(final String value) {
        this.value = value;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return value;
    }
}
