package net.vjdv.baz.pe;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author B187926
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setClassLoader(getClass().getClassLoader());
        Parent root = loader.load(getClass().getResource("Inicio.fxml").openStream());
        InicioController controller = loader.<InicioController>getController();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("sql-highlight.css").toExternalForm());
        stage.setTitle("Procesos Especiales SITCB");
        stage.getIcons().add(new Image("/net/vjdv/baz/pe/logoSIT.png"));
        stage.setScene(scene);
        stage.setOnShown(windosevent -> {
            controller.onVisible();
        });
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
