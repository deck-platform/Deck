package edge.droid.server.data;

public enum Source {

    SOURCE("SOURCE", "deck.stereotype.Source"),
    DB("DB", "deck.stereotype.DB"),
    FILE("FILE", "deck.stereotype.File")
    ;

    private String type;
    private String description;

    Source(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public static Source getByDescription(String description) {
        for (Source source : Source.values()) {
            if (description.equals(source.getDescription())) {
                return source;
            }
        }
        return null;
    }

    public String getDescription() {
        return this.description;
    }

}