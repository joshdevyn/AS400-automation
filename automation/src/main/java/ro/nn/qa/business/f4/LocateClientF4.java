package ro.nn.qa.business.f4;

import ro.nn.qa.business.BusinessObjectX;
import ro.nn.qa.business.f4.BusinessObjectF4;

/**
 * Created by Alexandru Giurovici on 18.09.2015.
 */
public class LocateClientF4 extends BusinessObjectF4
{
    public LocateClientF4(BusinessObjectX own)
    {
        super(own);
    }
    
    // Additional methods for comprehensive testing
    public void searchClient(String clientName) throws InterruptedException {
        search(clientName, 2);
    }
    
    public void searchById(String clientId) throws InterruptedException {
        send(clientId, 0);
        enter();
        tab(1);
        send("1", 0);
    }
    
    public void selectFirstResult() throws InterruptedException {
        tab(1);
        send("1", 0);
        enter();
    }
}
