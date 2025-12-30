package app.tempest.ims.controller;

import app.tempest.ims.entity.Item;
import app.tempest.ims.repository.ItemRepository;
import app.tempest.common.security.SecurityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for Item operations.
 * All operations are tenant-scoped - tenantId is extracted from JWT, never from request body.
 */
@RestController
@RequestMapping("/items")
public class ItemController {

    private static final Logger log = LoggerFactory.getLogger(ItemController.class);

    private final ItemRepository itemRepository;

    public ItemController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    /**
     * Get all items for the current tenant.
     * Accessible by ADMIN, MANAGER, WAREHOUSE_ASSOCIATE, and INTEGRATION roles.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'WAREHOUSE_ASSOCIATE', 'INTEGRATION')")
    public ResponseEntity<List<Item>> getItems(@AuthenticationPrincipal Jwt jwt) {
        String tenantId = SecurityUtils.requireTenantId(jwt);
        log.debug("Fetching items for tenant: {}", tenantId);

        List<Item> items = itemRepository.findByTenantId(tenantId);
        return ResponseEntity.ok(items);
    }

    /**
     * Get a specific item by ID.
     * Accessible by ADMIN, MANAGER, WAREHOUSE_ASSOCIATE, and INTEGRATION roles.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'WAREHOUSE_ASSOCIATE', 'INTEGRATION')")
    public ResponseEntity<Item> getItem(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String tenantId = SecurityUtils.requireTenantId(jwt);
        log.debug("Fetching item {} for tenant: {}", id, tenantId);

        return itemRepository.findByIdAndTenantId(id, tenantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get a specific item by SKU.
     * Accessible by ADMIN, MANAGER, WAREHOUSE_ASSOCIATE, and INTEGRATION roles.
     */
    @GetMapping("/sku/{sku}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'WAREHOUSE_ASSOCIATE', 'INTEGRATION')")
    public ResponseEntity<Item> getItemBySku(@PathVariable String sku, @AuthenticationPrincipal Jwt jwt) {
        String tenantId = SecurityUtils.requireTenantId(jwt);
        log.debug("Fetching item with SKU {} for tenant: {}", sku, tenantId);

        return itemRepository.findByTenantIdAndSku(tenantId, sku)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new item.
     * Accessible by ADMIN and MANAGER roles only.
     * tenantId is set from JWT, not from request body.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Item> createItem(@RequestBody CreateItemRequest request, @AuthenticationPrincipal Jwt jwt) {
        String tenantId = SecurityUtils.requireTenantId(jwt);
        String userId = SecurityUtils.getUserId(jwt).orElse(null);
        log.info("Creating item with SKU {} for tenant: {} by user: {}", request.sku(), tenantId, userId);

        Item item = Item.builder()
                .sku(request.sku())
                .name(request.name())
                .description(request.description())
                .active(true)
                .build();

        // Set tenant and audit fields from JWT - never from request body
        item.setTenantId(tenantId);
        item.setCreatedByUserId(userId);
        item.setUpdatedByUserId(userId);

        Item saved = itemRepository.save(item);
        log.info("Created item {} for tenant: {}", saved.getId(), tenantId);

        return ResponseEntity.ok(saved);
    }

    /**
     * Request DTO for creating an item.
     * Note: tenantId is NOT included - it comes from JWT.
     */
    public record CreateItemRequest(
            String sku,
            String name,
            String description
    ) {}
}
