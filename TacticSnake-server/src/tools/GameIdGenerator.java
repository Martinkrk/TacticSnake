package tools;

public class GameIdGenerator {
    private static int currentId = 1;
    private static final int MAX_ID = 10000;

    public static int getNextId() {
        int id = currentId;
        currentId++;
        if (currentId > MAX_ID) {
            currentId = 1;
        }
        return id;
    }
}

