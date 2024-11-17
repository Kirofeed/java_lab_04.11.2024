import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class DentalAppointmentServer {
    private static final int PORT = 12345;
    private static final List<String> availableSlots = Collections.synchronizedList(new ArrayList<>());
    private static final Map<String, ClientInfo> appointments = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) {
        // Инициализируем доступные слоты на неделю вперед
        initializeAvailableSlots();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер запущен и ожидает подключений...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket, availableSlots, appointments).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initializeAvailableSlots() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (int i = 0; i < 7; i++) { // На 7 дней вперед
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            for (int hour = 9; hour <= 17; hour++) { // Рабочие часы с 9 до 17
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, 0);
                String slot = sdf.format(calendar.getTime());
                availableSlots.add(slot);
            }
        }
    }
}

class ClientHandler extends Thread {
    private Socket socket;
    private List<String> availableSlots;
    private Map<String, ClientInfo> appointments;

    public ClientHandler(Socket socket, List<String> availableSlots, Map<String, ClientInfo> appointments) {
        this.socket = socket;
        this.availableSlots = availableSlots;
        this.appointments = appointments;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // Отправляем список доступных слотов
            out.println("AVAILABLE_SLOTS");
            synchronized (availableSlots) {
                for (String slot : availableSlots) {
                    out.println(slot);
                }
            }
            out.println("END");

            // Читаем запрос клиента
            String line = in.readLine();
            if ("BOOK".equals(line)) {
                String selectedSlot = in.readLine();
                String nameLine = in.readLine();
                String phoneLine = in.readLine();
                String complaintsLine = in.readLine();

                String name = nameLine.split(":", 2)[1];
                String phone = phoneLine.split(":", 2)[1];
                String complaints = complaintsLine.split(":", 2)[1];

                ClientInfo clientInfo = new ClientInfo(name, phone, complaints);

                // Пытаемся забронировать слот
                boolean success = false;
                synchronized (availableSlots) {
                    if (availableSlots.contains(selectedSlot)) {
                        availableSlots.remove(selectedSlot);
                        appointments.put(selectedSlot, clientInfo);
                        success = true;
                    }
                }

                if (success) {
                    out.println("CONFIRMED");
                    out.println(selectedSlot);
                } else {
                    out.println("ERROR");
                    out.println("Выбранное время уже занято. Пожалуйста, выберите другое время.");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {}
        }
    }
}

class ClientInfo {
    private String name;
    private String phone;
    private String complaints;

    public ClientInfo(String name, String phone, String complaints) {
        this.name = name;
        this.phone = phone;
        this.complaints = complaints;
    }

    // Геттеры и сеттеры можно добавить при необходимости
}
