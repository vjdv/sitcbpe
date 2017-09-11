package net.vjdv.baz.pe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.richtext.demo.JavaKeywordsAsync;

/**
 *
 * @author B187926
 */
public class ResultSetWindow {

    private final Scene scene;
    private final TableView<ObservableList<String>> tabla;
    private int rowcount;

    public ResultSetWindow(String resultset) {
        AnchorPane root = new AnchorPane();
        root.setPrefSize(600, 400);
        tabla = new TableView<>();
        tabla.getSelectionModel().setCellSelectionEnabled(true);
        tabla.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        //Menú
        ContextMenu menu = new ContextMenu();
        MenuItem mitem1 = new MenuItem("Copiar");
        MenuItem mitem2 = new MenuItem("Copiar con encabezados");
        MenuItem mitem5 = new MenuItem("Copiar como INSERT SQL");
        MenuItem mitem6 = new MenuItem("Copiar como UPDATE SQL");
        MenuItem mitem4 = new MenuItem("Expandir");
        MenuItem mitem3 = new MenuItem("Guardar como .csv");
        MenuItem mitem7 = new MenuItem("Guardar como .txt");
        menu.getItems().addAll(mitem1, mitem2, mitem5, mitem6, mitem4, new SeparatorMenuItem(), mitem3, mitem7);
        tabla.setContextMenu(menu);
        //Acciones menú
        EventHandler<ActionEvent> listener = (ActionEvent event) -> {
            boolean ponercabeceras = event.getSource().equals(mitem2);
            String cabeceras = "";
            StringBuilder clipboardString = new StringBuilder();
            ObservableList<TablePosition> positionList = tabla.getSelectionModel().getSelectedCells();
            int prevRow = -1;
            for (TablePosition position : positionList) {
                if (!ponercabeceras) {
                    break;
                }
                int row = position.getRow();
                int col = position.getColumn();
                if (prevRow == row) {
                    clipboardString.append("\t");
                } else if (prevRow != -1) {
                    clipboardString.append("\n");
                    break;
                }
                clipboardString.append(tabla.getColumns().get(col).getText());
                prevRow = row;
            }
            prevRow = -1;
            for (TablePosition position : positionList) {
                int row = position.getRow();
                int col = position.getColumn();
                Object cell = (Object) tabla.getColumns().get(col).getCellData(row);
                if (cell == null) {
                    cell = "";
                }
                if (prevRow == row) {
                    clipboardString.append("\t");
                } else if (prevRow != -1) {
                    clipboardString.append("\n");
                }
                String text = cell.toString();
                clipboardString.append(text);
                prevRow = row;
            }
            if (!cabeceras.isEmpty()) {
                cabeceras += "\n";
            }
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(cabeceras + clipboardString.toString());
            Clipboard.getSystemClipboard().setContent(clipboardContent);
        };
        mitem1.setOnAction(listener);
        mitem2.setOnAction(listener);
        mitem3.setOnAction(actionEvent -> {
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivo de valores separados por comas (*.csv)", "*.csv");
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(extFilter);
            File file = chooser.showSaveDialog(null);
            if (file != null) {
                try (PrintWriter out = new PrintWriter(file)) {
                    out.print(resultset.replaceAll("\t", ","));
                } catch (FileNotFoundException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Error al guardar el CSV: " + ex.toString());
                    alert.showAndWait();
                }
            }
        });
        mitem4.setOnAction(actionEvent -> {
            ObservableList<TablePosition> positionList = tabla.getSelectionModel().getSelectedCells();
            for (TablePosition position : positionList) {
                int row = position.getRow();
                int col = position.getColumn();
                String data = (String) tabla.getColumns().get(col).getCellData(row);
                ExpandedView view = new ExpandedView();
                view.setText(data);
                Scene scene2 = new Scene(view);
                scene2.getStylesheets().add(JavaKeywordsAsync.class.getResource("xml-highlighting.css").toExternalForm());
                Stage stage = new Stage();
                stage.setTitle(tabla.getColumns().get(col).getText());
                stage.setScene(scene2);
                stage.show();
            }
        });
        //Copiar como INSERT SQL
        mitem5.setOnAction(actionEvent -> {
            StringBuilder clipboardString = new StringBuilder();
            ObservableList<TablePosition> positionList = tabla.getSelectionModel().getSelectedCells();
            int prevRow = -1;
            clipboardString.append("INSERT INTO @TABLE (");
            for (TablePosition position : positionList) {
                int row = position.getRow();
                int col = position.getColumn();
                if (prevRow == row) {
                    clipboardString.append(",");
                } else if (prevRow != -1) {
                    clipboardString.append(") VALUES\n('");
                    break;
                }
                clipboardString.append(tabla.getColumns().get(col).getText());
                prevRow = row;
            }
            prevRow = -1;
            for (TablePosition position : positionList) {
                int row = position.getRow();
                int col = position.getColumn();
                Object cell = (Object) tabla.getColumns().get(col).getCellData(row);
                if (cell == null) {
                    cell = "";
                }
                if (prevRow == row) {
                    clipboardString.append("','");
                } else if (prevRow != -1) {
                    clipboardString.append("'),\n('");
                }
                String text = cell.toString();
                clipboardString.append(text);
                prevRow = row;
            }
            clipboardString.append("');");
            String x = clipboardString.toString().replaceAll("'null'", "null");
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(x);
            Clipboard.getSystemClipboard().setContent(clipboardContent);
        });
        //Copiar como UPDATE SQL
        mitem6.setOnAction(actionEvent -> {
            StringBuilder clipboardString = new StringBuilder();
            ObservableList<TablePosition> positionList = tabla.getSelectionModel().getSelectedCells();
            int prevRow = -1;
            for (TablePosition position : positionList) {
                if (prevRow == -1) {
                    clipboardString.append("UPDATE @TABLE SET\n");
                }
                int row = position.getRow();
                int col = position.getColumn();
                if (prevRow == row) {
                    clipboardString.append(",\n");
                } else if (prevRow != -1) {
                    clipboardString.append("\nWHERE 1=0\n\n");
                    break;
                }
                clipboardString.append(tabla.getColumns().get(col).getText()).append("=");
                clipboardString.append("'").append(tabla.getColumns().get(col).getCellData(row)).append("'");
                prevRow = row;
            }
            String x = clipboardString.toString().replaceAll("'null'", "null");
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(x);
            Clipboard.getSystemClipboard().setContent(clipboardContent);
        });
        //Guardar como .txt
        mitem7.setOnAction(actionEvent -> {
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivo de texto (*.txt)", "*.txt");
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(extFilter);
            File file = chooser.showSaveDialog(null);
            if (file != null) {
                try (PrintWriter out = new PrintWriter(file)) {
                    out.print(resultset);
                } catch (FileNotFoundException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Error al guardar el CSV: " + ex.toString());
                    alert.showAndWait();
                }
            }
        });
        //Interpretado de resultado
        StringTokenizer lineas = new StringTokenizer(resultset, "\r\n");
        String l1 = lineas.nextToken();
        String cols[] = l1.split("\t");
        for (int i = 0; i < cols.length; i++) {
            final int ifinal = i;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(cols[ifinal]);
            column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(ifinal)));
            column.setMaxWidth(500);
            tabla.getColumns().add(column);
        }
        while (lineas.hasMoreTokens()) {
            rowcount++;
            String linea = lineas.nextToken();
            String values[] = linea.split("\t");
            List<String> items = new ArrayList<>();
            items.addAll(Arrays.asList(values));
            tabla.getItems().add(FXCollections.observableArrayList(items));
        }
        AnchorPane.setTopAnchor(tabla, 0d);
        AnchorPane.setBottomAnchor(tabla, 0d);
        AnchorPane.setLeftAnchor(tabla, 0d);
        AnchorPane.setRightAnchor(tabla, 0d);
        root.getChildren().add(tabla);
        scene = new Scene(root);
    }

    public Scene getScene() {
        return scene;
    }

    public int getRowCount() {
        return rowcount;
    }

}
