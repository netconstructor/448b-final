package vis.data.model.meta;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import vis.data.model.DocLemma;

public class EntityHits {
	PreparedStatement query_;
	public EntityHits(Connection conn) throws SQLException {
		query_ = conn.prepareStatement("SELECT " + DocLemma.ENTITY_LIST + " FROM " + DocLemma.TABLE + " WHERE doc_id = ?");
	}
	public int[] getEntitiesForDocument(int doc_id) throws SQLException {
		query_.setInt(1, doc_id);
		ResultSet rs = query_.executeQuery();
		try {
			if(!rs.next())
				throw new RuntimeException("failed to find doc_id " + doc_id);
			
			byte[] data = rs.getBytes(1);
			int[] entity_ids = new int[data.length / (Integer.SIZE / 8) / 2];
			ByteBuffer bb = ByteBuffer.wrap(data);
			for(int i = 0; i < entity_ids.length; ++i) {
				entity_ids[i] = bb.getInt();
				/*int count =*/ bb.getInt();
			}
			return entity_ids;
		} finally {
			rs.close();
		}
	}
}
