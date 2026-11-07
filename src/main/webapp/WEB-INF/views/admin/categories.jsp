<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="../common/admin-header.jsp" %>
<c:set var="editing" value="${not empty editCategory}" />
<div class="admin-top">
    <h1>Quản lý danh mục</h1>
    <p>Thêm danh mục, sửa danh mục, ẩn/hiện và xóa danh mục.</p>
</div>

<section class="admin-card form-card" id="categoryFormBox">
<h2>
    <c:choose>
        <c:when test="${editing}">Sửa danh mục #${editCategory.maDM}</c:when>
        <c:otherwise>Thêm danh mục</c:otherwise>
    </c:choose>
</h2>
<form action="${ctx}/admin/categories" method="post" class="admin-form grid-form">
    <input type="hidden" name="maDM" id="maDM" value="${editing ? editCategory.maDM : ''}">
    <input name="tenDM" id="tenDM" placeholder="Tên danh mục" value="${editing ? fn:escapeXml(editCategory.tenDM) : ''}" required>
    <input name="moTa" id="moTa" placeholder="Mô tả" value="${editing ? fn:escapeXml(editCategory.moTa) : ''}">
    <select name="trangThai" id="trangThai">
        <option value="1" ${editing and editCategory.trangThai == 1 ? 'selected' : ''}>Hiện</option>
        <option value="0" ${editing and editCategory.trangThai == 0 ? 'selected' : ''}>Ẩn</option>
    </select>
    <div class="form-actions">
        <button class="btn btn-dark" type="submit" name="action" value="save">Lưu danh mục</button>
        <a class="btn btn-light" href="${ctx}/admin/categories">Thêm mới</a>
    </div>
</form>
</section>

<section class="admin-card">
<table class="data-table">
<thead>
<tr>
    <th>ID</th>
    <th>Tên danh mục</th>
    <th>Mô tả</th>
    <th>Trạng thái</th>
    <th>Thao tác</th>
</tr>
</thead>
<tbody>
<c:forEach var="c" items="${categories}">
<tr>
    <td>${c.maDM}</td>
    <td>${c.tenDM}</td>
    <td>${c.moTa}</td>
    <td><span class="badge">${c.trangThai == 1 ? 'Hiện' : 'Ẩn'}</span></td>
    <td>
        <a class="link-btn" href="${ctx}/admin/categories?edit=${c.maDM}#categoryFormBox">Sửa</a>
        <form action="${ctx}/admin/categories" method="post" class="inline-form">
            <input type="hidden" name="action" value="${c.trangThai == 1 ? 'hide' : 'show'}">
            <input type="hidden" name="id" value="${c.maDM}">
            <button type="submit" class="link-btn">${c.trangThai == 1 ? 'Ẩn' : 'Hiện'}</button>
        </form>
        <form action="${ctx}/admin/categories" method="post" class="inline-form" onsubmit="return confirm('Bạn có chắc muốn xóa danh mục này khỏi danh sách quản lý không?');">
            <input type="hidden" name="action" value="delete">
            <input type="hidden" name="id" value="${c.maDM}">
            <button type="submit" class="link-btn danger-link">Xóa</button>
        </form>
    </td>
</tr>
</c:forEach>
</tbody>
</table>
</section>
<%@ include file="../common/admin-footer.jsp" %>
