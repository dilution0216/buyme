package com.example.buyme.product.repository;


 import com.example.buyme.product.entity.WishList;
 import org.springframework.data.jpa.repository.JpaRepository;

 import java.util.List;

public interface WishListRepository extends JpaRepository<WishList, Long> {
    List<WishList> findByUserId(Long userId);
}
