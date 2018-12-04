package net.vjdv.baz.pe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

/**
 *
 * @author B187926
 */
public class Pestania extends Tab {

	private static int tabCount = 0;
	private File file;
	private CodeArea text;

	public Pestania() {
		tabCount++;
		text = new CodeArea();
		text.setStyle("-fx-font-size: 13px;");
		text.setParagraphGraphicFactory(LineNumberFactory.get(text));
		text.textProperty().addListener((obs, oldText, newText) -> {
			text.setStyleSpans(0, SqlPatterns.computeHighlight(newText));
		});
		setContent(new VirtualizedScrollPane<>(text));
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
			// StandardCharsets.UTF_8
			setText(file.getName());
			byte[] encoded = Files.readAllBytes(file.toPath());
			text.replaceText(new String(encoded, Charset.defaultCharset()));
		} catch (IOException ex) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setContentText("Error al abrir el archivo: " + ex.toString());
			alert.showAndWait();
		}
	}

	public CodeArea getEditor() {
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
