package com.example.application.views.henkilöidenmittaustiedot;

import com.example.application.data.Person;
import com.example.application.services.PersonService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.annotation.security.PermitAll;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Optional;

@PageTitle("Persons")
@Route(value = "persons", layout = MainLayout.class)
@RouteAlias(value = "persons/:personID?/:action?(edit)", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class HenkiloidenmittaustiedotView extends Div implements BeforeEnterObserver {

    /* ------------- konstanssit ------------- */
    private static final String PERSON_ID       = "personID";
    private static final String PERSON_EDIT_URL = "persons/%s/edit";

    /* ------------- filterit ---------------- */
    private final TextField      lastNameFilter = new TextField();
    private final Select<String> genderFilter   = new Select<>();

    /* ------------- grid + lomake ----------- */
    private final Grid<Person> grid = new Grid<>(Person.class, false);

    private TextField  firstName;
    private TextField  lastName;
    private TextField  email;
    private NumberField age;
    private Select<String> gender;
    private DatePicker dateOfBirth;

    private final Button cancel = new Button("Peru");
    private final Button save   = new Button("Tallenna");

    private final BeanValidationBinder<Person> binder;
    private Person currentPerson;

    private final PersonService personService;

    /* =================================================================== */
    public HenkiloidenmittaustiedotView(PersonService personService) {
        this.personService = personService;
        addClassName("henkilöidenmittaustiedot-view");

        /* ---------- FILTER-TOOLBAR ---------- */
        HorizontalLayout filters = new HorizontalLayout();
        lastNameFilter.setPlaceholder("Hae sukunimellä…");
        genderFilter.setItems("", "M", "F", "U");
        genderFilter.setPlaceholder("Sukupuoli");
        filters.add(lastNameFilter, genderFilter);
        add(filters);

        /* ---------- päälayout ---------- */
        SplitLayout split = new SplitLayout();
        createGridLayout(split);
        createEditorLayout(split);
        add(split);

        /* ---------- grid ---------- */
        grid.addColumn(Person::getFirstName).setHeader("Etunimi").setAutoWidth(true);
        grid.addColumn(Person::getLastName).setHeader("Sukunimi").setAutoWidth(true);
        grid.addColumn(Person::getEmail).setHeader("Sähköposti").setAutoWidth(true);
        grid.addColumn(Person::getAge).setHeader("Ikä").setAutoWidth(true);
        grid.addColumn(Person::getGender).setHeader("Sukupuoli").setAutoWidth(true);

        grid.setItems(q -> personService
                .listPersons(
                        VaadinSpringDataHelpers.toSpringPageRequest(q),
                        lastNameFilter.getValue(),
                        genderFilter.isEmpty() ? null : genderFilter.getValue())
                .stream());

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        grid.asSingleSelect().addValueChangeListener(ev -> {
            if (ev.getValue() != null) {
                UI.getCurrent().navigate(String.format(PERSON_EDIT_URL, ev.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(HenkiloidenmittaustiedotView.class);
            }
        });

        /* ---------- binder ---------- */
        binder = new BeanValidationBinder<>(Person.class);

        age.setReadOnly(true);
        binder.forField(age)                                    // ← FIX (Double vs int)
                .bind(p -> Double.valueOf(p.getAge()), (p, v) -> {});

        /* ---------- napit ---------- */
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (currentPerson == null) currentPerson = new Person();
                binder.writeBean(currentPerson);
                personService.savePerson(currentPerson);
                clearForm();
                refreshGrid();
                Notification.show("Tallennettu", 2500, Position.BOTTOM_START);
                UI.getCurrent().navigate(HenkiloidenmittaustiedotView.class);
            } catch (ObjectOptimisticLockingFailureException ex) {
                Notification n = Notification.show("Tallennus epäonnistui: tietue muuttui jo toisella käyttäjällä.");
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException ex) {
                Notification.show("Tarkista syötetyt arvot");
            }
        });

        /* ---------- filter-kuuntelijat ---------- */
        lastNameFilter.addValueChangeListener(e -> grid.getDataProvider().refreshAll());
        genderFilter.addValueChangeListener(e -> grid.getDataProvider().refreshAll());
    }

    /* ---------------- URL-parametrit ---------------- */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> id = event.getRouteParameters().get(PERSON_ID).map(Long::parseLong);
        if (id.isPresent()) {
            personService.findPerson(id.get())
                    .ifPresentOrElse(this::populateForm,
                            () -> {
                                Notification.show("Henkilöä ei löytynyt (ID=" + id.get() + ")", 3000,
                                        Position.BOTTOM_START);
                                refreshGrid();
                                event.forwardTo(HenkiloidenmittaustiedotView.class);
                            });
        }
    }

    /* ---------------- UI-helperit ------------------ */
    private void createEditorLayout(SplitLayout split) {
        Div editor = new Div();
        editor.addClassName("editor-layout");

        FormLayout form = new FormLayout();
        firstName   = new TextField("Etunimi");
        lastName    = new TextField("Sukunimi");
        email       = new TextField("Sähköposti");
        age         = new NumberField("Ikä");

        gender      = new Select<>();                     // ← FIX (tyypin päättely)
        gender.setItems("M", "F", "U");
        gender.setLabel("Sukupuoli");

        dateOfBirth = new DatePicker("Syntymäpäivä");

        form.add(firstName, lastName, email, age, gender, dateOfBirth);
        editor.add(form, new HorizontalLayout(save, cancel));

        split.addToSecondary(editor);
    }

    private void createGridLayout(SplitLayout split) {
        Div wrapper = new Div(grid);
        wrapper.addClassName("grid-wrapper");
        split.addToPrimary(wrapper);
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(Person p) {
        currentPerson = p;
        binder.readBean(currentPerson);
    }
}
