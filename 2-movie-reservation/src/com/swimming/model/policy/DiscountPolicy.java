package com.swimming.model.policy;

import com.swimming.model.Money;
import com.swimming.model.Screening;

public interface DiscountPolicy {

    Money calculateDiscountAmount(Screening screening);
}
