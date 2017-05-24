package net.vjdv.baz.pe;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author B187926
 */
public class InicioController implements Initializable {

    @FXML
    private TabPane tabs;
    @FXML
    private TextField uriText;
    @FXML
    private Label info;
    @FXML
    private Button submit;
    //Variables
    private final FileChooser filechooser = new FileChooser();
    private Alert alert;
    private URL url;

    @FXML
    public void guardarPestania(ActionEvent event) {
        Pestania tab = (Pestania) tabs.getSelectionModel().getSelectedItem();
        if (tab.getFile() == null) {
            File f = filechooser.showSaveDialog(null);
            if (f != null) {
                tab.setFile(f);
            }
        }
        if (tab.getFile() != null) {
            tab.guardar();
        }
    }

    @FXML
    public void abrirPestania(ActionEvent event) {
        File f = filechooser.showOpenDialog(null);
        if (f != null) {
            Pestania tab = new Pestania(f);
            tabs.getTabs().add(tab);
            tabs.getSelectionModel().select(tab);
        }
    }

    @FXML
    public void nuevaPestania(ActionEvent event) {
        Pestania tab = new Pestania();
        tabs.getTabs().add(tab);
        tabs.getSelectionModel().select(tab);
        tab.getEditor().requestFocus();
    }

    @FXML
    public void enviarConsulta(ActionEvent event) {
        alert = new Alert(Alert.AlertType.ERROR);
        try {
            url = new URL(uriText.getText());
        } catch (MalformedURLException ex) {
            alert.setContentText("La URL del servidor es inválida");
            alert.showAndWait();
            return;
        }
        submit.setDisable(true);
        Pestania tab = (Pestania) tabs.getSelectionModel().getSelectedItem();
        String param = tab.getQuery();
        PeticionHTTP peticion = new PeticionHTTP(url, param);
        info.textProperty().bind(peticion.messageProperty());
        peticion.setOnSucceeded(workerStateEvent -> {
            submit.setDisable(false);
            info.textProperty().unbind();
            info.setText("");
            Resultado r = peticion.getValue();
            if (r == null) {
                alert.setContentText("Error inesperado al enviar petición");
                alert.showAndWait();
            } else if (r.error != null) {
                alert.setContentText(r.error);
                alert.showAndWait();
            } else {
                info.setText("Mostrando consultas");
                for (String rs : r.resultsets) {
                    ResultSetWindow rsw = new ResultSetWindow(rs);
                    Stage stage = new Stage();
                    stage.setScene(rsw.getScene());
                    stage.show();
                }
                info.setText("");
            }
        });
        new Thread(peticion).start();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        uriText.setText("http://10.51.42.9:8080/SITCB/ProcesosEspeciales2");
        info.setText("");
    }

    public void onVisible() {
        nuevaPestania(null);
    }

}
