package de.interaapps.pastefy.analytics;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsResponse {
    public long totalVisits;
    public long uniqueVisitors;
    public long botVisits;
    public boolean botTrackingEnabled;
    public List<SeriesPoint> series = new ArrayList<>();
    public List<BreakdownPoint> breakdown = new ArrayList<>();

    public static class SeriesPoint {
        public String bucket;
        public long visits;
        public long uniqueVisitors;
    }

    public static class BreakdownPoint {
        public String value;
        public long visits;
        public long uniqueVisitors;
    }
}
