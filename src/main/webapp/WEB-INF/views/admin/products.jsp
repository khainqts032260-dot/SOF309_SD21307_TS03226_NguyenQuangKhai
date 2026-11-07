<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="../common/admin-header.jsp" %>
<c:set var="editing" value="${not empty editProduct}" />
<div class="admin-top">
    <h1>Quản lý sản phẩm</h1>
    <p>Thêm sản phẩm, sửa sản phẩm, ẩn/hiện và xóa sản phẩm.</p>
</div>

<c:if test="${param.saved == '1'}"><div class="alert success admin-save-notice">Đã lưu sản phẩm và cập nhật hình ảnh thành công.</div></c:if>
<section class="admin-card form-card" id="productFormBox">
<h2 id="productFormTitle">
    <c:choose>
        <c:when test="${editing}">Sửa sản phẩm #${editProduct.maSP}</c:when>
        <c:otherwise>Thêm sản phẩm</c:otherwise>
    </c:choose>
</h2>
<form action="${ctx}/admin/products" method="post" enctype="multipart/form-data" class="admin-form grid-form" id="productForm">
    <input type="hidden" name="maSP" id="maSP" value="${editing ? editProduct.maSP : ''}">
    <input type="hidden" name="currentHinhAnh" id="currentHinhAnh" value="${editing ? fn:escapeXml(editProduct.hinhAnh) : ''}">

    <input name="maSKU" id="maSKU" placeholder="Mã sản phẩm, ví dụ: BLZ-MAD-010" value="${editing ? fn:escapeXml(editProduct.maSKU) : ''}" required>
    <input name="tenSP" id="tenSP" placeholder="Tên sản phẩm" value="${editing ? fn:escapeXml(editProduct.tenSP) : ''}" required>
    <input name="donGia" id="donGia" placeholder="Đơn giá" type="number" min="0" step="1000" value="${editing ? editProduct.donGia : ''}" required>
    <input name="soLuongTon" id="soLuongTon" placeholder="Số lượng tồn" type="number" min="0" value="${editing ? editProduct.soLuongTon : ''}" required>

    <input name="tenDM" id="tenDM" placeholder="Loại sản phẩm, ví dụ: Chân váy" value="${editing ? fn:escapeXml(editProduct.tenDM) : ''}" required>
    <label class="file-field product-image-upload-field">
        <span>Hình ảnh sản phẩm</span>
        <div class="product-upload-preview">
            <c:choose>
                <c:when test="${editing and not empty editProduct.hinhAnh}"><img id="productImagePreview" src="${ctx}/${editProduct.hinhAnh}" alt="Ảnh hiện tại của sản phẩm"></c:when>
                <c:otherwise><span id="productImagePreviewEmpty"><i class="fa-regular fa-image"></i> Chưa chọn ảnh</span><img id="productImagePreview" src="" alt="Xem trước ảnh mới" hidden></c:otherwise>
            </c:choose>
        </div>
        <input type="file" name="imageFile" id="imageFile" accept="image/jpeg,image/png,image/webp,image/gif">
        <small id="currentImageText">
            <c:choose>
                <c:when test="${editing and not empty editProduct.hinhAnh}">
                    Ảnh hiện tại: ${editProduct.hinhAnh}. Chọn ảnh mới nếu muốn thay đổi.
                </c:when>
                <c:when test="${editing}">
                    Sản phẩm này chưa có ảnh. Hãy chọn ảnh từ máy tính.
                </c:when>
                <c:otherwise>
                    Chọn ảnh từ máy tính. Khi sửa sản phẩm, không chọn ảnh mới thì hệ thống giữ ảnh cũ.
                </c:otherwise>
            </c:choose>
        </small>
    </label>
    <input name="mauSac" id="mauSac" placeholder="Màu sắc" value="${editing ? fn:escapeXml(editProduct.mauSac) : ''}">

    <input name="kichThuoc" id="kichThuoc" placeholder="S,M,L" value="${editing ? fn:escapeXml(editProduct.kichThuoc) : ''}">
    <input name="chatLieu" id="chatLieu" placeholder="Chất liệu" value="${editing ? fn:escapeXml(editProduct.chatLieu) : ''}">
    <select name="trangThai" id="trangThai">
        <option value="1" ${editing and editProduct.trangThai == 1 ? 'selected' : ''}>Đang bán</option>
        <option value="0" ${editing and editProduct.trangThai == 0 ? 'selected' : ''}>Ẩn</option>
    </select>

    <textarea name="moTa" id="moTa" placeholder="Mô tả"><c:if test="${editing}">${fn:escapeXml(editProduct.moTa)}</c:if></textarea>
    <div class="form-actions">
        <button class="btn btn-dark" type="submit" name="action" value="save">Lưu sản phẩm</button>
        <a class="btn btn-light" href="${ctx}/admin/products">Thêm mới</a>
    </div>
</form>
</section>

<section class="admin-card">
<table class="data-table">
<thead>
<tr>
    <th>ID</th>
    <th>Ảnh</th>
    <th>Sản phẩm</th>
    <th>Danh mục</th>
    <th>Giá</th>
    <th>Tồn</th>
    <th>TT</th>
    <th>Thao tác</th>
</tr>
</thead>
<tbody>
<c:forEach var="p" items="${products}">

<tr>
    <td>${p.maSP}</td>
    <td>
        <c:set var="adminProductFallback" value="${ctx}/assets/images/fashion/card-01.jpg" />
        <c:choose>
            <c:when test="${not empty p.hinhAnh}"><img class="admin-product-thumb js-fashion-image" src="${ctx}/${p.hinhAnh}" data-fallback="${adminProductFallback}" alt="${fn:escapeXml(p.tenSP)}"></c:when>
            <c:otherwise><img class="admin-product-thumb js-fashion-image" src="${adminProductFallback}" alt="${fn:escapeXml(p.tenSP)}"></c:otherwise>
        </c:choose>
    </td>
    <td><strong>${p.tenSP}</strong><small class="admin-product-sku">${p.maSKU}</small></td>
    <td>
        <c:choose>
            <c:when test="${not empty p.maDM}">
                <a class="link-btn" href="${ctx}/admin/categories?edit=${p.maDM}#categoryFormBox">${p.tenDM}</a>
            </c:when>
            <c:otherwise>${p.tenDM}</c:otherwise>
        </c:choose>
    </td>
    <td><fmt:formatNumber value="${p.donGia}" type="number" groupingUsed="true" />đ</td>
    <td>${p.soLuongTon}</td>
    <td><span class="badge">${p.trangThai == 1 ? 'Hiện' : 'Ẩn'}</span></td>
    <td>
        <a class="link-btn" href="${ctx}/admin/products?edit=${p.maSP}#productFormBox">Sửa</a>
        <form action="${ctx}/admin/products" method="post" class="inline-form">
            <input type="hidden" name="action" value="${p.trangThai == 1 ? 'hide' : 'show'}">
            <input type="hidden" name="id" value="${p.maSP}">
            <button type="submit" class="link-btn">${p.trangThai == 1 ? 'Ẩn' : 'Hiện'}</button>
        </form>
        <form action="${ctx}/admin/products" method="post" class="inline-form" onsubmit="return confirm('Bạn có chắc muốn xóa sản phẩm này khỏi danh sách quản lý không?');">
            <input type="hidden" name="action" value="delete">
            <input type="hidden" name="id" value="${p.maSP}">
            <button type="submit" class="link-btn danger-link">Xóa</button>
        </form>
    </td>
</tr>
</c:forEach>
</tbody>
</table>
</section>
<%@ include file="../common/admin-footer.jsp" %>
