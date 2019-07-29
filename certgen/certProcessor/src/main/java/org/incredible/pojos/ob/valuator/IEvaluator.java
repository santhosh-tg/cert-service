package org.incredible.pojos.ob.valuator;

import org.incredible.pojos.ob.exeptions.InvalidDateFormatException;

public interface IEvaluator {

    String evaluates(Object inputVal) throws InvalidDateFormatException;
}
