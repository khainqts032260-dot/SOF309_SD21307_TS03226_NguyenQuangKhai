package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.VoucherDAO;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/** Chủ cửa hàng tạo và quản lý voucher. */
@WebServlet("/admin/vouchers")
public class AdminVoucherController extends BaseController {
    private final VoucherDAO voucherDAO = new VoucherDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireOwnerRole(req, resp)) return;
        try {
            req.setAttribute("vouchers", voucherDAO.all(req.getParameter("q")));
            int editId = integer(req.getParameter("edit"), 0);
            if (editId > 0) req.setAttribute("editVoucher", voucherDAO.byId(editId));
            view(req, resp, "admin/vouchers.jsp");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireOwnerRole(req, resp)) return;
        try {
            if ("status".equals(req.getParameter("action"))) {
                voucherDAO.toggle(integer(req.getParameter("id"), 0), integer(req.getParameter("status"), 0));
                resp.sendRedirect(req.getContextPath() + "/admin/vouchers?success=status");
                return;
            }

            int voucherId = integer(req.getParameter("maVoucher"), 0);
            String code = text(req.getParameter("maCode")).toUpperCase();
            String name = text(req.getParameter("tenVoucher"));
            String type = "FIXED".equals(req.getParameter("loaiGiam")) ? "FIXED" : "PERCENT";
            BigDecimal value = number(req.getParameter("giaTri"));
            BigDecimal minimumOrder = number(req.getParameter("donToiThieu"));
            int quantity = integer(req.getParameter("soLuot"), 0);
            LocalDateTime start = date(req.getParameter("ngayBatDau"));
            LocalDateTime end = date(req.getParameter("ngayKetThuc"));

            String error = null;
            if (!code.matches("[A-Z0-9_-]{3,40}")) error = "code";
            else if (name.isBlank()) error = "name";
            else if (voucherDAO.codeExists(code, voucherId)) error = "duplicate";
            else if (value.signum() <= 0 || ("PERCENT".equals(type) && value.compareTo(new BigDecimal("100")) > 0)) error = "value";
            else if (minimumOrder.signum() < 0 || quantity < 0) error = "number";
            else if (start == null || end == null || !end.isAfter(start)) error = "date";

            if (error != null) {
                String editPart = voucherId > 0 ? "&edit=" + voucherId : "";
                resp.sendRedirect(req.getContextPath() + "/admin/vouchers?error=" + error + editPart);
                return;
            }

            voucherDAO.save(String.valueOf(voucherId), code, name, type, req.getParameter("giaTri"),
                    req.getParameter("giamToiDa"), req.getParameter("donToiThieu"),
                    req.getParameter("ngayBatDau"), req.getParameter("ngayKetThuc"),
                    req.getParameter("soLuot"), "1".equals(req.getParameter("trangThai")) ? 1 : 0);
            resp.sendRedirect(req.getContextPath() + "/admin/vouchers?success=" + (voucherId > 0 ? "edit" : "add"));
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private String text(String value) {
        return value == null ? "" : value.trim();
    }

    private int integer(String value, int fallback) {
        try { return value == null || value.isBlank() ? fallback : Integer.parseInt(value.trim()); }
        catch (NumberFormatException e) { return fallback; }
    }

    private BigDecimal number(String value) {
        try { return new BigDecimal(value == null ? "0" : value.replace(",", "").trim()); }
        catch (NumberFormatException e) { return BigDecimal.ZERO; }
    }

    private LocalDateTime date(String value) {
        try { return value == null || value.isBlank() ? null : LocalDateTime.parse(value.trim()); }
        catch (DateTimeParseException e) { return null; }
    }
}
