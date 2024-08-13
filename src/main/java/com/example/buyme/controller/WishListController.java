package com.example.buyme.controller;

import com.example.buyme.entity.WishList;
import com.example.buyme.service.WishListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishListController {

    private final WishListService wishListService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<WishList>> getWishlistByUser(@PathVariable Long userId) {
        List<WishList> wishlist = wishListService.getWishlistByUser(userId);
        return ResponseEntity.ok(wishlist);
    }

    @PostMapping
    public ResponseEntity<WishList> addProductToWishList(@RequestBody WishList wishList) {
        WishList createdWishList = wishListService.addProductToWishList(wishList);
        return ResponseEntity.ok(createdWishList);
    }

    @PutMapping("/{wishlistId}")
    public ResponseEntity<WishList> updateWishListItem(@PathVariable Long wishlistId, @RequestBody WishList updatedWishList) {
        updatedWishList.setWishlistId(wishlistId);
        WishList wishList = wishListService.updateWishListItem(updatedWishList);
        return ResponseEntity.ok(wishList);
    }

    @DeleteMapping("/{wishlistId}")
    public ResponseEntity<Void> removeProductFromWishList(@PathVariable Long wishlistId) {
        wishListService.removeProductFromWishList(wishlistId);
        return ResponseEntity.ok().build();
    }
}
