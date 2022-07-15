package com.swimming.model.policy;

import com.swimming.model.Money;
import com.swimming.model.Screening;

public class NoneDiscountPolicy implements DiscountPolicy {

    @Override
    public Money calculateDiscountAmount(Screening screening) {
        return Money.ZERO;
    }
}
