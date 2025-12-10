package com.example.springtest.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.springtest.model.Product;
import com.example.springtest.model.ProductImage;
import com.example.springtest.model.ProductVariant;
import com.example.springtest.repository.ProductImageRepository;
import com.example.springtest.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, ProductImageRepository imageRepository) {
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Product getProductById(int id) {
        Product product = productRepository.findByIdWithVariants(id).orElse(null);
        if (product != null) {
            product.getImages().size();
        }

        return product;
    }

    public List<Product> getFilteredProducts(String category) {
        if (category == null || category.trim().isEmpty() || "all".equalsIgnoreCase(category.trim())) {
            return productRepository.findAll();
        } else {
            return productRepository.findByCategoryWithVariants(category); // 🌟 使用新的 Repository 方法
        }
    }

    // public Product createProduct(Product product) {
    // return productRepository.save(product);
    // }

    @Transactional // 確保資料庫操作和檔案處理（如果有的話）在一個事務中
    public Product createProductWithImages(
            String name, double price, String description, String category,
            int stock, String size,
            MultipartFile mainImage, List<MultipartFile> additionalImages) throws IOException {

        // 1. **建構 Product 主體**
        Product newProduct = new Product();
        newProduct.setName(name);
        newProduct.setPrice(price);
        newProduct.setDescription(description);
        newProduct.setCategory(category);

        // 2. **處理 Variants (庫存與尺寸)**
        ProductVariant variant = new ProductVariant();
        variant.setSize(size);
        variant.setStock(stock);
        variant.setProduct(newProduct); // 設置雙向關聯
        newProduct.setVariants(List.of(variant));

        // 3. **處理 Images (將 MultipartFile 轉換為 byte[])**
        List<ProductImage> imageList = new ArrayList<>();

        // 處理主圖 (imageismain)
        ProductImage main = new ProductImage();
        main.setImageData(mainImage.getBytes()); // 🌟 讀取檔案數據
        main.setMain(true);
        main.setProduct(newProduct); // 設置雙向關聯
        imageList.add(main);

        // 處理其他圖片 (imagedata)
        if (additionalImages != null && !additionalImages.isEmpty()) {
            for (MultipartFile file : additionalImages) {
                if (file.isEmpty())
                    continue; // 跳過空文件
                ProductImage img = new ProductImage();
                img.setImageData(file.getBytes()); // 🌟 讀取檔案數據
                img.setMain(false);
                img.setProduct(newProduct); // 設置雙向關聯
                imageList.add(img);
            }
        }
        newProduct.setImages(imageList);

        // 4. **儲存到資料庫** (Product 上的 CascadeType.ALL 會自動儲存 Variants 和 Images)
        return productRepository.save(newProduct);
    }

    public void deleteProduct(int id) {
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public byte[] getImageDataById(int imageId) {
        Optional<ProductImage> imageOptional = imageRepository.findById(imageId);

        return imageOptional.map(ProductImage::getImageData)
                .orElse(null);
    }

    @Transactional
    public Product updateProduct(int id, Product updatedProduct) {
        Product existingProduct = productRepository.findByIdWithVariants(id).orElse(null);

        if (existingProduct == null) {
            // 🌟 修正 2: 找不到商品時拋出異常，讓 Controller 返回 404
            throw new RuntimeException("Product not found with ID: " + id);
        }

        // 2. 更新商品基本欄位
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setCategory(updatedProduct.getCategory());
        if (existingProduct.getVariants() != null && !existingProduct.getVariants().isEmpty()) {
            ProductVariant mainVariant = existingProduct.getVariants().get(0);

            if (updatedProduct.getStock() >= 0) {
                mainVariant.setStock(updatedProduct.getStock());
            }

            if (updatedProduct.getSize() != null) {
                mainVariant.setSize(updatedProduct.getSize());
            }
        }
        return productRepository.save(existingProduct);
    }

}
