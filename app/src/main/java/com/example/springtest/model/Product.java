package com.example.springtest.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private double price;
    private String description;
    private String category;

    private transient int tempStockForUpdate;
    private transient String tempSizeForUpdate;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images;

    public Product() {
    }

    public Product(int id, String name, double price, String description, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<ProductVariant> getVariants() {
        return variants;
    }

    public void setVariants(List<ProductVariant> variants) {
        this.variants = variants;
    }

    public List<ProductImage> getImages() {
        return images;
    }

    public void setImages(List<ProductImage> images) {
        this.images = images;
    }

    public void setStock(int stock) {
        this.tempStockForUpdate = stock;
    }

    public void setSize(String size) {
        this.tempSizeForUpdate = size;
    }

    public int getStock() {
        // 如果是更新請求，返回暫存的值
        if (this.tempStockForUpdate > 0 || (this.variants != null && this.variants.isEmpty())) {
            return this.tempStockForUpdate;
        }
        // 否則，返回計算後的庫存（用於前端展示）
        if (this.variants == null) {
            return 0;
        }
        return this.variants.stream().mapToInt(ProductVariant::getStock).sum();
    }

    public String getSize() {
        // 如果是更新請求，返回暫存的值
        if (this.tempSizeForUpdate != null) {
            return this.tempSizeForUpdate;
        }
        // 否則，返回第一個變體的尺寸
        if (this.variants != null && !this.variants.isEmpty()) {
            return this.variants.get(0).getSize();
        }
        return null;
    }
}
