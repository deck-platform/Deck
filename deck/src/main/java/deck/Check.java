package deck;

public class Check {

    public static void codeCheck(String className) {
        if (className.contains("android")) {
            throw new RuntimeException("android not allow");
        }
    }
}
