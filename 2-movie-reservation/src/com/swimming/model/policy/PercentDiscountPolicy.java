package com.swimming.model.policy;

import com.swimming.model.condition.DiscountCondition;
import com.swimming.model.Money;
import com.swimming.model.Screening;

public class PercentDiscountPolicy extends DefaultDiscountPolicy {

    private double percent;

    public PercentDiscountPolicy(double percent, DiscountCondition... conditions) {
        super(conditions);
        this.percent = percent;
    }

    @Override
    protected Money getDiscountAmount(Screening screening) {
        return screening.getMovieFee().times(percent);
    }
}
