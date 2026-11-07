<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Thanh toán | Celine Closet" scope="request" />
<c:set var="needsMap" value="true" scope="request" />
<%@ include file="common/header.jsp" %>
<section class="fashion-page-banner checkout-banner"><div><span>Checkout</span><h1>Thanh toán</h1><p>Kiểm tra đơn hàng, áp dụng voucher và chọn phương thức thanh toán.</p></div></section>
<section class="fashion-section fashion-container">
<c:choose>
<c:when test="${empty items}"><div class="empty-box">Bạn chưa chọn sản phẩm nào. <a href="${ctx}/cart">Quay lại giỏ hàng</a></div></c:when>
<c:otherwise>
<c:if test="${not empty error}"><div class="alert error">${error}</div></c:if>
<c:if test="${param.paymentCancelled == '1'}"><div class="alert success">Đã hủy thanh toán chuyển khoản. Sản phẩm đã được đưa lại vào giỏ; bạn có thể đổi phương thức hoặc tạo QR lại.</div></c:if>
<div class="checkout-layout refined-checkout-layout">
<form class="checkout-form refined-checkout-form" action="${ctx}/checkout" method="post">
    <c:forEach var="sid" items="${selectedItemIds}"><input type="hidden" name="selectedItemId" value="${sid}"></c:forEach>
    <input type="hidden" id="checkoutVoucherCode" name="voucherCode" value="${voucherCode}">
    <div class="checkout-section-title"><span>01</span><div><h2>Thông tin nhận hàng</h2><p>Điền chính xác để nhân viên giao hàng liên hệ.</p></div></div>
    <div class="checkout-field-grid">
        <label>Họ tên người nhận<input name="hoTenNhan" value="${sessionScope.auth.hoTen}" required></label>
        <label>Số điện thoại<input name="phone" value="${sessionScope.auth.soDienThoai}" required inputmode="numeric" minlength="10" maxlength="10" pattern="0[0-9]{9}" title="Số điện thoại phải đủ 10 số và bắt đầu bằng 0"></label>
        <div class="full-field checkout-address-picker"
             data-address-picker
             data-map-api="${ctx}/api/map">
            <input type="hidden" name="address" data-full-address>
            <input type="hidden" name="deliveryLat" value="${param.deliveryLat}" data-delivery-lat>
            <input type="hidden" name="deliveryLng" value="${param.deliveryLng}" data-delivery-lng>

            <label class="checkout-address-field">
                Khu vực giao hàng
                <input name="addressArea"
                       value="${not empty param.addressArea ? param.addressArea : profile.diaChiMacDinh}"
                       placeholder="Nhập phường/xã, quận/huyện, tỉnh/thành..."
                       autocomplete="off"
                       required
                       data-address-area>
                <small>Nhập tối thiểu 2 ký tự rồi chọn địa chỉ gần nhất trong danh sách.</small>
            </label>
            <div class="address-suggestion-list" data-address-suggestions hidden></div>

            <label class="checkout-address-field">
                Địa chỉ chi tiết
                <input name="addressDetail"
                       value="${param.addressDetail}"
                       placeholder="Số nhà, tên đường, hẻm, tòa nhà..."
                       required minlength="10"
                       pattern="(?=.*[0-9])(?=.*[A-Za-zÀ-ỹ]).{10,}"
                       title="Địa chỉ chi tiết phải có số nhà và tên đường/hẻm/khu vực"
                       data-address-detail>
                <small>Ví dụ: Số 12 Nguyễn Tất Thành, hẻm 3, gọi trước khi giao.</small>
            </label>

            <div class="address-map-actions">
                <button type="button" class="address-map-button" data-open-address-map>
                    <i class="fa-solid fa-map-location-dot"></i> Mở bản đồ chọn vị trí
                </button>
                <button type="button" class="address-location-button" data-use-current-location>
                    <i class="fa-solid fa-location-crosshairs"></i> Dùng vị trí hiện tại
                </button>
            </div>

            <div class="checkout-address-map-panel" data-address-map-panel hidden>
                <div class="checkout-address-map" data-address-map></div>
                <div class="checkout-address-map-footer">
                    <p data-address-map-status>Bấm lên bản đồ hoặc kéo ghim tới đúng vị trí nhận hàng.</p>
                    <button type="button" data-close-address-map>Thu gọn bản đồ</button>
                </div>
            </div>
        </div>
        <label class="full-field">Ghi chú<textarea name="note" rows="3" placeholder="Ví dụ: gọi trước khi giao">${param.note}</textarea></label>
    </div>

    <div class="checkout-section-title" id="payment-method"><span>02</span><div><h2>Phương thức thanh toán</h2><p>Đơn chuyển khoản sẽ có QR riêng với đúng số tiền và mã đơn.</p></div></div>
    <div class="payment-choice-grid" data-payment-choices>
        <label class="payment-choice ${param.payment != 'BANK' ? 'active' : ''}"><input type="radio" name="payment" value="COD" ${param.payment != 'BANK' ? 'checked' : ''}><b>Thanh toán khi nhận hàng</b><span>Kiểm tra kiện hàng trước khi thanh toán.</span></label>
        <label class="payment-choice ${param.payment == 'BANK' ? 'active' : ''}"><input type="radio" name="payment" value="BANK" ${param.payment == 'BANK' ? 'checked' : ''}><b>Chuyển khoản TPBank / QR</b><span>QR tự điền số tiền và nội dung DH&lt;mã đơn&gt;.</span></label>
    </div>
    <div class="shop-qr-panel bank-preview-panel" data-bank-panel ${param.payment == 'BANK' ? '' : 'hidden'}>
        <div class="bank-preview-icon"><i class="fa-solid fa-qrcode"></i></div>
        <div><small>Thanh toán tự động qua SePay</small><h3>${shopBankName}</h3><p>Sau khi tạo đơn, hệ thống sẽ mở mã QR riêng. Khi SePay báo tiền vào đúng mã đơn và đủ số tiền, trạng thái sẽ tự chuyển sang <b>Đã thanh toán</b>.</p><p class="qr-warning">Không cần bấm xác nhận thủ công và không sửa nội dung chuyển khoản trên ứng dụng ngân hàng.</p></div>
    </div>
    <button class="btn btn-dark checkout-submit">Xác nhận đặt hàng</button>
