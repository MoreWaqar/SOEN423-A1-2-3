package ServerImpl;



public interface SOAPInterface {

    public String addItem(String managerID, String itemID, String itemName,int qty, int price);

    public String removeItem(String managerID, String itemID, int qty);

    public String listItemAvailability(String managerID);

    public String purchaseItem(String customerID, String itemID, String dateOfPurchase);

    public String returnItem(String customerID, String itemID, String dateOfReturn);

    public String findItem(String customerID, String itemName);

    public String exchangeLogic(String customerID, String itemID, String oldItemID, String dateOfReturn);
}
