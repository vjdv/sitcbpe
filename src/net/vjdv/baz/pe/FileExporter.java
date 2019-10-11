package net.vjdv.baz.pe;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
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
            Logger.getLogger("FileExporter").log(Level.WARNING, null, ex);
        }
    }

}
