package github.sangwook.ecommerce.catalog.api.dto;

import java.util.List;
import lombok.Getter;

@Getter
public class CategoryResponse {
    private final Long id;
    private final String name;
    private final List<CategoryResponse> children;

    public CategoryResponse(Long id, String name, List<CategoryResponse> children) {
        this.id = id;
        this.name = name;
        this.children = children;
    }
}
