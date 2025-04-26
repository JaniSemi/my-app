package com.example.application.views;

import com.example.application.views.home.HomeView;
import com.example.application.views.henkilöidenmittaustiedot.HenkiloidenmittaustiedotView;
import com.example.application.views.measurements.MeasurementView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;

/** Sovelluksen päälayout – vasemman laidan navigaatio + otsikkopalkki. */
@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout {

    private final H1 viewTitle = new H1();

    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        addHeaderContent();
        addDrawerContent();
    }

    /* ------------------------------------------------------------------ */
    /* HEADER                                                             */
    /* ------------------------------------------------------------------ */
    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    /* ------------------------------------------------------------------ */
    /* DRAWER / NAVIGAATIO                                                */
    /* ------------------------------------------------------------------ */
    private void addDrawerContent() {
        Span appName = new Span("Measurement-App");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    /** Luo vasemman reunan SideNav. */
    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        /*  --- Huom! SideNavItem kannattaa luoda merkkijono-reitillä,
             koska lista-näkymä ja editori jakavat saman luokan   --- */

        nav.addItem(new SideNavItem("Home", HomeView.class, icon(VaadinIcon.HOME)));

        //  persons – listan parametriton reitti
        nav.addItem(new SideNavItem("Persons", "persons", icon(VaadinIcon.USERS)));

        //  measurements – listan parametriton reitti
        nav.addItem(new SideNavItem("Measurements", "measurements",
                icon(VaadinIcon.LINE_CHART)));

        return nav;
    }

    /** Pieni apu ikonien luomiseen yhtenäisellä koolla. */
    private Component icon(VaadinIcon vIcon) {
        Icon icon = vIcon.create();
        icon.setSize("var(--lumo-icon-size-s)");
        return icon;
    }

    private Footer createFooter() {
        return new Footer();
    }

    /* ------------------------------------------------------------------ */
    /*  Näytettävän näkymän otsikko                                        */
    /* ------------------------------------------------------------------ */
    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title != null ? title.value() : "";
    }
}
