package netcracker.project.web.controllers;

import java.io.Serializable;
import java.util.Locale;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.springframework.stereotype.Component;

@Component
@ManagedBean(eager=true)
@SessionScoped
public class LocaleChanger implements Serializable {
           
    private Locale currentLocale = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();    
    
    public LocaleChanger() {
    }

    public void changeLocale(String localeCode) {
        currentLocale = new Locale(localeCode);
    }

    public Locale getCurrentLocale() {
        if(currentLocale == null) currentLocale = Locale.ENGLISH;
      return currentLocale;
    }


}
