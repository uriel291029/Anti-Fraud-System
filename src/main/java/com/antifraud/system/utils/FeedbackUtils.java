package com.antifraud.system.utils;

public class FeedbackUtils {

  private static final double FIRST_FACTOR = 0.8;

  private static final double SECOND_FACTOR = 0.2;

  public static long increaseLimit(long currentLimit, long transactionValue ){
    return (long) Math.ceil(FIRST_FACTOR * currentLimit + SECOND_FACTOR * transactionValue);
  }

  public static long decreaseLimit(long currentLimit, long transactionValue ){
    return (long) Math.ceil(FIRST_FACTOR * currentLimit - SECOND_FACTOR * transactionValue);
  }
}
