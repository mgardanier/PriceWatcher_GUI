package app.Model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by michael.gardanier on 6/6/17.
 */
public class WatchedItem {
    private double originalPrice = Double.POSITIVE_INFINITY;
    private double lastPrice = Double.POSITIVE_INFINITY;
    private double recentPrice;
    private String itemURL;
    private String itemName;
    private Date dateOfLastPrice;
    private boolean lowerPrice;
    private boolean firstUpdate = true;
    private Parameters queryParams;

    private String highlight = "*";

    public WatchedItem(String itemURL, String itemName){
        this.itemURL = itemURL;
        this.itemName = itemName;
        originalPrice = Double.POSITIVE_INFINITY;
    }

    @Override
    public String toString(){
        String dateString;
        if( dateOfLastPrice != null) {
            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            dateString = dateFormat.format( dateOfLastPrice);
        } else
            dateString = "N/A";
        String bPrice = String.format("%.2f", originalPrice);
        String rPrice = String.format("%.2f", recentPrice);
        String lineItem;
        if(lowerPrice) {
            lineItem = String.format("%-25s **\t$%-10s \t$%-12s \t%-15s" + "\n", itemName, rPrice, bPrice, dateString);
            this.lowerPrice = false;
        } else
            lineItem = String.format("%-25s \t$%-10s \t$%-12s \t%-15s\n", itemName, rPrice, bPrice, dateString);

        return lineItem;
    }

    public double getBestPrice() {
        return originalPrice;
    }

    public void setBestPrice(double recentPrice) {
        if(this.lastPrice > recentPrice) {
            this.lowerPrice = true;
            this.setDateOfLastPrice(new Date());
        }
        if(dateOfLastPrice == null)
            this.setDateOfLastPrice(new Date());
        this.lastPrice = recentPrice;
    }

    public double getRecentPrice() {
        return recentPrice;
    }

    public void setRecentPrice(double recentPrice) {
        setBestPrice(recentPrice);
        this.recentPrice = recentPrice;
        if(firstUpdate){
            this.setOriginalPrice(recentPrice);
            this.firstUpdate = false;
        }
    }

    public String getItemURL() {
        return itemURL;
    }

    public void setItemURL(String itemURL) {
        this.itemURL = itemURL;
    }

    public Date getDateOfLastPrice() {
        return  dateOfLastPrice;
    }

    public void setDateOfLastPrice(Date dateOfBestPrice) {
        this. dateOfLastPrice = dateOfBestPrice;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public boolean isLowerPrice() {
        return lowerPrice;
    }

    public void setLowerPrice(boolean lowerPrice) {
        this.lowerPrice = lowerPrice;
    }

    public Parameters getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Parameters queryParams) {
        this.queryParams = queryParams;
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }
}
