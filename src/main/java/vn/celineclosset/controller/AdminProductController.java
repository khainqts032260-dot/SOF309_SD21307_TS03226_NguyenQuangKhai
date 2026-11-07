package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.ProductDAO;
import vn.celineclosset.util.FileUploadUtil;

import java.io.IOException;

@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 12 * 1024 * 1024, maxRequestSize = 15 * 1024 * 1024)
@WebServlet("/admin/products")
public class AdminProductController extends BaseController {
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireOwnerRole(req, resp)) {
            return;
        }
        try {
            req.setAttribute("products", productDAO.products(req.getParameter("q"), null, true));
            String editId = req.getParameter("edit");
            if (editId != null && !editId.isBlank()) {
                req.setAttribute("editProduct", productDAO.product(Integer.parseInt(editId)));
            }
            view(req, resp, "admin/products.jsp");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireOwnerRole(req, resp)) {
            return;
        }
        try {
            String action = req.getParameter("action");
            if ("hide".equals(action)) {
                productDAO.setProductStatus(Integer.parseInt(req.getParameter("id")), 0);
            } else if ("show".equals(action)) {
                productDAO.setProductStatus(Integer.parseInt(req.getParameter("id")), 1);
            } else if ("delete".equals(action)) {
                productDAO.deleteProduct(Integer.parseInt(req.getParameter("id")));
            } else {
                String imagePath = FileUploadUtil.uploadImage(req, "imageFile", "product", "/assets/uploads/products");
                if (imagePath == null || imagePath.isBlank()) {
                    imagePath = req.getParameter("currentHinhAnh");
                }
                productDAO.saveProduct(req.getParameter("maSP"), req.getParameter("maSKU"), req.getParameter("tenSP"),
                        req.getParameter("moTa"), req.getParameter("donGia"), req.getParameter("soLuongTon"),
                        req.getParameter("trangThai"), req.getParameter("tenDM"), imagePath,
                        req.getParameter("mauSac"), req.getParameter("kichThuoc"), req.getParameter("chatLieu"));
            }
            String redirect = req.getContextPath() + "/admin/products";
            if (!"hide".equals(action) && !"show".equals(action) && !"delete".equals(action)) {
                redirect += "?saved=1";
            }
            resp.sendRedirect(redirect);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
