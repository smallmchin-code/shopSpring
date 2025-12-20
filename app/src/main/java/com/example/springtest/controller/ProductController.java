package com.example.springtest.controller;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.springtest.model.Product;
import com.example.springtest.model.ProductVariant;
import com.example.springtest.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> getAllProducts(@RequestParam(value = "category", required = false) String category,
            @RequestParam(required = false) String name) {
        if (name != null && !name.isEmpty()) {
            return productService.searchProductsByName(name); // 處理搜尋
        }
        if (category != null && !category.isEmpty()) {
            return productService.getFilteredProducts(category);
        }
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable int id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    // @PostMapping
    // public Product createProduct(@RequestBody Product product) {
    // return productService.createProduct(product);
    // }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> createProductWithImage(
            @RequestParam("name") String name,
            @RequestParam("price") double price,
            @RequestParam("description") String description,
            @RequestParam("category") String category,
            @RequestParam("variantsJson") String variantsJson,

            @RequestPart("imageismain") MultipartFile mainImage,
            // imagedata 欄位是 optional 且可多選的
            @RequestPart(value = "imagedata", required = false) List<MultipartFile> additionalImages) {
        ObjectMapper mapper = new ObjectMapper();
        List<ProductVariant> variants;
        try {
            variants = mapper.readValue(variantsJson, new TypeReference<List<ProductVariant>>() {
            });
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().build();
        }
        try {
            // 💡 加上 try-catch 處理 Service 拋出的 IOException
            Product savedProduct = productService.createProduct(name, price, description, category, variants, mainImage,
                    additionalImages);
            return ResponseEntity.ok(savedProduct);
        } catch (IOException e) {
            // 如果圖片轉換 byte[] 出錯
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @GetMapping("/images/{imageId}")
    public ResponseEntity<byte[]> getImage(@PathVariable int imageId) {
        byte[] imageData = productService.getImageDataById(imageId);
        System.out.println("圖片 ID：" + imageId + "；讀取到的數據長度（Bytes）：" + (imageData != null ? imageData.length : "null"));

        if (imageData == null) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentLength(imageData.length);

        // 返回 200 OK 狀態碼，並將 byte[] 放入 Response Body
        return new ResponseEntity<>(imageData, headers, org.springframework.http.HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable int id) {
        productService.deleteProduct(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable int id, @RequestBody Product updatedProduct) {
        try {
            Product result = productService.updateProduct(id, updatedProduct);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Product not found")) {
                return ResponseEntity.notFound().build();
            }
            System.err.println("更新商品失敗: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
