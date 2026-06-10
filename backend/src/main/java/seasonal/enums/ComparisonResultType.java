package seasonal.enums;

public enum ComparisonResultType {
    EARLY("빠름"),
    NORMAL("보통"),
    LATE("늦음"),
    UNKNOWN("비교 불가");

    private final String displayName;

    ComparisonResultType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
