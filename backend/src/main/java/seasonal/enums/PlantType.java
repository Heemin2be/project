package seasonal.enums;

public enum PlantType {
    MAEHWA("매화"),
    CHERRY_BLOSSOM("벚나무"),
    COSMOS("코스모스"),
    MAPLE("단풍나무");

    private final String displayName;

    PlantType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
