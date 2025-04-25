package com.example.application.views.measurements;

import com.example.application.data.Measurement;
import com.example.application.data.Person;
import com.example.application.services.MeasurementService;
import com.example.application.services.PersonService;

import com.example.application.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
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
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

/**
 * CRUD-näkymä mittauksille.
 */
@PageTitle("Mittaukset")
@Route(value = "measurements", layout = MainLayout.class)
@Menu(order = 2, icon = LineAwesomeIconUrl.RULER_VERTICAL_SOLID)   // näkyy sivupalkissa
@Uses(Icon.class)
public class MeasurementView extends Div {

    private final Grid<Measurement> grid = new Grid<>(Measurement.class, false);

    /* ---------- Lomake-kentät ---------- */
    private ComboBox<Person>   person;
    private NumberField        heightCm;
    private NumberField        weightKg;
    private DateTimePicker     measuredAt;

    private final Button cancel = new Button("Peru");
    private final Button save   = new Button("Tallenna");

    private final BeanValidationBinder<Measurement> binder;
    private Measurement measurement;

    private final MeasurementService measurementService;
    private final PersonService      personService;

    public MeasurementView(MeasurementService measurementService,
                           PersonService personService) {

        this.measurementService = measurementService;
        this.personService      = personService;

        addClassName("measurement-view");

        SplitLayout split = new SplitLayout();
        createGrid(split);
        createEditor(split);
        add(split);

        binder = new BeanValidationBinder<>(Measurement.class);
        binder.bindInstanceFields(this);

        grid.asSingleSelect().addValueChangeListener(e -> {
            measurement = e.getValue();
            binder.readBean(measurement);
        });

        cancel.addClickListener(e -> clearForm());
        save.addClickListener(e -> saveCurrent());
    }

    /* ---------- GRID ---------- */
    private void createGrid(SplitLayout split) {
        grid.addColumn(createPersonRenderer()).setHeader("Henkilö").setAutoWidth(true);
        grid.addColumn("heightCm").setHeader("Pituus (cm)");
        grid.addColumn("weightKg").setHeader("Paino (kg)");
        grid.addColumn("measuredAt").setHeader("Mitattu");

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        grid.setItems(query ->
                measurementService
                        .list(PageRequest.of(query.getPage(), query.getPageSize()))
                        .stream());

        Div wrapper = new Div(grid);
        wrapper.setClassName("grid-wrapper");
        split.addToPrimary(wrapper);
    }

    private Renderer<Measurement> createPersonRenderer() {
        return new TextRenderer<>(m ->
                m.getPerson() != null
                        ? m.getPerson().getFirstName() + " " + m.getPerson().getLastName()
                        : "");
    }

    /* ---------- LOMAKE ---------- */
    private void createEditor(SplitLayout split) {
        Div editorWrap = new Div();
        editorWrap.setClassName("editor-layout");

        FormLayout form = new FormLayout();

        person     = new ComboBox<>("Henkilö");
        person.setItems(personService.findAll());
        person.setItemLabelGenerator(p -> p.getFirstName() + " " + p.getLastName());

        heightCm   = new NumberField("Pituus (cm)");
        heightCm.setStep(0.1);

        weightKg   = new NumberField("Paino (kg)");
        weightKg.setStep(0.1);

        measuredAt = new DateTimePicker("Mitattu");

        form.add(person, heightCm, weightKg, measuredAt);
        editorWrap.add(form, createButtons());

        split.addToSecondary(editorWrap);
    }

    private HorizontalLayout createButtons() {
        HorizontalLayout hl = new HorizontalLayout();
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        hl.add(save, cancel);
        return hl;
    }

    private void clearForm() {
        measurement = null;
        binder.readBean(null);
        grid.deselectAll();
    }

    private void saveCurrent() {
        try {
            if (measurement == null) measurement = new Measurement();

            binder.writeBean(measurement);
            measurementService.save(measurement);

            Notification.show("Tallennettu", 2000, Position.TOP_CENTER);
            clearForm();
            grid.getDataProvider().refreshAll();
        } catch (ObjectOptimisticLockingFailureException e) {
            Notification n = Notification.show("Samanaikainen muokkausvirhe");
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (ValidationException e) {
            Notification.show("Tarkista syötetyt arvot");
        }
    }
}
