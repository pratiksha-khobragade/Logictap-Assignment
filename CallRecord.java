public class CallRecord {

    private final String callId;
    private final String status;
    private final int durationSecs;

    public CallRecord(String callId, String status, int durationSecs) {
        this.callId = callId;
        this.status = status;
        this.durationSecs = durationSecs;
    }

    public String getCallId() {
        return callId;
    }

    public String getStatus() {
        return status;
    }

    public int getDurationSecs() {
        return durationSecs;
    }

    public String toJson() {
        return "{"
                + "\"call_id\":\"" + escape(callId) + "\","
                + "\"status\":\"" + escape(status) + "\","
                + "\"duration_secs\":" + durationSecs
                + "}";
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}