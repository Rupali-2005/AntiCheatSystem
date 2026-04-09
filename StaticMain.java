package anticheat;

import java.util.ArrayList;

public class StaticMain {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("===== ANTI-CHEAT ENGINE (STATIC MODE) =====");
        System.out.println();

        AntiCheatEngine engine = new AntiCheatEngine();
        ActionQueue queue = new ActionQueue();
        GameServer server = new GameServer(queue, engine);
        Thread serverThread = new Thread(server);
        serverThread.setDaemon(true);
        serverThread.start();

        long now = System.currentTimeMillis();

        ArrayList<Action> actions = new ArrayList<>();

        actions.add(new Action("alice", "MOVE", now++, 500.0));
        actions.add(new Action("alice", "MOVE", now++, 1500.0));
        actions.add(new Action("alice", "SCORE", now++, 100.0));
        actions.add(new Action("alice", "SCORE", now++, 600.0));
        actions.add(new Action("alice", "SCORE", now++, -10.0));

        actions.add(new Action("bob", "MOVE", now++, 200.0));
        actions.add(new Action("bob", "MOVE", now++, 1200.0));
        actions.add(new Action("bob", "SHOOT", now++, 0.5));
        actions.add(new Action("bob", "SHOOT", now++, 90.0));

        actions.add(new Action("charlie", "MOVE", now++, 100.0));
        actions.add(new Action("charlie", "MOVE", now++, 1000.0));

        actions.add(new Action("hacker", "MOVE", now++, 100.0));
        actions.add(new Action("hacker", "MOVE", now++, 1100.0));
        actions.add(new Action("hacker", "SCORE", now++, 700.0));
        actions.add(new Action("hacker", "SCORE", now++, -50.0));
        for (int i = 0; i < 20; i++) {
            actions.add(new Action("hacker", "SHOOT", now++, 1.0));
        }

        System.out.println("----- Direct Evaluation Results -----");
        for (Action action : actions) {
            RiskResult result = engine.evaluate(action);
            String verdict = result.lastRule.equals("CLEAN") ? "CLEAN  " : "FLAGGED";
            System.out.println(verdict + " | " + result);
        }

        System.out.println();
        System.out.println("----- Queued Action (via GameServer) -----");
        Action queuedAction = new Action("bob", "SHOOT", System.currentTimeMillis(), 95.0);
        queue.enqueue(queuedAction);
        Thread.sleep(200);

        System.out.println();
        System.out.println("----- Player Status Summary -----");
        String[] players = {"alice", "bob", "charlie", "hacker"};
        for (String playerId : players) {
            PlayerRecord record = engine.getPlayerRecord(playerId);
            if (record != null) {
                System.out.println("Player:     " + record.playerId);
                System.out.println("Status:     " + record.getStatus());
                System.out.println("Risk Score: " + record.getRiskScore());
                System.out.println("Violations: " + record.getViolationCount());
                ArrayList<RuleViolation> violations = record.getViolations();
                if (!violations.isEmpty()) {
                    System.out.println("Details:");
                    for (RuleViolation v : violations) {
                        System.out.println("  " + v);
                    }
                }
                System.out.println();
            }
        }

        System.out.println("----- Full Violation Log -----");
        ViolationLogger.displayFullLog();

        System.out.println("----- Config Values -----");
        System.out.println("SPEED_THRESHOLD:    " + CheatConfig.SPEED_THRESHOLD);
        System.out.println("SCORE_THRESHOLD:    " + CheatConfig.SCORE_THRESHOLD);
        System.out.println("TELEPORT_THRESHOLD: " + CheatConfig.TELEPORT_THRESHOLD);
        System.out.println("AIMBOT_WINDOW:      " + CheatConfig.AIMBOT_WINDOW);
        System.out.println("SUSPICIOUS_SCORE:   " + CheatConfig.SUSPICIOUS_SCORE);
        System.out.println("BLOCKED_SCORE:      " + CheatConfig.BLOCKED_SCORE);

        server.stop();
    }
}
