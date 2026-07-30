package github.sangwook.ecommerce.catalog.infrastructure;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CategoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<CategoryFlat> findAllFlats() {
        return jdbcTemplate.query(
            """
            SELECT c.id, c.name, cc.ancestor AS parent_id
            FROM category c
            LEFT JOIN category_closure cc ON c.id = cc.descendant AND cc.depth = 1
            """,
            (rs, rowNum) -> new CategoryFlat(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getObject("parent_id", Long.class)
            ));
    }

}
