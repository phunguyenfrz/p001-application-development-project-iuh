package com.petshop.view.employee;

import com.petshop.model.Employee;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.io.ByteArrayInputStream;
import java.time.format.DateTimeFormatter;

/**
 * Hộp thoại hiển thị chi tiết hồ sơ nhân viên (EmployeeDetailDialog).
 * Trình bày toàn bộ thông tin cá nhân, liên hệ, công việc và tài khoản hệ thống trực quan.
 *
 * Tiêu chuẩn Definition of Done:
 * - Đặt tên điều khiển đúng tiền tố: lbl, btn, img.
 * - Nút hiển thị đầy đủ tên không bị co rút (setMinWidth(Region.USE_PREF_SIZE)).
 */
public class EmployeeDetailDialog extends Dialog<Void> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public EmployeeDetailDialog(Employee employee, Window owner) {
        setTitle("Chi Tiết Hồ Sơ Nhân Viên - " + (employee != null ? employee.hoTen() : ""));
        setHeaderText(null);
        initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            initOwner(owner);
        }

        DialogPane pnlDialogPane = getDialogPane();
        pnlDialogPane.getStylesheets().add(
                getClass().getResource("/com/petshop/view/styles.css").toExternalForm()
        );
        pnlDialogPane.setPrefSize(600, 560);
        pnlDialogPane.setStyle("-fx-background-color: #f7f9f6;");

        pnlDialogPane.setContent(buildContent(employee));
        configureButtons(pnlDialogPane);
    }

    public static void show(Employee employee, Window owner) {
        if (employee == null) return;
        EmployeeDetailDialog dialog = new EmployeeDetailDialog(employee, owner);
        dialog.showAndWait();
    }

    private VBox buildContent(Employee employee) {
        VBox pnlContainer = new VBox(16);
        pnlContainer.setPadding(new Insets(16, 20, 10, 20));

        if (employee == null) {
            pnlContainer.getChildren().add(new Label("Không có dữ liệu nhân viên."));
            return pnlContainer;
        }

        // 1. Header Profile Box
        HBox pnlHeaderCard = new HBox(16);
        pnlHeaderCard.setAlignment(Pos.CENTER_LEFT);
        pnlHeaderCard.setPadding(new Insets(16));
        pnlHeaderCard.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-radius: 10px; -fx-background-radius: 10px;");

        // Avatar
        ImageView imgAvatar = new ImageView();
        imgAvatar.setFitWidth(76);
        imgAvatar.setFitHeight(76);
        imgAvatar.setPreserveRatio(true);
        imgAvatar.setSmooth(true);

        if (employee.anh() != null && employee.anh().length > 0) {
            try {
                imgAvatar.setImage(new Image(new ByteArrayInputStream(employee.anh())));
            } catch (Exception ignored) {
                setDefaultAvatar(imgAvatar);
            }
        } else {
            setDefaultAvatar(imgAvatar);
        }

        Circle clipCircle = new Circle(38, 38, 38);
        imgAvatar.setClip(clipCircle);

        VBox pnlNameBox = new VBox(6);
        Label lblFullName = new Label(employee.hoTen());
        lblFullName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label lblEmpId = new Label("Mã nhân viên: " + employee.maNhanVien());
        lblEmpId.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #64748b; -fx-font-family: monospace;");

        HBox pnlBadges = new HBox(8);
        pnlBadges.setAlignment(Pos.CENTER_LEFT);

        Label lblRoleBadge = new Label(employee.chucVu());
        lblRoleBadge.getStyleClass().add(getRoleBadgeClass(employee.chucVu()));

        Label lblStatusBadge = new Label(employee.trangThai());
        lblStatusBadge.getStyleClass().add("Đang làm".equals(employee.trangThai()) ? "badge-active" : "badge-locked");

        pnlBadges.getChildren().addAll(lblRoleBadge, lblStatusBadge);
        pnlNameBox.getChildren().addAll(lblFullName, lblEmpId, pnlBadges);

        pnlHeaderCard.getChildren().addAll(imgAvatar, pnlNameBox);

        // 2. Thông tin cá nhân & liên hệ
        VBox pnlPersonalInfoCard = new VBox(10);
        pnlPersonalInfoCard.setPadding(new Insets(14));
        pnlPersonalInfoCard.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-radius: 10px; -fx-background-radius: 10px;");

        Label lblPersonalTitle = new Label("THÔNG TIN CÁ NHÂN & LIÊN HỆ");
        lblPersonalTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #334155;");

        GridPane gridInfo = new GridPane();
        gridInfo.setHgap(20);
        gridInfo.setVgap(8);

        gridInfo.add(createFieldLabel("Số điện thoại:"), 0, 0);
        gridInfo.add(createFieldValue(employee.soDienThoai()), 1, 0);

        gridInfo.add(createFieldLabel("Email:"), 0, 1);
        gridInfo.add(createFieldValue(employee.email() != null ? employee.email() : "Chưa cập nhật"), 1, 1);

        gridInfo.add(createFieldLabel("Địa chỉ:"), 0, 2);
        gridInfo.add(createFieldValue(employee.diaChi() != null ? employee.diaChi() : "Chưa cập nhật"), 1, 2);

        gridInfo.add(createFieldLabel("Ngày sinh:"), 0, 3);
        gridInfo.add(createFieldValue(employee.ngaySinh() != null ? DATE_FORMATTER.format(employee.ngaySinh()) : "Chưa cập nhật"), 1, 3);

        gridInfo.add(createFieldLabel("Giới tính:"), 0, 4);
        gridInfo.add(createFieldValue(employee.nam() ? "Nam" : "Nữ"), 1, 4);

        gridInfo.add(createFieldLabel("Ngày vào làm:"), 0, 5);
        gridInfo.add(createFieldValue(employee.ngayBatDau() != null ? DATE_FORMATTER.format(employee.ngayBatDau()) : "Chưa cập nhật"), 1, 5);

        pnlPersonalInfoCard.getChildren().addAll(lblPersonalTitle, new Separator(), gridInfo);

        // 3. Thông tin tài khoản đăng nhập
        VBox pnlAccountCard = new VBox(10);
        pnlAccountCard.setPadding(new Insets(14));
        pnlAccountCard.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-radius: 10px; -fx-background-radius: 10px;");

        Label lblAccountTitle = new Label("TÀI KHOẢN ĐĂNG NHẬP & PHÂN QUYỀN HỆ THỐNG");
        lblAccountTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #334155;");

        GridPane gridAccount = new GridPane();
        gridAccount.setHgap(20);
        gridAccount.setVgap(8);

        boolean hasAccount = employee.tenDangNhap() != null && !employee.tenDangNhap().isEmpty();

        gridAccount.add(createFieldLabel("Tên đăng nhập:"), 0, 0);
        gridAccount.add(createFieldValue(hasAccount ? employee.tenDangNhap() : "(Chưa cấp tài khoản)"), 1, 0);

        gridAccount.add(createFieldLabel("Vai trò hệ thống:"), 0, 1);
        gridAccount.add(createFieldValue(hasAccount && employee.phanQuyen() != null ? employee.phanQuyen() : "Không có"), 1, 1);

        gridAccount.add(createFieldLabel("Trạng thái tài khoản:"), 0, 2);
        Label lblAccStatus = new Label();
        if (!hasAccount) {
            lblAccStatus.setText("Chưa cấp tài khoản");
            lblAccStatus.getStyleClass().add("badge-none");
        } else if (Boolean.TRUE.equals(employee.taiKhoanHoatDong())) {
            lblAccStatus.setText("Đang hoạt động");
            lblAccStatus.getStyleClass().add("badge-active");
        } else {
            lblAccStatus.setText("Đang bị khóa");
            lblAccStatus.getStyleClass().add("badge-locked");
        }
        gridAccount.add(lblAccStatus, 1, 2);

        pnlAccountCard.getChildren().addAll(lblAccountTitle, new Separator(), gridAccount);

        pnlContainer.getChildren().addAll(pnlHeaderCard, pnlPersonalInfoCard, pnlAccountCard);
        return pnlContainer;
    }

    private void configureButtons(DialogPane pnlDialogPane) {
        ButtonType btnTypeClose = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
        pnlDialogPane.getButtonTypes().add(btnTypeClose);

        Button btnClose = (Button) pnlDialogPane.lookupButton(btnTypeClose);
        btnClose.getStyleClass().addAll("employee-secondary");
        btnClose.setMinWidth(Region.USE_PREF_SIZE);
    }

    private Label createFieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #64748b; -fx-font-weight: 500;");
        lbl.setPrefWidth(140);
        return lbl;
    }

    private Label createFieldValue(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #1e293b; -fx-font-weight: bold;");
        return lbl;
    }

    private void setDefaultAvatar(ImageView imgView) {
        try {
            imgView.setImage(new Image(getClass().getResourceAsStream("/com/petshop/view/logo.png")));
        } catch (Exception ignored) {}
    }

    private String getRoleBadgeClass(String chucVu) {
        if (chucVu == null) return "badge-none";
        return switch (chucVu) {
            case "Admin" -> "badge-admin";
            case "Quản lý" -> "badge-manager";
            case "Bán hàng" -> "badge-sale";
            case "Chăm sóc" -> "badge-care";
            default -> "badge-none";
        };
    }
}

