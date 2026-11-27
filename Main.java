import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ParkingModel model = new ParkingModel();
            ParkingView view = new ParkingView();
            ParkingController controller = new ParkingController(model, view);
            controller.startApp();
        });
    }
}
