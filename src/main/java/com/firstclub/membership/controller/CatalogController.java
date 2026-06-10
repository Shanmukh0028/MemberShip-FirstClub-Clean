package com.firstclub.membership.controller;

import com.firstclub.membership.dto.response.ApiResponse;
import com.firstclub.membership.dto.response.CatalogResponse;
import com.firstclub.membership.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalog")
@Tag(name = "Catalog", description = "Browse available membership plans and tiers")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    @Operation(summary = "Get all plans and tiers", description = "Returns the full membership catalog for user selection")
    public ResponseEntity<ApiResponse<CatalogResponse>> getCatalog() {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.getCatalog()));
    }
}
