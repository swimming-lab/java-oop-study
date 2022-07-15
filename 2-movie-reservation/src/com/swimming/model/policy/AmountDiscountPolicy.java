package com.swimming.model.policy;

import com.swimming.model.condition.DiscountCondition;
import com.swimming.model.Money;
import com.swimming.model.Screening;

public class AmountDiscountPolicy extends DefaultDiscountPolicy {

    private Money discountAmount;

    public AmountDiscountPolicy(Money discountAmount, DiscountCondition... conditions) {
        super(conditions);
        this.discountAmount = discountAmount;
    }

    @Override
    protected Money getDiscountAmount(Screening screening) {
        return discountAmount;
    }
}
