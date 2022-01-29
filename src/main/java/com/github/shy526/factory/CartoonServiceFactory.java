package com.github.shy526.factory;

import com.github.shy526.service.CartoonService;

/**
 * @author shy526
 */
public class CartoonServiceFactory {
    private final static CartoonService CARTOON_SERVICE = new CartoonService();

    public static CartoonService getInstance() {
        return CARTOON_SERVICE;
    }
}
