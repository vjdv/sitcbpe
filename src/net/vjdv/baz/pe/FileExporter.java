package net.vjdv.baz.pe;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.scene.control.Alert;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.*;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileExporter {

    private static ObjectMapper mapper = new ObjectMapper();

    public static void json(Result.ResultPage resultset) {
        ArrayNode s = mapper.createArrayNode();
        for (int i = 0; i < resultset.rows.size(); i++) {
            Object[] objs = resultset.rows.get(i);
            ObjectNode o = s.addObject();
            for (int j = 0; j < resultset.columns.length; j++) {
                if (objs[j] instanceof String) o.put(resultset.columns[j], (String) objs[j]);
                if (objs[j] instanceof Integer) o.put(resultset.columns[j], (Integer) objs[j]);
                if (objs[j] instanceof BigDecimal) o.put(resultset.columns[j], (BigDecimal) objs[j]);
                if (objs[j] instanceof Boolean) o.put(resultset.columns[j], (Boolean) objs[j]);
            }
        }
        try {
            Path tmp = Files.createTempFile("salida", ".json");
            Files.copy(new ByteArrayInputStream(mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(s)), tmp, StandardCopyOption.REPLACE_EXISTING);
            Desktop.getDesktop().open(tmp.toFile());
        } catch (IOException ex) {
            Alert alertDialog = new Alert(Alert.AlertType.ERROR);
            alertDialog.setContentText("Error al exportar: " + ex.getMessage());
            alertDialog.setTitle("Error");
            alertDialog.show();
            Logger.getLogger("FileExporter").log(Level.WARNING, null, ex);
        }
    }

    public static void txt(Result.ResultPage resultset) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < resultset.columns.length; i++) {
            sb.append(resultset.columns[i]);
            if (i != (resultset.columns.length - 1)) sb.append("\t");
        }
        sb.append("\r\n");
        for (Object[] datos : resultset.rows) {
            for (int i = 0; i < datos.length; i++) {
                sb.append(datos[i]);
                if (i != (datos.length - 1)) sb.append("\t");
            }
            sb.append("\r\n");
        }
        try {
            InputStream is = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
            Path tmp = Files.createTempFile("salida", ".txt");
            Files.copy(is, tmp, StandardCopyOption.REPLACE_EXISTING);
            Desktop.getDesktop().open(tmp.toFile());
        } catch (IOException ex) {
            Alert alertDialog = new Alert(Alert.AlertType.ERROR);
            alertDialog.setContentText("Error al exportar: " + ex.getMessage());
            alertDialog.setTitle("Error");
            alertDialog.show();
            Logger.getLogger("FileExporter").log(Level.WARNING, null, ex);
        }
    }

    public static void csv(Result.ResultPage resultset) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < resultset.columns.length; i++) {
            sb.append(resultset.columns[i]);
            if (i != (resultset.columns.length - 1)) sb.append(",");
        }
        sb.append("\r\n");
        for (Object[] datos : resultset.rows) {
            for (int i = 0; i < datos.length; i++) {
                if (datos[i] instanceof String) {
                    String tmp = (String) datos[i];
                    if (tmp.contains("\"") || tmp.contains(",")) {
                        tmp = tmp.contains("\"") ? tmp.replaceAll("\"", "\"\"") : tmp;
                        sb.append("\"");
                        sb.append(tmp);
                        sb.append("\"");
                    } else {
                        sb.append(datos[i]);
                    }
                } else {
                    sb.append(datos[i]);
                }
                if (i != (datos.length - 1)) sb.append(",");
            }
            sb.append("\r\n");
        }
        try {
            InputStream is = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
            Path tmp = Files.createTempFile("salida", ".csv");
            Files.copy(is, tmp, StandardCopyOption.REPLACE_EXISTING);
            Desktop.getDesktop().open(tmp.toFile());
        } catch (IOException ex) {
            Alert alertDialog = new Alert(Alert.AlertType.ERROR);
            alertDialog.setContentText("Error al exportar: " + ex.getMessage());
            alertDialog.setTitle("Error");
            alertDialog.show();
            Logger.getLogger("FileExporter").log(Level.WARNING, null, ex);
        }
    }

    public static void excel(Result result) {
        //Creando libro
        HSSFWorkbook wb = new HSSFWorkbook();
        //Estilos encabezado
        HSSFPalette palette = wb.getCustomPalette();
        palette.setColorAtIndex(IndexedColors.DARK_BLUE.index, (byte) 28, (byte) 43, (byte) 54);
        Font headerFont = wb.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        CellStyle headerStyle = wb.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.index);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //Estilos cuerpo
        Font bodyFont = wb.createFont();
        bodyFont.setBold(false);
        CellStyle bodyStyle = wb.createCellStyle();
        bodyStyle.setFont(bodyFont);
        Font kFont = wb.createFont();
        kFont.setBold(false);
        kFont.setItalic(true);
        CellStyle kStyle = wb.createCellStyle();
        kStyle.setFont(kFont);
        //Agregando hojas
        for (int x = 0; x < result.pages.size(); x++) {
            Result.ResultPage resultset = result.pages.get(x);
            Sheet s = wb.createSheet("Salida" + (x + 1));
            //Encabezado
            Row headerRow = s.createRow(0);
            for (int i = 0; i < resultset.columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellStyle(headerStyle);
                cell.setCellValue(resultset.columns[i]);
            }
            //Cuerpo
            int count = 1;
            for (Object[] datos : resultset.rows) {
                Row row = s.createRow(count);
                for (int i = 0; i < datos.length; i++) {
                    Cell cell = row.createCell(i);
                    if (datos[i] == null) {
                        cell.setCellValue("null");
                        cell.setCellStyle(kStyle);
                    } else if (datos[i] instanceof String) {
                        cell.setCellValue((String) datos[i]);
                    } else if (datos[i] instanceof BigDecimal) {
                        cell.setCellValue(((BigDecimal) datos[i]).doubleValue());
                    } else if (datos[i] instanceof Boolean) {
                        cell.setCellValue(datos[i].toString());
                        cell.setCellStyle(kStyle);
                    } else if (datos[i] instanceof Integer) {
                        cell.setCellValue((Integer) datos[i]);
                    } else if (datos[i] instanceof Double) {
                        cell.setCellValue((Double) datos[i]);
                    } else {
                        cell.setCellValue(datos[i].toString());
                    }
                }
                count++;
            }
            //Autosize
            for (int i = 0; i < s.getRow(0).getPhysicalNumberOfCells(); i++) {
                s.autoSizeColumn(i);
            }
        }
        try {
            File tmp = File.createTempFile("salida", ".xls");
            wb.write(tmp);
            Desktop.getDesktop().open(tmp);
        } catch (IOException ex) {
            Alert alertDialog = new Alert(Alert.AlertType.ERROR);
            alertDialog.setContentText("Error al exportar: " + ex.getMessage());
            alertDialog.setTitle("Error");
            alertDialog.show();
            Logger.getLogger("FileExporter").log(Level.WARNING, null, ex);
        }
    }

}
