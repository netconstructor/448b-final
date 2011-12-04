package vis.data.model.meta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import vis.data.model.AutoComplete;
import vis.data.model.AutoComplete.Type;
import vis.data.model.Term;
import vis.data.util.SQL;
import vis.data.util.SQL.NullForLastRowProcessor;

public class AutoCompleteAccessor {
	PreparedStatement insert_, query_, queryLimit_, queryType_, queryTypeLimit_;
	public AutoCompleteAccessor() throws SQLException {
		this(SQL.forThread());
	}
	public AutoCompleteAccessor(Connection conn) throws SQLException {
		query_ = conn.prepareStatement("SELECT " + 
			AutoComplete.TERM_ID + "," + Term.TERM + "," + AutoComplete.TYPE + "," + AutoComplete.REFERENCE_ID + "," + AutoComplete.SCORE + 
			" FROM " + AutoComplete.TABLE + 
			" JOIN " + Term.TABLE + " ON " + Term.ID + "=" + AutoComplete.TERM_ID + 
			" WHERE " + Term.TERM + " LIKE ?" +
			" ORDER BY " + AutoComplete.TYPE + "," + AutoComplete.SCORE + " DESC");
		queryLimit_ = conn.prepareStatement("SELECT " + 
			AutoComplete.TERM_ID + "," + Term.TERM + "," + AutoComplete.TYPE + "," + AutoComplete.REFERENCE_ID +
			" FROM " + AutoComplete.TABLE + 
			" JOIN " + Term.TABLE + " ON " + Term.ID + "=" + AutoComplete.TERM_ID + 
			" WHERE " + Term.TERM + " LIKE ?" + 
			" ORDER BY " + AutoComplete.TYPE + "," + AutoComplete.SCORE + " DESC LIMIT ?");
		queryType_ = conn.prepareStatement("SELECT " + 
			AutoComplete.TERM_ID + "," + Term.TERM + "," + AutoComplete.TYPE + "," + AutoComplete.REFERENCE_ID + "," + AutoComplete.SCORE + 
			" FROM " + AutoComplete.TABLE + 
			" JOIN " + Term.TABLE + " ON " + Term.ID + "=" + AutoComplete.TERM_ID + 
			" WHERE " + Term.TERM + " LIKE ?"  + " AND " + AutoComplete.TYPE + " = ?" +
			" ORDER BY " + AutoComplete.TYPE + "," + AutoComplete.SCORE + " DESC");
		queryTypeLimit_ = conn.prepareStatement("SELECT " + 
			AutoComplete.TERM_ID + "," + Term.TERM + "," + AutoComplete.TYPE + "," + AutoComplete.REFERENCE_ID +
			" FROM " + AutoComplete.TABLE + 
			" JOIN " + Term.TABLE + " ON " + Term.ID + "=" + AutoComplete.TERM_ID + 
			" WHERE " + Term.TERM + " LIKE ?" + " AND " + AutoComplete.TYPE + " = ?" +
			" ORDER BY " + AutoComplete.TYPE + "," + AutoComplete.SCORE + " DESC LIMIT ?");
		insert_ = conn.prepareStatement("INSERT INTO " + AutoComplete.TABLE + 
			" (" + AutoComplete.TERM_ID + "," + AutoComplete.TYPE + "," + AutoComplete.REFERENCE_ID + "," + AutoComplete.SCORE + ")" +
			" VALUES (?,?,?,?)");
	}
	public AutoComplete[] lookup(String q) throws SQLException {
		query_.setString(1, q);
		return processResults(query_.executeQuery());
	}
	public AutoComplete[] lookup(String q, AutoComplete.Type t) throws SQLException {
		queryLimit_.setString(1, q);
		queryLimit_.setInt(2, t.ordinal());
		return processResults(queryLimit_.executeQuery());
	}
	public AutoComplete[] lookup(String q, int limit) throws SQLException {
		queryType_.setString(1, q);
		queryType_.setInt(2, limit);
		return processResults(queryType_.executeQuery());
	}
	public AutoComplete[] lookup(String q, AutoComplete.Type t, int limit) throws SQLException {
		queryTypeLimit_.setString(1, q);
		queryTypeLimit_.setInt(2, limit);
		queryTypeLimit_.setInt(3, t.ordinal());
		return processResults(queryTypeLimit_.executeQuery());
	}
	public static class NamedAutoComplete extends AutoComplete {
		public String term_;
	}
	//trying a new pattern - since the resultset caches the whole thing anyway
	static NamedAutoComplete[] processResults(ResultSet rs) throws SQLException {
		try {
			rs.afterLast();
			int count = rs.getRow();
			NamedAutoComplete res[] = new NamedAutoComplete[count];
			rs.beforeFirst();
			int i = 0;
			while(rs.next()) {
				NamedAutoComplete ac = new NamedAutoComplete();
				ac.termId_ = rs.getInt(1);
				ac.term_ = rs.getString(2);
				ac.type_ = AutoComplete.Type.values()[rs.getInt(3)];
				ac.referenceId_ = rs.getInt(4);
				ac.score_ = rs.getInt(5);
				res[++i] = ac;
			}
			return res;
		} finally {
			rs.close();
		}
	}
	public void addAutoComplete(int term_id, Type type, int ref, int score) throws SQLException {
		insert_.setInt(1, term_id);
		insert_.setInt(2, type.ordinal());
		insert_.setInt(3, ref);
		insert_.setInt(4, score);
		insert_.executeUpdate();
	}
	public void addAutoCompleteBatch(int term_id, Type type, int ref, int score) throws SQLException {
		insert_.setInt(1, term_id);
		insert_.setInt(2, type.ordinal());
		insert_.setInt(3, ref);
		insert_.setInt(4, score);
		insert_.addBatch();
	}
	public static class ResultSetIterator extends org.apache.commons.dbutils.ResultSetIterator {
		public ResultSetIterator(ResultSet rs) {
			super(rs, new NullForLastRowProcessor());
		}
		
