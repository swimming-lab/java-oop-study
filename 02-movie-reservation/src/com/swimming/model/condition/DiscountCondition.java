package com.swimming.model.condition;

import com.swimming.model.Screening;

public interface DiscountCondition {

    boolean isSatisfiedBy(Screening screening);
}
