package vis.data.model.meta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import vis.data.model.WikiPage;
import vis.data.model.WikiRedirect;
import vis.data.util.SQL;
import vis.data.util.StringArrayResultSetIterator;


public class WikiRedirectAccessor {
	PreparedStatement queryList_, queryListLimited_;
	public WikiRedirectAccessor() throws SQLException {
		Connection conn = SQL.forThread();
		queryList_ = conn.prepareStatement("SELECT " + WikiPage.TITLE + "," + WikiRedirect.TITLE + " FROM " + WikiPage.TABLE + " JOIN " + WikiRedirect.TABLE + " ON " + WikiPage.ID + " = " + WikiRedirect.FROM + " WHERE " + WikiPage.IS_REDIRECT + " = 1 AND " + WikiPage.NAMESPACE + " = 0");
		//stream these
		queryList_.setFetchSize(Integer.MIN_VALUE);
		queryListLimited_ = conn.prepareStatement("SELECT " + WikiPage.TITLE + "," + WikiRedirect.TITLE + " FROM " + WikiPage.TABLE + " JOIN " + WikiRedirect.TABLE + " ON " + WikiPage.ID + " = " + WikiRedirect.FROM + " WHERE " + WikiRedirect.FROM + " >= ? AND " + WikiRedirect.FROM + " < ? AND " + WikiPage.IS_REDIRECT + " = 1 AND " + WikiPage.NAMESPACE + " = 0");
		//stream these
		queryListLimited_.setFetchSize(Integer.MIN_VALUE);
	}
	public StringArrayResultSetIterator redirectIterator() throws SQLException {
		ResultSet rs = queryList_.executeQuery();
		return new StringArrayResultSetIterator(rs);
	}
	public StringArrayResultSetIterator redirectIterator(int beginInclusive, int endExclusive) throws SQLException {
		queryListLimited_.setInt(1, beginInclusive);
		queryListLimited_.setInt(2, endExclusive);
		ResultSet rs = queryListLimited_.executeQuery();
		return new StringArrayResultSetIterator(rs);
	}
}