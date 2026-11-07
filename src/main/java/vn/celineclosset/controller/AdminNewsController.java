package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.NewsDAO;
import vn.celineclosset.dao.NewsCategoryDAO;

import java.io.IOException;

@WebServlet("/admin/news")
public class AdminNewsController extends BaseController {
    private final NewsDAO newsDAO = new NewsDAO();
    private final NewsCategoryDAO newsCategoryDAO = new NewsCategoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireOwnerRole(req, resp)) return;
        try {
            req.setAttribute("newsList", newsDAO.all(req.getParameter("q")));
            req.setAttribute("newsCategories", newsCategoryDAO.all(true));
            String edit = req.getParameter("edit");
            if (edit != null && !edit.isBlank()) {
                req.setAttribute("editNews", newsDAO.byId(Integer.parseInt(edit)));
            }
            view(req, resp, "admin/news.jsp");
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
                newsDAO.toggle(Integer.parseInt(req.getParameter("id")), Integer.parseInt(req.getParameter("status")));
                resp.sendRedirect(req.getContextPath() + "/admin/news?success=status");
                return;
            }
            if ("delete".equals(action)) {
                newsDAO.delete(Integer.parseInt(req.getParameter("id")));
                resp.sendRedirect(req.getContextPath() + "/admin/news?success=delete");
                return;
            }

            String id = req.getParameter("maTin");
            boolean creating = id == null || id.isBlank();
            String title = req.getParameter("tieuDe");
            if (title == null || title.isBlank()) {
                resp.sendRedirect(req.getContextPath() + "/admin/news?error=title" + (creating ? "" : "&edit=" + id));
                return;
            }
            int status = "1".equals(req.getParameter("trangThai")) ? 1 : 0;
            int categoryId = 0;
            try { categoryId = Integer.parseInt(req.getParameter("maLoaiTin")); } catch (Exception ignored) { }
            newsDAO.save(id, title, req.getParameter("tomTat"), req.getParameter("noiDung"),
                    req.getParameter("hinhAnh"), categoryId, status, authId(req));
            resp.sendRedirect(req.getContextPath() + "/admin/news?success=" + (creating ? "add" : "edit"));
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
