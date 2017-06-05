/*
 * Util.java
 *
 * Created on 17 de febrero de 2009, 04:22 PM
 */
package net.vjdv.baz.pe;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrador
 */
public class Util {

    /**
     * Devuelve la IP actual de la máquina
     *
     * @return IPv4
     * @autor VJDV
     */
    public static String getCurrentMachineIP() {
        try {
            //Obtenemos todas las IP disponibles en todas las interfaces
            List<String> ipDisponibles = new ArrayList<>();
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements()) {
                    InetAddress i = (InetAddress) ee.nextElement();
                    ipDisponibles.add(i.getHostAddress());
                }
            }
            //Regresamos la que más se adapta a nuestra arquitectura
            for (String ip : ipDisponibles) {
                if (ip.equals("127.0.0.1")) {
                    continue;
                }
                if (ip.startsWith("10.")) {
                    return ip;
                }
            }
            //Regresamos la primera IPv4 que encontremos
            for (String ip : ipDisponibles) {
                if (ip.equals("127.0.0.1")) {
                    continue;
                }
                if (ip.matches("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)")) {
                    return ip;
                }
            }
            //Quizá no hay ninguna disponible
            return "127.0.0.1";
        } catch (SocketException ex) {
            Logger.getLogger("Util").log(Level.WARNING, "Error al obtener IP", ex);
            return "127.0.0.1";
        }
    }

}
