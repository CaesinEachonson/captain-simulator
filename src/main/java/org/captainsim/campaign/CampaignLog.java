package org.captainsim.campaign;

import java.util.ArrayList;
import java.util.List;

public class CampaignLog {

    private final List<LogEntry> entries;

    public CampaignLog() {
        this.entries = new ArrayList<>();
    }

    public void addEntry(int round, String narrative, String summary) {
        entries.add(new LogEntry(round, narrative, summary));
    }

    public void addEntry(int round, String narrative) {
        entries.add(new LogEntry(round, narrative, ""));
    }

    public List<LogEntry> getEntries() {
        return entries;
    }

    public record LogEntry(int round, String narrative, String summary) {
        public String formatted() {
            if (summary.isEmpty()) {
                return String.format("Week %d — %s", round, narrative);
            }
            return String.format("Week %d — %s\n  → %s", round, narrative, summary);
        }
    }
}
