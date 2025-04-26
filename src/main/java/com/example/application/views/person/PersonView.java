package com.example.application.views.person;

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
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Optional;

@PageTitle("Persons")
@Route(value = "persons", layout = MainLayout.class)
@RouteAlias(value = "persons/:personID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed({"USER","ADMIN"})
@Uses(Icon.class)
public class PersonView extends Div implements BeforeEnterObserver {

    private static final String PERSON_ID       = "personID";
    private static final String PERSON_EDIT_URL = "persons/%s/edit";

    private final TextField      lastNameFilter = new TextField();
    private final Select<String> genderFilter   = new Select<>();

    private final Grid<Person>   grid = new Grid<>(Person.class, false);

    private TextField    firstName;
    private TextField    lastName;
    private TextField    email;
    private IntegerField ageField;
    private Select<String> gender;
    private DatePicker   dateOfBirth;

    private final Button cancel = new Button("Cancel");
    private final Button save   = new Button("Save");

    private BeanValidationBinder<Person> binder;
    private Person currentPerson;

    private final PersonService personService;

    public PersonView(PersonService personService) {
        this.personService = personService;
        addClassName("person-view");

        // Filter toolbar
        HorizontalLayout filters = new HorizontalLayout();
        lastNameFilter.setPlaceholder("Search by last name...");
        genderFilter.setItems("", "M", "F", "U");
        genderFilter.setPlaceholder("Gender");
        filters.add(lastNameFilter, genderFilter);
        add(filters);

        // Main split layout
        SplitLayout split = new SplitLayout();
        createGridLayout(split);
        createEditorLayout(split);
        add(split);

        // Grid configuration
        grid.addColumn(Person::getFirstName).setHeader("First Name").setAutoWidth(true);
        grid.addColumn(Person::getLastName).setHeader("Last Name").setAutoWidth(true);
        grid.addColumn(Person::getEmail).setHeader("Email").setAutoWidth(true);
        grid.addColumn(Person::getAge).setHeader("Age").setAutoWidth(true);
        grid.addColumn(Person::getGender).setHeader("Gender").setAutoWidth(true);
        // Delete action column
        grid.addComponentColumn(person -> {
            Button delete = new Button("Delete", e -> {
                personService.delete(person.getId());
                clearForm();
                refreshGrid();
                Notification.show("Deleted", 2000, Position.TOP_CENTER);
            });
            delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
            return delete;
        }).setHeader("Actions");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addColumn(Person::getFirstName).setHeader("First Name").setAutoWidth(true);
        grid.addColumn(Person::getLastName).setHeader("Last Name").setAutoWidth(true);
        grid.addColumn(Person::getEmail).setHeader("Email").setAutoWidth(true);
        grid.addColumn(Person::getAge).setHeader("Age").setAutoWidth(true);
        grid.addColumn(Person::getGender).setHeader("Gender").setAutoWidth(true);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setItems(query ->
                personService.listPersons(
                        VaadinSpringDataHelpers.toSpringPageRequest(query),
                        lastNameFilter.getValue(),
                        genderFilter.isEmpty() ? null : genderFilter.getValue()
                ).stream()
        );
        grid.asSingleSelect().addValueChangeListener(event -> {
            Person selected = event.getValue();
            if (selected != null) {
                currentPerson = selected;
                binder.readBean(currentPerson);
                UI.getCurrent().navigate(String.format(PERSON_EDIT_URL, selected.getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(PersonView.class);
            }
        });

        // Binder and field binding
        binder = new BeanValidationBinder<>(Person.class);
        binder.bindInstanceFields(this);
        // Manual binding for age field
        binder.forField(ageField)
                .bind(Person::getAge, (person, age) -> {});

        // Buttons
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        cancel.addClickListener(e -> {
            clearForm();
        });

        save.addClickListener(e -> {
            try {
                if (currentPerson == null) {
                    currentPerson = new Person();
                }
                binder.writeBean(currentPerson);
                personService.save(currentPerson);
                Notification.show("Saved", 2500, Position.BOTTOM_START);
                clearForm();
                grid.getDataProvider().refreshAll();
                UI.getCurrent().navigate(PersonView.class);
            } catch (ObjectOptimisticLockingFailureException ex) {
                Notification n = Notification.show("Save failed: concurrent modification error");
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException ex) {
                Notification.show("Please check the entered values");
            }
        });

        lastNameFilter.addValueChangeListener(e -> grid.getDataProvider().refreshAll());
        genderFilter.addValueChangeListener(e -> grid.getDataProvider().refreshAll());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> optionalId = event.getRouteParameters().get(PERSON_ID).map(Long::parseLong);
        if (optionalId.isPresent()) {
            personService.get(optionalId.get()).ifPresentOrElse(
                    person -> {
                        currentPerson = person;
                        binder.readBean(person);
                    },
                    () -> {
                        Notification.show("Person not found (ID=" + optionalId.get() + ")", 3000, Position.BOTTOM_START);
                        refreshGrid();
                        event.forwardTo(PersonView.class);
                    }
            );
        }
    }

    private void createEditorLayout(SplitLayout split) {
        Div editor = new Div();
        editor.addClassName("editor-layout");
        FormLayout form = new FormLayout();

        firstName = new TextField("First Name");
        lastName  = new TextField("Last Name");
        email     = new TextField("Email");
        ageField  = new IntegerField("Age");
        ageField.setReadOnly(true);
        gender    = new Select<>();
        gender.setItems("M", "F", "U");
        gender.setLabel("Gender");
        dateOfBirth = new DatePicker("Date of Birth");

        form.add(firstName, lastName, email, ageField, gender, dateOfBirth);
        editor.add(form, new HorizontalLayout(save, cancel));
        split.addToSecondary(editor);
    }

    private void createGridLayout(SplitLayout split) {
        Div wrapper = new Div(grid);
        wrapper.addClassName("grid-wrapper");
        split.addToPrimary(wrapper);
    }

    private void clearForm() {
        currentPerson = null;
        binder.readBean(null);
        grid.deselectAll();
        refreshGrid();
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    private void populateForm(Person person) {
        currentPerson = person;
        binder.readBean(person);
    }
}
