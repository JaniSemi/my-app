package com.example.application.views.measurements;

import com.example.application.data.Measurement;
import com.example.application.data.Person;
import com.example.application.services.MeasurementService;
import com.example.application.services.PersonService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
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
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.annotation.security.PermitAll;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@PageTitle("Measurements")
@Route(value = "measurements", layout = MainLayout.class)
@RouteAlias(value = "measurements/:measurementID?", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class MeasurementView extends Div {

    private final Grid<Measurement> grid = new Grid<>(Measurement.class, false);

    /* -------- filter -------- */
    private final ComboBox<Person> personFilter = new ComboBox<>("Person");

    /* -------- form -------- */
    private ComboBox<Person> person;
    private NumberField      heightCm;
    private NumberField      weightKg;
    private DateTimePicker   measuredAt;

    private final Button cancel = new Button("Cancel");
    private final Button save   = new Button("Save");

    private final BeanValidationBinder<Measurement> binder;
    private Measurement current;

    private final MeasurementService measurementService;
    private final PersonService      personService;

    /* =================================================== */
    public MeasurementView(MeasurementService measurementService,
                           PersonService personService) {

        this.measurementService = measurementService;
        this.personService      = personService;
        addClassName("measurement-view");

        /* ---------- FILTER-TOOLBAR ---------- */
        HorizontalLayout filters = new HorizontalLayout();
        personFilter.setItems(personService.findAll());
        personFilter.setItemLabelGenerator(p -> p.getFirstName() + " " + p.getLastName());
        personFilter.setPlaceholder("Filter by person");
        filters.add(personFilter);
        add(filters);

        /* ---------- main layout ---------- */
        SplitLayout split = new SplitLayout();
        createGrid(split);
        createEditor(split);
        add(split);

        /* ---------- binder ---------- */
        binder = new BeanValidationBinder<>(Measurement.class);
        binder.bindInstanceFields(this);

        // person-ComboBox manual binding
        binder.forField(person)
                .asRequired("Please select a person")
                .bind(Measurement::getPerson, Measurement::setPerson);

        /* ---------- grid selection ---------- */
        grid.asSingleSelect().addValueChangeListener(e -> {
            current = e.getValue();
            binder.readBean(current);
        });

        /* ---------- buttons ---------- */
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        cancel.addClickListener(e -> clearForm());
        save.addClickListener(e -> saveCurrent());

        /* ---------- filter-listener ---------- */
        personFilter.addValueChangeListener(e -> grid.getDataProvider().refreshAll());
    }

    /* =================================================== */
    /* GRID */
    private void createGrid(SplitLayout split) {
        grid.addColumn(new TextRenderer<>(m ->
                        m.getPerson() != null
                                ? m.getPerson().getFirstName() + " " + m.getPerson().getLastName()
                                : ""))
                .setHeader("Person").setAutoWidth(true);

        grid.addColumn("heightCm").setHeader("Height (cm)");
        grid.addColumn("weightKg").setHeader("Weight (kg)");
        grid.addColumn("measuredAt").setHeader("Measured at");

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        grid.setItems(q -> measurementService
                .list(
                        VaadinSpringDataHelpers.toSpringPageRequest(q),
                        personFilter.getValue() != null ? personFilter.getValue().getId() : null
                ).stream());

        Div wrapper = new Div(grid);
        wrapper.addClassName("grid-wrapper");
        split.addToPrimary(wrapper);
    }

    /* =================================================== */
    /* EDITOR */
    private void createEditor(SplitLayout split) {
        Div editorWrap = new Div();
        editorWrap.addClassName("editor-layout");

        FormLayout form = new FormLayout();

        person = new ComboBox<>("Person");
        person.setItems(personService.findAll());
        person.setItemLabelGenerator(p -> p.getFirstName() + " " + p.getLastName());

        heightCm = new NumberField("Height (cm)");
        heightCm.setStep(0.1);

        weightKg = new NumberField("Weight (kg)");
        weightKg.setStep(0.1);

        measuredAt = new DateTimePicker("Measured at");

        form.add(person, heightCm, weightKg, measuredAt);
        editorWrap.add(form, createButtons());

        split.addToSecondary(editorWrap);
    }

    private HorizontalLayout createButtons() {
        HorizontalLayout hl = new HorizontalLayout(save, cancel);
        hl.addClassName("button-layout");
        return hl;
    }

    /* =================================================== */
    /* HELPERS */
    private void clearForm() {
        current = null;
        binder.readBean(null);
        grid.deselectAll();
    }

    private void saveCurrent() {
        try {
            if (current == null) current = new Measurement();

            binder.writeBean(current);
            measurementService.save(current);

            Notification.show("Saved", 2000, Position.TOP_CENTER);
            clearForm();
            grid.getDataProvider().refreshAll();
        } catch (ObjectOptimisticLockingFailureException ex) {
            Notification n = Notification.show("Concurrent modification error");
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (ValidationException ex) {
            Notification.show("Please check the entered values");
        }
    }
}