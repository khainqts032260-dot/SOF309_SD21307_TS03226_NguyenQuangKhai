<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Thanh toán chuyển khoản | Celine Closet" scope="request" />
<%@ include file="common/header.jsp" %>

<section class="fashion-page-banner payment-banner">
    <div>
        <span>Secure bank transfer</span>
        <h1>Thanh toán đơn #${payment.maDH}</h1>
        <p>Quét VietQR để ứng dụng ngân hàng tự điền đúng số tiền và mã đơn.</p>
    </div>
</section>

<section class="fashion-section fashion-container bank-payment-page"
         data-bank-payment
         data-order-id="${payment.maDH}"
         data-status-url="${ctx}/api/payment-status"
         data-abandon-url="${ctx}/api/payment-abandon"
         data-orders-url="${ctx}/orders?id=${payment.maDH}"
         data-orders-list-url="${ctx}/orders"
         data-checkout-url="${ctx}/checkout"
         data-payment-settled="${payment.trangThai == 'PAID'}"
         data-payment-cancelled="${payment.trangThai == 'CANCELLED'}"
         data-payment-failed="${payment.trangThai == 'FAILED'}"
         data-seconds-remaining="${paymentSecondsRemaining}"
         data-expiration-minutes="${paymentExpiresMinutes}">
    <c:choose>
        <c:when test="${!bankConfigured}">
            <div class="bank-config-warning">
                <i class="fa-solid fa-triangle-exclamation"></i>
                <div>
                    <h2>Chưa cấu hình tài khoản TPBank</h2>
                    <p>Điền <code>shop.bankAccount</code> và <code>shop.bankOwner</code> trong <code>src/main/resources/app.properties</code>, sau đó build và chạy lại project.</p>
                    <a class="btn btn-dark" href="${ctx}/orders?id=${payment.maDH}">Xem đơn hàng</a>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="bank-checkout-shell">
                <aside class="bank-order-summary">
                    <div class="bank-order-heading">
                        <div class="bank-merchant-mark">C&amp;C</div>
                        <div>
                            <span>Thông tin đơn hàng</span>
                            <h2>Celine Closet</h2>
                        </div>
                    </div>

                    <div class="bank-order-products">
                        <c:forEach var="item" items="${paymentItems}">
                            <article class="bank-order-product">
                                <img src="${ctx}/${item.hinhAnh}" alt="${item.tenSP}">
                                <div>
                                    <strong>${item.tenSP}</strong>
                                    <small>
                                        SL: ${item.soLuong}
                                        <c:if test="${not empty item.mauSac}"> · ${item.mauSac}</c:if>
                                        <c:if test="${not empty item.kichThuoc}"> · Size ${item.kichThuoc}</c:if>
                                    </small>
                                </div>
                                <b><fmt:formatNumber value="${item.thanhTien}" type="number" groupingUsed="true" />đ</b>
                            </article>
                        </c:forEach>
                    </div>

                    <dl class="bank-order-meta">
                        <div><dt>Mã đơn</dt><dd>#${payment.maDH}</dd></div>
                        <div><dt>Số sản phẩm</dt><dd>${paymentItemCount}</dd></div>
                        <div><dt>Ngày đặt</dt><dd><fmt:formatDate value="${payment.ngayDat}" pattern="dd/MM/yyyy HH:mm" /></dd></div>
                        <div class="bank-order-total"><dt>Số tiền thanh toán</dt><dd><fmt:formatNumber value="${payment.soTien}" type="number" groupingUsed="true" />đ</dd></div>
                        <div><dt>Nội dung</dt><dd><code data-payment-code>${paymentCode}</code></dd></div>
                    </dl>

                    <div class="bank-order-state">
                        <span class="payment-live-status ${payment.trangThai == 'PAID' ? 'paid' : ''} ${(payment.trangThai == 'CANCELLED' || payment.trangThai == 'FAILED') ? 'cancelled' : ''}" data-payment-status>
                            <c:choose>
                                <c:when test="${payment.trangThai == 'PAID'}">Đã thanh toán</c:when>
                                <c:when test="${payment.trangThai == 'FAILED'}">Thanh toán không thành công</c:when>
                                <c:when test="${payment.trangThai == 'CANCELLED'}">Đã hủy thanh toán</c:when>
                                <c:otherwise>Chờ thanh toán</c:otherwise>
                            </c:choose>
                        </span>
                        <p>Giữ nguyên số tiền và nội dung để SePay ghép đúng giao dịch với đơn hàng.</p>
                    </div>
                </aside>

                <main class="bank-qr-checkout">
                    <div class="bank-qr-title">
                        <p class="eyebrow">VietQR · TPBank</p>
                        <h2 data-payment-title>
                            <c:choose>
                                <c:when test="${payment.trangThai == 'PAID'}">Thanh toán thành công</c:when>
                                <c:when test="${payment.trangThai == 'FAILED'}">Thanh toán không thành công</c:when>
                                <c:when test="${payment.trangThai == 'CANCELLED'}">Phiên thanh toán đã hủy</c:when>
                                <c:otherwise>Quét QR để thanh toán</c:otherwise>
                            </c:choose>
                        </h2>
                        <p>QR đã chứa sẵn tài khoản, số tiền và nội dung <b>${paymentCode}</b>.</p>
                    </div>

                    <div class="tpbank-qr-frame">
                        <div class="tpbank-brand-row">
                            <span class="tpbank-logo-mark"><i></i></span>
                            <strong>TPBank</strong>
                        </div>
                        <div class="tpbank-account-owner">${shopBankOwner}</div>
                        <div class="tpbank-account-number">${shopBankAccount}</div>
                        <img src="${paymentQrUrl}" alt="VietQR thanh toán đơn ${payment.maDH}" class="bank-payment-qr">
                        <div class="vietqr-caption">
                            <span>VIETQR</span>
                            <span>NAPAS 247</span>
                        </div>
                    </div>

                    <div class="payment-waiting-box ${payment.trangThai == 'PAID' ? 'success' : ''} ${(payment.trangThai == 'CANCELLED' || payment.trangThai == 'FAILED') ? 'error' : ''}" data-payment-message>
                        <span class="payment-thinking-dots" aria-hidden="true"><i></i><i></i><i></i></span>
                        <p>
                            <c:choose>
                                <c:when test="${payment.trangThai == 'PAID'}">SePay đã xác nhận giao dịch. Đơn hàng đã được cập nhật.</c:when>
                                <c:when test="${payment.trangThai == 'FAILED'}">Quá ${paymentExpiresMinutes} phút chưa nhận được thanh toán. Đơn đã được ẩn khỏi lịch sử mua hàng.</c:when>
                                <c:when test="${payment.trangThai == 'CANCELLED'}">Phiên thanh toán đã được hủy.</c:when>
                                <c:otherwise>Hệ thống đang kiểm tra giao dịch tự động. Bạn có thể bấm xác nhận sau khi đã chuyển khoản.</c:otherwise>
                            </c:choose>
                        </p>
                    </div>

                    <div class="bank-payment-actions">
                        <button type="button" class="btn btn-dark" data-check-payment
                                style="${payment.trangThai == 'PAID' || payment.trangThai == 'FAILED' ? 'display:none' : ''}">
                            <i class="fa-solid fa-circle-check"></i> Tôi đã chuyển khoản · Xác nhận
                        </button>
                        <button type="button" class="btn btn-outline payment-cancel-button" data-cancel-payment
                                style="${payment.trangThai == 'PAID' || payment.trangThai == 'FAILED' ? 'display:none' : ''}">
                            Hủy thanh toán
                        </button>
                        <a class="btn btn-dark" data-payment-history-button
                           href="${ctx}/orders?id=${payment.maDH}"
                           style="${payment.trangThai == 'PAID' ? '' : 'display:none'}">
                            <i class="fa-solid fa-clock-rotate-left"></i> Trở về lịch sử mua hàng của đơn này
                        </a>
                        <a class="btn btn-outline" data-payment-history-list-button
                           href="${ctx}/orders"
                           style="${payment.trangThai == 'FAILED' ? '' : 'display:none'}">
                            <i class="fa-solid fa-list"></i> Trở về lịch sử mua hàng
                        </a>
                    </div>

                    <p class="bank-auto-check-note">
                        <i class="fa-solid fa-rotate"></i>
                        Website tự kiểm tra mỗi 3 giây. Thời gian còn lại:
                        <strong data-payment-countdown>--:--</strong>.
                    </p>
                    <p class="bank-exit-warning">
                        Bạn có thể thoát trang. Nếu sau ${paymentExpiresMinutes} phút hệ thống vẫn chưa nhận được xác nhận thanh toán, đơn sẽ thất bại và tự ẩn khỏi lịch sử mua hàng.
                    </p>
                    <div class="bank-received-line">Đã nhận: <strong data-received-amount><fmt:formatNumber value="${payment.soTienDaNhan}" type="number" groupingUsed="true" />đ</strong></div>
                </main>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<%@ include file="common/footer.jsp" %>
