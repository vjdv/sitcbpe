package net.vjdv.baz.pe;

import java.util.ArrayList;
import java.util.List;

public class Result {

	public String error = null;
	public List<Integer> affected = new ArrayList<>();
	public List<ResultPage> pages = new ArrayList<>();

	public static class ResultPage {

		public String[] columns;
		public final List<Object[]> rows = new ArrayList<>();
	}
}
