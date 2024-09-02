package com.example.buyme.product.service;

import com.example.buyme.product.dto.UserDTO;
import com.example.buyme.product.entity.WishList;
import com.example.buyme.product.repository.WishListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishListService {

    private final WishListRepository wishListRepository;
    private final WebClient.Builder webClientBuilder;

    public List<WishList> getWishlistByUser(Long userId) {
        // user-service로부터 추가적인 사용자 정보를 가져오는 로직을 추가 가능함
        // 예: UserDTO user = getUserById(userId);
        return wishListRepository.findByUserId(userId);
    }

    public WishList addProductToWishList(WishList wishList) {
        // user-service로부터 userId에 해당하는 유저 정보를 가져오는 로직 추가 가능함
        return wishListRepository.save(wishList);
    }

    public void removeProductFromWishList(Long wishlistId) {
        wishListRepository.deleteById(wishlistId);
    }

    public WishList updateWishListItem(WishList updatedWishList) {
        return wishListRepository.save(updatedWishList);
    }


    private UserDTO getUserById(Long userId) {
        return webClientBuilder.build()
                .get()
                .uri("http://gateway-service/api/users/" + userId)
                .retrieve()
                .bodyToMono(UserDTO.class)
                .block();
    }
}