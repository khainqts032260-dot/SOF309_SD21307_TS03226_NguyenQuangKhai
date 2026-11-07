<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="../common/admin-header.jsp" %>
<div class="admin-top refined-admin-top">
    <div><p class="admin-eyebrow">Nội dung website</p><h1>Quản lý tin tức</h1><p>Tạo bài tin, gắn loại nội dung và xuất bản lên trang Tin tức.</p></div>
    <a class="btn" href="${ctx}/admin/news-categories">Quản lý loại tin</a>
</div>

<c:if test="${param.error == 'title'}"><div class="alert error">Vui lòng nhập tiêu đề tin tức.</div></c:if>
<c:if test="${param.success == 'add'}"><div class="alert success">Đã thêm tin tức.</div></c:if>
<c:if test="${param.success == 'edit'}"><div class="alert success">Đã cập nhật tin tức.</div></c:if>
<c:if test="${param.success == 'status'}"><div class="alert success">Đã đổi trạng thái tin tức.</div></c:if>
<c:if test="${param.success == 'delete'}"><div class="alert success">Đã xóa tin tức.</div></c:if>

<section class="admin-card news-editor-card">
    <div class="admin-card-heading"><div><h2>${empty editNews ? 'Thêm tin tức' : 'Chỉnh sửa tin tức'}</h2><p>Nội dung được lưu trong cơ sở dữ liệu để sẵn sàng sử dụng khi bạn mở trang Tin tức công khai.</p></div></div>
    <form action="${ctx}/admin/news" method="post" class="admin-form news-admin-form">
        <input type="hidden" name="maTin" value="${editNews.maTin}">
        <label class="full">Tiêu đề<input name="tieuDe" maxlength="220" value="${editNews.tieuDe}" placeholder="Nhập tiêu đề bài tin" required></label>
        <label class="full">Tóm tắt<textarea name="tomTat" rows="3" maxlength="700" placeholder="Mô tả ngắn cho bài tin">${editNews.tomTat}</textarea></label>
        <label class="full">Nội dung<textarea name="noiDung" rows="9" placeholder="Nội dung chi tiết">${editNews.noiDung}</textarea></label>
        <label>Loại tin<select name="maLoaiTin"><option value="">Chưa phân loại</option><c:forEach var="category" items="${newsCategories}"><option value="${category.maLoaiTin}" ${editNews.maLoaiTin == category.maLoaiTin ? 'selected' : ''}>${category.tenLoai}</option></c:forEach></select></label>
        <label>Đường dẫn ảnh<input name="hinhAnh" value="${editNews.hinhAnh}" placeholder="assets/images/fashion/hero-01.jpg"></label>
        <label>Trạng thái<select name="trangThai"><option value="0" ${(empty editNews or editNews.trangThai == 0) ? 'selected' : ''}>Bản nháp</option><option value="1" ${editNews.trangThai == 1 ? 'selected' : ''}>Sẵn sàng đăng</option></select></label>
        <div class="form-actions full"><button class="btn btn-dark">${empty editNews ? 'Lưu tin tức' : 'Lưu thay đổi'}</button><c:if test="${not empty editNews}"><a class="btn" href="${ctx}/admin/news">Hủy chỉnh sửa</a></c:if></div>
    </form>
</section>

<section class="admin-card">
    <div class="admin-card-heading"><div><h2>Danh sách tin tức</h2><p>${fn:length(newsList)} nội dung đã lưu</p></div><form class="search-form" action="${ctx}/admin/news" method="get"><input name="q" value="${param.q}" placeholder="Tìm theo tiêu đề hoặc nội dung"><button class="btn btn-dark">Tìm</button></form></div>
    <div class="table-scroll"><table class="data-table news-admin-table"><thead><tr><th>ID</th><th>Tiêu đề</th><th>Loại tin</th><th>Trạng thái</th><th>Cập nhật</th><th>Thao tác</th></tr></thead><tbody>
        <c:forEach var="n" items="${newsList}"><tr><td>#${n.maTin}</td><td><strong>${n.tieuDe}</strong><small>${n.tomTat}</small></td><td>${empty n.tenLoai ? 'Chưa phân loại' : n.tenLoai}</td><td><span class="status-pill ${n.trangThai == 1 ? 'active' : 'inactive'}">${n.trangThai == 1 ? 'Sẵn sàng đăng' : 'Bản nháp'}</span></td><td>${n.ngayCapNhat}</td><td class="table-actions"><a class="link-btn" href="${ctx}/admin/news?edit=${n.maTin}">Sửa</a><form action="${ctx}/admin/news" method="post" style="display:inline"><input type="hidden" name="action" value="status"><input type="hidden" name="id" value="${n.maTin}"><input type="hidden" name="status" value="${n.trangThai == 1 ? 0 : 1}"><button class="link-btn">${n.trangThai == 1 ? 'Chuyển nháp' : 'Đánh dấu sẵn sàng'}</button></form><form action="${ctx}/admin/news" method="post" style="display:inline" onsubmit="return confirm('Xóa tin tức này?')"><input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${n.maTin}"><button class="link-btn danger-text">Xóa</button></form></td></tr></c:forEach>
        <c:if test="${empty newsList}"><tr><td colspan="6">Chưa có tin tức.</td></tr></c:if>
    </tbody></table></div>
</section>
<%@ include file="../common/admin-footer.jsp" %>
