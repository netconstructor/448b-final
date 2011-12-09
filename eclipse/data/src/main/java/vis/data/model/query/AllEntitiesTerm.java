package vis.data.model.query;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.tuple.Pair;

import vis.data.model.meta.IdListAccessor;

public class AllEntitiesTerm extends Term {
	public static class Parameters implements Term.Parameters {		
		@Override
		public int hashCode() {
			int hashCode = 0;
			hashCode ^= AllEntitiesTerm.class.hashCode();
			return hashCode;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(!Parameters.class.isInstance(obj))
				return false;
			return true;

		}

		@Override
		public void validate() {
		}

		@Override
		public ResultType resultType() {
			return ResultType.ENTITY_HITS;
		}
		@Override
		public Collection<Term.Parameters> withChildren() {
			return Arrays.asList((Term.Parameters)this);
		}
		@Override
		public void setFilterOnly() {
			//always is
		}
	}
	
	public final Parameters parameters_;
	public final int entities_[];
	public AllEntitiesTerm(Parameters p) throws SQLException {
		parameters_ = p;
		entities_ = IdListAccessor.allEntitiesInOrder();
	}

	public Term.Parameters parameters() {
		return parameters_;
	}	
	@Override
	public Pair<int[], int[]> compute() throws SQLException {
		return Pair.of(entities_, new int[entities_.length]);
	}
}
