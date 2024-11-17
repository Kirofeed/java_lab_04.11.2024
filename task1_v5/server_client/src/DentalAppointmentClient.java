import java.io.*;
import java.net.*;
import java.util.*;

public class DentalAppointmentClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in)
        ) {
            // Получаем список доступных слотов от сервера
            List<String> availableSlots = new ArrayList<>();
            String line = in.readLine();
            if ("AVAILABLE_SLOTS".equals(line)) {
                while (!(line = in.readLine()).equals("END")) {
                    availableSlots.add(line);
                }
            }

            System.out.println("Доступные слоты для записи:");
            for (int i = 0; i < availableSlots.size(); i++) {
                System.out.println((i + 1) + ". " + availableSlots.get(i));
            }

            // Выбор слота
            System.out.print("Введите номер желаемого времени: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            String selectedSlot = availableSlots.get(choice - 1);

            // Ввод данных клиента
            System.out.print("Введите ваше имя: ");
            String name = scanner.nextLine();
            System.out.print("Введите ваш телефон: ");
            String phone = scanner.nextLine();
            System.out.print("Введите ваши жалобы: ");
            String complaints = scanner.nextLine();

            // Отправляем запрос на бронирование
            out.println("BOOK");
            out.println(selectedSlot);
            out.println("NAME:" + name);
            out.println("PHONE:" + phone);
            out.println("COMPLAINTS:" + complaints);

            // Получаем ответ от сервера
            line = in.readLine();
            if ("CONFIRMED".equals(line)) {
                String confirmedSlot = in.readLine();
                System.out.println("Запись подтверждена на " + confirmedSlot);
            } else if ("ERROR".equals(line)) {
                String errorMessage = in.readLine();
                System.out.println("Ошибка: " + errorMessage);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
