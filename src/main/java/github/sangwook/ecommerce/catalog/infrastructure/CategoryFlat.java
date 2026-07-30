package github.sangwook.ecommerce.catalog.infrastructure;

import jakarta.annotation.Nullable;
import lombok.Getter;

@Getter
public class CategoryFlat {
    private final Long id;
    private final String name;
    private final Long parentId;

    public CategoryFlat(Long id, String name, @Nullable Long parentId) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
    }
}
