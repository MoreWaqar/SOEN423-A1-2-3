package Model;

public class Purchase {

    private String itemID;
    private String dateOfPurchase;
    private String customerID;

    public Purchase(String itemID, String dateOfPurchase) {
        this.itemID = itemID;
        this.dateOfPurchase = dateOfPurchase;
    }


    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getDateOfPurchase() {
        return dateOfPurchase;
    }

    public void setDateOfPurchase(String dateOfPurchase) {
        this.dateOfPurchase = dateOfPurchase;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }
}
