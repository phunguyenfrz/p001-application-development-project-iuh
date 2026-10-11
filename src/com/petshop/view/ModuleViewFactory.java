package com.petshop.view;

import com.petshop.model.Screen;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public final class ModuleViewFactory {
    private static final Map<Screen, Function<String, AppView>> FACTORIES = Map.ofEntries(
            Map.entry(Screen.OVERVIEW, OverviewView::new),
            Map.entry(Screen.SHIFTS, ShiftView::new),
            Map.entry(Screen.SUPPLIERS, SupplierModuleView::new),
            Map.entry(Screen.PROMOTIONS, PromotionView::new),
            Map.entry(Screen.PURCHASE_ORDERS, PurchaseOrderView::new),
            Map.entry(Screen.INVOICE_CREATE, InvoiceView::new),
            Map.entry(Screen.INVOICE_HISTORY, InvoiceHistoryView::new),
            Map.entry(Screen.RETURNS, ReturnsView::new),
            Map.entry(Screen.REVENUE_REPORT, RevenueReportView::new),
            Map.entry(Screen.INVENTORY_REPORT, InventoryReportView::new),
            Map.entry(Screen.BEST_SELLERS_REPORT, BestSellersReportView::new));

    private ModuleViewFactory() {
    }

    public static AppView create(Screen screen, String label) {
        Function<String, AppView> factory = FACTORIES.get(screen);
        if (factory == null) {
            throw new IllegalArgumentException("Chưa đăng ký view cho màn hình: " + screen);
        }
        return Objects.requireNonNull(factory.apply(label));
    }

    private abstract static class ModuleFrame extends VBox implements AppView {
        private final String title;

        protected ModuleFrame(String title, String description, Node... content) {
            super(14);
            this.title = title;
            getStyleClass().add("module-view");
            Label heading = new Label(title);
            heading.getStyleClass().add("welcome-title");
            Label summary = new Label(description);
            summary.getStyleClass().add("welcome-sub");
            getChildren().addAll(heading, summary);
            getChildren().addAll(content);
        }

        protected static Node todo(String text) {
            VBox panel = new VBox(8, new Label(text),
                    new Label("TODO: kết nối controller, dữ liệu và quy tắc nghiệp vụ."));
            panel.getStyleClass().add("card-panel");
            return panel;
        }

        protected static TableView<String> emptyTable(String message) {
            TableView<String> table = new TableView<>();
            table.setPlaceholder(new Label(message));
            VBox.setVgrow(table, Priority.ALWAYS);
            return table;
        }

        protected static Button button(String text) {
            Button button = new Button(text);
            button.getStyleClass().add("sidebar-btn");
            button.setOnAction(event -> showTodoDialog(text));
            return button;
        }

        private static void showTodoDialog(String action) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle(action);
            alert.setHeaderText(null);
            alert.setContentText("TODO: chức năng này chưa được nối nghiệp vụ.");
            alert.show();
        }

        @Override
        public final Node getNode() {
            return this;
        }

        @Override
        public final String getTitle() {
            return title;
        }
    }

    public static final class OverviewView extends ModuleFrame {
        OverviewView(String title) {
            super(title, "Thống kê tổng quan",
                    todo("Tổng quan sẽ hiển thị thống kê theo vai trò."));
        }
    }

    public static final class SupplierModuleView extends ModuleFrame {
        SupplierModuleView(String title) {
            super(title, "UC004 · Quản lý nhà cung cấp",
                    todo("Danh sách nhà cung cấp"));
        }
    }

    public static final class ShiftView extends ModuleFrame {
        ShiftView(String title) {
            super(title, "UC003 · Xem lịch tuần/tháng và quản lý ca chưa diễn ra",
                    new HBox(8, button("Tuần"), button("Tháng"), button("Thêm ca")),
                    todo("Lịch phân ca theo tuần hoặc tháng"));
        }
    }

    public static final class PromotionView extends ModuleFrame {
        PromotionView(String title) {
            super(title, "UC006 · Danh sách và thông tin khuyến mãi",
                    todo("Danh sách khuyến mãi"));
        }
    }

    public static final class PurchaseOrderView extends ModuleFrame {
        PurchaseOrderView(String title) {
            super(title, "UC011 · Lập đơn nhập và xác nhận riêng khi nhận hàng",
                    new HBox(8, button("Lập đơn nhập"), button("Xác nhận nhận hàng")),
                    todo("Đơn Chờ nhận hàng → Hoàn tất"));
        }
    }

    public static final class InvoiceHistoryView extends ModuleFrame {
        InvoiceHistoryView(String title) {
            super(title, "UC015 · Tra cứu hóa đơn đã lập",
                    todo("Danh sách hóa đơn"));
        }
    }

    public static final class InvoiceView extends ModuleFrame {
        InvoiceView(String title) {
            super(title, "UC015 · Lập hóa đơn",
                    todo("Mở phiên đăng nhập để tạo hóa đơn."));
        }
    }

    public static final class ReturnsView extends ModuleFrame {
        ReturnsView(String title) {
            super(title, "Đổi/trả là chức năng ngoài phạm vi UC015 đã nêu.",
                    todo("Chưa xác minh use case đổi/trả trong SRS"));
        }
    }

    public static final class RevenueReportView extends ModuleFrame {
        RevenueReportView(String title) {
            super(title, "UC007 · Báo cáo doanh thu theo kỳ",
                    todo("Chọn ngày, tuần hoặc tháng"));
        }
    }

    public static final class InventoryReportView extends ModuleFrame {
        InventoryReportView(String title) {
            super(title, "UC007 · Báo cáo tồn kho",
                    todo("Bảng và biểu đồ tồn kho"));
        }
    }

    public static final class BestSellersReportView extends ModuleFrame {
        BestSellersReportView(String title) {
            super(title, "UC007 · Báo cáo sản phẩm/dịch vụ bán chạy",
                    todo("Bảng và biểu đồ bán chạy"));
        }
    }
}
