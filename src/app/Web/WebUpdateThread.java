package app.Web;

import app.FileIO.StatusLogger;
import app.Model.ItemList;
import app.Model.WatchedItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * Created by michael.gardanier on 6/13/17.
 */
public class WebUpdateThread implements Runnable {

    @Override
    public void run() {
        while(!ItemList.getInstance().isUpToDate()){
            WatchedItem item = ItemList.getInstance().getNextWatchedItem();
            try {
                item.setRecentPrice(getUpdatedPrice(item));
            } catch (IOException e) {
                StatusLogger.getInstance().logError("Error updating price for: " + item.getItemName());
            }
            ItemList.getInstance().addItem(item);
        }
    }


    public double getUpdatedPrice(WatchedItem inputItem) throws IOException {
        Document doc = Jsoup.connect(inputItem.getItemURL()).get();
        String priceStr;
        switch (inputItem.getQueryParams().getSelector()) {
            case CLASS:
                priceStr = selectOnClass(doc, inputItem.getQueryParams().getSelectorValue());
                break;
            case ID:
                priceStr = selectOnID(doc, inputItem.getQueryParams().getSelectorValue());
                break;
            default:
                return -1;
        }
        try {
            if(priceStr == null)
                return -1;
            priceStr = priceStr.replaceAll("[$]", "");
            priceStr = priceStr.replaceAll("[^\\d.,]", "");
            return parsePriceToDouble(priceStr);
        } catch (ParseException e) {
            StatusLogger.getInstance().logError("Price parsing error");
            return -1;
        }
    }

    public double parsePriceToDouble(String priceStr) throws ParseException {
        NumberFormat format = NumberFormat.getInstance(Locale.US);
        Number number = format.parse(priceStr);
        return number.doubleValue();
    }

    private String selectOnClass(Document doc, String className){

        Elements els = doc.select("[class=" + className);
        if(els.size() < 1)
            return null;
        return els.get(0).ownText();
    }

    private String selectOnID(Document doc, String id){
        Elements els = doc.select("[id=" + id);
        if(els.size() < 1)
            return null;
        return els.get(0).ownText();
    }

}
