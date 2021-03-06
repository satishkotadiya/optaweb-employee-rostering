/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.employeerostering.gwtui.client.common;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.TakesValue;
import elemental2.dom.Event;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLInputElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Templated
public class LocalDateTimePicker implements TakesValue<LocalDateTime>,
                                            HasValueChangeHandlers<LocalDateTime> {

    @Inject
    @DataField("date-time-picker-text-field")
    private HTMLInputElement datePickerTextField;

    @Inject
    @DataField("date-time-picker")
    private HTMLDivElement datePickerElement;

    private Collection<ValueChangeHandler<LocalDateTime>> valueChangeHandlers;

    private BootstrapDateTimePicker datePicker;

    private LocalDateTime currentValue;

    private static final String DATE_FORMAT_STRING = "LLLL";

    @PostConstruct
    public void init() {
        valueChangeHandlers = new HashSet<>();
        datePicker = BootstrapDateTimePicker.get(datePickerElement);
        BootstrapDateTimePicker.BootstrapDateTimePickerOptions options = new BootstrapDateTimePicker.BootstrapDateTimePickerOptions();
        options.format = DATE_FORMAT_STRING;
        options.showTodayButton = true;
        options.allowInputToggle = true;
        options.toolbarPlacement = "bottom";
        options.sideBySide = true;
        BootstrapDateTimePicker.BootstrapDateTimePickerIcons icons = new BootstrapDateTimePicker.BootstrapDateTimePickerIcons();
        icons.today = "today-button-pf";
        options.icons = icons;
        datePicker.datetimepicker(options);
        datePicker.on("dp.show", () -> {
            JQuery.get(datePickerElement).children(".bootstrap-datetimepicker-widget").css("height", "min-content").css("margin-bottom", "-100%")
                    .css("bottom", "unset");
        });
        datePicker.on("dp.change", () -> {
            String value = datePickerTextField.value;
            LocalDateTime oldValue = getValue();
            if (value != null && !value.isEmpty()) {
                setValue(LocalDateTime.parse(MomentJs.moment(value, DATE_FORMAT_STRING).format("YYYY-MM-DDTHH:mm")));
            } else {
                setValue(null);
            }
            ValueChangeEvent.fireIfNotEqual(this, oldValue, getValue());
        });
    }

    public boolean reportValidity() {
        return datePickerTextField.reportValidity();
    }

    @Override
    public void setValue(LocalDateTime value) {
        if (currentValue == null || !currentValue.equals(value)) {
            currentValue = value;
            if (value != null) {
                ((BootstrapDateTimePicker.BootstrapDateTimePickerData) datePicker.data("DateTimePicker"))
                        .date(MomentJs.moment(value.toString(), "YYYY-MM-DDTHH:mm")
                                      .toDate());
            } else {
                ((BootstrapDateTimePicker.BootstrapDateTimePickerData) datePicker.data("DateTimePicker"))
                        .date((String) null);
            }
        }
    }

    @Override
    public LocalDateTime getValue() {
        return currentValue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void fireEvent(GwtEvent<?> event) {
        if (event.getAssociatedType().equals(ValueChangeEvent.getType())) {
            valueChangeHandlers.forEach(h -> h.onValueChange((ValueChangeEvent<LocalDateTime>) event));
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<LocalDateTime> handler) {
        valueChangeHandlers.add(handler);
        return () -> valueChangeHandlers.remove(handler);
    }

    @EventHandler("date-time-picker")
    public void onDateChange(@ForEvent("change") Event e) {
        ValueChangeEvent.fire(this, getValue());
    }
}
