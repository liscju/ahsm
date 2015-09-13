package attatrol.ahsm;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO that holds errors and status of a site
 * @author atta_troll
 *
 */
public class SiteReport {
  
  private boolean isValid = true;
  
  private List<String> notifications = new ArrayList<>();
  
  public boolean isValid() {
    return isValid;
  }
  public void setValid(boolean isValid) {
    this.isValid = isValid;
  }
  public List<String> getNotifications() {
    return notifications;
  }
  public void setNotification(String notification) {
    this.notifications.add(notification);
  }
      
}