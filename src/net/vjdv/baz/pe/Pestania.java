package net.vjdv.baz.pe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;

/**
 *
 * @author B187926
 */
public class Pestania extends Tab {

    private static int tabCount = 0;
    private File file;
    private TextArea text;

    public Pestania() {
        tabCount++;
        text = new TextArea();
        setContent(text);
        setText("Sin guardar " + tabCount);
    }

    public Pestania(File f) {
        this();
        file = f;
        abrir();
    }

    public final void guardar() {
        try (PrintWriter out = new PrintWriter(file)) {
            out.print(text.getText());
        } catch (FileNotFoundException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error al guardar el archivo: " + ex.toString());
            alert.showAndWait();
        }
    }

    public final void abrir() {
        try {
            //StandardCharsets.UTF_8
            setText(file.getName());
            byte[] encoded = Files.readAllBytes(file.toPath());
            text.setText(new String(encoded, Charset.defaultCharset()));
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error al abrir el archivo: " + ex.toString());
            alert.showAndWait();
        }
    }

    public TextArea getEditor() {
        return text;
    }

    public String getQuery() {
        return text.getText();
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

}
