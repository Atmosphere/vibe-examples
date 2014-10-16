package simple;

import org.atmosphere.vibe.server.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Clock {
    @Autowired
    private Server server;

    @Scheduled(fixedRate = 5000)
    public void tick() {
        server.all().send("chat", "tick: " + System.currentTimeMillis());
    }
}