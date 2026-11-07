package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.CategoryDAO;

import java.io.IOException;

@WebServlet("/admin/categories")
public class AdminCategoryController extends BaseController {
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireOwnerRole(req, resp)) {
            return;
        }
        try {
            req.setAttribute("categories", categoryDAO.categories(false));
            String editId = req.getParameter("edit");
            if (editId != null && !editId.isBlank()) {
                req.setAttribute("editCategory", categoryDAO.category(Integer.parseInt(editId)));
            }
            view(req, resp, "admin/categories.jsp");
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
                categoryDAO.setCategoryStatus(Integer.parseInt(req.getParameter("id")), 0);
            } else if ("show".equals(action)) {
                categoryDAO.setCategoryStatus(Integer.parseInt(req.getParameter("id")), 1);
            } else if ("delete".equals(action)) {
                categoryDAO.deleteCategory(Integer.parseInt(req.getParameter("id")));
            } else {
                categoryDAO.saveCategory(req.getParameterMap());
            }
            resp.sendRedirect(req.getContextPath() + "/admin/categories");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
