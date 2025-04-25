package com.example.application.views.login;

import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Yksinkertainen kirjautumisnäkymä, jota Spring Security käyttää.
 *
 *  – @AnonymousAllowed  → sivu on aina julkinen
 *  – Implements BeforeEnterObserver → voimme näyttää virheilmoituksen, jos
 *    Spring Security on jo vaihtanut URL:ään ?error
 */
@Route("/login")
@PageTitle("Login")
@AnonymousAllowed
public class LoginView extends LoginOverlay implements BeforeEnterObserver {

    public LoginView() {
        setAction("login");           // Spring Securityn oletus POST-endpoint

        // (Valinnainen)‐Kielellinen mukautus
        LoginI18n i18n = LoginI18n.createDefault();
        i18n.getForm().setTitle("Vaadin-demo");
        i18n.getForm().setUsername("Käyttäjätunnus");
        i18n.getForm().setPassword("Salasana");
        i18n.getForm().setSubmit("Kirjaudu");
        i18n.getErrorMessage().setTitle("Virheellinen tunnus tai salasana");
        i18n.getErrorMessage().setMessage("Tarkista tunnus ja yritä uudelleen.");
        setI18n(i18n);

        setForgotPasswordButtonVisible(false); // ei “Forgot password?”-linkkiä
        setOpened(true);                       // näytetään heti
    }

    /** Näytetään virheilmoitus, jos URL:ssä on ?error= */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation().getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            setError(true);
        }
    }
}
