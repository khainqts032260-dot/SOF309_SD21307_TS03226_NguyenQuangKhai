<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="../common/admin-header.jsp" %>
<div class="admin-top refined-admin-top">
    <div><p class="admin-eyebrow">Nội dung website</p><h1>Loại tin tức</h1><p>Tạo nhóm nội dung để phân loại bài tin rõ ràng trên website.</p></div>
    <a class="btn" href="${ctx}/admin/news">Quản lý bài tin</a>
</div>
<c:if test="${param.error == 'name'}"><div class="alert error">Vui lòng nhập tên loại tin.</div></c:if>
<c:if test="${not empty param.success}"><div class="alert success">Đã cập nhật loại tin tức.</div></c:if>
<section class="admin-card form-card" id="newsCategoryForm">
    <h2>${empty editCategory ? 'Thêm loại tin tức' : 'Sửa loại tin tức'}</h2>
    <form action="${ctx}/admin/news-categories" method="post" class="admin-form grid-form">
        <input type="hidden" name="maLoaiTin" value="${editCategory.maLoaiTin}">
        <input name="tenLoai" maxlength="120" value="${editCategory.tenLoai}" placeholder="Tên loại tin, ví dụ: Khuyến mãi" required>
        <select name="trangThai"><option value="1" ${empty editCategory or editCategory.trangThai == 1 ? 'selected' : ''}>Đang dùng</option><option value="0" ${editCategory.trangThai == 0 ? 'selected' : ''}>Tạm ẩn</option></select>
        <textarea class="span-2" name="moTa" rows="3" maxlength="400" placeholder="Mô tả ngắn">${editCategory.moTa}</textarea>
        <div class="form-actions span-2"><button class="btn btn-dark">Lưu loại tin</button><a class="btn btn-light" href="${ctx}/admin/news-categories">Thêm mới</a></div>
    </form>
</section>
<section class="admin-card">
    <div class="admin-card-heading"><div><h2>Danh sách loại tin</h2><p>${fn:length(newsCategories)} loại đang được quản lý</p></div></div>
    <div class="table-scroll"><table class="data-table"><thead><tr><th>ID</th><th>Loại tin</th><th>Mô tả</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody>
        <c:forEach var="c" items="${newsCategories}"><tr><td>#${c.maLoaiTin}</td><td><strong>${c.tenLoai}</strong></td><td>${c.moTa}</td><td><span class="status-pill ${c.trangThai == 1 ? 'active' : 'inactive'}">${c.trangThai == 1 ? 'Đang dùng' : 'Tạm ẩn'}</span></td><td class="table-actions"><a class="link-btn" href="${ctx}/admin/news-categories?edit=${c.maLoaiTin}#newsCategoryForm">Sửa</a><form action="${ctx}/admin/news-categories" method="post" class="inline-form"><input type="hidden" name="action" value="status"><input type="hidden" name="id" value="${c.maLoaiTin}"><input type="hidden" name="status" value="${c.trangThai == 1 ? 0 : 1}"><button class="link-btn">${c.trangThai == 1 ? 'Ẩn' : 'Hiện'}</button></form><form action="${ctx}/admin/news-categories" method="post" class="inline-form" onsubmit="return confirm('Xóa loại tin này khỏi danh sách quản lý?')"><input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${c.maLoaiTin}"><button class="link-btn danger-link">Xóa</button></form></td></tr></c:forEach>
    </tbody></table></div>
</section>
<%@ include file="../common/admin-footer.jsp" %>
