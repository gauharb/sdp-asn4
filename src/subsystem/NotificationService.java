package subsystem;

import java.util.ArrayList;
import java.util.List;

public class NotificationService {
    private final List<String> outbox = new ArrayList<>();

    public void send(String email, String subject, String body) {
        outbox.add(email + " | " + subject);
        System.out.printf("  [email -> %s] %s: %s%n", email, subject, body);
    }

    public List<String> outbox() {
        return List.copyOf(outbox);
    }
}
