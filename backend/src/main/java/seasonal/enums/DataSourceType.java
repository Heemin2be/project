package seasonal.enums;

public enum DataSourceType {
    CSV("CSV"),
    API("기상청 API"),
    MANUAL("수동 입력");

    private final String displayName;

    DataSourceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
