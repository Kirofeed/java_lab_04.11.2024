// AppointmentServer.java
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class AppointmentServer {
    private static final int PORT = 12345;
    private Set<LocalDateTime> bookedAppointments = ConcurrentHashMap.newKeySet();
    private List<Appointment> appointments = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        new AppointmentServer().startServer();
    }

    public void startServer() {
        ExecutorService pool = Executors.newFixedThreadPool(10);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер запущен на порту " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Новое подключение от " + clientSocket.getInetAddress());
                pool.execute(new ClientHandler(clientSocket, this));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }
    }

    // Синхронизированный метод для бронирования
    public synchronized boolean bookAppointment(Appointment appointment) {
        if (bookedAppointments.contains(appointment.getDateTime())) {
            return false;
        }
        bookedAppointments.add(appointment.getDateTime());
        appointments.add(appointment);
        // Здесь можно добавить сохранение в файл или базу данных
        System.out.println("Запись добавлена: " + appointment.getFullName() + " на " + appointment.getDateTime());
        return true;
    }

    // Получение доступных слотов на неделю вперед
    public synchronized List<LocalDateTime> getAvailableSlots() {
        List<LocalDateTime> available = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int day = 0; day < 7; day++) {
            LocalDateTime date = now.plusDays(day).withHour(9).withMinute(0).withSecond(0).withNano(0);
            for (int hour = 9; hour < 17; hour++) { // Рабочие часы 9:00-17:00
                LocalDateTime slot = date.withHour(hour);
                if (!bookedAppointments.contains(slot)) {
                    available.add(slot);
                }
            }
        }
        return available;
    }
}
