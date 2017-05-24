package net.vjdv.baz.pe;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author B187926
 */
@XmlRootElement(name = "ROOT")
public class Resultado {

    @XmlElement(name = "RESULTSET")
    public List<String> resultsets = new ArrayList<>();
    @XmlElement(name = "AFECTADOS")
    public List<String> afectados = new ArrayList<>();
    @XmlElement(name = "ERROR")
    public String error = null;
}
