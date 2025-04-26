package com.example.application.views;

import com.example.application.views.home.HomeView;
import com.example.application.views.person.PersonView;
import com.example.application.views.measurements.MeasurementView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.AppLayout.Section;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;

@AnonymousAllowed
public class MainLayout extends AppLayout {

    private final H1 viewTitle = new H1();

    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        addHeaderContent();
        addDrawerContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.NONE
        );
        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        Span appName = new Span("Measurement-App");
        appName.addClassNames(
                LumoUtility.FontWeight.SEMIBOLD,
                LumoUtility.FontSize.LARGE
        );
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        Footer footer = new Footer(new Span("© 2025 Measurement App"));
        footer.addClassNames(
                LumoUtility.Background.CONTRAST_5,
                LumoUtility.Padding.Vertical.SMALL
        );
        footer.getStyle()
                .set("color", "white")
                .set("text-align", "center");

        addToDrawer(header, scroller, footer);
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("Home", HomeView.class, icon(VaadinIcon.HOME)));
        nav.addItem(new SideNavItem("Persons", PersonView.class, icon(VaadinIcon.USERS)));
        nav.addItem(new SideNavItem("Measurements", MeasurementView.class, icon(VaadinIcon.LINE_CHART)));
        return nav;
    }

    private Component icon(VaadinIcon vIcon) {
        Icon icon = vIcon.create();
        icon.setSize("var(--lumo-icon-size-s)");
        return icon;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        var annotation = getContent().getClass().getAnnotation(com.vaadin.flow.router.PageTitle.class);
        return annotation != null ? annotation.value() : "";
    }
}
