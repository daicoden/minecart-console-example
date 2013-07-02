package com.pizza;

public class PizzaApp {
  public static void main(String[] args) {
    Pizza pizza = new PizzaKitchen().makePepperoni();
    System.out.println(pizza);
  }
}
