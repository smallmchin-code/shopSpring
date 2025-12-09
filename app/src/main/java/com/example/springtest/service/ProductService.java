package com.example.springtest.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public Product getProductById(int id) {
        return productRepository.findById(id).orElse(null);
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

    public byte[] getImageDataById(int imageId) {
        return imageRepository.findById(imageId)
                .map(ProductImage::getImageData)
                .orElse(null);
    }
}
