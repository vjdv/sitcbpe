package net.vjdv.baz.pe;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

/**
 *
 * @author B187926
 */
public class AutocompletionTextField extends TextField {

    public static int CONTAINS = 0;
    public static int STARTS = 1;
    public static int ENDS = 2;
    public static int STARTS_ENDS = 3;
    private final SortedSet<String> entries;
    private ContextMenu entriesMenu;
    private int maxEntries = 10;
    private int searchMethod = CONTAINS;
    private boolean caseSensitive = false;

    public AutocompletionTextField() {
        super();
        this.entries = new TreeSet<>();
        this.entriesMenu = new ContextMenu();
        //Agrega sugerencias según el texto
        textProperty().addListener(new Listener());
        //Oculta el menú en focusIn o focusOut
        focusedProperty().addListener((observable, oldValue, newValue) -> {
            entriesMenu.hide();
        });
    }

    private List<String> buscarCoincidencias(String userInput) {
        List<String> coincidencias = new ArrayList<>();
        if (userInput.trim().isEmpty() || entries.isEmpty()) {
            return coincidencias;
        }
        if (!caseSensitive) {
            userInput = userInput.toUpperCase();
        }
        for (String entry : entries) {
            String tmp = entry;
            if (!caseSensitive) {
                tmp = entry.toUpperCase();
            }
            if (searchMethod == CONTAINS && tmp.contains(userInput)) {
                coincidencias.add(entry);
            } else if (searchMethod == STARTS && tmp.startsWith(userInput)) {
                coincidencias.add(entry);
            } else if (searchMethod == ENDS && tmp.endsWith(userInput)) {
                coincidencias.add(entry);
            } else if (searchMethod == STARTS_ENDS && (tmp.startsWith(userInput) || tmp.endsWith(userInput))) {
                coincidencias.add(entry);
            }
            if (coincidencias.size() >= maxEntries) {
                break;
            }
        }
        return coincidencias;
    }

    public void agregarSugerencia(String sugerencia) {
        entries.add(sugerencia);
    }

    public void setMaxEntries(int maxentries) {
        maxEntries = maxentries;
    }

    public void setSearchMethod(int method) {
        searchMethod = method;
    }

    public void setCaseSensitive(boolean flag) {
        caseSensitive = flag;
    }

    class Listener implements ChangeListener<String> {

        @Override
        public void changed(ObservableValue observable, String oldValue, String newValue) {
            List<String> coincidencias = buscarCoincidencias(newValue);
            if (coincidencias.isEmpty()) {
                entriesMenu.hide();
            } else {
                entriesMenu.getItems().clear();
                for (String coincidencia : coincidencias) {
                    MenuItem item = new MenuItem(coincidencia);
                    entriesMenu.getItems().add(item);
                    item.setOnAction(event->{
                        setText(coincidencia);
                        positionCaret(coincidencia.length());
                        entriesMenu.hide();
                    });
                }
                if (!entriesMenu.isShowing()) {
                    entriesMenu.show(AutocompletionTextField.this, Side.BOTTOM, 0, 0);
                }
            }
        }

    }

}
