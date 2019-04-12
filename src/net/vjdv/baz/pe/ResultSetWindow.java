package net.vjdv.baz.pe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.vjdv.baz.pe.Result.ResultPage;

/**
 *
 * @author B187926
 */
public class ResultSetWindow {

    private final Scene scene;
    private final TableView<ObservableList<Object>> tabla;
    private int rowcount;

    public ResultSetWindow(ResultPage resultset) {
        AnchorPane root = new AnchorPane();
        root.setPrefSize(600, 400);
        tabla = new TableView<>();
        tabla.getSelectionModel().setCellSelectionEnabled(true);
        tabla.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // Menú
        ContextMenu menu = new ContextMenu();
        MenuItem mitem1 = new MenuItem("Copiar");
        MenuItem mitem2 = new MenuItem("Copiar con encabezados");
        MenuItem mitem5 = new MenuItem("para INSERT");
        MenuItem mitem6 = new MenuItem("para UPDATE");
        MenuItem mitem8 = new MenuItem("para WHERE IN");
        MenuItem mitem4 = new MenuItem("Expandir");
        MenuItem mitem12 = new MenuItem("Ver en Notepad++");
        MenuItem mitem3 = new MenuItem("Guardar como .csv");
        MenuItem mitem7 = new MenuItem("Guardar como .txt");
        Menu mitem9 = new Menu("Copiar como sql...");
        MenuItem mitem10 = new MenuItem("Ir a columna");
        MenuItem mitem11 = new MenuItem("Etiquetar ventana");
        mitem9.getItems().addAll(mitem5, mitem6, mitem8);
        menu.getItems().addAll(mitem1, mitem2, mitem9, new SeparatorMenuItem(), mitem4, mitem12, new SeparatorMenuItem(), mitem11, mitem10, new SeparatorMenuItem(), mitem3, mitem7);
        tabla.setContextMenu(menu);
        // Acciones menú
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
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                    "Archivo de valores separados por comas (*.csv)", "*.csv");
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(extFilter);
            File file = chooser.showSaveDialog(null);
            if (file != null) {
                try (PrintWriter out = new PrintWriter(file)) {
                    out.print(resultset.toString());
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
                scene2.getStylesheets().add(getClass().getResource("xml-highlight.css").toExternalForm());
                Stage stage = new Stage();
                stage.setTitle(tabla.getColumns().get(col).getText());
                stage.setScene(scene2);
                stage.show();
            }
        });
        // Copiar como INSERT SQL
        mitem5.setOnAction(actionEvent -> {
            String tablename = prompt("Nombre de tabla:", "@TABLE");
            if (tablename == null) {
                return;
            }
            StringBuilder insertString = new StringBuilder();
            StringBuilder clipboardString = new StringBuilder();
            ObservableList<TablePosition> positionList = tabla.getSelectionModel().getSelectedCells();
            int prevRow = -1;
            insertString.append("INSERT INTO ").append(tablename).append("(");
            for (TablePosition position : positionList) {
                int row = position.getRow();
                int col = position.getColumn();
                if (prevRow == row) {
                    insertString.append(",");
                } else if (prevRow != -1) {
                    break;
                }
                insertString.append(tabla.getColumns().get(col).getText());
                prevRow = row;
            }
            insertString.append(") VALUES\n(");
            clipboardString.append(insertString);
            prevRow = -1;
            for (TablePosition position : positionList) {
                int row = position.getRow();
                int col = position.getColumn();
                Object value = (Object) tabla.getColumns().get(col).getCellData(row);
                if (value instanceof String) {
                    value = ((String) value).replaceAll("'", "''");
                    value = "'" + value + "'";
                } else if (value instanceof Boolean) {
                    value = (Boolean) value ? "1" : "0";
                }
                if (prevRow == row) {
                    clipboardString.append(",");
                } else if (prevRow != -1) {
                    if (prevRow != 0 && prevRow % 500 == 0) {
                        clipboardString.append(");\n").append(insertString);
                    } else {
                        clipboardString.append("),\n(");
                    }
                }
                clipboardString.append(value);
                prevRow = row;
            }
            clipboardString.append(");");
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(clipboardString.toString());
            Clipboard.getSystemClipboard().setContent(clipboardContent);
        });
        // Copiar como UPDATE SQL
        mitem6.setOnAction(actionEvent -> {
            String tablename = prompt("Nombre de tabla:", "@TABLE");
            if (tablename == null) {
                return;
            }
            StringBuilder clipboardString = new StringBuilder();
            ObservableList<TablePosition> positionList = tabla.getSelectionModel().getSelectedCells();
            int prevRow = -1;
            for (TablePosition position : positionList) {
                if (prevRow == -1) {
                    clipboardString.append("UPDATE ").append(tablename).append(" SET\n");
                }
                int row = position.getRow();
                int col = position.getColumn();
                if (prevRow == row) {
                    clipboardString.append(",\n");
                } else if (prevRow != -1) {
                    clipboardString.append("\nWHERE 1=0\n\n");
                    break;
                }
                Object value = tabla.getColumns().get(col).getCellData(row);
                if (value instanceof String) {
                    value = ((String) value).replaceAll("'", "''");
                    value = "'" + value + "'";
                } else if (value instanceof Boolean) {
                    value = (Boolean) value ? "1" : "0";
                }
                clipboardString.append(tabla.getColumns().get(col).getText()).append("=");
                clipboardString.append(value);
                prevRow = row;
            }
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(clipboardString.toString());
            Clipboard.getSystemClipboard().setContent(clipboardContent);
        });
        // Copiar WHERE IN
        mitem8.setOnAction(actionEvent -> {
            Set<String> values = new HashSet<>();
            StringBuilder clipboardString = new StringBuilder("WHERE ");
            ObservableList<TablePosition> positionList = tabla.getSelectionModel().getSelectedCells();
            int prevCol = positionList.get(0).getColumn();
            clipboardString.append(tabla.getColumns().get(prevCol).getText()).append(" IN(");
            for (TablePosition position : positionList) {
                int row = position.getRow();
                int col = position.getColumn();
                if (prevCol != col) {
                    continue;
                }
                Object value = tabla.getColumns().get(col).getCellData(row);
                if (value == null) {
                    continue;
                }
                if (value instanceof String) {
                    value = ((String) value).replaceAll("'", "'");
                    value = "'" + value + "'";
                }
                values.add(value.toString());
            }
            clipboardString.append(String.join(",", values.toArray(new String[1]))).append(")");
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(clipboardString.toString());
            Clipboard.getSystemClipboard().setContent(clipboardContent);
        });
        // Guardar como .txt
        mitem7.setOnAction(actionEvent -> {
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivo de texto (*.txt)",
                    "*.txt");
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
        // Buscar columna
        mitem10.setOnAction(actionEvent -> {
            TextInputDialog filteringDialog = new TextInputDialog();
            filteringDialog.setHeaderText(null);
            filteringDialog.setTitle("Nombre de columna:");
            filteringDialog.setContentText(null);
            filteringDialog.initModality(Modality.NONE);
            filteringDialog.initStyle(StageStyle.UTILITY);
            Stage stage = (Stage) filteringDialog.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            filteringDialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
                String newValue2 = Normalizer.normalize(newValue, Normalizer.Form.NFD)
                        .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "").toLowerCase();
                for (int i = 0; i < resultset.columns.length; i++) {
                    if (resultset.columns[i].toLowerCase().contains(newValue2)) {
                        tabla.scrollToColumnIndex(i);
                        break;
                    }
                }
            });
            filteringDialog.show();
        });
        // Ver en notepad
        mitem12.setOnAction(actionEvent -> {
            ObservableList<TablePosition> positionList = tabla.getSelectionModel().getSelectedCells();
            positionList.stream().map((position) -> {
                int row = position.getRow();
                int col = position.getColumn();
                String str = (String) tabla.getColumns().get(col).getCellData(row);
                return str;
            }).forEachOrdered((str) -> {
                String ext = str.startsWith("<") ? ".xml" : ".txt";
                try {
                    File tmp = File.createTempFile("sitcbpe", ext);
                    tmp.deleteOnExit();
                    try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tmp), StandardCharsets.UTF_8)) {
                        writer.write(str);
                    }
                    String[] args = {"nppp", tmp.getAbsolutePath()};
                    new ProcessBuilder(args).start();
                } catch (IOException ex) {
                    Alert alertDialog = new Alert(Alert.AlertType.INFORMATION);
                    alertDialog.setContentText(ex.getMessage());
                    alertDialog.setHeaderText(ex.getClass().getName());
                    alertDialog.setTitle("Error");
                    alertDialog.show();
                }
            });

        });
        // Shortcuts
        mitem1.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));
        mitem4.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN));
        mitem5.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN));
        mitem6.setAccelerator(new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN));
        mitem7.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        mitem8.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));
        mitem10.setAccelerator(new KeyCodeCombination(KeyCode.G, KeyCombination.CONTROL_DOWN));
        // Interpretado de resultado
        for (int i = 0; i < resultset.columns.length; i++) {
            final int ifinal = i;
            TableColumn<ObservableList<Object>, Object> column = new TableColumn<>(resultset.columns[i]);
            column.setCellFactory(col -> {
                return new TableCell<ObservableList<Object>, Object>() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null && !empty) {
                            setText("null");
                            setStyle("-fx-font-style: italic;");
                        } else if (!empty) {
                            setText(item.toString());
                        }
                    }
                };
            });
            column.setCellValueFactory(param -> {
                Object value = param.getValue().get(ifinal);
                if (value != null && value instanceof Double) {
                    value = BigDecimal.valueOf((Double) value);
                }
                return new ReadOnlyObjectWrapper<>(value);
            });
            column.setMaxWidth(500);
            tabla.getColumns().add(column);
        }
        for (Object[] row : resultset.rows) {
            rowcount++;
            List<Object> items = new ArrayList<>();
            items.addAll(Arrays.asList(row));
            tabla.getItems().add(FXCollections.observableArrayList(items));
        }
        AnchorPane.setTopAnchor(tabla, 0d);
        AnchorPane.setBottomAnchor(tabla, 0d);
        AnchorPane.setLeftAnchor(tabla, 0d);
        AnchorPane.setRightAnchor(tabla, 0d);
        root.getChildren().add(tabla);
        scene = new Scene(root);
        // Etiquetar ventana
        mitem11.setOnAction(actionEvent -> {
            TextInputDialog input = new TextInputDialog();
            input.setHeaderText(null);
            input.setTitle("Nombre de ventana:");
            input.setContentText(null);
            input.initModality(Modality.NONE);
            input.initStyle(StageStyle.UTILITY);
            Stage stage = (Stage) input.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            Optional<String> title = input.showAndWait();
            if (title.isPresent()) {
                ((Stage) scene.getWindow()).setTitle(title.get() + " (" + resultset.rows.size() + " registros)");
            }
        });
    }

    public Scene getScene() {
        return scene;
    }

    public int getRowCount() {
        return rowcount;
    }

    private String prompt(String message, String defvalue) {
        TextInputDialog filteringDialog = new TextInputDialog(defvalue);
        filteringDialog.setHeaderText(null);
        filteringDialog.setTitle(message);
        filteringDialog.setContentText(null);
        Optional<String> opt = filteringDialog.showAndWait();
        if (opt.isPresent()) {
            return opt.get();
        } else {
            return null;
        }
    }

}
