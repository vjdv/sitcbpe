package net.vjdv.baz.pe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import javafx.concurrent.Task;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author B187926
 */
public class PeticionHTTP extends Task<Resultado> {

    private Resultado resultado = new Resultado();
    private final String query;
    private final URL url;
    private String response;

    public PeticionHTTP(URL url, String consulta) {
        query = consulta;
        this.url = url;
    }

    public void enviarConsulta() {
        try {
            //Creando parámetros a enviar
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String token = Util.getToken(timestamp.getTime());
            Map<String, Object> params = new HashMap<>();
            params.put("tran", query);
            params.put("timestamp", timestamp.getTime());
            params.put("token", token);
            byte[] postDataBytes = parsearPost(params);
            //Conectando con el servidor
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", Integer.toString(postDataBytes.length));
            conn.setUseCaches(false);
            //Enviando parámetros
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);
            //Leyendo respuesta
            String encoding = conn.getContentEncoding();
            if (encoding == null) {
                encoding = "ISO-8859-1";
            }
            int contentLength = conn.getContentLength();
            int contentReaded = 0;
            if (contentLength == -1) {
                updateMessage("Leyendo respuesta");
            }
            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), encoding));
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0;) {
                contentReaded++;
                sb.append((char) c);
                if (contentLength != -1) {
                    updateProgress(contentReaded, contentLength);
                    updateMessage("Leyendo respuesta " + Math.round(contentReaded * 100 / contentLength) + "%");
                }
            }
            response = sb.toString();
            //Convirtiendo respuesta en Resultado
            JAXBContext jc = JAXBContext.newInstance(Resultado.class);
            Unmarshaller m = jc.createUnmarshaller();
            resultado = (Resultado) m.unmarshal(new StringReader(response));
        } catch (ConnectException ex) {
            resultado.error = "No se pudo establecer una conexión con el servidor";
        } catch (JAXBException ex) {
            resultado.error = "No es posible interpretar la respuesta del servidor: " + response;
        } catch (IOException ex) {
            resultado.error = ex.toString();
        }
    }

    private byte[] parsearPost(Map<String, Object> params) throws UnsupportedEncodingException {
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (postData.length() != 0) {
                postData.append('&');
            }
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }
        return postData.toString().getBytes("UTF-8");
    }

    @Override
    protected Resultado call() throws Exception {
        updateMessage("Enviando consulta");
        enviarConsulta();
        return resultado;
    }

}
