import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Task implements Serializable {
    private String title;
    private String description;
    private boolean done;
    private String priority; // Low, Medium, High
    private String due; // optional due date/time as string

    public Task(String title, String description, String priority, String due, boolean done) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.due = due;
        this.done = done;
    }

    public Task(String csvLine) {
        // csv format: title|description|priority|due|done
        String[] p = csvLine.split("\\|", -1);
        this.title = p.length>0?p[0]:"";
        this.description = p.length>1?p[1]:"";
        this.priority = p.length>2?p[2]:"Medium";
        this.due = p.length>3?p[3]:"";
        this.done = p.length>4?Boolean.parseBoolean(p[4]):false;
    }

    public String toCSV() {
        return escape(title) + "|" + escape(description) + "|" + priority + "|" + due + "|" + done;
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("|", "/PIPE/").replace("\n", "/NL/");
    }

    private String unescape(String s) {
        if (s == null) return "";
        return s.replace("/PIPE/", "|").replace("/NL/", "\n");
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isDone() { return done; }
    public String getPriority() { return priority; }
    public String getDue() { return due; }

    public void setTitle(String t) { title = t; }
    public void setDescription(String d) { description = d; }
    public void setDone(boolean d) { done = d; }
    public void setPriority(String p) { priority = p; }
    public void setDue(String due) { this.due = due; }

    @Override
    public String toString() {
        String d = (due != null && !due.isEmpty()) ? " (due " + due + ")" : "";
        return (done ? "[✓] " : "[ ] ") + title + d + " {" + priority + "}";
    }
}