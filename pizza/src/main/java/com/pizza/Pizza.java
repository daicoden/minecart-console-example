package com.pizza;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

public class Pizza {
  private final String size;
  private final String toppings;
  private final String price;

  public Pizza(String size, String toppings, String price) {
    this.size = size;
    this.toppings = toppings;
    this.price = price;
  }

  public Map<String, String> properties() {
    return ImmutableMap.of("Size", size, "Toppings", toppings, "Price", price);
  }

  @Override public String toString() {
    return "Size: " + size + ", Toppings: " + toppings + ", Price: " + price;
  }
}
