package com.example.springtest.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.springtest.model.Product;
import com.example.springtest.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable int id) {
        return productService.getProductById(id);
    }

    // @PostMapping
    // public Product createProduct(@RequestBody Product product) {
    // return productService.createProduct(product);
    // }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> createProductWithImage(
            // 接收文字欄位 (使用 @RequestParam)
            @RequestParam("name") String name,
            @RequestParam("price") double price,
            @RequestParam("description") String description,
            @RequestParam("category") String category,
            @RequestParam("stock") int stock,
            @RequestParam("size") String size,

            // 接收檔案欄位
            @RequestPart("imageismain") MultipartFile mainImage,
            // imagedata 欄位是 optional 且可多選的
            @RequestPart(value = "imagedata", required = false) List<MultipartFile> additionalImages) {
        try {
            // 呼叫 Service Layer 處理業務邏輯和檔案儲存
            Product newProduct = productService.createProductWithImages(
                    name, price, description, category, stock, size, mainImage, additionalImages);
            return new ResponseEntity<>(newProduct, HttpStatus.CREATED); // 返回 201 Created

        } catch (Exception e) {
            // 處理檔案讀取或儲存失敗等服務端錯誤
            System.err.println("新增商品失敗: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR); // 返回 500
        }
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable int id) {
        productService.deleteProduct(id);
    }
}
