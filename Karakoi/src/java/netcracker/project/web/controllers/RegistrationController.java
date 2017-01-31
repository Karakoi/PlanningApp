package netcracker.project.web.controllers;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.event.ValueChangeEvent;
import netcracker.project.web.roles.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@ManagedBean(eager = true)
@RequestScoped
public class RegistrationController implements Serializable {

    @Autowired
    @Qualifier("user")
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    //<editor-fold defaultstate="collapsed" desc="отримуємо значення полів зі сторінки реєстрації">
    public void firstNameChanged(ValueChangeEvent e) {
        getUser().setFirstName(e.getNewValue().toString());
    }

    public void lastNameChanged(ValueChangeEvent e) {
        getUser().setFirstName(e.getNewValue().toString());
    }

    public void ageChanged(ValueChangeEvent e) {
        getUser().setFirstName(e.getNewValue().toString());
    }

    public void emailChanged(ValueChangeEvent e) {
        getUser().setFirstName(e.getNewValue().toString());
    }

    public void addressChanged(ValueChangeEvent e) {
        getUser().setFirstName(e.getNewValue().toString());
    }

    public void phoneChanged(ValueChangeEvent e) {
        getUser().setFirstName(e.getNewValue().toString());
    }

    public void loginChanged(ValueChangeEvent e) {
        getUser().setFirstName(e.getNewValue().toString());
    }

    public void passwordChanged(ValueChangeEvent e) {
        getUser().setFirstName(e.getNewValue().toString());
    }
    //</editor-fold>


}
