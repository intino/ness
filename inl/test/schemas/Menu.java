package schemas;

public class Menu {
    public String[] meals;
    public Double[] prices;
    public Boolean[] availability;

    public Menu() {
    }

    public Menu(String[] meals, Double[] prices, Boolean[] availability) {
        this.meals = meals;
        this.prices = prices;
        this.availability = availability;
    }
}
