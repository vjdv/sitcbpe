package net.vjdv.baz.pe;

import static net.vjdv.baz.pe.Util.getCurrentMachineIP;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.concurrent.Task;

/**
 *
 * @author B187926
 */
public class PeticionHTTP extends Task<Result> {

	private final String query;
	private final URL url;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public PeticionHTTP(URL url, String consulta) {
		query = consulta;
		this.url = url;
	}

	public Result enviarConsulta() {
		Result result = new Result();
		try (CloseableHttpClient httpclient = HttpClients.custom().build()) {
			// Creando par√°metros a enviar
			Token token = new Token("SITCB2" + getCurrentMachineIP());
			HttpPost httppost = new HttpPost(url.toString());
			// Enviando par·metros
			List<NameValuePair> params = new ArrayList<>(3);
			params.add(new BasicNameValuePair("tran", query));
			params.add(new BasicNameValuePair("timestamp", "" + token.getTimestamp()));
			params.add(new BasicNameValuePair("token", token.get()));
			httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			// Leyendo respuesta
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			updateMessage("Leyendo respuesta");
			String json = EntityUtils.toString(entity);
			updateMessage("Parseando respuesta");
			result = objectMapper.readValue(json, Result.class);
		} catch (ConnectException ex) {
			result.error = "No se pudo establecer una conexi\u00f3n con el servidor";
		} catch (IOException ex) {
			result.error = ex.getMessage();
		}
		return result;
	}

	@Override
	protected Result call() throws Exception {
		updateMessage("Enviando consulta");
		return enviarConsulta();
	}

}
