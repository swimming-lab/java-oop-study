package com.swimming.domain.condition;

import com.swimming.domain.Screening;

public interface DiscountCondition {

    public boolean isSatisfiedBy(Screening screening);
}
