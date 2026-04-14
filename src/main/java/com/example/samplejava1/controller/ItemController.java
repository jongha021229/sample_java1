package com.example.samplejava1.controller;

import com.example.samplejava1.model.Item;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final Map<Long, Item> items = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @GetMapping
    public List<Map<String, Object>> listItems() {
        List<Map<String, Object>> result = new ArrayList<>();
        items.forEach((id, item) -> result.add(itemToMap(id, item)));
        return result;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getItem(@PathVariable Long id) {
        Item item = items.get(id);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "not found"));
        }
        return ResponseEntity.ok(itemToMap(id, item));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createItem(@Valid @RequestBody Item item) {
        long id = nextId.getAndIncrement();
        items.put(id, item);
        return ResponseEntity.status(HttpStatus.CREATED).body(itemToMap(id, item));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteItem(@PathVariable Long id) {
        Item removed = items.remove(id);
        if (removed == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "not found"));
        }
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }

    @GetMapping("/search")
    public List<Map<String, Object>> searchItems(
            @RequestParam(defaultValue = "") @Size(max = 100) String q) {
        List<Map<String, Object>> result = new ArrayList<>();
        String query = q.toLowerCase();
        items.forEach((id, item) -> {
            if (item.getName().toLowerCase().contains(query)) {
                result.add(itemToMap(id, item));
            }
        });
        return result;
    }

    private Map<String, Object> itemToMap(Long id, Item item) {
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        map.put("id", id);
        map.put("name", item.getName());
        map.put("price", item.getPrice());
        map.put("description", item.getDescription());
        return map;
    }
}
