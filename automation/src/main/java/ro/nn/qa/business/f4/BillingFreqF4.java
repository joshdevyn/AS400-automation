package ro.nn.qa.business.f4;

import ro.nn.qa.business.BusinessObjectX;

/**
 * Created by Alexandru Giurovici on 18.09.2015.
 */
public class BillingFreqF4 extends BusinessObjectF4
{
    public BillingFreqF4(BusinessObjectX own)
    {
        super(own);
    }
    
    // Additional methods for comprehensive testing
    public void selectOption(String frequency) throws InterruptedException {
        // Map frequency codes: M=Monthly, Q=Quarterly, A=Annual
        search(frequency, 1);
    }
    
    public void selectMonthly() throws InterruptedException {
        selectOption("M");
    }
    
    public void selectQuarterly() throws InterruptedException {
        selectOption("Q");
    }
    
    public void selectAnnual() throws InterruptedException {
        selectOption("A");
    }
}
