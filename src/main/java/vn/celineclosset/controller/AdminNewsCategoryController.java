package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.NewsCategoryDAO;

import java.io.IOException;

@WebServlet("/admin/news-categories")
public class AdminNewsCategoryController extends BaseController {
    private final NewsCategoryDAO categoryDAO = new NewsCategoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireOwnerRole(req, resp)) return;
        try {
            req.setAttribute("newsCategories", categoryDAO.all(false));
            String edit = req.getParameter("edit");
            if (edit != null && !edit.isBlank()) {
                req.setAttribute("editCategory", categoryDAO.byId(Integer.parseInt(edit)));
            }
            view(req, resp, "admin/news-categories.jsp");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireOwnerRole(req, resp)) return;
        try {
            String action = req.getParameter("action");
            if ("status".equals(action)) {
                categoryDAO.setStatus(Integer.parseInt(req.getParameter("id")), Integer.parseInt(req.getParameter("status")));
                resp.sendRedirect(req.getContextPath() + "/admin/news-categories?success=status");
                return;
            }
            if ("delete".equals(action)) {
                categoryDAO.delete(Integer.parseInt(req.getParameter("id")));
                resp.sendRedirect(req.getContextPath() + "/admin/news-categories?success=delete");
                return;
            }
            String name = req.getParameter("tenLoai");
            String id = req.getParameter("maLoaiTin");
            if (name == null || name.isBlank()) {
                resp.sendRedirect(req.getContextPath() + "/admin/news-categories?error=name" + (id == null || id.isBlank() ? "" : "&edit=" + id));
                return;
            }
            int status = "0".equals(req.getParameter("trangThai")) ? 0 : 1;
            categoryDAO.save(id, name, req.getParameter("moTa"), status);
            resp.sendRedirect(req.getContextPath() + "/admin/news-categories?success=save");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
