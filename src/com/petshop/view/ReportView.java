package com.petshop.view;

import com.petshop.controller.ReportController;
import com.petshop.model.ReportData.Period;
import com.petshop.model.ReportData.Result;
import com.petshop.model.ReportData.Row;
import com.petshop.model.ReportData.Type;
import com.petshop.util.Formats;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.stage.FileChooser;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

public abstract class ReportView extends VBox implements AppView {
    private final String title;
    private final Type type;
    private final ReportController controller;
    private final DatePicker referenceDate = new DatePicker(LocalDate.now());
    private final javafx.scene.control.ComboBox<Period> period =
            new javafx.scene.control.ComboBox<>(
                    FXCollections.observableArrayList(Period.values()));
    private final TableView<Row> table = new TableView<>();
    private final BarChart<String, Number> chart;
    private final Label feedback = new Label();
    private final Button exportPdf = new Button("Xuất PDF");
    private final Button exportExcel = new Button("Xuất Excel");
    private Result currentResult;

    protected ReportView(String title, Type type, ReportController controller) {
        super(14);
        this.title = title;
        this.type = type;
        this.controller = controller;
        getChildren().addAll(header(), toolbar());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Không có dữ liệu"));
        table.getColumns().setAll(
                column("Nhóm", Row::category),
                column("Tên / Ngày", Row::name),
                column("Số lượng", row -> Long.toString(row.quantity())),
                column("Giá trị", row -> Formats.money(row.amount())));
        CategoryAxis categoryAxis = new CategoryAxis();
        NumberAxis numberAxis = new NumberAxis();
        numberAxis.setLabel(type == Type.REVENUE ? "Doanh thu (₫)" : "Số lượng");
        chart = new BarChart<>(categoryAxis, numberAxis);
        chart.setTitle(type.label());
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        VBox panel = new VBox(12, table, chart);
        panel.getStyleClass().add("card-panel");
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox.setVgrow(panel, Priority.ALWAYS);
        feedback.getStyleClass().add("welcome-sub");
        getChildren().addAll(panel, feedback);
        loadReport();
    }

    private Node header() {
        VBox header = new VBox(5);
        Label heading = new Label(title);
        heading.getStyleClass().add("welcome-title");
        Label description = new Label(type == Type.INVENTORY
                ? "Báo cáo tồn kho là ảnh chụp số liệu hiện tại; cơ sở dữ liệu chưa có lịch sử biến động kho."
                : "Chọn kỳ báo cáo để xem bảng và biểu đồ.");
        description.getStyleClass().add("welcome-sub");
        header.getChildren().addAll(heading, description);
        return header;
    }

    private Node toolbar() {
        period.setValue(Period.DAY);
        Button load = new Button("Xem báo cáo");
        load.getStyleClass().add("sidebar-btn");
        load.setOnAction(event -> loadReport());
        exportPdf.getStyleClass().add("sidebar-btn");
        exportPdf.setDisable(true);
        exportPdf.setOnAction(event -> export(true));
        exportExcel.getStyleClass().add("sidebar-btn");
        exportExcel.setDisable(true);
        exportExcel.setOnAction(event -> export(false));
        HBox toolbar = new HBox(10, new Label("Ngày tham chiếu"), referenceDate,
                new Label("Kỳ"), period, load, exportPdf, exportExcel);
        toolbar.getStyleClass().add("search-box");
        return toolbar;
    }

