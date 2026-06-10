package seasonal.enums;

public enum PhenologyStage {
    BEFORE_START("관측 전", "#9CA3AF"),
    STARTED("시작", "#60A5FA"),
    IN_PROGRESS("진행 중", "#34D399"),
    FULL_BLOOM("만발", "#F472B6"),
    PEAK("절정", "#F59E0B"),
    ENDED("종료", "#6B7280");

    private final String displayName;
    private final String color;

    PhenologyStage(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }
}
