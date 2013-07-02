package com.pizza;

public class PizzaKitchen {
  public Pizza makePepperoni() {
    System.out.println("Making a pepperoni pizza now!");
    return new Pizza("Large", "Pepperoni", "$20.00");
  }
}
