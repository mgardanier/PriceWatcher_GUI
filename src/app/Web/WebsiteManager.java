package app.Web;

import app.FileIO.StatusLogger;
import app.Model.ItemList;
import app.Model.Parameters;
import app.Model.WatchedItem;
import app.UserIO.IUserInputManager;
import app.UserIO.UserInputManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by michael.gardanier on 6/9/17.
 */
public class WebsiteManager implements IWebsiteManager {

    private final int MAX_THREADS = 20;

    @Override
    public Parameters getPriceParameters(WatchedItem item) {
        Map<String, Integer> priceElementMap = new HashMap<>();
        Elements allPossible;
        try {
            allPossible = this.getElements(item.getItemURL());
            new UserInputManager().displayToUser("\n---Please confirm the current price---\n\n");
            if(allPossible == null)
                return null;
            for(int i = 0; i < allPossible.size(); i++){
                if(i >= 15)
                    break;
                String text = allPossible.get(i).ownText();
                text = text.replaceAll("[^\\d.,]", "");
                if(text.equals("")) {
                    continue;
                }
                //Map price to position in list
                priceElementMap.put(text, i);
            }
        } catch (IOException e) {
            StatusLogger.getInstance().logError("URL doesn't exist");
            return null;
        }

        //iterate through map and display price while accessing elements based on index in list.
        for(Map.Entry<String, Integer> entry : priceElementMap.entrySet()){
            if(presentOptionToUser(entry.getKey())) {
                Parameters params = new Parameters();
                String value = allPossible.get(entry.getValue()).ownText();
                value = value.replaceAll("[^\\d.,]", "");
                params.setValue(value);
                String elementType = allPossible.get(entry.getValue()).tag().getName();
                params = fillInElementType(params, elementType);
                params = fillInElementSelector(params, allPossible.get(entry.getValue()));
                return params;
            }
        }

        return null;
    }

    private Parameters fillInElementSelector(Parameters inputParameters, Element element){
        Parameters outputParameters = new Parameters();
        outputParameters.setValue(inputParameters.getValue());
        outputParameters.setElementType(inputParameters.getElementType());

        if(!element.id().equals("")){
            outputParameters.setSelectorValue(element.id());
            outputParameters.setSelector(Parameters.Selector.ID);
        } else if(!element.className().equals("")){
            outputParameters.setSelectorValue(element.className());
            outputParameters.setSelector(Parameters.Selector.CLASS);
        } else {
            outputParameters.setSelector(Parameters.Selector.OTHER);
            StatusLogger.getInstance().logError("Unrecognized selector");
        }
        return outputParameters;
    }

    private Parameters fillInElementType(Parameters inputParams, String elementType){
        Parameters outputParams = new Parameters();
        outputParams.setValue(inputParams.getValue());
        if(elementType.equals("span")){
            outputParams.setElementType(Parameters.ElementType.SPAN);
        } else if(elementType.equals("p")){
            outputParams.setElementType(Parameters.ElementType.P);
        } else {
            outputParams.setElementType(Parameters.ElementType.OTHER);
            StatusLogger.getInstance().logError("Unrecognized element type: " + elementType);
        }
        return outputParams;
    }

    @Override
    public WatchedItem getItemInitial(WatchedItem inputItem) {
        Parameters priceParams = getPriceParameters(inputItem);
        if(priceParams != null){
            WatchedItem item = new WatchedItem(inputItem.getItemURL(), inputItem.getItemName());
            item.setQueryParams(priceParams);
            item.setDateOfLastPrice(new Date());
            return item;
        }
        return null;

    }

    @Override
    public void updateItemPrices() {
        ItemList.getInstance().setUpToDate(false);
        //Create threads
        int numThreads;
        if(ItemList.getInstance().getSize() > MAX_THREADS)
            numThreads = MAX_THREADS;
        else
            numThreads = ItemList.getInstance().getSize();
        //numThreads = 1;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        for(int i = 0; i < numThreads; i++){
            Runnable thread = new WebUpdateThread();
            executor.execute(thread);
        }

        executor.shutdown();
        while(!executor.isTerminated()){
            //Wait until all threads done executing
        }
    }

    private boolean presentOptionToUser(String price){
       IUserInputManager userInputManager = new UserInputManager();
       String response;
       while(true) {
           userInputManager.displayToUser("Is ( $" + price + " ) the current price (y/n)? \n% ");
           response = userInputManager.getUserInput();
           if (response.toLowerCase().equals("y") || response.toLowerCase().equals("n"))
               break;
           else
               userInputManager.displayToUser("\nInvalid response.Try again.\n");
       }
       return response.toLowerCase().equals("y");

    }

    private Elements getElements(String url) throws IOException {
        try {
            Document doc = Jsoup.connect(url).get();
            Elements prices = doc.select("[class*=price], [id*=price]");
            return prices;
        } catch (MalformedURLException | IllegalArgumentException e){
            StatusLogger.getInstance().logError("Bad URL");
            new UserInputManager().displayToUser("\n****Bad URL.***\n");
            return null;
        }
    }
}
