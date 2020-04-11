package com.shenxy13.project;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import java.util.Random;
final public class RuntimeDatastore {
    final public static long PERMISSION_USER = 0, PERMISSION_ADMIN = 1, PERMISSION_OWNER = 2, KEY_LENGTH=26, TYPE_NULL = 0, TYPE_POLL = 1, TYPE_FEEDBACK = 2, TYPE_SUGGESTION = 3;
    final private static Random rng = new Random();
    final public static FirebaseAuth auth = FirebaseAuth.getInstance();
    final public static FirebaseDatabase database = FirebaseDatabase.getInstance();
    final public static FirebaseStorage store = FirebaseStorage.getInstance();
    final private static char[] characters = new char[62];
    public static double dpRatio;
    public static long currentPermissions, currentPostType, myCurrentScore, displayedUserPermissions;
    public static String currentOrganisation, displayedUser, currentPost;
    public static String generateKey() {
        for (int i = 0; i < 10; ++i) characters[i] = (char) ('0' + i);
        for (int i = 0; i < 26; ++i) characters[i + 10] = (char) ('A' + i);
        for (int i = 0; i < 26; ++i) characters[i + 36] = (char) ('a' + i);
        String ans = "";
        for (int i = 0; i < KEY_LENGTH; ++i) ans += characters[rng.nextInt(62)];
        return ans;
    }
    public static long max(long a, long b) {
        if (a < b) return b;
        return a;
    }
    public static long min(long a, long b) {
        if (a < b) return a;
        return b;
    }
    private RuntimeDatastore() {

    }
}