		public NamedAutoComplete nextLemma() {
			Object fields[] = super.next();
			if(fields == null)
				return null;
			NamedAutoComplete ac = new NamedAutoComplete();
			ac.termId_ = (Integer)fields[0];
			ac.term_ = (String)fields[1];
			ac.type_ = AutoComplete.Type.values()[(Integer)fields[2]];
			ac.referenceId_ = (Integer)fields[3];
			ac.score_ = (Integer)fields[4];
			return ac;
		}
	}
	public ResultSetIterator autoCompleteIterator(String q) throws SQLException {
		int fetch_size = query_.getFetchSize();
		try {
			query_.setFetchSize(Integer.MIN_VALUE);
			query_.setString(1, q);
			return new ResultSetIterator(query_.executeQuery());
		} finally {
			query_.setFetchSize(fetch_size);
		}
	}
	public ResultSetIterator autoCompleteIterator(String q, AutoComplete.Type t) throws SQLException {
		int fetch_size = query_.getFetchSize();
		try {
			queryLimit_.setString(1, q);
			queryLimit_.setInt(2, t.ordinal());
			return new ResultSetIterator(queryLimit_.executeQuery());
		} finally {
			queryLimit_.setFetchSize(fetch_size);
		}
	}
	public ResultSetIterator autoCompleteIterator(String q, int limit) throws SQLException {
		int fetch_size = query_.getFetchSize();
		try {
			queryType_.setString(1, q);
			queryType_.setInt(2, limit);
			return new ResultSetIterator(queryType_.executeQuery());
		} finally {
			queryType_.setFetchSize(fetch_size);
		}
	}
	public ResultSetIterator autoCompleteIterator(String q, AutoComplete.Type t, int limit) throws SQLException {
		int fetch_size = query_.getFetchSize();
		try {
			queryTypeLimit_.setString(1, q);
			queryTypeLimit_.setInt(2, limit);
			queryTypeLimit_.setInt(3, t.ordinal());
			return new ResultSetIterator(queryTypeLimit_.executeQuery());
		} finally {
			queryTypeLimit_.setFetchSize(fetch_size);
		}
	}
}