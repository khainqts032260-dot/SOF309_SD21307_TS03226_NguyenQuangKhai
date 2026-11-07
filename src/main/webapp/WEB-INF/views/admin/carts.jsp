<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="../common/admin-header.jsp" %>
<div class="admin-top"><h1>Quản lý giỏ hàng</h1><p>Theo dõi giỏ hàng đang mở, xem sản phẩm trong giỏ, sửa số lượng hoặc dọn giỏ.</p></div>

<section class="admin-card">
<div class="admin-toolbar">
    <h2>Danh sách giỏ hàng</h2>
    <form action="${ctx}/admin/carts" method="get" class="search-form">
        <input name="q" value="${param.q}" placeholder="Tìm mã giỏ, tên, email, SĐT">
        <button class="btn btn-dark">Tìm</button>
    </form>
</div>
<table class="data-table">
<thead><tr><th>Mã giỏ</th><th>Khách hàng</th><th>Email</th><th>SĐT</th><th>Số mặt hàng</th><th>Tạm tính</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
<tbody>
<c:forEach var="g" items="${carts}">
<tr>
    <td><a href="${ctx}/admin/carts?id=${g.maGH}">#${g.maGH}</a></td>
    <td>${g.hoTen}</td>
    <td>${g.email}</td>
    <td>${g.soDienThoai}</td>
    <td>${g.soMatHang}</td>
    <td><fmt:formatNumber value="${g.tongTien}" type="number" groupingUsed="true" />đ</td>
    <td><span class="badge">${g.trangThai == 1 ? 'Đang mở' : 'Đã khóa'}</span></td>
    <td>
        <form action="${ctx}/admin/carts" method="post" class="inline-form">
            <input type="hidden" name="action" value="status">
            <input type="hidden" name="maGH" value="${g.maGH}">
            <input type="hidden" name="status" value="${g.trangThai == 1 ? 0 : 1}">
            <button class="link-btn">${g.trangThai == 1 ? 'Khóa' : 'Mở'}</button>
        </form>
    </td>
</tr>
</c:forEach>
</tbody>
</table>
</section>

<c:if test="${not empty cart}">
<section class="admin-card">
<div class="admin-toolbar">
    <div>
        <h2>Chi tiết giỏ #${cart.maGH}</h2>
        <p>Khách hàng: <b>${cart.hoTen}</b> - ${cart.email}</p>
    </div>
    <form action="${ctx}/admin/carts" method="post">
        <input type="hidden" name="action" value="clear">
        <input type="hidden" name="maGH" value="${cart.maGH}">
        <button class="btn btn-dark" onclick="return confirm('Xóa toàn bộ sản phẩm trong giỏ này?')">Dọn giỏ</button>
    </form>
</div>
<table class="data-table">
<thead><tr><th>Sản phẩm</th><th>Danh mục</th><th>Đơn giá</th><th>Số lượng</th><th>Tồn kho</th><th>Thành tiền</th><th>Thao tác</th></tr></thead>
<tbody>
<c:forEach var="i" items="${items}">

<tr>
    <c:set var="adminCartFallback" value="${ctx}/assets/images/fashion/card-01.jpg" />
<td class="cart-product"><c:choose><c:when test="${not empty i.hinhAnh}"><img class="js-fashion-image" src="${ctx}/${i.hinhAnh}" data-fallback="${adminCartFallback}" alt="${i.tenSP}"></c:when><c:otherwise><img class="js-fashion-image" src="${adminCartFallback}" alt="${i.tenSP}"></c:otherwise></c:choose><a class="link-btn" href="${ctx}/admin/products?edit=${i.maSP}#productFormBox">${i.tenSP}</a></td>
    <td><a class="link-btn" href="${ctx}/admin/categories?edit=${i.maDM}#categoryFormBox">${i.tenDM}</a></td>
    <td><fmt:formatNumber value="${i.donGia}" type="number" groupingUsed="true" />đ</td>
    <td>
        <form action="${ctx}/admin/carts" method="post" class="inline-form">
            <input type="hidden" name="action" value="update">
            <input type="hidden" name="maGH" value="${cart.maGH}">
            <input type="hidden" name="maCTGH" value="${i.maCTGH}">
            <input type="number" name="quantity" value="${i.soLuong}" min="0">
            <button>Lưu</button>
        </form>
    </td>
    <td>${i.soLuongTon}</td>
    <td><fmt:formatNumber value="${i.thanhTien}" type="number" groupingUsed="true" />đ</td>
    <td>
        <form action="${ctx}/admin/carts" method="post" class="inline-form">
            <input type="hidden" name="action" value="delete">
            <input type="hidden" name="maGH" value="${cart.maGH}">
            <input type="hidden" name="maCTGH" value="${i.maCTGH}">
            <button class="link-btn">Xóa</button>
        </form>
    </td>
</tr>
</c:forEach>
</tbody>
</table>
</section>
</c:if>

<%@ include file="../common/admin-footer.jsp" %>
