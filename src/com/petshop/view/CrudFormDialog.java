package com.petshop.view;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.VBox;
import javafx.concurrent.Task;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class CrudFormDialog<T> {
    public record FieldError(String field, String message) {
    }

    private final Dialog<Void> dialog = new Dialog<>();
    private final Map<String, Node> controls;
    private final Map<String, Label> errorLabels;
    private final Supplier<T> extractor;
    private final Function<T, List<FieldError>> validator;
    private final BooleanSupplier dirty;
    private final Consumer<T> submit;
    private final Label formError = new Label();
    private boolean saved;
    private boolean saving;

    public CrudFormDialog(
            String title,
            Node content,
            Map<String, Node> controls,
            Map<String, Label> errorLabels,
            Supplier<T> extractor,
            Function<T, List<FieldError>> validator,
            BooleanSupplier dirty,
            Consumer<T> submit) {
        this.controls = Map.copyOf(controls);
        this.errorLabels = Map.copyOf(errorLabels);
        this.extractor = Objects.requireNonNull(extractor);
        this.validator = Objects.requireNonNull(validator);
        this.dirty = Objects.requireNonNull(dirty);
        this.submit = Objects.requireNonNull(submit);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        ButtonType save = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(save, ButtonType.CANCEL);
        formError.getStyleClass().add("feedback-error");
        formError.setWrapText(true);
        VBox wrapper = new VBox(10, content, formError);
        dialog.getDialogPane().setContent(wrapper);
        dialog.getDialogPane().setMinWidth(430);
        dialog.getDialogPane().lookupButton(save).addEventFilter(
                ActionEvent.ACTION, event -> save(event));
        dialog.setOnCloseRequest(event -> {
            if (!saved && (saving
                    || dirty.getAsBoolean() && !ConfirmDialog.confirmCancelForm())) {
                event.consume();
            }
        });
    }

    public void show() {
        dialog.showAndWait();
    }

    private void save(ActionEvent event) {
        clearErrors();
        T item;
        try {
            item = extractor.get();
        } catch (RuntimeException exception) {
            formError.setText(exception.getMessage() == null
                    ? "Dữ liệu nhập không hợp lệ." : exception.getMessage());
            event.consume();
            return;
        }
        List<FieldError> errors = validator.apply(item);
        if (!errors.isEmpty()) {
            errors.forEach(this::showFieldError);
            event.consume();
            return;
        }
        try {
            saving = true;
            dialog.getDialogPane().getButtonTypes().forEach(type -> {
                Node button = dialog.getDialogPane().lookupButton(type);
                if (button != null) {
                    button.setDisable(true);
                }
            });
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    submit.accept(item);
                    return null;
                }
            };
            task.setOnSucceeded(done -> {
                saving = false;
                saved = true;
                dialog.close();
            });
            task.setOnFailed(failed -> {
                saving = false;
                dialog.getDialogPane().getButtonTypes().forEach(type -> {
                    Node button = dialog.getDialogPane().lookupButton(type);
                    if (button != null) {
                        button.setDisable(false);
                    }
                });
                Throwable cause = task.getException();
                formError.setText(cause == null || cause.getMessage() == null
                        ? "Không thể lưu dữ liệu." : cause.getMessage());
            });
            Thread worker = new Thread(task, "crud-form-save");
            worker.setDaemon(true);
            worker.start();
            event.consume();
        } catch (RuntimeException exception) {
            formError.setText(exception.getMessage() == null
                    ? "Không thể lưu dữ liệu." : exception.getMessage());
            event.consume();
        }
    }

    private void clearErrors() {
        formError.setText("");
        controls.values().forEach(node -> node.getStyleClass().remove("field-invalid"));
        errorLabels.values().forEach(label -> label.setText(""));
    }

    private void showFieldError(FieldError error) {
        Node control = controls.get(error.field());
        Label label = errorLabels.get(error.field());
        if (control != null) {
            control.getStyleClass().add("field-invalid");
            if (control instanceof TextInputControl textInput) {
                textInput.requestFocus();
            }
        }
        if (label != null) {
            label.setText(error.message());
        } else {
            formError.setText(formError.getText().isBlank()
                    ? error.message() : formError.getText() + "\n" + error.message());
        }
    }
}
