package net.vjdv.baz.pe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

/**
 *
 * @author B187926
 */
public class ResultSetWindow {

    private final Scene scene;
    private final TableView<ObservableList<String>> tabla;

    public ResultSetWindow(String resultset) {
        AnchorPane root = new AnchorPane();
        root.setPrefSize(600, 400);
        tabla = new TableView<>();
        StringTokenizer lineas = new StringTokenizer(resultset, "\r\n");
        String l1 = lineas.nextToken();
        String cols[] = l1.split("\t");
        for (int i = 0; i < cols.length; i++) {
            final int ifinal = i;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(cols[ifinal]);
            column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(ifinal)));
            column.setMaxWidth(500);
            column.setEditable(true);
            tabla.getColumns().add(column);
        }
        while (lineas.hasMoreTokens()) {
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

}
