package ro.nn.qa.business;

import ro.nn.qa.business.f4.BillingFreqF4;
import ro.nn.qa.business.f4.LocateClientF4;

/**
 * Created by Alexandru Giurovici on 18.09.2015.
 */
public class NewEndowmentX1 extends BusinessObjectX
{
    public NewEndowmentX1(BusinessObjectX owner)
    {
        this.screen = owner.getScreen();
    }

    public NewContractProposalX back() throws InterruptedException
    {
        f3();
        f3();
        return new NewContractProposalX(this);
    }

    public void setContractOwner(String search) throws InterruptedException
    {
        f4();
        LocateClientF4 client = new LocateClientF4(this);
        client.search(search, 2);
        client.submit();
        f5();
    }

    public void setRiskCommDate(String date) throws InterruptedException
    {
        send(date);
        f5();
    }

    public void setBillingFreq(String freq) throws InterruptedException
    {
        f4();
        BillingFreqF4 billingFreq = new BillingFreqF4(this);
        billingFreq.search(freq, 1);
        billingFreq.submit();
        f5();
        // don't know why F5 doesn't advance to payment method
        tab(1);
    }

    public void setPaymentMethod(String method) throws InterruptedException
    {
        send(method);
        f5();
    }

    public void setSerialNumber(String arg1) throws InterruptedException
    {
        send(arg1);
        f5();
    }

    public void setAgentBySearch(String arg1) throws InterruptedException
    {
        f4();
        LocateClientF4 agent = new LocateClientF4(this);
        agent.search(arg1, 2);
        agent.submit();
        f5();
    }

    public void setAgentById(String arg1) throws InterruptedException
    {
        send(arg1);
        f5();
    }
    public NewEndowmentX2 next() throws InterruptedException {
        enter();
        return new NewEndowmentX2(this);
    }

    // Additional methods for comprehensive endowment testing
    public void setEndowmentAmount(String amount) throws InterruptedException {
        send(amount);
        f5();
    }

    public void setBeneficiary(String beneficiary) throws InterruptedException {
        send(beneficiary);
        f5();
    }

    public void setTerm(String term) throws InterruptedException {
        send(term);
        f5();
    }

    public void setInvestmentOption(String investment) throws InterruptedException {
        send(investment);
        f5();
    }

    public String getCalculatedPremium() throws InterruptedException {
        // Return calculated premium from screen
        return getField(10); // Assuming field 10 contains premium
    }

    public String getMaturityValue() throws InterruptedException {
        // Return maturity value from screen
        return getField(11); // Assuming field 11 contains maturity value
    }
}
