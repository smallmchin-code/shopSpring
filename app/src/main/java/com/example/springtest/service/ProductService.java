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

    @Transactional
    public Product createProduct(
            String name, double price, String description, String category,
            List<ProductVariant> variants,
            MultipartFile mainImage, List<MultipartFile> additionalImages) throws IOException {

        // 1. **建構 Product 主體**
        Product newProduct = new Product();
        newProduct.setName(name);
        newProduct.setPrice(price);
        newProduct.setDescription(description);
        newProduct.setCategory(category);

        // 2. **處理 Variants (庫存與尺寸)**
        for (ProductVariant variant : variants) {
            variant.setProduct(newProduct); // 告訴變體它屬於哪個商品
        }
        newProduct.setVariants(variants);

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

    @Transactional
    public void deleteProduct(int id) {
        Product product = productRepository.findByIdWithVariants(id).orElse(null);
        productRepository.delete(product);
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
        if (updatedProduct.getVariants() != null) {
            existingProduct.getVariants().clear(); // 清空舊規格
            for (ProductVariant v : updatedProduct.getVariants()) {
                v.setProduct(existingProduct);
                existingProduct.getVariants().add(v);
            }
        }
        return productRepository.save(existingProduct);
    }

    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContainingWithVariants(name);
    }

}
