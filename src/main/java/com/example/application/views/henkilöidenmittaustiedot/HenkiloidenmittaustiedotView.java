package com.example.application.views.henkilöidenmittaustiedot;

import com.example.application.data.Person;
import com.example.application.services.PersonService;
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
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import java.util.Optional;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Henkilöiden mittaustiedot")
@Route(value = "persons/:personID?/:action?(edit)")
@Menu(order = 1, icon = LineAwesomeIconUrl.USERS_SOLID)
@Uses(Icon.class)
public class HenkiloidenmittaustiedotView extends Div implements BeforeEnterObserver {

    /* --------------------------------------------------------------------- */
    /* --- route param constant & templates ------------------------------- */

    private static final String PERSON_ID = "personID";
    private static final String PERSON_EDIT_ROUTE_TEMPLATE = "persons/%s/edit";

    /* --------------------------------------------------------------------- */
    /* --- components ------------------------------------------------------ */

    private final Grid<Person> grid = new Grid<>(Person.class, false);

    private TextField firstName;
    private TextField lastName;
    private TextField email;
    private NumberField age;
    private Select<String> gender;
    private DatePicker dateOfBirth; // jos pidät tämän kentän

    private final Button cancel = new Button("Peru");
    private final Button save   = new Button("Tallenna");

    private final BeanValidationBinder<Person> binder;
    private Person currentPerson;

    /* --------------------------------------------------------------------- */
    /* --- backend service ------------------------------------------------- */

    private final PersonService personService;

    /* --------------------------------------------------------------------- */
    /* --- constructor ----------------------------------------------------- */

    public HenkiloidenmittaustiedotView(PersonService personService) {
        this.personService = personService;
        addClassName("henkilöidenmittaustiedot-view");

        /* ---------- layout ---------- */
        SplitLayout splitLayout = new SplitLayout();
        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);
        add(splitLayout);

        /* ---------- grid ---------- */
        grid.addColumn(Person::getFirstName).setHeader("Etunimi").setAutoWidth(true);
        grid.addColumn(Person::getLastName).setHeader("Sukunimi").setAutoWidth(true);
        grid.addColumn(Person::getEmail).setHeader("Sähköposti").setAutoWidth(true);
        grid.addColumn(Person::getAge).setHeader("Ikä").setAutoWidth(true);
        grid.addColumn(Person::getGender).setHeader("Sukupuoli").setAutoWidth(true);

        grid.setItems(q ->
                personService.listPersons(VaadinSpringDataHelpers.toSpringPageRequest(q)).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        grid.asSingleSelect().addValueChangeListener(e -> {
            if (e.getValue() != null) {
                UI.getCurrent().navigate(String.format(PERSON_EDIT_ROUTE_TEMPLATE, e.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(HenkiloidenmittaustiedotView.class);
            }
        });

        /* ---------- binder ---------- */
        binder = new BeanValidationBinder<>(Person.class);
        binder.bindInstanceFields(this);

        /* ---------- buttons ---------- */
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (currentPerson == null) {
                    currentPerson = new Person();
                }
                binder.writeBean(currentPerson);
                personService.savePerson(currentPerson);
                clearForm();
                refreshGrid();
                Notification.show("Tallennettu", 2500, Position.BOTTOM_START);
                UI.getCurrent().navigate(HenkiloidenmittaustiedotView.class);
            } catch (ObjectOptimisticLockingFailureException ex) {
                Notification n = Notification.show(
                        "Tallennus epäonnistui: tietue muuttui jo toisella käyttäjällä.");
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException ex) {
                Notification.show("Tarkista syötetyt arvot");
            }
        });
    }

    /* --------------------------------------------------------------------- */
    /* --- beforeEnter: haetaan URL-parametrin mukainen henkilö ------------ */

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> id = event.getRouteParameters().get(PERSON_ID).map(Long::parseLong);
        if (id.isPresent()) {
            Optional<Person> p = personService.findPerson(id.get());
            if (p.isPresent()) {
                populateForm(p.get());
            } else {
                Notification.show("Henkilöä ei löytynyt (ID=" + id.get() + ")", 3000,
                        Position.BOTTOM_START);
                refreshGrid();
                event.forwardTo(HenkiloidenmittaustiedotView.class);
            }
        }
    }

    /* --------------------------------------------------------------------- */
    /* --- UI helpers ------------------------------------------------------ */

    private void createEditorLayout(SplitLayout split) {
        Div editorLayout = new Div();
        editorLayout.addClassName("editor-layout");

        FormLayout form = new FormLayout();
        firstName   = new TextField("Etunimi");
        lastName    = new TextField("Sukunimi");
        email       = new TextField("Sähköposti");
        age         = new NumberField("Ikä");
        age.setStep(1);
        age.setMin(0);
        gender      = new Select<>();
        gender.setLabel("Sukupuoli");
        gender.setItems("M", "N", "Jokin muu");
        dateOfBirth = new DatePicker("Syntymäpäivä");

        form.add(firstName, lastName, email, age, gender, dateOfBirth);
        editorLayout.add(form);

        HorizontalLayout buttons = new HorizontalLayout(save, cancel);
        buttons.addClassName("button-layout");
        editorLayout.add(buttons);

        split.addToSecondary(editorLayout);
    }

    private void createGridLayout(SplitLayout split) {
        Div wrapper = new Div();
        wrapper.addClassName("grid-wrapper");
        wrapper.add(grid);
        split.addToPrimary(wrapper);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(Person value) {
        currentPerson = value;
        binder.readBean(currentPerson);
    }
}
