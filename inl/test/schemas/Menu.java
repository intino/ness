package schemas;

public class Menu {
    public String[] meals = new String[0];
    public Double[] prices = new Double[0];
    public Boolean[] availability;

    public Menu() {
    }

    public Menu(String[] meals, Double[] prices, Boolean[] availability) {
        this.meals = meals;
        this.prices = prices;
        this.availability = availability;
    }
}
