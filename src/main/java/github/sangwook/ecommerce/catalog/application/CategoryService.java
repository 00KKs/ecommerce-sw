package github.sangwook.ecommerce.catalog.application;

import github.sangwook.ecommerce.catalog.api.dto.CategoryResponse;
import github.sangwook.ecommerce.catalog.infrastructure.CategoryFlat;
import github.sangwook.ecommerce.catalog.infrastructure.CategoryRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getAllCategories() {
        List<CategoryFlat> flats = categoryRepository.findAllFlats();
        return buildTree(flats);
    }

    public List<CategoryResponse> buildTree(List<CategoryFlat> flats) {
        Map<Long, CategoryResponse> nodes = new HashMap<>();
        for (CategoryFlat flat : flats) {
            nodes.put(flat.getId(), new CategoryResponse(flat.getId(), flat.getName(), new ArrayList<>()));
        }

        List<CategoryResponse> roots = new ArrayList<>();
        for (CategoryFlat flat : flats) {
            CategoryResponse node = nodes.get(flat.getId());
            if (flat.getParentId() == null) {
                roots.add(node);
            } else {
                nodes.get(flat.getParentId()).getChildren().add(node);
            }
        }

        return roots;
    }

}
