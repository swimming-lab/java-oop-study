package com.swimming.domain.condition;

import com.swimming.domain.Screening;

public class SequenceCondition implements DiscountCondition{

    private int sequence;

    public boolean isSatisfiedBy(Screening screening) {
        return sequence == screening.getSequence();
    }
}
