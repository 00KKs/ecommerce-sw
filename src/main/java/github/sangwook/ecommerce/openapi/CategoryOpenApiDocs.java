package github.sangwook.ecommerce.openapi;

import github.sangwook.ecommerce.catalog.api.dto.CategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Tag(name = "Category", description = "카테고리 조회 API")
public interface CategoryOpenApiDocs {

    @Operation(
        summary = "전체 카테고리 조회",
        description = "등록된 모든 카테고리 목록을 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<List<CategoryResponse>> getAllCategories();
}