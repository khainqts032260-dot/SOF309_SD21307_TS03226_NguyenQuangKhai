package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.ReturnRequestDAO;
import vn.celineclosset.util.FileUploadUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Khách hàng tạo yêu cầu trả hàng và sửa tài khoản nhận tiền. */
@WebServlet("/returns")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 18 * 1024 * 1024)
public class ReturnRequestController extends BaseController {
    private final ReturnRequestDAO returnDAO = new ReturnRequestDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireCustomer(req, resp)) return;
        int orderId = parseInt(req.getParameter("maDH"));
        try {
            String action = clean(req.getParameter("action"));
            if ("editBank".equals(action)) {
                int requestId = parseInt(req.getParameter("maYCTH"));
                returnDAO.updateBank(requestId, authId(req), req.getParameter("nganHang"),
                        req.getParameter("soTaiKhoan"), req.getParameter("chuTaiKhoan"));
                resp.sendRedirect(req.getContextPath() + "/orders?id=" + orderId + "&returnBankUpdated=1");
                return;
            }

            List<String> images = new ArrayList<>();
            for (int index = 1; index <= 3; index++) {
                String image = FileUploadUtil.uploadImage(req, "returnImage" + index,
                        "return-order-" + orderId + "-" + index, "/assets/uploads/returns");
                if (image != null) images.add(image);
            }
            returnDAO.createRequest(orderId, authId(req), req.getParameter("lyDo"),
                    req.getParameter("nganHang"), req.getParameter("soTaiKhoan"),
                    req.getParameter("chuTaiKhoan"), images);
            resp.sendRedirect(req.getContextPath() + "/orders?id=" + orderId + "&returnCreated=1");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            req.getSession().setAttribute("returnError", exception.getMessage());
            resp.sendRedirect(req.getContextPath() + "/orders?id=" + orderId + "&returnError=1");
        } catch (Exception exception) {
            throw new ServletException(exception);
        }
    }

    private int parseInt(String value) {
        try { return Integer.parseInt(value == null ? "0" : value.trim()); }
        catch (NumberFormatException exception) { throw new IllegalArgumentException("Mã yêu cầu không hợp lệ."); }
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
