package schemas;

public class MenuX {
    public String[] _meals;
    public Double[] _prices;
    public Boolean[] _availability;

    public MenuX() {
    }

    public MenuX(String[] meals, Double[] prices, Boolean[] availability) {
        this._meals = meals;
        this._prices = prices;
        this._availability = availability;
    }
}
