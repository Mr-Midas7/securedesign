import java.util.*;

class Role {
    public static final String STUDENT = "STUDENT";
    public static final String TEACHER = "TEACHER";
    public static final String ADMIN   = "ADMIN";
}

class Permission {
    public static final String VIEW   = "VIEW";
    public static final String EDIT   = "EDIT";
    public static final String DELETE = "DELETE";   // Full access for admin
}

// Simple user info
class User {
    private String username, role;

    public User(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public String getUsername() { return username; }
    public String getRole()     { return role; }
}

// Manages what each role can do
class PermissionManager {
    private static final Map<String, String[]> perms = new HashMap<>();

    static {
        perms.put(Role.STUDENT, new String[]{ Permission.VIEW });
        perms.put(Role.TEACHER, new String[]{ Permission.VIEW, Permission.EDIT });
        perms.put(Role.ADMIN,   new String[]{ Permission.VIEW, Permission.EDIT, Permission.DELETE });
    }

    // Check if user has permission
    public boolean hasPermission(User user, String p) {
        for (String allowed : perms.get(user.getRole())) {
            if (allowed.equals(p)) return true;
        }
        return false;
    }

    // Show what user can do
    public void show(User user) {
        System.out.println("\nAllowed actions for " + user.getUsername() + " (" + user.getRole() + "):");
        for (String p : perms.get(user.getRole())) {
            System.out.println("- " + p);
        }
    }
}

// Login security + account blocking
class SecurityLayer {
    private Map<String, String> passwords = new HashMap<>();
    private Map<String, Integer> fails = new HashMap<>();
    private static final int LIMIT = 3;

    public SecurityLayer(Map<String, User> users) {
        for (String u : users.keySet()) {
            if (u.equals("admin1")) passwords.put(u, "adminpass");
            else if (u.equals("teacher1")) passwords.put(u, "teachpass");
            else passwords.put(u, "pass123");  // default student pass
            fails.put(u, 0);
        }
    }

    public void addUser(String u, String p) {
        passwords.put(u, p);
        fails.put(u, 0);
    }

    public boolean login(String u, String p) {
        if (fails.get(u) >= LIMIT) {
            System.out.println("Account blocked. Ask admin.");
            return false;
        }

        if (!passwords.containsKey(u) || !passwords.get(u).equals(p)) {
            fails.put(u, fails.get(u) + 1);
            System.out.println("Wrong password.");

            if (fails.get(u) >= LIMIT) System.out.println("Too many tries. Blocked.");

            return false;
        }

        fails.put(u, 0);
        System.out.println("Login successful!");
        return true;
    }
}

public class Activity {
    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);

        // Initial users
        Map<String, User> users = new HashMap<>();
        users.put("admin1",   new User("admin1", Role.ADMIN));
        users.put("teacher1", new User("teacher1", Role.TEACHER));

        SecurityLayer security = new SecurityLayer(users);
        PermissionManager perms = new PermissionManager();

        System.out.println("SYSTEM ONLINE!");
        

        while (true) {
            // Pick role first
            System.out.println("\nChoose your role:");
            System.out.println("1. Student");
            System.out.println("2. Teacher");
            System.out.println("3. Admin");
            System.out.println("4. Exit");
            System.out.print("choice: ");
            String r = in.nextLine();

            String role;
            switch (r) {
                case "1":
                    role = Role.STUDENT;
                    break;
                case "2":
                    role = Role.TEACHER;
                    break;
                case "3":
                    role = Role.ADMIN;
                    break;
                case "4":
                    return;
                default:
                    System.out.println("Invalid.\n");
                    continue;
            }

            while (true) {
                System.out.println("\n1. Login");
                System.out.println("2. Sign up (students only)");
                System.out.println("3. Back");
                System.out.print("Choice: ");
                String c = in.nextLine();

                if (c.equals("1")) {
                    System.out.print("Username: ");
                    String u = in.nextLine();

                    if (!users.containsKey(u)) {
                        System.out.println("User not found.");
                        continue;
                    }

                    System.out.print("Password: ");
                    String p = in.nextLine();

                    if (security.login(u, p)) {
                        User user = users.get(u);

                        if (!user.getRole().equals(role)) {
                            System.out.println("Wrong role. No access.");
                            continue;
                        }

                        perms.show(user);

                        System.out.print("\nTry action (VIEW/EDIT/DELETE): ");
                        String act = in.nextLine().toUpperCase();

                        if (perms.hasPermission(user, act))
                            System.out.println("Action allowed.");
                        else
                            System.out.println("Not allowed.");

                        break;
                    }
                }

                else if (c.equals("2")) {
                    if (!role.equals(Role.STUDENT)) {
                        System.out.println("Only students can sign up.");
                        continue;
                    }

                    System.out.print("New username: ");
                    String u = in.nextLine();

                    if (users.containsKey(u)) {
                        System.out.println("Name taken.");
                        continue;
                    }

                    System.out.print("Password: ");
                    String p = in.nextLine();

                    users.put(u, new User(u, Role.STUDENT));
                    security.addUser(u, p);

                    System.out.println("Sign up complete! Login now.");
                }

                else if (c.equals("3")) break;
                else System.out.println("Invalid.");
            }
        }
    }
}
