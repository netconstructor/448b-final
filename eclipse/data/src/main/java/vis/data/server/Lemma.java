package vis.data.server;

import java.sql.Connection;
import java.sql.SQLException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import vis.data.model.RawLemma;
import vis.data.model.meta.LemmaRaw;
import vis.data.util.SQL;

public class Lemma {
	@Path("/word/{word}/lemma")
	public static class LemmasForWord {
		@GET
		@Produces("application/json")
		public RawLemma[] get(@PathParam("word") String word) throws SQLException {
			//TODO: actually stem the word	
			Connection conn = SQL.forThread();
			LemmaRaw lr = new LemmaRaw(conn);
			return lr.lookupLemma(word);
		}
	}
	@Path("/word/{word}/pos/{pos}/lemma")
	public static class LemmasForWordAndPos {
		@GET
		@Produces("application/json")
		public RawLemma get(@PathParam("word") String word, @PathParam("pos") String pos) throws SQLException {
			//TODO: actually stem the word	
			Connection conn = SQL.forThread();
			LemmaRaw lr = new LemmaRaw(conn);
			return lr.lookupLemma(word, pos);
		}
	}
}
