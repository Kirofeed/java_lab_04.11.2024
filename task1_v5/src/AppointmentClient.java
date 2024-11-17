// AppointmentClient.java
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class AppointmentClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Scanner scanner;

    public static void main(String[] args) {
        new AppointmentClient().startClient();
    }

    public void startClient() {
        scanner = new Scanner(System.in, "UTF-8");
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            System.out.println("Подключено к серверу.");

            boolean running = true;
            while (running) {
                System.out.println("\nВыберите действие:");
                System.out.println("1. Просмотреть доступные слоты");
                System.out.println("2. Записаться на приём");
                System.out.println("3. Выйти");
                System.out.print("Ваш выбор: ");
                String choice = scanner.nextLine();
                switch (choice) {
                    case "1":
                        getAvailableSlots();
                        break;
                    case "2":
                        bookAppointment();
                        break;
                    case "3":
                        out.println("EXIT");
                        running = false;
                        break;
                    default:
                        System.out.println("Некорректный выбор. Попробуйте снова.");
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка подключения к серверу: " + e.getMessage());
        } finally {
            try {
                if (socket != null) socket.close();
                System.out.println("Соединение закрыто.");
            } catch (IOException e) {
                // Игнорируем
            }
        }
    }

    private void getAvailableSlots() throws IOException {
        out.println("GET_AVAILABLE_SLOTS");
        String response = in.readLine();
        if (response.equals("AVAILABLE_SLOTS")) {
            System.out.println("\nДоступные слоты:");
            while (!(response = in.readLine()).equals("END_SLOTS")) {
                System.out.println(response);
            }
        } else {
            System.out.println("Ошибка при получении доступных слотов.");
        }
    }

    private void bookAppointment() throws IOException {
        System.out.print("Введите ФИО: ");
        String fullName = scanner.nextLine();
        System.out.print("Введите телефон: ");
        String phone = scanner.nextLine();
        System.out.print("Введите жалобы: ");
        String complaints = scanner.nextLine();
        System.out.print("Введите дату (yyyy-MM-dd): ");
        String date = scanner.nextLine();
        System.out.print("Введите время (HH:mm): ");
        String time = scanner.nextLine();

        // Отправка данных на сервер
        out.println("BOOK_APPOINTMENT");
        out.println("ФИО:" + fullName);
        out.println("Телефон:" + phone);
        out.println("Жалобы:" + complaints);
        out.println("Дата:" + date);
        out.println("Время:" + time);
        out.println(); // Пустая строка для завершения сообщения

        // Чтение ответа
        String response = in.readLine();
        if (response.equals("BOOKING_SUCCESS")) {
            System.out.println("Запись успешно оформлена.");
        } else if (response.startsWith("BOOKING_FAILED")) {
            System.out.println("Не удалось оформить запись: " + response.substring("BOOKING_FAILED".length()).trim());
        } else {
            System.out.println("Неизвестный ответ от сервера.");
        }
    }
}
