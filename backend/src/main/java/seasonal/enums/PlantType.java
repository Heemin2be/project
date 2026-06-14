package seasonal.enums;

public enum PlantType {
    MAEHWA("매화"),
    FORSYTHIA("개나리"),
    AZALEA("진달래"),
    CHERRY_BLOSSOM("벚나무"),
    MAPLE("단풍나무");

    private final String displayName;

    PlantType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
