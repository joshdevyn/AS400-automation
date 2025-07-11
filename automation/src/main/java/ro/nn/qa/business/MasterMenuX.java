package ro.nn.qa.business;

import static java.lang.Thread.sleep;

/**
 * Created by Alexandru Giurovici on 18.09.2015.
 */
public class MasterMenuX extends BusinessObjectX
{
    public MasterMenuX(BusinessObjectX owner)
    {
        this.screen = owner.getScreen();
    }

    public ClientsAdminX getClientsMenu() throws InterruptedException {
        sleep(PAGE_DELAY);
        tab(2);
        enter();
        return new ClientsAdminX(this);
    }

    public NewBusinessMenuX getNewBusinessMenu() throws InterruptedException {
        sleep(PAGE_DELAY);
        tab(4);
        enter();
        return new NewBusinessMenuX(this);
    }
    
    public ClientsAdminX getClientsAdmin() throws InterruptedException {
        return getClientsMenu(); // Alias for consistency
    }

}
