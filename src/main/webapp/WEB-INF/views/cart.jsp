<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Giỏ hàng | Celine Closset" scope="request" />
<%@ include file="common/header.jsp" %>
<section class="page-hero small"><h1>Giỏ hàng</h1><p>Chọn sản phẩm muốn mua, chỉnh số lượng bằng dấu cộng/trừ hoặc bỏ sản phẩm khỏi giỏ.</p></section>
<section class="section">
<c:choose>
<c:when test="${empty items}"><div class="empty-box">Giỏ hàng đang trống. <a href="${ctx}/products">Tiếp tục mua sắm</a></div></c:when>
<c:otherwise>
<div class="cart-toolbar">
    <label class="check-line"><input type="checkbox" id="selectAllCart" checked> Chọn tất cả sản phẩm</label>
    <span>Tổng giỏ hàng: <b><fmt:formatNumber value="${total}" type="number" groupingUsed="true" />đ</b></span>
</div>
<div class="table-wrap"><table class="data-table cart-table">
<thead><tr><th>Chọn</th><th>Sản phẩm</th><th>Đơn giá</th><th>Số lượng</th><th>Thành tiền</th><th>Thao tác</th></tr></thead>
<tbody>
<c:forEach var="i" items="${items}">

<tr>
<td><input class="cart-select" form="checkoutForm" type="checkbox" name="selectedItemId" value="${i.maCTGH}" checked></td>
<c:set var="cartFallback" value="${ctx}/assets/images/fashion/card-01.jpg" />
<td class="cart-product"><a class="cart-product-link" href="${ctx}/product-detail?id=${i.maSP}" title="Xem lại ${i.tenSP}"><c:choose><c:when test="${not empty i.hinhAnh}"><img class="js-fashion-image" src="${ctx}/${i.hinhAnh}" data-fallback="${cartFallback}" alt="${i.tenSP}"></c:when><c:otherwise><img class="js-fashion-image" src="${cartFallback}" alt="${i.tenSP}"></c:otherwise></c:choose><span>${i.tenSP}<small>${i.tenDM}</small><em>Xem chi tiết sản phẩm →</em></span></a></td>
<td><fmt:formatNumber value="${i.donGia}" type="number" groupingUsed="true" />đ</td>
<td>
    <div class="qty-stepper">
        <form action="${ctx}/cart" method="post"><input type="hidden" name="action" value="minus"><input type="hidden" name="itemId" value="${i.maCTGH}"><button type="submit">−</button></form>
        <input type="text" value="${i.soLuong}" readonly>
        <form action="${ctx}/cart" method="post"><input type="hidden" name="action" value="plus"><input type="hidden" name="itemId" value="${i.maCTGH}"><button type="submit">+</button></form>
    </div>
</td>
<td><fmt:formatNumber value="${i.thanhTien}" type="number" groupingUsed="true" />đ</td>
<td>
    <form action="${ctx}/cart" method="post" onsubmit="return confirm('Bỏ sản phẩm này khỏi giỏ hàng?');">
        <input type="hidden" name="action" value="remove">
        <input type="hidden" name="itemId" value="${i.maCTGH}">
        <button class="link-danger" type="submit">Bỏ khỏi giỏ</button>
    </form>
</td>
</tr>
</c:forEach>
</tbody>
</table></div>
<form id="checkoutForm" class="cart-summary checkout-select-form" action="${ctx}/checkout" method="get">
    <div class="voucher-row">
        <label>Voucher</label>
        <input name="voucherCode" placeholder="Nhập mã: CELINE10, FREESHIP, WELCOME50">
    </div>
    <button class="btn btn-dark" type="submit">Mua sản phẩm đã chọn</button>
</form>
</c:otherwise>
</c:choose>
</section>
<script>
const selectAllCart = document.getElementById('selectAllCart');
if (selectAllCart) {
    selectAllCart.addEventListener('change', function () {
        document.querySelectorAll('.cart-select').forEach(cb => cb.checked = selectAllCart.checked);
    });
}
const checkoutForm = document.getElementById('checkoutForm');
if (checkoutForm) {
    checkoutForm.addEventListener('submit', function (e) {
        const checked = document.querySelectorAll('.cart-select:checked').length;
        if (checked === 0) {
            e.preventDefault();
            alert('Vui lòng chọn ít nhất một sản phẩm để mua.');
        }
    });
}
</script>
<%@ include file="common/footer.jsp" %>
