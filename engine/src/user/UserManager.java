package user;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


    public class UserManager {
        private final Set<String> users;

        public UserManager() {
            users = new HashSet<>();
        }

        public synchronized void addUser(String username) {
            users.add(username);
        }


        public synchronized void removeUser(String username) {
            users.remove(username);
        }

        // Returns an unmodifiable view of the users set.
        // This method is synchronized to ensure thread-safe access.
        public synchronized Set<String> getUsers() {
            return Collections.unmodifiableSet(users);
        }

        public boolean isUserExists(String username) {
            for (String user : users) {
                if (user.equalsIgnoreCase(username)) {
                    return true;
                }
            }
            return false;
        }
    }