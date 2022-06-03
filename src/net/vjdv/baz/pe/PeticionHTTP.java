package net.vjdv.baz.pe;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javafx.concurrent.Task;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import org.apache.http.entity.StringEntity;

/**
 *
 * @author B187926
 */
public class PeticionHTTP extends Task<Result> {

  private static String myip = null;
  private final String query;
  private final URL url;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public PeticionHTTP(URL url, String consulta) {
    query = consulta;
    this.url = url;
  }

  public Result enviarConsulta() {
    Result result = new Result();
    RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10000).build();
    try (CloseableHttpClient httpclient = HttpClients.custom()
            .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
            .build()) {
      //Obtiene ip
      if (myip == null) {
        myip = getIps();
      }
      // Creando token
      Algorithm algorithm = Algorithm.HMAC256("3.ProEsp1415926");
      String token = JWT.create()
              .withIssuer("auth0")
              .withClaim("ip", myip)
              .withClaim("query", query)
              .sign(algorithm);
      HttpPost httppost = new HttpPost(url.toString());
      httppost.setConfig(requestConfig);
      // Enviando par�metros
      httppost.setEntity(new StringEntity(token, "UTF-8"));
      // Leyendo respuesta
      HttpResponse response = httpclient.execute(httppost);
      HttpEntity entity = response.getEntity();
      updateMessage("Leyendo respuesta");
      String json = EntityUtils.toString(entity, StandardCharsets.UTF_8);
      updateMessage("Parseando respuesta");
      result = objectMapper.readValue(json, Result.class);
    } catch (ConnectException ex) {
      result.error = "No se pudo establecer una conexi\u00f3n con el servidor";
    } catch (IOException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException ex) {
      result.error = ex.getMessage();
    }
    return result;
  }

  private String getIps() {
    String ips = "";
    try {
      Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
      while (e.hasMoreElements()) {
        NetworkInterface n = e.nextElement();
        Enumeration ee = n.getInetAddresses();
        while (ee.hasMoreElements()) {
          InetAddress i = (InetAddress) ee.nextElement();
          String ip = i.getHostAddress();
          if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            continue;
          }
          if (!ips.isEmpty()) {
            ips += ",";
          }
          ips += ip;
        }
      }
    } catch (SocketException ex) {
      ips = "none";
    }
    return ips;
  }

  @Override
  protected Result call() throws Exception {
    updateMessage("Enviando consulta");
    return enviarConsulta();
  }

}
