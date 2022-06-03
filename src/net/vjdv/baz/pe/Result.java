package net.vjdv.baz.pe;

import java.util.ArrayList;
import java.util.List;

public class Result {

	public String error = null;
	public List<Integer> affected = new ArrayList<>();
	public List<ResultPage> pages = new ArrayList<>();
    public long elapsedTime = 0;

	public static class ResultPage {

		public Columna[] columns;
		public final List<Object[]> rows = new ArrayList<>();
	}
    
    public static class Columna {
        public String descripcion;
        public String tipo;
    }
    
}
