// Appointment.java
import java.io.Serializable;
import java.time.LocalDateTime;

public class Appointment implements Serializable {
    private String fullName;
    private String phone;
    private String complaints;
    private LocalDateTime dateTime;

    public Appointment(String fullName, String phone, String complaints, LocalDateTime dateTime) {
        this.fullName = fullName;
        this.phone = phone;
        this.complaints = complaints;
        this.dateTime = dateTime;
    }

    // Геттеры
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getComplaints() { return complaints; }
    public LocalDateTime getDateTime() { return dateTime; }
}
