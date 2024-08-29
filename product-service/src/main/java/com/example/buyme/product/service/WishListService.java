package com.example.buyme.product.service;

import com.example.buyme.product.entity.WishList;
import com.example.buyme.product.repository.WishListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishListService {

    private final WishListRepository wishListRepository;

    public List<WishList> getWishlistByUser(Long userId) {
        return wishListRepository.findByUserId(userId);
    }

    public WishList addProductToWishList(WishList wishList) {
        return wishListRepository.save(wishList);
    }

    public void removeProductFromWishList(Long wishlistId) {
        wishListRepository.deleteById(wishlistId);
    }

    public WishList updateWishListItem(WishList updatedWishList) {
        return wishListRepository.save(updatedWishList);
    }
}