</form>

<aside class="order-summary refined-order-summary">
    <p class="eyebrow">Order summary</p><h2>Đơn hàng của bạn</h2>
    <div class="checkout-item-list">
        <c:forEach var="i" items="${items}"><c:set var="checkoutFallback" value="${ctx}/assets/images/fashion/card-01.jpg" /><article><c:choose><c:when test="${not empty i.hinhAnh}"><img class="js-fashion-image" src="${ctx}/${i.hinhAnh}" data-fallback="${checkoutFallback}" alt="${i.tenSP}"></c:when><c:otherwise><img class="js-fashion-image" src="${checkoutFallback}" alt="${i.tenSP}"></c:otherwise></c:choose><span><b>${i.tenSP}</b><small>Màu: ${empty i.mauSac ? 'Theo hình' : i.mauSac} · Size: ${empty i.kichThuoc ? '—' : i.kichThuoc} · Số lượng: ${i.soLuong}</small></span><strong><fmt:formatNumber value="${i.thanhTien}" type="number" groupingUsed="true" />đ</strong></article></c:forEach>
    </div>
    <div class="voucher-apply instant-voucher-box" data-voucher-box data-endpoint="${ctx}/api/voucher-check">
        <label for="voucherCodeInput">Mã voucher</label>
        <div class="voucher-inline">
            <input id="voucherCodeInput" value="${voucherCode}" placeholder="Nhập mã ưu đãi" autocomplete="off">
            <button id="applyVoucherButton" type="button">Áp dụng</button>
        </div>
        <p id="voucherMessage" class="voucher-message ${voucherValid == false ? 'error' : ''}">
            <c:if test="${voucherValid == false}">Mã không hợp lệ, hết hạn, hết lượt hoặc đơn hàng chưa đủ điều kiện.</c:if>
        </p>
        <div class="voucher-suggestions">
            <c:forEach var="v" items="${publicVouchers}"><button type="button" data-voucher-code="${v.maCode}">${v.maCode}</button></c:forEach>
            <c:forEach var="v" items="${myVouchers}"><c:if test="${v.trangThaiCaNhan == 'AVAILABLE'}"><button type="button" data-voucher-code="${v.maCode}">${v.maCode} · của bạn</button></c:if></c:forEach>
        </div>
    </div>
    <div class="summary-totals" data-order-total="${total}">
        <p><span>Tạm tính</span><b><fmt:formatNumber value="${total}" type="number" groupingUsed="true" />đ</b></p>
        <p><span>Voucher</span><b id="voucherDiscountValue">-<fmt:formatNumber value="${voucherDiscount}" type="number" groupingUsed="true" />đ</b></p>
        <p><span>Phí vận chuyển</span><b>Miễn phí</b></p>
        <hr><h3><span>Tổng thanh toán</span><b id="payableTotalValue"><fmt:formatNumber value="${payableTotal}" type="number" groupingUsed="true" />đ</b></h3>
        <small>Bạn dự kiến nhận được <b id="checkoutPointPreview"><fmt:formatNumber value="${payableTotal / 10000}" maxFractionDigits="0" /></b> điểm sau khi đơn hoàn thành.</small>
    </div>
</aside>
</div>
</c:otherwise>
</c:choose>
</section>
<%@ include file="common/footer.jsp" %>
