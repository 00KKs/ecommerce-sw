package github.sangwook.ecommerce.openapi;

import github.sangwook.ecommerce.catalog.api.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Tag(name = "Product", description = "상품 관리 API")
public interface ProductOpenApiDocs {

    @Operation(
        summary = "상품 등록",
        description = "카테고리에 새 상품을 등록합니다."
    )
    @ApiResponse(responseCode = "200", description = "등록 성공")
    ResponseEntity<Void> create(ProductCreateRequest request);

    @Operation(
        summary = "카테고리별 상품 목록 조회",
        description = "지정한 카테고리와 그 하위 카테고리에 속한 판매중인 상품 목록을 최저가와 함께 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<List<ProductSummaryResponse>> getProductsByCategoryId(Long categoryId);

    @Operation(
        summary = "상품 수정",
        description = "상품의 이름과 설명을 수정하고 수정된 결과를 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "수정 성공")
    ResponseEntity<ProductUpdateResponse> update(Long productId, ProductUpdateRequest request);

    @Operation(
        summary = "상품 상세 조회",
        description = "지정한 상품 ID의 상세 정보를 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<ProductDetailResponse> getDetail(Long productId);
    @Operation(
            summary = "상품 판매상태 변경",
            description = "상품의 판매상태를 변경합니다. SELLING으로 전환하려면 SELLING 상태인 SKU가 최소 하나 있어야 하며, DRAFT로는 되돌릴 수 없습니다."
    )

    @ApiResponse(responseCode = "200", description = "변경 성공")
    ResponseEntity<ProductStatusResponse> changeStatus(Long productId, ProductStatusRequest request);
}