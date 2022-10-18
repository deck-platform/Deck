package edge.droid.server.data;

public enum AuthorityResult {

    SUCCESS(0, "SUCCESS"),
    IMPORT_ILLEGAL_USE(1,"IMPORT_ILLEGAL_USE"),
    REFLECT(2,"REFLECT"),
    UNKNOWN_ERROR(100, "UNKNOWN_ERROR")
    ;

    private int code;
    private String description;

    AuthorityResult(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

}
