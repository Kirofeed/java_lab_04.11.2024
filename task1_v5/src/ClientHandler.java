// ClientHandler.java
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private AppointmentServer server;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket, AppointmentServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            String request;
            while ((request = in.readLine()) != null) {
                if (request.equals("GET_AVAILABLE_SLOTS")) {
                    handleGetAvailableSlots();
                } else if (request.equals("BOOK_APPOINTMENT")) {
                    handleBookAppointment();
                } else if (request.equals("EXIT")) {
                    break;
                } else {
                    out.println("UNKNOWN_COMMAND");
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка соединения с клиентом: " + e.getMessage());
        } finally {
            try {
                socket.close();
                System.out.println("Соединение с клиентом закрыто.");
            } catch (IOException e) {
                // Игнорируем
            }
        }
    }

    private void handleGetAvailableSlots() {
        List<LocalDateTime> slots = server.getAvailableSlots();
        out.println("AVAILABLE_SLOTS");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (LocalDateTime slot : slots) {
            out.println(slot.format(formatter));
        }
        out.println("END_SLOTS");
    }

    private void handleBookAppointment() throws IOException {
        String fullName = null, phone = null, complaints = null;
        String dateStr = null, timeStr = null;
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            if (line.startsWith("ФИО:")) {
                fullName = line.substring(4).trim();
            } else if (line.startsWith("Телефон:")) {
                phone = line.substring(8).trim();
            } else if (line.startsWith("Жалобы:")) {
                complaints = line.substring(7).trim();
            } else if (line.startsWith("Дата:")) {
                dateStr = line.substring(5).trim();
            } else if (line.startsWith("Время:")) {
                timeStr = line.substring(6).trim();
            }
        }
        if (fullName == null || phone == null || dateStr == null || timeStr == null) {
            out.println("BOOKING_FAILED Некорректные данные");
            return;
        }
        LocalDateTime dateTime;
        try {
            dateTime = LocalDateTime.parse(dateStr + " " + timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (Exception e) {
            out.println("BOOKING_FAILED Некорректный формат даты или времени");
            return;
        }
        Appointment appointment = new Appointment(fullName, phone, complaints, dateTime);
        boolean success = server.bookAppointment(appointment);
        if (success) {
            out.println("BOOKING_SUCCESS");
        } else {
            out.println("BOOKING_FAILED Время уже занято");
        }
    }
}