    private void loadReport() {
        if (referenceDate.getValue() == null || period.getValue() == null) {
            feedback.setText("Chọn ngày và kỳ báo cáo.");
            return;
        }
        feedback.setText("Đang tải báo cáo...");
        setExportEnabled(false);
        Type requestedType = type;
        Period requestedPeriod = period.getValue();
        LocalDate requestedDate = referenceDate.getValue();
        Task<Result> task = new Task<>() {
            @Override
            protected Result call() {
                return controller.load(requestedType, requestedPeriod, requestedDate);
            }
        };
        task.setOnSucceeded(event -> {
            currentResult = task.getValue();
            table.getItems().setAll(currentResult.rows());
            table.setPlaceholder(new Label(currentResult.rows().isEmpty()
                    ? "Không có dữ liệu" : "Không tìm thấy dữ liệu"));
            updateChart(currentResult.rows());
            feedback.setText(currentResult.rows().isEmpty()
                    ? "Không có dữ liệu trong kỳ đã chọn." : "");
            setExportEnabled(!currentResult.rows().isEmpty());
        });
        task.setOnFailed(event -> {
            Throwable failure = task.getException();
            java.util.logging.Logger.getLogger(ReportView.class.getName())
                    .log(java.util.logging.Level.SEVERE, "Không thể tải báo cáo.", failure);
            feedback.setText("Không thể tải báo cáo. Vui lòng thử lại sau.");
            showAlert(Alert.AlertType.ERROR, "Lỗi báo cáo",
                    "Không thể tải dữ liệu báo cáo. Vui lòng thử lại sau.");
        });
        run(task, "report-load");
    }

    private void updateChart(List<Row> rows) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Row row : rows) {
            Number value = type == Type.REVENUE
                    ? row.amount() : row.quantity();
            series.getData().add(new XYChart.Data<>(row.name(), value));
        }
        chart.getData().setAll(series);
    }

    private void export(boolean pdf) {
        if (currentResult == null || currentResult.rows().isEmpty()) {
            return;
        }
        FileChooser chooser = new FileChooser();
        String extension = pdf ? ".pdf" : ".xlsx";
        chooser.setTitle(pdf ? "Xuất báo cáo PDF" : "Xuất báo cáo Excel");
        chooser.setInitialFileName(type.name().toLowerCase() + extension);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                pdf ? "Tệp PDF" : "Tệp Excel", "*" + extension));
        Window window = getScene() == null ? null : getScene().getWindow();
        java.io.File file = chooser.showSaveDialog(window);
        if (file == null) {
            return;
        }
        Path target = file.toPath();
        Result result = currentResult;
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                if (pdf) {
                    controller.exportPdf(result, target);
                } else {
                    controller.exportExcel(result, target);
                }
                return null;
            }
        };
        task.setOnSucceeded(event ->
                showAlert(Alert.AlertType.INFORMATION, "Xuất báo cáo", "Đã xuất báo cáo."));
        task.setOnFailed(event -> {
            java.util.logging.Logger.getLogger(ReportView.class.getName())
                    .log(java.util.logging.Level.SEVERE, "Không thể xuất báo cáo.",
                            task.getException());
            showAlert(Alert.AlertType.ERROR, "Lỗi xuất báo cáo",
                    "Không thể xuất tệp. Vui lòng kiểm tra quyền ghi của thư mục.");
        });
        run(task, pdf ? "report-pdf-export" : "report-excel-export");
    }

    private void setExportEnabled(boolean enabled) {
        exportPdf.setDisable(!enabled);
        exportExcel.setDisable(!enabled);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    private void run(Task<?> task, String threadName) {
        Thread worker = new Thread(task, threadName);
        worker.setDaemon(true);
        worker.start();
    }

    private TableColumn<Row, String> column(String heading, Function<Row, String> value) {
        TableColumn<Row, String> column = new TableColumn<>(heading);
        column.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyStringWrapper(
                value.apply(cell.getValue())));
        return column;
    }

    @Override
    public final Node getNode() {
        return this;
    }

    @Override
    public final String getTitle() {
        return title;
    }

    public static final class Revenue extends ReportView {
        public Revenue(String title, ReportController controller) {
            super(title, Type.REVENUE, controller);
        }
    }

    public static final class Inventory extends ReportView {
        public Inventory(String title, ReportController controller) {
            super(title, Type.INVENTORY, controller);
        }
    }

    public static final class BestSellers extends ReportView {
        public BestSellers(String title, ReportController controller) {
            super(title, Type.BEST_SELLERS, controller);
        }
    }
}
